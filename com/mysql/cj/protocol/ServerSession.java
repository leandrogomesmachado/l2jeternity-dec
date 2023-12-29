package com.mysql.cj.protocol;

import com.mysql.cj.ServerVersion;
import java.util.Map;
import java.util.TimeZone;

public interface ServerSession {
   int TRANSACTION_NOT_STARTED = 0;
   int TRANSACTION_IN_PROGRESS = 1;
   int TRANSACTION_STARTED = 2;
   int TRANSACTION_COMPLETED = 3;
   String LOCAL_CHARACTER_SET_RESULTS = "local.character_set_results";

   ServerCapabilities getCapabilities();

   void setCapabilities(ServerCapabilities var1);

   int getStatusFlags();

   void setStatusFlags(int var1);

   void setStatusFlags(int var1, boolean var2);

   int getOldStatusFlags();

   void setOldStatusFlags(int var1);

   int getServerDefaultCollationIndex();

   void setServerDefaultCollationIndex(int var1);

   int getTransactionState();

   boolean inTransactionOnServer();

   boolean cursorExists();

   boolean isAutocommit();

   boolean hasMoreResults();

   boolean isLastRowSent();

   boolean noGoodIndexUsed();

   boolean noIndexUsed();

   boolean queryWasSlow();

   long getClientParam();

   void setClientParam(long var1);

   boolean useMultiResults();

   boolean isEOFDeprecated();

   boolean hasLongColumnInfo();

   void setHasLongColumnInfo(boolean var1);

   Map<String, String> getServerVariables();

   String getServerVariable(String var1);

   int getServerVariable(String var1, int var2);

   void setServerVariables(Map<String, String> var1);

   boolean characterSetNamesMatches(String var1);

   ServerVersion getServerVersion();

   boolean isVersion(ServerVersion var1);

   String getServerDefaultCharset();

   String getErrorMessageEncoding();

   void setErrorMessageEncoding(String var1);

   int getMaxBytesPerChar(String var1);

   int getMaxBytesPerChar(Integer var1, String var2);

   String getEncodingForIndex(int var1);

   void configureCharacterSets();

   String getCharacterSetMetadata();

   void setCharacterSetMetadata(String var1);

   int getMetadataCollationIndex();

   void setMetadataCollationIndex(int var1);

   String getCharacterSetResultsOnServer();

   void setCharacterSetResultsOnServer(String var1);

   boolean isLowerCaseTableNames();

   boolean storesLowerCaseTableNames();

   boolean isQueryCacheEnabled();

   boolean isNoBackslashEscapesSet();

   boolean useAnsiQuotedIdentifiers();

   long getThreadId();

   void setThreadId(long var1);

   boolean isAutoCommit();

   void setAutoCommit(boolean var1);

   TimeZone getServerTimeZone();

   void setServerTimeZone(TimeZone var1);

   TimeZone getDefaultTimeZone();

   void setDefaultTimeZone(TimeZone var1);
}
