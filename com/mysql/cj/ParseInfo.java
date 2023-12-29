package com.mysql.cj;

import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.util.StringUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ParseInfo {
   protected static final String[] ON_DUPLICATE_KEY_UPDATE_CLAUSE = new String[]{"ON", "DUPLICATE", "KEY", "UPDATE"};
   private char firstStmtChar = 0;
   private boolean foundLoadData = false;
   long lastUsed = 0L;
   int statementLength = 0;
   int statementStartPos = 0;
   boolean canRewriteAsMultiValueInsert = false;
   byte[][] staticSql = (byte[][])null;
   boolean isOnDuplicateKeyUpdate = false;
   int locationOfOnDuplicateKeyUpdate = -1;
   String valuesClause;
   boolean parametersInDuplicateKeyClause = false;
   String charEncoding;
   private ParseInfo batchHead;
   private ParseInfo batchValues;
   private ParseInfo batchODKUClause;

   private ParseInfo(
      byte[][] staticSql,
      char firstStmtChar,
      boolean foundLoadData,
      boolean isOnDuplicateKeyUpdate,
      int locationOfOnDuplicateKeyUpdate,
      int statementLength,
      int statementStartPos
   ) {
      this.firstStmtChar = firstStmtChar;
      this.foundLoadData = foundLoadData;
      this.isOnDuplicateKeyUpdate = isOnDuplicateKeyUpdate;
      this.locationOfOnDuplicateKeyUpdate = locationOfOnDuplicateKeyUpdate;
      this.statementLength = statementLength;
      this.statementStartPos = statementStartPos;
      this.staticSql = staticSql;
   }

   public ParseInfo(String sql, Session session, String encoding) {
      this(sql, session, encoding, true);
   }

   public ParseInfo(String sql, Session session, String encoding, boolean buildRewriteInfo) {
      try {
         if (sql == null) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("PreparedStatement.61"), session.getExceptionInterceptor()
            );
         }

         this.charEncoding = encoding;
         this.lastUsed = System.currentTimeMillis();
         String quotedIdentifierString = session.getIdentifierQuoteString();
         char quotedIdentifierChar = 0;
         if (quotedIdentifierString != null && !quotedIdentifierString.equals(" ") && quotedIdentifierString.length() > 0) {
            quotedIdentifierChar = quotedIdentifierString.charAt(0);
         }

         this.statementLength = sql.length();
         ArrayList<int[]> endpointList = new ArrayList<>();
         boolean inQuotes = false;
         char quoteChar = 0;
         boolean inQuotedId = false;
         int lastParmEnd = 0;
         boolean noBackslashEscapes = session.getServerSession().isNoBackslashEscapesSet();
         this.statementStartPos = findStartOfStatement(sql);

         label217:
         for(int i = this.statementStartPos; i < this.statementLength; ++i) {
            char c = sql.charAt(i);
            if (this.firstStmtChar == 0 && Character.isLetter(c)) {
               this.firstStmtChar = Character.toUpperCase(c);
               if (this.firstStmtChar == 'I') {
                  this.locationOfOnDuplicateKeyUpdate = getOnDuplicateKeyLocation(
                     sql,
                     session.getPropertySet().getBooleanProperty("dontCheckOnDuplicateKeyUpdateInSQL").getValue(),
                     session.getPropertySet().getBooleanProperty("rewriteBatchedStatements").getValue(),
                     session.getServerSession().isNoBackslashEscapesSet()
                  );
                  this.isOnDuplicateKeyUpdate = this.locationOfOnDuplicateKeyUpdate != -1;
               }
            }

            if (!noBackslashEscapes && c == '\\' && i < this.statementLength - 1) {
               ++i;
            } else {
               if (!inQuotes && quotedIdentifierChar != 0 && c == quotedIdentifierChar) {
                  inQuotedId = !inQuotedId;
               } else if (!inQuotedId) {
                  if (inQuotes) {
                     if ((c == '\'' || c == '"') && c == quoteChar) {
                        if (i < this.statementLength - 1 && sql.charAt(i + 1) == quoteChar) {
                           ++i;
                           continue;
                        }

                        inQuotes = !inQuotes;
                        quoteChar = 0;
                     } else if ((c == '\'' || c == '"') && c == quoteChar) {
                        inQuotes = !inQuotes;
                        quoteChar = 0;
                     }
                  } else {
                     if (c == '#' || c == '-' && i + 1 < this.statementLength && sql.charAt(i + 1) == '-') {
                        for(int endOfStmt = this.statementLength - 1; i < endOfStmt; ++i) {
                           c = sql.charAt(i);
                           if (c == '\r' || c == '\n') {
                              continue label217;
                           }
                        }
                        continue;
                     }

                     if (c == '/' && i + 1 < this.statementLength) {
                        char cNext = sql.charAt(i + 1);
                        if (cNext == '*') {
                           i += 2;

                           for(int j = i; j < this.statementLength; ++j) {
                              ++i;
                              cNext = sql.charAt(j);
                              if (cNext == '*' && j + 1 < this.statementLength && sql.charAt(j + 1) == '/') {
                                 if (++i < this.statementLength) {
                                    c = sql.charAt(i);
                                 }
                                 break;
                              }
                           }
                        }
                     } else if (c == '\'' || c == '"') {
                        inQuotes = true;
                        quoteChar = c;
                     }
                  }
               }

               if (c == '?' && !inQuotes && !inQuotedId) {
                  endpointList.add(new int[]{lastParmEnd, i});
                  lastParmEnd = i + 1;
                  if (this.isOnDuplicateKeyUpdate && i > this.locationOfOnDuplicateKeyUpdate) {
                     this.parametersInDuplicateKeyClause = true;
                  }
               }
            }
         }

         if (this.firstStmtChar == 'L') {
            if (StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA")) {
               this.foundLoadData = true;
            } else {
               this.foundLoadData = false;
            }
         } else {
            this.foundLoadData = false;
         }

         endpointList.add(new int[]{lastParmEnd, this.statementLength});
         this.staticSql = new byte[endpointList.size()][];

         for(int var21 = 0; var21 < this.staticSql.length; ++var21) {
            int[] ep = (int[])endpointList.get(var21);
            int end = ep[1];
            int begin = ep[0];
            int len = end - begin;
            if (this.foundLoadData) {
               this.staticSql[var21] = StringUtils.getBytes(sql, begin, len);
            } else if (encoding != null) {
               this.staticSql[var21] = StringUtils.getBytes(sql, begin, len, encoding);
            } else {
               byte[] buf = new byte[len];

               for(int j = 0; j < len; ++j) {
                  buf[j] = (byte)sql.charAt(begin + j);
               }

               this.staticSql[var21] = buf;
            }
         }
      } catch (StringIndexOutOfBoundsException var20) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("PreparedStatement.62", new Object[]{sql}), var20, session.getExceptionInterceptor()
         );
      }

      if (buildRewriteInfo) {
         this.canRewriteAsMultiValueInsert = canRewrite(sql, this.isOnDuplicateKeyUpdate, this.locationOfOnDuplicateKeyUpdate, this.statementStartPos)
            && !this.parametersInDuplicateKeyClause;
         if (this.canRewriteAsMultiValueInsert && session.getPropertySet().getBooleanProperty("rewriteBatchedStatements").getValue()) {
            this.buildRewriteBatchedParams(sql, session, encoding);
         }
      }
   }

   public byte[][] getStaticSql() {
      return this.staticSql;
   }

   public String getValuesClause() {
      return this.valuesClause;
   }

   public int getLocationOfOnDuplicateKeyUpdate() {
      return this.locationOfOnDuplicateKeyUpdate;
   }

   public boolean canRewriteAsMultiValueInsertAtSqlLevel() {
      return this.canRewriteAsMultiValueInsert;
   }

   public boolean containsOnDuplicateKeyUpdateInSQL() {
      return this.isOnDuplicateKeyUpdate;
   }

   private void buildRewriteBatchedParams(String sql, Session session, String encoding) {
      this.valuesClause = this.extractValuesClause(sql, session.getIdentifierQuoteString());
      String odkuClause = this.isOnDuplicateKeyUpdate ? sql.substring(this.locationOfOnDuplicateKeyUpdate) : null;
      String headSql = null;
      if (this.isOnDuplicateKeyUpdate) {
         headSql = sql.substring(0, this.locationOfOnDuplicateKeyUpdate);
      } else {
         headSql = sql;
      }

      this.batchHead = new ParseInfo(headSql, session, encoding, false);
      this.batchValues = new ParseInfo("," + this.valuesClause, session, encoding, false);
      this.batchODKUClause = null;
      if (odkuClause != null && odkuClause.length() > 0) {
         this.batchODKUClause = new ParseInfo("," + this.valuesClause + " " + odkuClause, session, encoding, false);
      }
   }

   private String extractValuesClause(String sql, String quoteCharStr) {
      int indexOfValues = -1;
      int valuesSearchStart = this.statementStartPos;

      while(indexOfValues == -1) {
         if (quoteCharStr.length() > 0) {
            indexOfValues = StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES", quoteCharStr, quoteCharStr, StringUtils.SEARCH_MODE__MRK_COM_WS);
         } else {
            indexOfValues = StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES");
         }

         if (indexOfValues <= 0) {
            break;
         }

         char c = sql.charAt(indexOfValues - 1);
         if (!Character.isWhitespace(c) && c != ')' && c != '`') {
            valuesSearchStart = indexOfValues + 6;
            indexOfValues = -1;
         } else {
            c = sql.charAt(indexOfValues + 6);
            if (!Character.isWhitespace(c) && c != '(') {
               valuesSearchStart = indexOfValues + 6;
               indexOfValues = -1;
            }
         }
      }

      if (indexOfValues == -1) {
         return null;
      } else {
         int indexOfFirstParen = sql.indexOf(40, indexOfValues + 6);
         if (indexOfFirstParen == -1) {
            return null;
         } else {
            int endOfValuesClause = sql.lastIndexOf(41);
            if (endOfValuesClause == -1) {
               return null;
            } else {
               if (this.isOnDuplicateKeyUpdate) {
                  endOfValuesClause = this.locationOfOnDuplicateKeyUpdate - 1;
               }

               return sql.substring(indexOfFirstParen, endOfValuesClause + 1);
            }
         }
      }
   }

   public synchronized ParseInfo getParseInfoForBatch(int numBatch) {
      AppendingBatchVisitor apv = new AppendingBatchVisitor();
      this.buildInfoForBatch(numBatch, apv);
      return new ParseInfo(
         apv.getStaticSqlStrings(),
         this.firstStmtChar,
         this.foundLoadData,
         this.isOnDuplicateKeyUpdate,
         this.locationOfOnDuplicateKeyUpdate,
         this.statementLength,
         this.statementStartPos
      );
   }

   public String getSqlForBatch(int numBatch) throws UnsupportedEncodingException {
      ParseInfo batchInfo = this.getParseInfoForBatch(numBatch);
      return batchInfo.getSqlForBatch();
   }

   public String getSqlForBatch() throws UnsupportedEncodingException {
      int size = 0;
      byte[][] sqlStrings = this.staticSql;
      int sqlStringsLength = sqlStrings.length;

      for(int i = 0; i < sqlStringsLength; ++i) {
         size += sqlStrings[i].length;
         ++size;
      }

      StringBuilder buf = new StringBuilder(size);

      for(int i = 0; i < sqlStringsLength - 1; ++i) {
         buf.append(StringUtils.toString(sqlStrings[i], this.charEncoding));
         buf.append("?");
      }

      buf.append(StringUtils.toString(sqlStrings[sqlStringsLength - 1]));
      return buf.toString();
   }

   private void buildInfoForBatch(int numBatch, BatchVisitor visitor) {
      byte[][] headStaticSql = this.batchHead.staticSql;
      int headStaticSqlLength = headStaticSql.length;
      if (headStaticSqlLength > 1) {
         for(int i = 0; i < headStaticSqlLength - 1; ++i) {
            visitor.append(headStaticSql[i]).increment();
         }
      }

      byte[] endOfHead = headStaticSql[headStaticSqlLength - 1];
      byte[][] valuesStaticSql = this.batchValues.staticSql;
      byte[] beginOfValues = valuesStaticSql[0];
      visitor.merge(endOfHead, beginOfValues).increment();
      int numValueRepeats = numBatch - 1;
      if (this.batchODKUClause != null) {
         --numValueRepeats;
      }

      int valuesStaticSqlLength = valuesStaticSql.length;
      byte[] endOfValues = valuesStaticSql[valuesStaticSqlLength - 1];

      for(int i = 0; i < numValueRepeats; ++i) {
         for(int j = 1; j < valuesStaticSqlLength - 1; ++j) {
            visitor.append(valuesStaticSql[j]).increment();
         }

         visitor.merge(endOfValues, beginOfValues).increment();
      }

      if (this.batchODKUClause != null) {
         byte[][] batchOdkuStaticSql = this.batchODKUClause.staticSql;
         byte[] beginOfOdku = batchOdkuStaticSql[0];
         visitor.decrement().merge(endOfValues, beginOfOdku).increment();
         int batchOdkuStaticSqlLength = batchOdkuStaticSql.length;
         if (numBatch > 1) {
            for(int i = 1; i < batchOdkuStaticSqlLength; ++i) {
               visitor.append(batchOdkuStaticSql[i]).increment();
            }
         } else {
            visitor.decrement().append(batchOdkuStaticSql[batchOdkuStaticSqlLength - 1]);
         }
      } else {
         visitor.decrement().append(this.staticSql[this.staticSql.length - 1]);
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

   public static int getOnDuplicateKeyLocation(
      String sql, boolean dontCheckOnDuplicateKeyUpdateInSQL, boolean rewriteBatchedStatements, boolean noBackslashEscapes
   ) {
      return dontCheckOnDuplicateKeyUpdateInSQL && !rewriteBatchedStatements
         ? -1
         : StringUtils.indexOfIgnoreCase(
            0, sql, ON_DUPLICATE_KEY_UPDATE_CLAUSE, "\"'`", "\"'`", noBackslashEscapes ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL
         );
   }

   protected static boolean canRewrite(String sql, boolean isOnDuplicateKeyUpdate, int locationOfOnDuplicateKeyUpdate, int statementStartPos) {
      if (StringUtils.startsWithIgnoreCaseAndWs(sql, "INSERT", statementStartPos)) {
         if (StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) != -1) {
            return false;
         } else {
            if (isOnDuplicateKeyUpdate) {
               int updateClausePos = StringUtils.indexOfIgnoreCase(locationOfOnDuplicateKeyUpdate, sql, " UPDATE ");
               if (updateClausePos != -1) {
                  return StringUtils.indexOfIgnoreCase(updateClausePos, sql, "LAST_INSERT_ID", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1;
               }
            }

            return true;
         }
      } else {
         return StringUtils.startsWithIgnoreCaseAndWs(sql, "REPLACE", statementStartPos)
            && StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1;
      }
   }

   public boolean isFoundLoadData() {
      return this.foundLoadData;
   }

   public char getFirstStmtChar() {
      return this.firstStmtChar;
   }
}
