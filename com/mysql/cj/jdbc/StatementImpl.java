package com.mysql.cj.jdbc;

import com.mysql.cj.CancelQueryTask;
import com.mysql.cj.CharsetMapping;
import com.mysql.cj.Constants;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.NativeSession;
import com.mysql.cj.ParseInfo;
import com.mysql.cj.PingTarget;
import com.mysql.cj.Query;
import com.mysql.cj.Session;
import com.mysql.cj.SimpleQuery;
import com.mysql.cj.TransactionEventHandler;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.CJTimeoutException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.OperationCancelledException;
import com.mysql.cj.exceptions.StatementIsClosedException;
import com.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.CachedResultSetMetaData;
import com.mysql.cj.jdbc.result.ResultSetFactory;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.log.ProfilerEventHandler;
import com.mysql.cj.log.ProfilerEventHandlerFactory;
import com.mysql.cj.log.ProfilerEventImpl;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.result.ByteArrayRow;
import com.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import com.mysql.cj.result.DefaultColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.Row;
import com.mysql.cj.util.LogUtils;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatementImpl implements JdbcStatement {
   protected static final String PING_MARKER = "/* ping */";
   protected NativeMessageBuilder commandBuilder = new NativeMessageBuilder();
   public static final byte USES_VARIABLES_FALSE = 0;
   public static final byte USES_VARIABLES_TRUE = 1;
   public static final byte USES_VARIABLES_UNKNOWN = -1;
   protected String charEncoding = null;
   protected volatile JdbcConnection connection = null;
   protected boolean doEscapeProcessing = true;
   protected boolean isClosed = false;
   protected long lastInsertId = -1L;
   protected int maxFieldSize = (Integer)PropertyDefinitions.getPropertyDefinition("maxAllowedPacket").getDefaultValue();
   public int maxRows = -1;
   protected Set<ResultSetInternalMethods> openResults = new HashSet<>();
   protected boolean pedantic = false;
   protected String pointOfOrigin;
   protected boolean profileSQL = false;
   protected ResultSetInternalMethods results = null;
   protected ResultSetInternalMethods generatedKeysResults = null;
   protected int resultSetConcurrency = 0;
   protected long updateCount = -1L;
   protected boolean useUsageAdvisor = false;
   protected SQLWarning warningChain = null;
   protected boolean holdResultsOpenOverClose = false;
   protected ArrayList<Row> batchedGeneratedKeys = null;
   protected boolean retrieveGeneratedKeys = false;
   protected boolean continueBatchOnError = false;
   protected PingTarget pingTarget = null;
   protected ExceptionInterceptor exceptionInterceptor;
   protected boolean lastQueryIsOnDupKeyUpdate = false;
   private boolean isImplicitlyClosingResults = false;
   protected RuntimeProperty<Boolean> dontTrackOpenResources;
   protected RuntimeProperty<Boolean> dumpQueriesOnException;
   protected boolean logSlowQueries = false;
   protected RuntimeProperty<Boolean> rewriteBatchedStatements;
   protected RuntimeProperty<Integer> maxAllowedPacket;
   protected boolean dontCheckOnDuplicateKeyUpdateInSQL;
   protected RuntimeProperty<Boolean> sendFractionalSeconds;
   protected ResultSetFactory resultSetFactory;
   protected Query query;
   protected NativeSession session = null;
   private Resultset.Type originalResultSetType = Resultset.Type.FORWARD_ONLY;
   private int originalFetchSize = 0;
   private boolean isPoolable = true;
   private boolean closeOnCompletion = false;

   public StatementImpl(JdbcConnection c, String catalog) throws SQLException {
      if (c != null && !c.isClosed()) {
         this.connection = c;
         this.session = (NativeSession)c.getSession();
         this.exceptionInterceptor = c.getExceptionInterceptor();
         this.initQuery();
         this.query.setCurrentCatalog(catalog);
         JdbcPropertySet pset = c.getPropertySet();
         this.dontTrackOpenResources = pset.getBooleanProperty("dontTrackOpenResources");
         this.dumpQueriesOnException = pset.getBooleanProperty("dumpQueriesOnException");
         this.continueBatchOnError = pset.getBooleanProperty("continueBatchOnError").getValue();
         this.pedantic = pset.getBooleanProperty("pedantic").getValue();
         this.rewriteBatchedStatements = pset.getBooleanProperty("rewriteBatchedStatements");
         this.charEncoding = pset.getStringProperty("characterEncoding").getValue();
         this.profileSQL = pset.getBooleanProperty("profileSQL").getValue();
         this.useUsageAdvisor = pset.getBooleanProperty("useUsageAdvisor").getValue();
         this.logSlowQueries = pset.getBooleanProperty("logSlowQueries").getValue();
         this.maxAllowedPacket = pset.getIntegerProperty("maxAllowedPacket");
         this.dontCheckOnDuplicateKeyUpdateInSQL = pset.getBooleanProperty("dontCheckOnDuplicateKeyUpdateInSQL").getValue();
         this.sendFractionalSeconds = pset.getBooleanProperty("sendFractionalSeconds");
         this.doEscapeProcessing = pset.getBooleanProperty("enableEscapeProcessing").getValue();
         this.maxFieldSize = this.maxAllowedPacket.getValue();
         if (!this.dontTrackOpenResources.getValue()) {
            c.registerStatement(this);
         }

         int defaultFetchSize = pset.getIntegerProperty("defaultFetchSize").getValue();
         if (defaultFetchSize != 0) {
            this.setFetchSize(defaultFetchSize);
         }

         boolean profiling = this.profileSQL || this.useUsageAdvisor || this.logSlowQueries;
         if (profiling) {
            this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());

            try {
               this.query.setEventSink(ProfilerEventHandlerFactory.getInstance(this.session));
            } catch (CJException var7) {
               throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
            }
         }

         int maxRowsConn = pset.getIntegerProperty("maxRows").getValue();
         if (maxRowsConn != -1) {
            this.setMaxRows(maxRowsConn);
         }

         this.holdResultsOpenOverClose = pset.getBooleanProperty("holdResultsOpenOverStatementClose").getValue();
         this.resultSetFactory = new ResultSetFactory(this.connection, this);
      } else {
         throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003", null);
      }
   }

   protected void initQuery() {
      this.query = new SimpleQuery(this.session);
   }

   @Override
   public void addBatch(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (sql != null) {
               this.query.addBatch(sql);
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public void addBatch(Object batch) {
      this.query.addBatch(batch);
   }

   @Override
   public List<Object> getBatchedArgs() {
      return this.query.getBatchedArgs();
   }

   @Override
   public void cancel() throws SQLException {
      try {
         if (this.query.getStatementExecuting().get()) {
            if (!this.isClosed && this.connection != null) {
               JdbcConnection cancelConn = null;
               Statement cancelStmt = null;

               try {
                  HostInfo hostInfo = this.session.getHostInfo();
                  String database = hostInfo.getDatabase();
                  String user = StringUtils.isNullOrEmpty(hostInfo.getUser()) ? "" : hostInfo.getUser();
                  String password = StringUtils.isNullOrEmpty(hostInfo.getPassword()) ? "" : hostInfo.getPassword();
                  NativeSession newSession = new NativeSession(this.session.getHostInfo(), this.session.getPropertySet());
                  newSession.connect(hostInfo, user, password, database, 30000, new TransactionEventHandler() {
                     @Override
                     public void transactionCompleted() {
                     }

                     @Override
                     public void transactionBegun() {
                     }
                  });
                  newSession.sendCommand(
                     new NativeMessageBuilder().buildComQuery(newSession.getSharedSendPacket(), "KILL QUERY " + this.session.getThreadId()), false, 0
                  );
                  this.setCancelStatus(Query.CancelStatus.CANCELED_BY_USER);
               } catch (IOException var13) {
                  throw SQLExceptionsMapping.translateException(var13, this.exceptionInterceptor);
               } finally {
                  if (cancelStmt != null) {
                     cancelStmt.close();
                  }

                  if (cancelConn != null) {
                     cancelConn.close();
                  }
               }
            }
         }
      } catch (CJException var15) {
         throw SQLExceptionsMapping.translateException(var15, this.getExceptionInterceptor());
      }
   }

   protected JdbcConnection checkClosed() {
      JdbcConnection c = this.connection;
      if (c == null) {
         throw (StatementIsClosedException)ExceptionFactory.createException(
            StatementIsClosedException.class, Messages.getString("Statement.AlreadyClosed"), this.getExceptionInterceptor()
         );
      } else {
         return c;
      }
   }

   protected void checkForDml(String sql, char firstStatementChar) throws SQLException {
      if (firstStatementChar == 'I'
         || firstStatementChar == 'U'
         || firstStatementChar == 'D'
         || firstStatementChar == 'A'
         || firstStatementChar == 'C'
         || firstStatementChar == 'T'
         || firstStatementChar == 'R') {
         String noCommentSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true);
         if (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "INSERT")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "UPDATE")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DELETE")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DROP")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "CREATE")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "ALTER")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "TRUNCATE")
            || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "RENAME")) {
            throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", this.getExceptionInterceptor());
         }
      }
   }

   protected void checkNullOrEmptyQuery(String sql) throws SQLException {
      if (sql == null) {
         throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009", this.getExceptionInterceptor());
      } else if (sql.length() == 0) {
         throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009", this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearBatch() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.clearBatchedArgs();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearBatchedArgs() {
      this.query.clearBatchedArgs();
   }

   @Override
   public void clearWarnings() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.setClearWarningsCalled(true);
            this.warningChain = null;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void close() throws SQLException {
      try {
         this.realClose(true, true);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected void closeAllOpenResults() throws SQLException {
      JdbcConnection locallyScopedConn = this.connection;
      if (locallyScopedConn != null) {
         synchronized(locallyScopedConn.getConnectionMutex()) {
            if (this.openResults != null) {
               for(ResultSetInternalMethods element : this.openResults) {
                  try {
                     element.realClose(false);
                  } catch (SQLException var7) {
                     AssertionFailedException.shouldNotHappen(var7);
                  }
               }

               this.openResults.clear();
            }
         }
      }
   }

   protected void implicitlyCloseAllOpenResults() throws SQLException {
      this.isImplicitlyClosingResults = true;

      try {
         if (!this.holdResultsOpenOverClose && !this.dontTrackOpenResources.getValue()) {
            if (this.results != null) {
               this.results.realClose(false);
            }

            if (this.generatedKeysResults != null) {
               this.generatedKeysResults.realClose(false);
            }

            this.closeAllOpenResults();
         }
      } finally {
         this.isImplicitlyClosingResults = false;
      }
   }

   @Override
   public void removeOpenResultSet(ResultSetInternalMethods rs) {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               if (this.openResults != null) {
                  this.openResults.remove(rs);
               }

               boolean hasMoreResults = rs.getNextResultset() != null;
               if (this.results == rs && !hasMoreResults) {
                  this.results = null;
               }

               if (this.generatedKeysResults == rs) {
                  this.generatedKeysResults = null;
               }

               if (!this.isImplicitlyClosingResults && !hasMoreResults) {
                  this.checkAndPerformCloseOnCompletionAction();
               }
            }
         } catch (StatementIsClosedException var7) {
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getOpenResultSetCount() {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               return this.openResults != null ? this.openResults.size() : 0;
            }
         } catch (StatementIsClosedException var5) {
            return 0;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   private void checkAndPerformCloseOnCompletionAction() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.isCloseOnCompletion()
               && !this.dontTrackOpenResources.getValue()
               && this.getOpenResultSetCount() == 0
               && (this.results == null || !this.results.hasRows() || this.results.isClosed())
               && (this.generatedKeysResults == null || !this.generatedKeysResults.hasRows() || this.generatedKeysResults.isClosed())) {
               this.realClose(false, false);
            }
         }
      } catch (SQLException var4) {
      }
   }

   private ResultSetInternalMethods createResultSetUsingServerFetch(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            PreparedStatement pStmt = this.connection.prepareStatement(sql, this.query.getResultType().getIntValue(), this.resultSetConcurrency);
            pStmt.setFetchSize(this.query.getResultFetchSize());
            if (this.maxRows > -1) {
               pStmt.setMaxRows(this.maxRows);
            }

            this.statementBegins();
            pStmt.execute();
            ResultSetInternalMethods rs = ((StatementImpl)pStmt).getResultSetInternal();
            rs.setStatementUsedForFetchingRows((ClientPreparedStatement)pStmt);
            this.results = rs;
            return rs;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected boolean createStreamingResultSet() {
      return this.query.getResultType() == Resultset.Type.FORWARD_ONLY
         && this.resultSetConcurrency == 1007
         && this.query.getResultFetchSize() == Integer.MIN_VALUE;
   }

   @Override
   public void enableStreamingResults() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.originalResultSetType = this.query.getResultType();
            this.originalFetchSize = this.query.getResultFetchSize();
            this.setFetchSize(Integer.MIN_VALUE);
            this.setResultSetType(Resultset.Type.FORWARD_ONLY);
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void disableStreamingResults() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.query.getResultFetchSize() == Integer.MIN_VALUE && this.query.getResultType() == Resultset.Type.FORWARD_ONLY) {
               this.setFetchSize(this.originalFetchSize);
               this.setResultSetType(this.originalResultSetType);
            }
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   protected void setupStreamingTimeout(JdbcConnection con) throws SQLException {
      int netTimeoutForStreamingResults = this.session.getPropertySet().getIntegerProperty("netTimeoutForStreamingResults").getValue();
      if (this.createStreamingResultSet() && netTimeoutForStreamingResults > 0) {
         this.executeSimpleNonQuery(con, "SET net_write_timeout=" + netTimeoutForStreamingResults);
      }
   }

   @Override
   public CancelQueryTask startQueryTimer(Query stmtToCancel, int timeout) {
      return this.query.startQueryTimer(stmtToCancel, timeout);
   }

   @Override
   public void stopQueryTimer(CancelQueryTask timeoutTask, boolean rethrowCancelReason, boolean checkCancelTimeout) {
      this.query.stopQueryTimer(timeoutTask, rethrowCancelReason, checkCancelTimeout);
   }

   @Override
   public boolean execute(String sql) throws SQLException {
      try {
         return this.executeInternal(sql, false);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   private boolean executeInternal(String sql, boolean returnGeneratedKeys) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.checkClosed();
         synchronized(locallyScopedConn.getConnectionMutex()) {
            this.checkClosed();
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            this.implicitlyCloseAllOpenResults();
            if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
               this.doPingInstead();
               return true;
            } else {
               char firstNonWsChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
               boolean maybeSelect = firstNonWsChar == 'S';
               this.retrieveGeneratedKeys = returnGeneratedKeys;
               this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys && firstNonWsChar == 'I' && this.containsOnDuplicateKeyInString(sql);
               if (!maybeSelect && locallyScopedConn.isReadOnly()) {
                  throw SQLError.createSQLException(
                     Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009", this.getExceptionInterceptor()
                  );
               } else {
                  boolean var33;
                  try {
                     this.setupStreamingTimeout(locallyScopedConn);
                     if (this.doEscapeProcessing) {
                        Object escapedSqlResult = EscapeProcessor.escapeSQL(
                           sql,
                           this.session.getServerSession().getDefaultTimeZone(),
                           this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                           this.getExceptionInterceptor()
                        );
                        sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                     }

                     CachedResultSetMetaData cachedMetaData = null;
                     ResultSetInternalMethods rs = null;
                     this.batchedGeneratedKeys = null;
                     if (this.useServerFetch()) {
                        rs = this.createResultSetUsingServerFetch(sql);
                     } else {
                        CancelQueryTask timeoutTask = null;
                        String oldCatalog = null;

                        try {
                           timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                           if (!locallyScopedConn.getCatalog().equals(this.getCurrentCatalog())) {
                              oldCatalog = locallyScopedConn.getCatalog();
                              locallyScopedConn.setCatalog(this.getCurrentCatalog());
                           }

                           if (locallyScopedConn.getPropertySet().getBooleanProperty("cacheResultSetMetadata").getValue()) {
                              cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                           }

                           locallyScopedConn.setSessionMaxRows(maybeSelect ? this.maxRows : -1);
                           this.statementBegins();
                           rs = ((NativeSession)locallyScopedConn.getSession())
                              .execSQL(
                                 this,
                                 sql,
                                 this.maxRows,
                                 null,
                                 this.createStreamingResultSet(),
                                 this.getResultSetFactory(),
                                 this.getCurrentCatalog(),
                                 cachedMetaData,
                                 false
                              );
                           if (timeoutTask != null) {
                              this.stopQueryTimer(timeoutTask, true, true);
                              timeoutTask = null;
                           }
                        } catch (OperationCancelledException | CJTimeoutException var26) {
                           throw SQLExceptionsMapping.translateException(var26, this.exceptionInterceptor);
                        } finally {
                           this.stopQueryTimer(timeoutTask, false, false);
                           if (oldCatalog != null) {
                              locallyScopedConn.setCatalog(oldCatalog);
                           }
                        }
                     }

                     if (rs != null) {
                        this.lastInsertId = rs.getUpdateID();
                        this.results = rs;
                        rs.setFirstCharOfQuery(firstNonWsChar);
                        if (rs.hasRows()) {
                           if (cachedMetaData != null) {
                              locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                           } else if (this.session.getPropertySet().getBooleanProperty("cacheResultSetMetadata").getValue()) {
                              locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                           }
                        }
                     }

                     var33 = rs != null && rs.hasRows();
                  } finally {
                     this.query.getStatementExecuting().set(false);
                  }

                  return var33;
               }
            }
         }
      } catch (CJException var30) {
         throw SQLExceptionsMapping.translateException(var30, this.getExceptionInterceptor());
      }
   }

   @Override
   public void statementBegins() {
      this.query.statementBegins();
   }

   @Override
   public void resetCancelledState() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.resetCancelledState();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute(String sql, int returnGeneratedKeys) throws SQLException {
      try {
         return this.executeInternal(sql, returnGeneratedKeys == 1);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute(String sql, int[] generatedKeyIndices) throws SQLException {
      try {
         return this.executeInternal(sql, generatedKeyIndices != null && generatedKeyIndices.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute(String sql, String[] generatedKeyNames) throws SQLException {
      try {
         return this.executeInternal(sql, generatedKeyNames != null && generatedKeyNames.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public int[] executeBatch() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeBatchInternal());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected long[] executeBatchInternal() throws SQLException {
      JdbcConnection locallyScopedConn = this.checkClosed();
      synchronized(locallyScopedConn.getConnectionMutex()) {
         if (locallyScopedConn.isReadOnly()) {
            throw SQLError.createSQLException(Messages.getString("Statement.34") + Messages.getString("Statement.35"), "S1009", this.getExceptionInterceptor());
         } else {
            this.implicitlyCloseAllOpenResults();
            List<Object> batchedArgs = this.query.getBatchedArgs();
            if (batchedArgs != null && batchedArgs.size() != 0) {
               int individualStatementTimeout = this.getTimeoutInMillis();
               this.setTimeoutInMillis(0);
               CancelQueryTask timeoutTask = null;

               try {
                  this.resetCancelledState();
                  this.statementBegins();

                  try {
                     this.retrieveGeneratedKeys = true;
                     long[] updateCounts = null;
                     if (batchedArgs != null) {
                        int nbrCommands = batchedArgs.size();
                        this.batchedGeneratedKeys = new ArrayList<>(batchedArgs.size());
                        boolean multiQueriesEnabled = locallyScopedConn.getPropertySet().getBooleanProperty("allowMultiQueries").getValue();
                        if (multiQueriesEnabled
                           || locallyScopedConn.getPropertySet().getBooleanProperty("rewriteBatchedStatements").getValue() && nbrCommands > 4) {
                           long[] var32 = this.executeBatchUsingMultiQueries(multiQueriesEnabled, nbrCommands, individualStatementTimeout);
                           return var32;
                        }

                        timeoutTask = this.startQueryTimer(this, individualStatementTimeout);
                        updateCounts = new long[nbrCommands];

                        for(int i = 0; i < nbrCommands; ++i) {
                           updateCounts[i] = -3L;
                        }

                        SQLException sqlEx = null;
                        int commandIndex = 0;

                        for(int var30 = 0; var30 < nbrCommands; ++var30) {
                           try {
                              String sql = (String)batchedArgs.get(var30);
                              updateCounts[var30] = this.executeUpdateInternal(sql, true, true);
                              if (timeoutTask != null) {
                                 this.checkCancelTimeout();
                              }

                              this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString(sql) ? 1 : 0);
                           } catch (SQLException var25) {
                              updateCounts[var30] = -3L;
                              if (!this.continueBatchOnError
                                 || var25 instanceof MySQLTimeoutException
                                 || var25 instanceof MySQLStatementCancelledException
                                 || this.hasDeadlockOrTimeoutRolledBackTx(var25)) {
                                 long[] newUpdateCounts = new long[var30];
                                 if (this.hasDeadlockOrTimeoutRolledBackTx(var25)) {
                                    for(int i = 0; i < newUpdateCounts.length; ++i) {
                                       newUpdateCounts[i] = -3L;
                                    }
                                 } else {
                                    System.arraycopy(updateCounts, 0, newUpdateCounts, 0, var30);
                                 }

                                 if (var25 != null) {
                                    throw SQLError.createBatchUpdateException(var25, updateCounts, this.getExceptionInterceptor());
                                 }
                                 break;
                              }
                           }
                        }
                     }

                     if (timeoutTask != null) {
                        this.stopQueryTimer(timeoutTask, true, true);
                        timeoutTask = null;
                     }

                     long[] nbrCommands = updateCounts != null ? updateCounts : new long[0];
                     return nbrCommands;
                  } finally {
                     this.query.getStatementExecuting().set(false);
                  }
               } finally {
                  this.stopQueryTimer(timeoutTask, false, false);
                  this.resetCancelledState();
                  this.setTimeoutInMillis(individualStatementTimeout);
                  this.clearBatch();
               }
            } else {
               return new long[0];
            }
         }
      }
   }

   protected final boolean hasDeadlockOrTimeoutRolledBackTx(SQLException ex) {
      int vendorCode = ex.getErrorCode();
      switch(vendorCode) {
         case 1205:
            return false;
         case 1206:
         case 1213:
            return true;
         default:
            return false;
      }
   }

   private long[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, int individualStatementTimeout) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.checkClosed();
         synchronized(locallyScopedConn.getConnectionMutex()) {
            if (!multiQueriesEnabled) {
               this.session.enableMultiQueries();
            }

            Statement batchStmt = null;
            CancelQueryTask timeoutTask = null;

            long[] var53;
            try {
               long[] updateCounts = new long[nbrCommands];

               for(int i = 0; i < nbrCommands; ++i) {
                  updateCounts[i] = -3L;
               }

               int commandIndex = 0;
               StringBuilder queryBuf = new StringBuilder();
               batchStmt = locallyScopedConn.createStatement();
               timeoutTask = this.startQueryTimer((StatementImpl)batchStmt, individualStatementTimeout);
               int counter = 0;
               String connectionEncoding = locallyScopedConn.getPropertySet().getStringProperty("characterEncoding").getValue();
               int numberOfBytesPerChar = StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")
                  ? 3
                  : (CharsetMapping.isMultibyteCharset(connectionEncoding) ? 2 : 1);
               int escapeAdjust = 1;
               batchStmt.setEscapeProcessing(this.doEscapeProcessing);
               if (this.doEscapeProcessing) {
                  escapeAdjust = 2;
               }

               SQLException sqlEx = null;
               int argumentSetsInBatchSoFar = 0;

               for(commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                  String nextQuery = (String)this.query.getBatchedArgs().get(commandIndex);
                  if (((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.maxAllowedPacket.getValue()) {
                     try {
                        batchStmt.execute(queryBuf.toString(), 1);
                     } catch (SQLException var46) {
                        sqlEx = this.handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, var46);
                     }

                     counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                     queryBuf = new StringBuilder();
                     argumentSetsInBatchSoFar = 0;
                  }

                  queryBuf.append(nextQuery);
                  queryBuf.append(";");
                  ++argumentSetsInBatchSoFar;
               }

               if (queryBuf.length() > 0) {
                  try {
                     batchStmt.execute(queryBuf.toString(), 1);
                  } catch (SQLException var45) {
                     sqlEx = this.handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, var45);
                  }

                  counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
               }

               if (timeoutTask != null) {
                  this.stopQueryTimer(timeoutTask, true, true);
                  timeoutTask = null;
               }

               if (sqlEx != null) {
                  throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
               }

               var53 = updateCounts != null ? updateCounts : new long[0];
            } finally {
               this.stopQueryTimer(timeoutTask, false, false);
               this.resetCancelledState();

               try {
                  if (batchStmt != null) {
                     batchStmt.close();
                  }
               } finally {
                  if (!multiQueriesEnabled) {
                     this.session.disableMultiQueries();
                  }
               }
            }

            return var53;
         }
      } catch (CJException var50) {
         throw SQLExceptionsMapping.translateException(var50, this.getExceptionInterceptor());
      }
   }

   protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, long[] updateCounts) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
            boolean doGenKeys = this.batchedGeneratedKeys != null;
            byte[][] row = (byte[][])null;
            if (doGenKeys) {
               long generatedKey = batchedStatement.getLastInsertID();
               row = new byte[][]{StringUtils.getBytes(Long.toString(generatedKey))};
               this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
            }

            while(batchedStatement.getMoreResults() || batchedStatement.getLargeUpdateCount() != -1L) {
               updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
               if (doGenKeys) {
                  long generatedKey = batchedStatement.getLastInsertID();
                  row = new byte[][]{StringUtils.getBytes(Long.toString(generatedKey))};
                  this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
               }
            }

            return updateCountCounter;
         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, long[] updateCounts, SQLException ex) throws BatchUpdateException, SQLException {
      for(int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; --j) {
         updateCounts[j] = -3L;
      }

      if (this.continueBatchOnError
         && !(ex instanceof MySQLTimeoutException)
         && !(ex instanceof MySQLStatementCancelledException)
         && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
         return ex;
      } else {
         long[] newUpdateCounts = new long[endOfBatchIndex];
         System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
         throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSet executeQuery(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            JdbcConnection locallyScopedConn = this.connection;
            this.retrieveGeneratedKeys = false;
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            this.implicitlyCloseAllOpenResults();
            if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
               this.doPingInstead();
               return this.results;
            } else {
               this.setupStreamingTimeout(locallyScopedConn);
               if (this.doEscapeProcessing) {
                  Object escapedSqlResult = EscapeProcessor.escapeSQL(
                     sql,
                     this.session.getServerSession().getDefaultTimeZone(),
                     this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                     this.getExceptionInterceptor()
                  );
                  sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
               }

               char firstStatementChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
               this.checkForDml(sql, firstStatementChar);
               CachedResultSetMetaData cachedMetaData = null;
               if (this.useServerFetch()) {
                  this.results = this.createResultSetUsingServerFetch(sql);
                  return this.results;
               } else {
                  CancelQueryTask timeoutTask = null;
                  String oldCatalog = null;

                  try {
                     timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                     if (!locallyScopedConn.getCatalog().equals(this.getCurrentCatalog())) {
                        oldCatalog = locallyScopedConn.getCatalog();
                        locallyScopedConn.setCatalog(this.getCurrentCatalog());
                     }

                     if (locallyScopedConn.getPropertySet().getBooleanProperty("cacheResultSetMetadata").getValue()) {
                        cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                     }

                     locallyScopedConn.setSessionMaxRows(this.maxRows);
                     this.statementBegins();
                     this.results = ((NativeSession)locallyScopedConn.getSession())
                        .execSQL(
                           this,
                           sql,
                           this.maxRows,
                           null,
                           this.createStreamingResultSet(),
                           this.getResultSetFactory(),
                           this.getCurrentCatalog(),
                           cachedMetaData,
                           false
                        );
                     if (timeoutTask != null) {
                        this.stopQueryTimer(timeoutTask, true, true);
                        timeoutTask = null;
                     }
                  } catch (OperationCancelledException | CJTimeoutException var16) {
                     throw SQLExceptionsMapping.translateException(var16, this.exceptionInterceptor);
                  } finally {
                     this.query.getStatementExecuting().set(false);
                     this.stopQueryTimer(timeoutTask, false, false);
                     if (oldCatalog != null) {
                        locallyScopedConn.setCatalog(oldCatalog);
                     }
                  }

                  this.lastInsertId = this.results.getUpdateID();
                  if (cachedMetaData != null) {
                     locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                  } else if (this.connection.getPropertySet().getBooleanProperty("cacheResultSetMetadata").getValue()) {
                     locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                  }

                  return this.results;
               }
            }
         }
      } catch (CJException var19) {
         throw SQLExceptionsMapping.translateException(var19, this.getExceptionInterceptor());
      }
   }

   protected void doPingInstead() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.pingTarget != null) {
               try {
                  this.pingTarget.doPing();
               } catch (SQLException var5) {
                  throw var5;
               } catch (Exception var6) {
                  throw SQLError.createSQLException(var6.getMessage(), "08S01", var6, this.getExceptionInterceptor());
               }
            } else {
               this.connection.ping();
            }

            ResultSetInternalMethods fakeSelectOneResultSet = this.generatePingResultSet();
            this.results = fakeSelectOneResultSet;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            String encoding = this.session.getServerSession().getCharacterSetMetadata();
            int collationIndex = this.session.getServerSession().getMetadataCollationIndex();
            Field[] fields = new Field[]{new Field(null, "1", collationIndex, encoding, MysqlType.BIGINT, 1)};
            ArrayList<Row> rows = new ArrayList<>();
            byte[] colVal = new byte[]{49};
            rows.add(new ByteArrayRow(new byte[][]{colVal}, this.getExceptionInterceptor()));
            return this.resultSetFactory.createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(rows, new DefaultColumnDefinition(fields)));
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   public void executeSimpleNonQuery(JdbcConnection c, String nonQuery) throws SQLException {
      try {
         synchronized(c.getConnectionMutex()) {
            ((NativeSession)c.getSession())
               .execSQL(this, nonQuery, -1, null, false, this.getResultSetFactory(), this.getCurrentCatalog(), null, false)
               .close();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql));
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   protected long executeUpdateInternal(String sql, boolean isBatch, boolean returnGeneratedKeys) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            JdbcConnection locallyScopedConn = this.connection;
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            char firstStatementChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
            this.retrieveGeneratedKeys = returnGeneratedKeys;
            this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys && firstStatementChar == 'I' && this.containsOnDuplicateKeyInString(sql);
            ResultSetInternalMethods rs = null;
            if (this.doEscapeProcessing) {
               Object escapedSqlResult = EscapeProcessor.escapeSQL(
                  sql,
                  this.session.getServerSession().getDefaultTimeZone(),
                  this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                  this.getExceptionInterceptor()
               );
               sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
            }

            if (locallyScopedConn.isReadOnly(false)) {
               throw SQLError.createSQLException(
                  Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009", this.getExceptionInterceptor()
               );
            } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "select")) {
               throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03", this.getExceptionInterceptor());
            } else {
               this.implicitlyCloseAllOpenResults();
               CancelQueryTask timeoutTask = null;
               String oldCatalog = null;

               try {
                  timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                  if (!locallyScopedConn.getCatalog().equals(this.getCurrentCatalog())) {
                     oldCatalog = locallyScopedConn.getCatalog();
                     locallyScopedConn.setCatalog(this.getCurrentCatalog());
                  }

                  locallyScopedConn.setSessionMaxRows(-1);
                  this.statementBegins();
                  rs = ((NativeSession)locallyScopedConn.getSession())
                     .execSQL(this, sql, -1, null, false, this.getResultSetFactory(), this.getCurrentCatalog(), null, isBatch);
                  if (timeoutTask != null) {
                     this.stopQueryTimer(timeoutTask, true, true);
                     timeoutTask = null;
                  }
               } catch (OperationCancelledException | CJTimeoutException var18) {
                  throw SQLExceptionsMapping.translateException(var18, this.exceptionInterceptor);
               } finally {
                  this.stopQueryTimer(timeoutTask, false, false);
                  if (oldCatalog != null) {
                     locallyScopedConn.setCatalog(oldCatalog);
                  }

                  if (!isBatch) {
                     this.query.getStatementExecuting().set(false);
                  }
               }

               this.results = rs;
               rs.setFirstCharOfQuery(firstStatementChar);
               this.updateCount = rs.getUpdateCount();
               this.lastInsertId = rs.getUpdateID();
               return this.updateCount;
            }
         }
      } catch (CJException var21) {
         throw SQLExceptionsMapping.translateException(var21, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, autoGeneratedKeys));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnIndexes));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnNames));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public Connection getConnection() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.connection;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getFetchDirection() throws SQLException {
      try {
         return 1000;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getFetchSize() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.query.getResultFetchSize();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (!this.retrieveGeneratedKeys) {
               throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), "S1009", this.getExceptionInterceptor());
            } else if (this.batchedGeneratedKeys == null) {
               return this.lastQueryIsOnDupKeyUpdate
                  ? (this.generatedKeysResults = this.getGeneratedKeysInternal(1L))
                  : (this.generatedKeysResults = this.getGeneratedKeysInternal());
            } else {
               String encoding = this.session.getServerSession().getCharacterSetMetadata();
               int collationIndex = this.session.getServerSession().getMetadataCollationIndex();
               Field[] fields = new Field[]{new Field("", "GENERATED_KEY", collationIndex, encoding, MysqlType.BIGINT_UNSIGNED, 20)};
               this.generatedKeysResults = this.resultSetFactory
                  .createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(this.batchedGeneratedKeys, new DefaultColumnDefinition(fields)));
               return this.generatedKeysResults;
            }
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods getGeneratedKeysInternal() throws SQLException {
      long numKeys = this.getLargeUpdateCount();
      return this.getGeneratedKeysInternal(numKeys);
   }

   protected ResultSetInternalMethods getGeneratedKeysInternal(long numKeys) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            String encoding = this.session.getServerSession().getCharacterSetMetadata();
            int collationIndex = this.session.getServerSession().getMetadataCollationIndex();
            Field[] fields = new Field[]{new Field("", "GENERATED_KEY", collationIndex, encoding, MysqlType.BIGINT_UNSIGNED, 20)};
            ArrayList<Row> rowSet = new ArrayList<>();
            long beginAt = this.getLastInsertID();
            if (this.results != null) {
               String serverInfo = this.results.getServerInfo();
               if (numKeys > 0L && this.results.getFirstCharOfQuery() == 'R' && serverInfo != null && serverInfo.length() > 0) {
                  numKeys = this.getRecordCountFromInfo(serverInfo);
               }

               if (beginAt != 0L && numKeys > 0L) {
                  for(int i = 0; (long)i < numKeys; ++i) {
                     byte[][] row = new byte[1][];
                     if (beginAt > 0L) {
                        row[0] = StringUtils.getBytes(Long.toString(beginAt));
                     } else {
                        byte[] asBytes = new byte[]{
                           (byte)((int)(beginAt >>> 56)),
                           (byte)((int)(beginAt >>> 48)),
                           (byte)((int)(beginAt >>> 40)),
                           (byte)((int)(beginAt >>> 32)),
                           (byte)((int)(beginAt >>> 24)),
                           (byte)((int)(beginAt >>> 16)),
                           (byte)((int)(beginAt >>> 8)),
                           (byte)((int)(beginAt & 255L))
                        };
                        BigInteger val = new BigInteger(1, asBytes);
                        row[0] = val.toString().getBytes();
                     }

                     rowSet.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
                     beginAt += (long)this.connection.getAutoIncrementIncrement();
                  }
               }
            }

            return this.resultSetFactory.createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(rowSet, new DefaultColumnDefinition(fields)));
         }
      } catch (CJException var18) {
         throw SQLExceptionsMapping.translateException(var18, this.getExceptionInterceptor());
      }
   }

   public long getLastInsertID() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.lastInsertId;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public long getLongUpdateCount() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
               return -1L;
            } else {
               return this.results.hasRows() ? -1L : this.updateCount;
            }
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getMaxFieldSize() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.maxFieldSize;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getMaxRows() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.maxRows <= 0 ? 0 : this.maxRows;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean getMoreResults() throws SQLException {
      try {
         return this.getMoreResults(1);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean getMoreResults(int current) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
               return false;
            } else {
               boolean streamingMode = this.createStreamingResultSet();
               if (streamingMode && this.results.hasRows()) {
                  while(this.results.next()) {
                  }
               }

               ResultSetInternalMethods nextResultSet = (ResultSetInternalMethods)this.results.getNextResultset();
               switch(current) {
                  case 1:
                     if (this.results != null) {
                        if (!streamingMode && !this.dontTrackOpenResources.getValue()) {
                           this.results.realClose(false);
                        }

                        this.results.clearNextResultset();
                     }
                     break;
                  case 2:
                     if (!this.dontTrackOpenResources.getValue()) {
                        this.openResults.add(this.results);
                     }

                     this.results.clearNextResultset();
                     break;
                  case 3:
                     if (this.results != null) {
                        if (!streamingMode && !this.dontTrackOpenResources.getValue()) {
                           this.results.realClose(false);
                        }

                        this.results.clearNextResultset();
                     }

                     this.closeAllOpenResults();
                     break;
                  default:
                     throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009", this.getExceptionInterceptor());
               }

               this.results = nextResultSet;
               if (this.results == null) {
                  this.updateCount = -1L;
                  this.lastInsertId = -1L;
               } else if (this.results.hasRows()) {
                  this.updateCount = -1L;
                  this.lastInsertId = -1L;
               } else {
                  this.updateCount = this.results.getUpdateCount();
                  this.lastInsertId = this.results.getUpdateID();
               }

               boolean moreResults = this.results != null && this.results.hasRows();
               if (!moreResults) {
                  this.checkAndPerformCloseOnCompletionAction();
               }

               return moreResults;
            }
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getQueryTimeout() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.getTimeoutInMillis() / 1000;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   private long getRecordCountFromInfo(String serverInfo) {
      StringBuilder recordsBuf = new StringBuilder();
      long recordsCount = 0L;
      long duplicatesCount = 0L;
      char c = '\u0000';
      int length = serverInfo.length();

      int i;
      for(i = 0; i < length; ++i) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      recordsBuf.append(c);
      ++i;

      while(i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         recordsBuf.append(c);
         ++i;
      }

      recordsCount = Long.parseLong(recordsBuf.toString());

      StringBuilder duplicatesBuf;
      for(duplicatesBuf = new StringBuilder(); i < length; ++i) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      duplicatesBuf.append(c);
      ++i;

      while(i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         duplicatesBuf.append(c);
         ++i;
      }

      duplicatesCount = Long.parseLong(duplicatesBuf.toString());
      return recordsCount - duplicatesCount;
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.results != null && this.results.hasRows() ? this.results : null;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getResultSetConcurrency() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.resultSetConcurrency;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      try {
         return 1;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods getResultSetInternal() {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               return this.results;
            }
         } catch (StatementIsClosedException var5) {
            return this.results;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getResultSetType() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.query.getResultType().getIntValue();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getUpdateCount() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.getLargeUpdateCount());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.isClearWarningsCalled()) {
               return null;
            } else {
               SQLWarning pendingWarningsFromServer = this.session.getProtocol().convertShowWarningsToSQLWarnings(0, false);
               if (this.warningChain != null) {
                  this.warningChain.setNextWarning(pendingWarningsFromServer);
               } else {
                  this.warningChain = pendingWarningsFromServer;
               }

               return this.warningChain;
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
      JdbcConnection locallyScopedConn = this.connection;
      if (locallyScopedConn != null && !this.isClosed) {
         if (!this.dontTrackOpenResources.getValue()) {
            locallyScopedConn.unregisterStatement(this);
         }

         if (this.useUsageAdvisor && !calledExplicitly) {
            String message = Messages.getString("Statement.63") + Messages.getString("Statement.64");
            this.query
               .getEventSink()
               .consumeEvent(
                  new ProfilerEventImpl(
                     (byte)0,
                     "",
                     this.getCurrentCatalog(),
                     this.session.getThreadId(),
                     this.getId(),
                     -1,
                     System.currentTimeMillis(),
                     0L,
                     Constants.MILLIS_I18N,
                     null,
                     this.pointOfOrigin,
                     message
                  )
               );
         }

         if (closeOpenResults) {
            closeOpenResults = !this.holdResultsOpenOverClose && !this.dontTrackOpenResources.getValue();
         }

         if (closeOpenResults) {
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

            this.closeAllOpenResults();
         }

         this.isClosed = true;
         this.closeQuery();
         this.results = null;
         this.generatedKeysResults = null;
         this.connection = null;
         this.session = null;
         this.warningChain = null;
         this.openResults = null;
         this.batchedGeneratedKeys = null;
         this.pingTarget = null;
         this.resultSetFactory = null;
      }
   }

   @Override
   public void setCursorName(String name) throws SQLException {
      try {
         ;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setEscapeProcessing(boolean enable) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.doEscapeProcessing = enable;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setFetchDirection(int direction) throws SQLException {
      try {
         switch(direction) {
            case 1000:
            case 1001:
            case 1002:
               return;
            default:
               throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009", this.getExceptionInterceptor());
         }
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setFetchSize(int rows) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if ((rows >= 0 || rows == Integer.MIN_VALUE) && (this.maxRows <= 0 || rows <= this.getMaxRows())) {
               this.query.setResultFetchSize(rows);
            } else {
               throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009", this.getExceptionInterceptor());
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose) {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               this.holdResultsOpenOverClose = holdResultsOpenOverClose;
            }
         } catch (StatementIsClosedException var6) {
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setMaxFieldSize(int max) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (max < 0) {
               throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009", this.getExceptionInterceptor());
            } else {
               int maxBuf = this.maxAllowedPacket.getValue();
               if (max > maxBuf) {
                  throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[]{(long)maxBuf}), "S1009", this.getExceptionInterceptor());
               } else {
                  this.maxFieldSize = max;
               }
            }
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setMaxRows(int max) throws SQLException {
      try {
         this.setLargeMaxRows((long)max);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setQueryTimeout(int seconds) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (seconds < 0) {
               throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009", this.getExceptionInterceptor());
            } else {
               this.setTimeoutInMillis(seconds * 1000);
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   void setResultSetConcurrency(int concurrencyFlag) throws SQLException {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               this.resultSetConcurrency = concurrencyFlag;
               this.resultSetFactory = new ResultSetFactory(this.connection, this);
            }
         } catch (StatementIsClosedException var6) {
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   void setResultSetType(Resultset.Type typeFlag) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.setResultType(typeFlag);
            this.resultSetFactory = new ResultSetFactory(this.connection, this);
         }
      } catch (StatementIsClosedException var5) {
      }
   }

   void setResultSetType(int typeFlag) throws SQLException {
      Resultset.Type.fromValue(typeFlag, Resultset.Type.FORWARD_ONLY);
   }

   protected void getBatchedGeneratedKeys(Statement batchedStatement) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.retrieveGeneratedKeys) {
               ResultSet rs = null;

               try {
                  rs = batchedStatement.getGeneratedKeys();

                  while(rs.next()) {
                     this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                  }
               } finally {
                  if (rs != null) {
                     rs.close();
                  }
               }
            }
         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   protected void getBatchedGeneratedKeys(int maxKeys) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.retrieveGeneratedKeys) {
               ResultSet rs = null;

               try {
                  rs = maxKeys == 0 ? this.getGeneratedKeysInternal() : this.getGeneratedKeysInternal((long)maxKeys);

                  while(rs.next()) {
                     this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                  }
               } finally {
                  this.isImplicitlyClosingResults = true;

                  try {
                     if (rs != null) {
                        rs.close();
                     }
                  } finally {
                     this.isImplicitlyClosingResults = false;
                  }
               }
            }
         }
      } catch (CJException var27) {
         throw SQLExceptionsMapping.translateException(var27, this.getExceptionInterceptor());
      }
   }

   private boolean useServerFetch() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.session.getPropertySet().getBooleanProperty("useCursorFetch").getValue()
               && this.query.getResultFetchSize() > 0
               && this.query.getResultType() == Resultset.Type.FORWARD_ONLY;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isClosed() throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn == null) {
            return true;
         } else {
            synchronized(locallyScopedConn.getConnectionMutex()) {
               return this.isClosed;
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isPoolable() throws SQLException {
      try {
         return this.isPoolable;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setPoolable(boolean poolable) throws SQLException {
      try {
         this.isPoolable = poolable;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      try {
         this.checkClosed();
         return iface.isInstance(this);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      try {
         try {
            return iface.cast(this);
         } catch (ClassCastException var4) {
            throw SQLError.createSQLException(
               Messages.getString("Common.UnableToUnwrap", new Object[]{iface.toString()}), "S1009", this.getExceptionInterceptor()
            );
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   protected static int findStartOfStatement(String sql) {
      int statementStartPos = 0;
      if (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*")) {
         statementStartPos = sql.indexOf("*/");
         if (statementStartPos == -1) {
            statementStartPos = 0;
         } else {
            statementStartPos += 2;
         }
      } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "--") || StringUtils.startsWithIgnoreCaseAndWs(sql, "#")) {
         statementStartPos = sql.indexOf(10);
         if (statementStartPos == -1) {
            statementStartPos = sql.indexOf(13);
            if (statementStartPos == -1) {
               statementStartPos = 0;
            }
         }
      }

      return statementStartPos;
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return this.session.getLocalInfileInputStream();
   }

   @Override
   public void setLocalInfileInputStream(InputStream stream) {
      this.session.setLocalInfileInputStream(stream);
   }

   @Override
   public void setPingTarget(PingTarget pingTarget) {
      this.pingTarget = pingTarget;
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   protected boolean containsOnDuplicateKeyInString(String sql) {
      return ParseInfo.getOnDuplicateKeyLocation(
            sql, this.dontCheckOnDuplicateKeyUpdateInSQL, this.rewriteBatchedStatements.getValue(), this.session.getServerSession().isNoBackslashEscapesSet()
         )
         != -1;
   }

   @Override
   public void closeOnCompletion() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.closeOnCompletion = true;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.closeOnCompletion;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public long[] executeLargeBatch() throws SQLException {
      try {
         return this.executeBatchInternal();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, false);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, autoGeneratedKeys == 1);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, columnIndexes != null && columnIndexes.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, columnNames != null && columnNames.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getLargeMaxRows() throws SQLException {
      try {
         return (long)this.getMaxRows();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getLargeUpdateCount() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
               return -1L;
            } else {
               return this.results.hasRows() ? -1L : this.results.getUpdateCount();
            }
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setLargeMaxRows(long max) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (max <= 50000000L && max >= 0L) {
               if (max == 0L) {
                  max = -1L;
               }

               this.maxRows = (int)max;
            } else {
               throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009", this.getExceptionInterceptor());
            }
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getCurrentCatalog() {
      return this.query.getCurrentCatalog();
   }

   public long getServerStatementId() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, Messages.getString("Statement.65"));
   }

   @Override
   public <T extends Resultset, M extends Message> ProtocolEntityFactory<T, M> getResultSetFactory() {
      return this.resultSetFactory;
   }

   @Override
   public int getId() {
      return this.query.getId();
   }

   @Override
   public void setCancelStatus(Query.CancelStatus cs) {
      this.query.setCancelStatus(cs);
   }

   @Override
   public void checkCancelTimeout() {
      try {
         this.query.checkCancelTimeout();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public Session getSession() {
      return this.session;
   }

   @Override
   public Object getCancelTimeoutMutex() {
      return this.query.getCancelTimeoutMutex();
   }

   @Override
   public void closeQuery() {
      if (this.query != null) {
         this.query.closeQuery();
      }
   }

   @Override
   public int getResultFetchSize() {
      return this.query.getResultFetchSize();
   }

   @Override
   public void setResultFetchSize(int fetchSize) {
      this.query.setResultFetchSize(fetchSize);
   }

   @Override
   public Resultset.Type getResultType() {
      return this.query.getResultType();
   }

   @Override
   public void setResultType(Resultset.Type resultSetType) {
      this.query.setResultType(resultSetType);
   }

   @Override
   public int getTimeoutInMillis() {
      return this.query.getTimeoutInMillis();
   }

   @Override
   public void setTimeoutInMillis(int timeoutInMillis) {
      this.query.setTimeoutInMillis(timeoutInMillis);
   }

   @Override
   public ProfilerEventHandler getEventSink() {
      return this.query.getEventSink();
   }

   @Override
   public void setEventSink(ProfilerEventHandler eventSink) {
      this.query.setEventSink(eventSink);
   }

   @Override
   public AtomicBoolean getStatementExecuting() {
      return this.query.getStatementExecuting();
   }

   @Override
   public void setCurrentCatalog(String currentCatalog) {
      this.query.setCurrentCatalog(currentCatalog);
   }

   @Override
   public boolean isClearWarningsCalled() {
      return this.query.isClearWarningsCalled();
   }

   @Override
   public void setClearWarningsCalled(boolean clearWarningsCalled) {
      this.query.setClearWarningsCalled(clearWarningsCalled);
   }

   @Override
   public Query getQuery() {
      return this.query;
   }
}
