package com.mysql.cj.jdbc;

import com.mysql.cj.CancelQueryTask;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.ParseInfo;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.ServerPreparedQuery;
import com.mysql.cj.ServerPreparedQueryBindValue;
import com.mysql.cj.ServerPreparedQueryBindings;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ServerPreparedStatement extends ClientPreparedStatement {
   private boolean hasOnDuplicateKeyUpdate = false;
   private boolean invalid = false;
   private CJException invalidationException;
   protected boolean isCached = false;

   protected static ServerPreparedStatement getInstance(JdbcConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
      return new ServerPreparedStatement(conn, sql, catalog, resultSetType, resultSetConcurrency);
   }

   protected ServerPreparedStatement(JdbcConnection conn, String sql, String catalog, int resultSetType, int resultSetConcurrency) throws SQLException {
      super(conn, catalog);
      this.checkNullOrEmptyQuery(sql);
      String statementComment = this.session.getProtocol().getQueryComment();
      ((PreparedQuery)this.query).setOriginalSql(statementComment == null ? sql : "/* " + statementComment + " */ " + sql);
      ((PreparedQuery)this.query).setParseInfo(new ParseInfo(((PreparedQuery)this.query).getOriginalSql(), this.session, this.charEncoding));
      this.hasOnDuplicateKeyUpdate = ((PreparedQuery)this.query).getParseInfo().getFirstStmtChar() == 'I' && this.containsOnDuplicateKeyInString(sql);

      try {
         this.serverPrepare(sql);
      } catch (SQLException | CJException var8) {
         this.realClose(false, true);
         throw SQLExceptionsMapping.translateException(var8, this.exceptionInterceptor);
      }

      this.setResultSetType(resultSetType);
      this.setResultSetConcurrency(resultSetConcurrency);
   }

   @Override
   protected void initQuery() {
      this.query = ServerPreparedQuery.getInstance(this.session);
   }

   @Override
   public String toString() {
      StringBuilder toStringBuf = new StringBuilder();
      toStringBuf.append(this.getClass().getName() + "[");
      toStringBuf.append(((ServerPreparedQuery)this.query).getServerStatementId());
      toStringBuf.append("]: ");

      try {
         toStringBuf.append(this.asSql());
      } catch (SQLException var3) {
         toStringBuf.append(Messages.getString("ServerPreparedStatement.6"));
         toStringBuf.append(var3);
      }

      return toStringBuf.toString();
   }

   @Override
   public void addBatch() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.addBatch(((PreparedQuery)this.query).getQueryBindings().clone());
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
      synchronized(this.checkClosed().getConnectionMutex()) {
         ClientPreparedStatement pStmtForSub = null;

         String var19;
         try {
            pStmtForSub = ClientPreparedStatement.getInstance(this.connection, ((PreparedQuery)this.query).getOriginalSql(), this.getCurrentCatalog());
            int numParameters = ((PreparedQuery)pStmtForSub.query).getParameterCount();
            int ourNumParameters = ((PreparedQuery)this.query).getParameterCount();
            ServerPreparedQueryBindValue[] parameterBindings = ((ServerPreparedQuery)this.query).getQueryBindings().getBindValues();

            for(int i = 0; i < numParameters && i < ourNumParameters; ++i) {
               if (parameterBindings[i] != null) {
                  if (parameterBindings[i].isNull()) {
                     pStmtForSub.setNull(i + 1, MysqlType.NULL);
                  } else {
                     ServerPreparedQueryBindValue bindValue = parameterBindings[i];
                     switch(bindValue.bufferType) {
                        case 1:
                           pStmtForSub.setByte(i + 1, (byte)((int)bindValue.longBinding));
                           break;
                        case 2:
                           pStmtForSub.setShort(i + 1, (short)((int)bindValue.longBinding));
                           break;
                        case 3:
                           pStmtForSub.setInt(i + 1, (int)bindValue.longBinding);
                           break;
                        case 4:
                           pStmtForSub.setFloat(i + 1, bindValue.floatBinding);
                           break;
                        case 5:
                           pStmtForSub.setDouble(i + 1, bindValue.doubleBinding);
                           break;
                        case 6:
                        case 7:
                        default:
                           pStmtForSub.setObject(i + 1, parameterBindings[i].value);
                           break;
                        case 8:
                           pStmtForSub.setLong(i + 1, bindValue.longBinding);
                     }
                  }
               }
            }

            var19 = pStmtForSub.asSql(quoteStreamsAndUnknowns);
         } finally {
            if (pStmtForSub != null) {
               try {
                  pStmtForSub.close();
               } catch (SQLException var16) {
               }
            }
         }

         return var19;
      }
   }

   @Override
   protected JdbcConnection checkClosed() {
      if (this.invalid) {
         throw this.invalidationException;
      } else {
         return super.checkClosed();
      }
   }

   @Override
   public void clearParameters() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ((ServerPreparedQuery)this.query).clearParameters(true);
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   protected void setClosed(boolean flag) {
      this.isClosed = flag;
   }

   @Override
   public void close() throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn != null) {
            synchronized(locallyScopedConn.getConnectionMutex()) {
               if (this.isCached && this.isPoolable() && !this.isClosed) {
                  this.clearParameters();
                  this.isClosed = true;
                  this.connection.recachePreparedStatement(this);
               } else {
                  this.isClosed = false;
                  this.realClose(true, true);
               }
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   protected long[] executeBatchSerially(int batchTimeout) throws SQLException {
      synchronized(this.checkClosed().getConnectionMutex()) {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn.isReadOnly()) {
            throw SQLError.createSQLException(
               Messages.getString("ServerPreparedStatement.2") + Messages.getString("ServerPreparedStatement.3"), "S1009", this.exceptionInterceptor
            );
         } else {
            this.clearWarnings();
            ServerPreparedQueryBindValue[] oldBindValues = ((ServerPreparedQuery)this.query).getQueryBindings().getBindValues();

            int nbrCommands;
            try {
               long[] updateCounts = null;
               if (this.query.getBatchedArgs() != null) {
                  nbrCommands = this.query.getBatchedArgs().size();
                  updateCounts = new long[nbrCommands];
                  if (this.retrieveGeneratedKeys) {
                     this.batchedGeneratedKeys = new ArrayList<>(nbrCommands);
                  }

                  for(int i = 0; i < nbrCommands; ++i) {
                     updateCounts[i] = -3L;
                  }

                  SQLException sqlEx = null;
                  int commandIndex = 0;
                  ServerPreparedQueryBindValue[] previousBindValuesForBatch = null;
                  CancelQueryTask timeoutTask = null;

                  try {
                     timeoutTask = this.startQueryTimer(this, batchTimeout);

                     for(commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                        Object arg = this.query.getBatchedArgs().get(commandIndex);

                        try {
                           if (arg instanceof String) {
                              updateCounts[commandIndex] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                              this.getBatchedGeneratedKeys(
                                 this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg) ? 1 : 0
                              );
                           } else {
                              ((ServerPreparedQuery)this.query).setQueryBindings((ServerPreparedQueryBindings)arg);
                              ServerPreparedQueryBindValue[] parameterBindings = ((ServerPreparedQuery)this.query).getQueryBindings().getBindValues();
                              if (previousBindValuesForBatch != null) {
                                 for(int j = 0; j < parameterBindings.length; ++j) {
                                    if (parameterBindings[j].bufferType != previousBindValuesForBatch[j].bufferType) {
                                       ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
                                       break;
                                    }
                                 }
                              }

                              try {
                                 updateCounts[commandIndex] = this.executeUpdateInternal(false, true);
                              } finally {
                                 previousBindValuesForBatch = parameterBindings;
                              }

                              this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdateInSQL() ? 1 : 0);
                           }
                        } catch (SQLException var34) {
                           updateCounts[commandIndex] = -3L;
                           if (!this.continueBatchOnError
                              || var34 instanceof MySQLTimeoutException
                              || var34 instanceof MySQLStatementCancelledException
                              || this.hasDeadlockOrTimeoutRolledBackTx(var34)) {
                              long[] newUpdateCounts = new long[commandIndex];
                              System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                              throw SQLError.createBatchUpdateException(var34, newUpdateCounts, this.exceptionInterceptor);
                           }

                           sqlEx = var34;
                        }
                     }
                  } finally {
                     this.stopQueryTimer(timeoutTask, false, false);
                     this.resetCancelledState();
                  }

                  if (sqlEx != null) {
                     throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                  }
               }

               nbrCommands = (int)(updateCounts != null ? updateCounts : new long[0]);
            } finally {
               ((ServerPreparedQuery)this.query).getQueryBindings().setBindValues(oldBindValues);
               ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
               this.clearBatch();
            }

            return (long[])nbrCommands;
         }
      }
   }

   private static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend, ExceptionInterceptor interceptor) {
      String sqlState = sqlEx.getSQLState();
      int vendorErrorCode = sqlEx.getErrorCode();
      SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(sqlEx.getMessage() + messageToAppend, sqlState, vendorErrorCode, interceptor);
      sqlExceptionWithNewMessage.setStackTrace(sqlEx.getStackTrace());
      return sqlExceptionWithNewMessage;
   }

   @Override
   protected <M extends Message> ResultSetInternalMethods executeInternal(
      int maxRowsToRetrieve, M sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, ColumnDefinition metadata, boolean isBatch
   ) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ((PreparedQuery)this.query).getQueryBindings().setNumberOfExecutions(((PreparedQuery)this.query).getQueryBindings().getNumberOfExecutions() + 1);

            ResultSetInternalMethods var10000;
            try {
               var10000 = this.serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadata);
            } catch (SQLException var14) {
               SQLException sqlEx = var14;
               if (this.session.getPropertySet().getBooleanProperty("enablePacketDebug").getValue()) {
                  this.session.dumpPacketRingBuffer();
               }

               if (this.dumpQueriesOnException.getValue()) {
                  String extractedSql = this.toString();
                  StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                  messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                  messageBuf.append(extractedSql);
                  messageBuf.append("\n\n");
                  sqlEx = appendMessageToException(var14, messageBuf.toString(), this.exceptionInterceptor);
               }

               throw sqlEx;
            } catch (Exception var15) {
               if (this.session.getPropertySet().getBooleanProperty("enablePacketDebug").getValue()) {
                  this.session.dumpPacketRingBuffer();
               }

               SQLException sqlEx = SQLError.createSQLException(var15.toString(), "S1000", var15, this.exceptionInterceptor);
               if (this.dumpQueriesOnException.getValue()) {
                  String extractedSql = this.toString();
                  StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                  messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                  messageBuf.append(extractedSql);
                  messageBuf.append("\n\n");
                  sqlEx = appendMessageToException(sqlEx, messageBuf.toString(), this.exceptionInterceptor);
               }

               throw sqlEx;
            }

            return var10000;
         }
      } catch (CJException var17) {
         throw SQLExceptionsMapping.translateException(var17, this.getExceptionInterceptor());
      }
   }

   protected ServerPreparedQueryBindValue getBinding(int parameterIndex, boolean forLongData) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            int i = this.getCoreParameterIndex(parameterIndex);
            return ((ServerPreparedQuery)this.query).getQueryBindings().getBinding(i, forLongData);
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSetMetaData getMetaData() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ColumnDefinition resultFields = ((ServerPreparedQuery)this.query).getResultFields();
            return resultFields != null && resultFields.getFields() != null
               ? new com.mysql.cj.jdbc.result.ResultSetMetaData(
                  this.session,
                  resultFields.getFields(),
                  this.session.getPropertySet().getBooleanProperty("useOldAliasMetadataBehavior").getValue(),
                  this.session.getPropertySet().getBooleanProperty("yearIsDateType").getValue(),
                  this.exceptionInterceptor
               )
               : null;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.parameterMetaData == null) {
               this.parameterMetaData = new MysqlParameterMetadata(
                  this.session,
                  ((ServerPreparedQuery)this.query).getParameterFields(),
                  ((PreparedQuery)this.query).getParameterCount(),
                  this.exceptionInterceptor
               );
            }

            return this.parameterMetaData;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isNull(int paramIndex) {
      throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
   }

   @Override
   public void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn != null) {
            synchronized(locallyScopedConn.getConnectionMutex()) {
               if (this.connection != null) {
                  CJException exceptionDuringClose = null;
                  if (calledExplicitly && !this.connection.isClosed()) {
                     synchronized(this.connection.getConnectionMutex()) {
                        try {
                           this.session
                              .sendCommand(this.commandBuilder.buildComStmtClose(null, ((ServerPreparedQuery)this.query).getServerStatementId()), true, 0);
                        } catch (CJException var11) {
                           exceptionDuringClose = var11;
                        }
                     }
                  }

                  if (this.isCached) {
                     this.connection.decachePreparedStatement(this);
                     this.isCached = false;
                  }

                  super.realClose(calledExplicitly, closeOpenResults);
                  ((ServerPreparedQuery)this.query).clearParameters(false);
                  if (exceptionDuringClose != null) {
                     throw exceptionDuringClose;
                  }
               }
            }
         }
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   protected void rePrepare() {
      synchronized(this.checkClosed().getConnectionMutex()) {
         this.invalidationException = null;

         try {
            this.serverPrepare(((PreparedQuery)this.query).getOriginalSql());
         } catch (Exception var7) {
            this.invalidationException = ExceptionFactory.createException(var7.getMessage(), var7);
         }

         if (this.invalidationException != null) {
            this.invalid = true;
            this.query.closeQuery();
            if (this.results != null) {
               try {
                  this.results.close();
               } catch (Exception var6) {
               }
            }

            if (this.generatedKeysResults != null) {
               try {
                  this.generatedKeysResults.close();
               } catch (Exception var5) {
               }
            }

            try {
               this.closeAllOpenResults();
            } catch (Exception var4) {
            }

            if (this.connection != null && !this.dontTrackOpenResources.getValue()) {
               this.connection.unregisterStatement(this);
            }
         }
      }
   }

   protected ResultSetInternalMethods serverExecute(int maxRowsToRetrieve, boolean createStreamingResultSet, ColumnDefinition metadata) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.results = ((ServerPreparedQuery)this.query).serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadata, this.resultSetFactory);
            return this.results;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected void serverPrepare(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            try {
               ServerPreparedQuery q = (ServerPreparedQuery)this.query;
               q.serverPrepare(sql);
            } catch (IOException var14) {
               throw SQLError.createCommunicationsException(
                  this.connection,
                  this.session.getProtocol().getPacketSentTimeHolder(),
                  this.session.getProtocol().getPacketReceivedTimeHolder(),
                  var14,
                  this.exceptionInterceptor
               );
            } catch (CJException var15) {
               SQLException ex = SQLExceptionsMapping.translateException(var15);
               if (this.dumpQueriesOnException.getValue()) {
                  StringBuilder messageBuf = new StringBuilder(((PreparedQuery)this.query).getOriginalSql().length() + 32);
                  messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                  messageBuf.append(((PreparedQuery)this.query).getOriginalSql());
                  ex = appendMessageToException(ex, messageBuf.toString(), this.exceptionInterceptor);
               }

               throw ex;
            } finally {
               this.session.clearInputStream();
            }
         }
      } catch (CJException var18) {
         throw SQLExceptionsMapping.translateException(var18, this.getExceptionInterceptor());
      }
   }

   @Override
   protected void checkBounds(int parameterIndex, int parameterIndexOffset) throws SQLException {
      int paramCount = ((PreparedQuery)this.query).getParameterCount();
      if (paramCount == 0) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("ServerPreparedStatement.8"), this.session.getExceptionInterceptor()
         );
      } else if (parameterIndex < 0 || parameterIndex > paramCount) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            Messages.getString("ServerPreparedStatement.9") + (parameterIndex + 1) + Messages.getString("ServerPreparedStatement.10") + paramCount,
            this.session.getExceptionInterceptor()
         );
      }
   }

   @Deprecated
   @Override
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
      try {
         this.checkClosed();
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setURL(int parameterIndex, URL x) throws SQLException {
      try {
         this.checkClosed();
         this.setString(parameterIndex, x.toString());
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getServerStatementId() {
      return ((ServerPreparedQuery)this.query).getServerStatementId();
   }

   @Override
   protected int setOneBatchedParameterSet(PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
      ServerPreparedQueryBindValue[] paramArg = ((ServerPreparedQueryBindings)paramSet).getBindValues();

      for(int j = 0; j < paramArg.length; ++j) {
         if (paramArg[j].isNull()) {
            batchedStatement.setNull(batchedParamIndex++, MysqlType.NULL.getJdbcType());
         } else if (paramArg[j].isLongData) {
            Object value = paramArg[j].value;
            if (value instanceof InputStream) {
               batchedStatement.setBinaryStream(batchedParamIndex++, (InputStream)value, (int)paramArg[j].bindLength);
            } else {
               batchedStatement.setCharacterStream(batchedParamIndex++, (Reader)value, (int)paramArg[j].bindLength);
            }
         } else {
            switch(paramArg[j].bufferType) {
               case 0:
               case 15:
               case 246:
               case 253:
               case 254:
                  Object value = paramArg[j].value;
                  if (value instanceof byte[]) {
                     batchedStatement.setBytes(batchedParamIndex, (byte[])value);
                  } else {
                     batchedStatement.setString(batchedParamIndex, (String)value);
                  }

                  if (batchedStatement instanceof ServerPreparedStatement) {
                     ServerPreparedQueryBindValue asBound = ((ServerPreparedStatement)batchedStatement).getBinding(batchedParamIndex, false);
                     asBound.bufferType = paramArg[j].bufferType;
                  }

                  ++batchedParamIndex;
                  break;
               case 1:
                  batchedStatement.setByte(batchedParamIndex++, (byte)((int)paramArg[j].longBinding));
                  break;
               case 2:
                  batchedStatement.setShort(batchedParamIndex++, (short)((int)paramArg[j].longBinding));
                  break;
               case 3:
                  batchedStatement.setInt(batchedParamIndex++, (int)paramArg[j].longBinding);
                  break;
               case 4:
                  batchedStatement.setFloat(batchedParamIndex++, paramArg[j].floatBinding);
                  break;
               case 5:
                  batchedStatement.setDouble(batchedParamIndex++, paramArg[j].doubleBinding);
                  break;
               case 7:
               case 12:
                  batchedStatement.setTimestamp(batchedParamIndex++, (Timestamp)paramArg[j].value);
                  break;
               case 8:
                  batchedStatement.setLong(batchedParamIndex++, paramArg[j].longBinding);
                  break;
               case 10:
                  batchedStatement.setDate(batchedParamIndex++, (Date)paramArg[j].value);
                  break;
               case 11:
                  batchedStatement.setTime(batchedParamIndex++, (Time)paramArg[j].value);
                  break;
               default:
                  throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.26", new Object[]{batchedParamIndex}));
            }
         }
      }

      return batchedParamIndex;
   }

   @Override
   protected boolean containsOnDuplicateKeyUpdateInSQL() {
      return this.hasOnDuplicateKeyUpdate;
   }

   @Override
   protected ClientPreparedStatement prepareBatchedInsertSQL(JdbcConnection localConn, int numBatches) throws SQLException {
      synchronized(this.checkClosed().getConnectionMutex()) {
         ClientPreparedStatement var10000;
         try {
            ClientPreparedStatement pstmt = localConn.prepareStatement(
                  ((PreparedQuery)this.query).getParseInfo().getSqlForBatch(numBatches), this.resultSetConcurrency, this.query.getResultType().getIntValue()
               )
               .unwrap(ClientPreparedStatement.class);
            pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
            var10000 = pstmt;
         } catch (UnsupportedEncodingException var7) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.27"), "S1000", this.exceptionInterceptor);
            sqlEx.initCause(var7);
            throw sqlEx;
         }

         return var10000;
      }
   }

   @Override
   public void setPoolable(boolean poolable) throws SQLException {
      try {
         if (!poolable) {
            this.connection.decachePreparedStatement(this);
         }

         super.setPoolable(poolable);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }
}
