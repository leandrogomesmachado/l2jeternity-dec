package com.mysql.cj.protocol.x;

import com.mysql.cj.ServerVersion;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.ServerCapabilities;
import com.mysql.cj.protocol.ServerSession;
import java.util.Map;
import java.util.TimeZone;

public class XServerSession implements ServerSession {
   XServerCapabilities serverCapabilities = null;
   private long clientId = -1L;
   private TimeZone defaultTimeZone = TimeZone.getDefault();

   @Override
   public ServerCapabilities getCapabilities() {
      return this.serverCapabilities;
   }

   @Override
   public void setCapabilities(ServerCapabilities capabilities) {
      this.serverCapabilities = (XServerCapabilities)capabilities;
   }

   @Override
   public int getStatusFlags() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setStatusFlags(int statusFlags) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setStatusFlags(int statusFlags, boolean saveOldStatusFlags) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getOldStatusFlags() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setOldStatusFlags(int statusFlags) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getServerDefaultCollationIndex() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setServerDefaultCollationIndex(int serverDefaultCollationIndex) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getTransactionState() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean inTransactionOnServer() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean cursorExists() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isAutocommit() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean hasMoreResults() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isLastRowSent() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean noGoodIndexUsed() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean noIndexUsed() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean queryWasSlow() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public long getClientParam() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setClientParam(long clientParam) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean useMultiResults() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isEOFDeprecated() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean hasLongColumnInfo() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setHasLongColumnInfo(boolean hasLongColumnInfo) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public Map<String, String> getServerVariables() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getServerVariable(String name) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getServerVariable(String variableName, int fallbackValue) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setServerVariables(Map<String, String> serverVariables) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean characterSetNamesMatches(String mysqlEncodingName) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public ServerVersion getServerVersion() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isVersion(ServerVersion version) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getServerDefaultCharset() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getErrorMessageEncoding() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setErrorMessageEncoding(String errorMessageEncoding) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getMaxBytesPerChar(String javaCharsetName) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getMaxBytesPerChar(Integer charsetIndex, String javaCharsetName) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getEncodingForIndex(int collationIndex) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void configureCharacterSets() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getCharacterSetMetadata() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setCharacterSetMetadata(String characterSetMetadata) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public int getMetadataCollationIndex() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setMetadataCollationIndex(int metadataCollationIndex) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getCharacterSetResultsOnServer() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setCharacterSetResultsOnServer(String characterSetResultsOnServer) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isLowerCaseTableNames() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean storesLowerCaseTableNames() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isQueryCacheEnabled() {
      return false;
   }

   @Override
   public boolean isNoBackslashEscapesSet() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean useAnsiQuotedIdentifiers() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public long getThreadId() {
      return this.clientId;
   }

   @Override
   public void setThreadId(long threadId) {
      this.clientId = threadId;
   }

   @Override
   public boolean isAutoCommit() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setAutoCommit(boolean autoCommit) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public TimeZone getServerTimeZone() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setServerTimeZone(TimeZone serverTimeZone) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public TimeZone getDefaultTimeZone() {
      return this.defaultTimeZone;
   }

   @Override
   public void setDefaultTimeZone(TimeZone defaultTimeZone) {
      this.defaultTimeZone = defaultTimeZone;
   }
}
