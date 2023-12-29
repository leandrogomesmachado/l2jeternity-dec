package com.mysql.cj;

import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.ExceptionInterceptorChain;
import com.mysql.cj.exceptions.OperationCancelledException;
import com.mysql.cj.exceptions.PasswordExpiredException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.log.Log;
import com.mysql.cj.log.ProfilerEventHandler;
import com.mysql.cj.log.ProfilerEventHandlerFactory;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.NetworkResources;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.SocketConnection;
import com.mysql.cj.protocol.SocketFactory;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.protocol.a.NativeProtocol;
import com.mysql.cj.protocol.a.NativeServerSession;
import com.mysql.cj.protocol.a.NativeSocketConnection;
import com.mysql.cj.protocol.a.ResultsetFactory;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.IntegerValueFactory;
import com.mysql.cj.result.LongValueFactory;
import com.mysql.cj.result.Row;
import com.mysql.cj.result.StringValueFactory;
import com.mysql.cj.result.ValueFactory;
import com.mysql.cj.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class NativeSession extends CoreSession implements Serializable {
   private static final long serialVersionUID = 5323638898749073419L;
   private CacheAdapter<String, Map<String, String>> serverConfigCache;
   private static final Map<String, Map<Integer, String>> customIndexToCharsetMapByUrl = new HashMap<>();
   private static final Map<String, Map<String, Integer>> customCharsetToMblenMapByUrl = new HashMap<>();
   private boolean requiresEscapingEncoder;
   private long lastQueryFinishedTime = 0L;
   private boolean needsPing = false;
   private NativeMessageBuilder commandBuilder = new NativeMessageBuilder();
   private boolean isClosed = true;
   private Throwable forceClosedReason;
   private CopyOnWriteArrayList<WeakReference<Session.SessionEventListener>> listeners = new CopyOnWriteArrayList<>();
   private transient Timer cancelTimer;
   private static final String SERVER_VERSION_STRING_VAR_NAME = "server_version_string";

   public NativeSession(HostInfo hostInfo, PropertySet propSet) {
      super(hostInfo, propSet);
   }

   public void connect(HostInfo hi, String user, String password, String database, int loginTimeout, TransactionEventHandler transactionManager) throws IOException {
      this.hostInfo = hi;
      this.setSessionMaxRows(-1);
      SocketConnection socketConnection = new NativeSocketConnection();
      socketConnection.connect(this.hostInfo.getHost(), this.hostInfo.getPort(), this.propertySet, this.getExceptionInterceptor(), this.log, loginTimeout);
      if (this.protocol == null) {
         this.protocol = NativeProtocol.getInstance(this, socketConnection, this.propertySet, this.log, transactionManager);
      } else {
         this.protocol.init(this, socketConnection, this.propertySet, transactionManager);
      }

      this.protocol.connect(user, password, database);
      this.protocol.getServerSession().setErrorMessageEncoding(this.protocol.getAuthenticationProvider().getEncodingForHandshake());
      this.isClosed = false;
   }

   public NativeProtocol getProtocol() {
      return (NativeProtocol)this.protocol;
   }

   @Override
   public void quit() {
      if (this.protocol != null) {
         try {
            ((NativeProtocol)this.protocol).quit();
         } catch (Exception var3) {
         }
      }

      synchronized(this) {
         if (this.cancelTimer != null) {
            this.cancelTimer.cancel();
            this.cancelTimer = null;
         }
      }

      this.isClosed = true;
   }

   @Override
   public void forceClose() {
      if (this.protocol != null) {
         try {
            this.protocol.getSocketConnection().forceClose();
            ((NativeProtocol)this.protocol).releaseResources();
         } catch (Throwable var3) {
         }
      }

      synchronized(this) {
         if (this.cancelTimer != null) {
            this.cancelTimer.cancel();
            this.cancelTimer = null;
         }
      }

      this.isClosed = true;
   }

   public void enableMultiQueries() {
      this.sendCommand(this.commandBuilder.buildComSetOption(((NativeProtocol)this.protocol).getSharedSendPacket(), 0), false, 0);
   }

   public void disableMultiQueries() {
      this.sendCommand(this.commandBuilder.buildComSetOption(((NativeProtocol)this.protocol).getSharedSendPacket(), 1), false, 0);
   }

   @Override
   public boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag) {
      return ((NativeServerSession)this.protocol.getServerSession()).isSetNeededForAutoCommitMode(autoCommitFlag, false);
   }

   public int getSessionMaxRows() {
      return this.sessionMaxRows;
   }

   public void setSessionMaxRows(int sessionMaxRows) {
      this.sessionMaxRows = sessionMaxRows;
   }

   public HostInfo getHostInfo() {
      return this.hostInfo;
   }

   public void setQueryInterceptors(List<QueryInterceptor> queryInterceptors) {
      ((NativeProtocol)this.protocol).setQueryInterceptors(queryInterceptors);
   }

   public boolean isServerLocal(Session sess) {
      SocketFactory factory = this.protocol.getSocketConnection().getSocketFactory();
      return factory.isLocallyConnected(sess);
   }

   public void shutdownServer() {
      if (this.versionMeetsMinimum(5, 7, 9)) {
         this.sendCommand(this.commandBuilder.buildComQuery(this.getSharedSendPacket(), "SHUTDOWN"), false, 0);
      } else {
         this.sendCommand(this.commandBuilder.buildComShutdown(this.getSharedSendPacket()), false, 0);
      }
   }

   public void setSocketTimeout(int milliseconds) {
      this.getPropertySet().<Integer>getProperty("socketTimeout").setValue(milliseconds);
      ((NativeProtocol)this.protocol).setSocketTimeout(milliseconds);
   }

   public int getSocketTimeout() {
      RuntimeProperty<Integer> sto = this.getPropertySet().getProperty("socketTimeout");
      return sto.getValue();
   }

   public void checkForCharsetMismatch() {
      ((NativeProtocol)this.protocol).checkForCharsetMismatch();
   }

   public NativePacketPayload getSharedSendPacket() {
      return ((NativeProtocol)this.protocol).getSharedSendPacket();
   }

   public void dumpPacketRingBuffer() {
      ((NativeProtocol)this.protocol).dumpPacketRingBuffer();
   }

   public <T extends Resultset> T invokeQueryInterceptorsPre(Supplier<String> sql, Query interceptedQuery, boolean forceExecute) {
      return ((NativeProtocol)this.protocol).invokeQueryInterceptorsPre(sql, interceptedQuery, forceExecute);
   }

   public <T extends Resultset> T invokeQueryInterceptorsPost(Supplier<String> sql, Query interceptedQuery, T originalResultSet, boolean forceExecute) {
      return ((NativeProtocol)this.protocol).invokeQueryInterceptorsPost(sql, interceptedQuery, originalResultSet, forceExecute);
   }

   public boolean shouldIntercept() {
      return ((NativeProtocol)this.protocol).getQueryInterceptors() != null;
   }

   public long getCurrentTimeNanosOrMillis() {
      return ((NativeProtocol)this.protocol).getCurrentTimeNanosOrMillis();
   }

   public final NativePacketPayload sendCommand(NativePacketPayload queryPacket, boolean skipCheck, int timeoutMillis) {
      return (NativePacketPayload)this.protocol.sendCommand(queryPacket, skipCheck, timeoutMillis);
   }

   public long getSlowQueryThreshold() {
      return ((NativeProtocol)this.protocol).getSlowQueryThreshold();
   }

   public String getQueryTimingUnits() {
      return ((NativeProtocol)this.protocol).getQueryTimingUnits();
   }

   public boolean hadWarnings() {
      return ((NativeProtocol)this.protocol).hadWarnings();
   }

   public void clearInputStream() {
      ((NativeProtocol)this.protocol).clearInputStream();
   }

   public NetworkResources getNetworkResources() {
      return this.protocol.getSocketConnection().getNetworkResources();
   }

   @Override
   public boolean isSSLEstablished() {
      return this.protocol.getSocketConnection().isSSLEstablished();
   }

   public int getCommandCount() {
      return ((NativeProtocol)this.protocol).getCommandCount();
   }

   @Override
   public SocketAddress getRemoteSocketAddress() {
      return this.protocol.getSocketConnection().getMysqlSocket().getRemoteSocketAddress();
   }

   public ProfilerEventHandler getProfilerEventHandlerInstanceFunction() {
      return ProfilerEventHandlerFactory.getInstance(this);
   }

   public InputStream getLocalInfileInputStream() {
      return this.protocol.getLocalInfileInputStream();
   }

   public void setLocalInfileInputStream(InputStream stream) {
      this.protocol.setLocalInfileInputStream(stream);
   }

   public void registerQueryExecutionTime(long queryTimeMs) {
      ((NativeProtocol)this.protocol).getMetricsHolder().registerQueryExecutionTime(queryTimeMs);
   }

   public void reportNumberOfTablesAccessed(int numTablesAccessed) {
      ((NativeProtocol)this.protocol).getMetricsHolder().reportNumberOfTablesAccessed(numTablesAccessed);
   }

   public void incrementNumberOfPreparedExecutes() {
      if (this.gatherPerfMetrics.getValue()) {
         ((NativeProtocol)this.protocol).getMetricsHolder().incrementNumberOfPreparedExecutes();
      }
   }

   public void incrementNumberOfPrepares() {
      if (this.gatherPerfMetrics.getValue()) {
         ((NativeProtocol)this.protocol).getMetricsHolder().incrementNumberOfPrepares();
      }
   }

   public void incrementNumberOfResultSetsCreated() {
      if (this.gatherPerfMetrics.getValue()) {
         ((NativeProtocol)this.protocol).getMetricsHolder().incrementNumberOfResultSetsCreated();
      }
   }

   public void reportMetrics() {
      if (this.gatherPerfMetrics.getValue()) {
      }
   }

   private void configureCharsetProperties() {
      if (this.characterEncoding.getValue() != null) {
         try {
            String testString = "abc";
            StringUtils.getBytes(testString, this.characterEncoding.getValue());
         } catch (WrongArgumentException var4) {
            String oldEncoding = this.characterEncoding.getValue();
            this.characterEncoding.setValue(CharsetMapping.getJavaEncodingForMysqlCharset(oldEncoding));
            if (this.characterEncoding.getValue() == null) {
               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class, Messages.getString("Connection.5", new Object[]{oldEncoding}), this.getExceptionInterceptor()
               );
            }

            String testStringx = "abc";
            StringUtils.getBytes(testStringx, this.characterEncoding.getValue());
         }
      }
   }

   public boolean configureClientCharacterSet(boolean dontCheckServerMatch) {
      String realJavaEncoding = this.characterEncoding.getValue();
      RuntimeProperty<String> characterSetResults = this.getPropertySet().getProperty("characterSetResults");
      boolean characterSetAlreadyConfigured = false;

      try {
         characterSetAlreadyConfigured = true;
         this.configureCharsetProperties();
         realJavaEncoding = this.characterEncoding.getValue();

         try {
            String serverEncodingToSet = CharsetMapping.getJavaEncodingForCollationIndex(this.protocol.getServerSession().getServerDefaultCollationIndex());
            if (serverEncodingToSet == null || serverEncodingToSet.length() == 0) {
               if (realJavaEncoding == null) {
                  throw ExceptionFactory.createException(
                     Messages.getString("Connection.6", new Object[]{this.protocol.getServerSession().getServerDefaultCollationIndex()}),
                     this.getExceptionInterceptor()
                  );
               }

               this.characterEncoding.setValue(realJavaEncoding);
            }

            if ("ISO8859_1".equalsIgnoreCase(serverEncodingToSet)) {
               serverEncodingToSet = "Cp1252";
            }

            if ("UnicodeBig".equalsIgnoreCase(serverEncodingToSet)
               || "UTF-16".equalsIgnoreCase(serverEncodingToSet)
               || "UTF-16LE".equalsIgnoreCase(serverEncodingToSet)
               || "UTF-32".equalsIgnoreCase(serverEncodingToSet)) {
               serverEncodingToSet = "UTF-8";
            }

            this.characterEncoding.setValue(serverEncodingToSet);
         } catch (ArrayIndexOutOfBoundsException var20) {
            if (realJavaEncoding == null) {
               throw ExceptionFactory.createException(
                  Messages.getString("Connection.6", new Object[]{this.protocol.getServerSession().getServerDefaultCollationIndex()}),
                  this.getExceptionInterceptor()
               );
            }

            this.characterEncoding.setValue(realJavaEncoding);
         }

         if (this.characterEncoding.getValue() == null) {
            this.characterEncoding.setValue("ISO8859_1");
         }

         if (realJavaEncoding != null) {
            if (!realJavaEncoding.equalsIgnoreCase("UTF-8") && !realJavaEncoding.equalsIgnoreCase("UTF8")) {
               String mysqlCharsetName = CharsetMapping.getMysqlCharsetForJavaEncoding(
                  realJavaEncoding.toUpperCase(Locale.ENGLISH), this.getServerSession().getServerVersion()
               );
               if (mysqlCharsetName != null && (dontCheckServerMatch || !this.protocol.getServerSession().characterSetNamesMatches(mysqlCharsetName))) {
                  this.sendCommand(this.commandBuilder.buildComQuery(null, "SET NAMES " + mysqlCharsetName), false, 0);
                  this.protocol.getServerSession().getServerVariables().put("character_set_client", mysqlCharsetName);
                  this.protocol.getServerSession().getServerVariables().put("character_set_connection", mysqlCharsetName);
               }

               this.characterEncoding.setValue(realJavaEncoding);
            } else {
               boolean useutf8mb4 = CharsetMapping.UTF8MB4_INDEXES.contains(this.protocol.getServerSession().getServerDefaultCollationIndex());
               if (!this.useOldUTF8Behavior.getValue()) {
                  if (dontCheckServerMatch
                     || !this.protocol.getServerSession().characterSetNamesMatches("utf8")
                     || !this.protocol.getServerSession().characterSetNamesMatches("utf8mb4")) {
                     this.sendCommand(this.commandBuilder.buildComQuery(null, "SET NAMES " + (useutf8mb4 ? "utf8mb4" : "utf8")), false, 0);
                     this.protocol.getServerSession().getServerVariables().put("character_set_client", useutf8mb4 ? "utf8mb4" : "utf8");
                     this.protocol.getServerSession().getServerVariables().put("character_set_connection", useutf8mb4 ? "utf8mb4" : "utf8");
                  }
               } else {
                  this.sendCommand(this.commandBuilder.buildComQuery(null, "SET NAMES latin1"), false, 0);
                  this.protocol.getServerSession().getServerVariables().put("character_set_client", "latin1");
                  this.protocol.getServerSession().getServerVariables().put("character_set_connection", "latin1");
               }

               this.characterEncoding.setValue(realJavaEncoding);
            }
         } else if (this.characterEncoding.getValue() != null) {
            String mysqlCharsetName = this.getServerSession().getServerDefaultCharset();
            if (this.useOldUTF8Behavior.getValue()) {
               mysqlCharsetName = "latin1";
            }

            boolean ucs2 = false;
            if ("ucs2".equalsIgnoreCase(mysqlCharsetName)
               || "utf16".equalsIgnoreCase(mysqlCharsetName)
               || "utf16le".equalsIgnoreCase(mysqlCharsetName)
               || "utf32".equalsIgnoreCase(mysqlCharsetName)) {
               mysqlCharsetName = "utf8";
               ucs2 = true;
               if (characterSetResults.getValue() == null) {
                  characterSetResults.setValue("UTF-8");
               }
            }

            if (dontCheckServerMatch || !this.protocol.getServerSession().characterSetNamesMatches(mysqlCharsetName) || ucs2) {
               try {
                  this.sendCommand(this.commandBuilder.buildComQuery(null, "SET NAMES " + mysqlCharsetName), false, 0);
                  this.protocol.getServerSession().getServerVariables().put("character_set_client", mysqlCharsetName);
                  this.protocol.getServerSession().getServerVariables().put("character_set_connection", mysqlCharsetName);
               } catch (PasswordExpiredException var21) {
                  if (this.disconnectOnExpiredPasswords.getValue()) {
                     throw var21;
                  }
               }
            }

            realJavaEncoding = this.characterEncoding.getValue();
         }

         String onServer = this.protocol.getServerSession().getServerVariable("character_set_results");
         if (characterSetResults.getValue() == null) {
            if (onServer != null && onServer.length() > 0 && !"NULL".equalsIgnoreCase(onServer)) {
               try {
                  this.sendCommand(this.commandBuilder.buildComQuery(null, "SET character_set_results = NULL"), false, 0);
               } catch (PasswordExpiredException var22) {
                  if (this.disconnectOnExpiredPasswords.getValue()) {
                     throw var22;
                  }
               }

               this.protocol.getServerSession().getServerVariables().put("local.character_set_results", null);
            } else {
               this.protocol.getServerSession().getServerVariables().put("local.character_set_results", onServer);
            }
         } else {
            if (this.useOldUTF8Behavior.getValue()) {
               try {
                  this.sendCommand(this.commandBuilder.buildComQuery(null, "SET NAMES latin1"), false, 0);
                  this.protocol.getServerSession().getServerVariables().put("character_set_client", "latin1");
                  this.protocol.getServerSession().getServerVariables().put("character_set_connection", "latin1");
               } catch (PasswordExpiredException var24) {
                  if (this.disconnectOnExpiredPasswords.getValue()) {
                     throw var24;
                  }
               }
            }

            String charsetResults = characterSetResults.getValue();
            String mysqlEncodingName = null;
            if ("UTF-8".equalsIgnoreCase(charsetResults) || "UTF8".equalsIgnoreCase(charsetResults)) {
               mysqlEncodingName = "utf8";
            } else if ("null".equalsIgnoreCase(charsetResults)) {
               mysqlEncodingName = "NULL";
            } else {
               mysqlEncodingName = CharsetMapping.getMysqlCharsetForJavaEncoding(
                  charsetResults.toUpperCase(Locale.ENGLISH), this.getServerSession().getServerVersion()
               );
            }

            if (mysqlEncodingName == null) {
               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class, Messages.getString("Connection.7", new Object[]{charsetResults}), this.getExceptionInterceptor()
               );
            }

            if (!mysqlEncodingName.equalsIgnoreCase(this.protocol.getServerSession().getServerVariable("character_set_results"))) {
               StringBuilder setBuf = new StringBuilder("SET character_set_results = ".length() + mysqlEncodingName.length());
               setBuf.append("SET character_set_results = ").append(mysqlEncodingName);

               try {
                  this.sendCommand(this.commandBuilder.buildComQuery(null, setBuf.toString()), false, 0);
               } catch (PasswordExpiredException var23) {
                  if (this.disconnectOnExpiredPasswords.getValue()) {
                     throw var23;
                  }
               }

               this.protocol.getServerSession().getServerVariables().put("local.character_set_results", mysqlEncodingName);
               this.protocol.getServerSession().setErrorMessageEncoding(charsetResults);
            } else {
               this.protocol.getServerSession().getServerVariables().put("local.character_set_results", onServer);
            }
         }

         String connectionCollation = this.getPropertySet().getStringProperty("connectionCollation").getStringValue();
         if (connectionCollation != null) {
            StringBuilder setBuf = new StringBuilder("SET collation_connection = ".length() + connectionCollation.length());
            setBuf.append("SET collation_connection = ").append(connectionCollation);

            try {
               this.sendCommand(this.commandBuilder.buildComQuery(null, setBuf.toString()), false, 0);
            } catch (PasswordExpiredException var25) {
               if (this.disconnectOnExpiredPasswords.getValue()) {
                  throw var25;
               }
            }
         }
      } finally {
         this.characterEncoding.setValue(realJavaEncoding);
      }

      try {
         CharsetEncoder enc = Charset.forName(this.characterEncoding.getValue()).newEncoder();
         CharBuffer cbuf = CharBuffer.allocate(1);
         ByteBuffer bbuf = ByteBuffer.allocate(1);
         cbuf.put("¥");
         ((Buffer)cbuf).position(0);
         enc.encode(cbuf, bbuf, true);
         if (bbuf.get(0) == 92) {
            this.requiresEscapingEncoder = true;
         } else {
            ((Buffer)cbuf).clear();
            ((Buffer)bbuf).clear();
            cbuf.put("₩");
            ((Buffer)cbuf).position(0);
            enc.encode(cbuf, bbuf, true);
            if (bbuf.get(0) == 92) {
               this.requiresEscapingEncoder = true;
            }
         }
      } catch (UnsupportedCharsetException var19) {
         byte[] bbuf = StringUtils.getBytes("¥", this.characterEncoding.getValue());
         if (bbuf[0] == 92) {
            this.requiresEscapingEncoder = true;
         } else {
            bbuf = StringUtils.getBytes("₩", this.characterEncoding.getValue());
            if (bbuf[0] == 92) {
               this.requiresEscapingEncoder = true;
            }
         }
      }

      return characterSetAlreadyConfigured;
   }

   public boolean getRequiresEscapingEncoder() {
      return this.requiresEscapingEncoder;
   }

   private void createConfigCacheIfNeeded(Object syncMutex) {
      synchronized(syncMutex) {
         if (this.serverConfigCache == null) {
            try {
               Class<?> factoryClass = Class.forName(this.getPropertySet().getStringProperty("serverConfigCacheFactory").getStringValue());
               CacheAdapterFactory<String, Map<String, String>> cacheFactory = (CacheAdapterFactory)factoryClass.newInstance();
               this.serverConfigCache = cacheFactory.getInstance(syncMutex, this.hostInfo.getDatabaseUrl(), Integer.MAX_VALUE, Integer.MAX_VALUE);
               ExceptionInterceptor evictOnCommsError = new ExceptionInterceptor() {
                  @Override
                  public ExceptionInterceptor init(Properties config, Log log1) {
                     return this;
                  }

                  @Override
                  public void destroy() {
                  }

                  @Override
                  public Exception interceptException(Exception sqlEx) {
                     if (sqlEx instanceof SQLException && ((SQLException)sqlEx).getSQLState() != null && ((SQLException)sqlEx).getSQLState().startsWith("08")) {
                        NativeSession.this.serverConfigCache.invalidate(NativeSession.this.hostInfo.getDatabaseUrl());
                     }

                     return null;
                  }
               };
               if (this.exceptionInterceptor == null) {
                  this.exceptionInterceptor = evictOnCommsError;
               } else {
                  ((ExceptionInterceptorChain)this.exceptionInterceptor).addRingZero(evictOnCommsError);
               }
            } catch (ClassNotFoundException var7) {
               throw ExceptionFactory.createException(
                  Messages.getString(
                     "Connection.CantFindCacheFactory",
                     new Object[]{this.getPropertySet().getStringProperty("parseInfoCacheFactory").getValue(), "parseInfoCacheFactory"}
                  ),
                  var7,
                  this.getExceptionInterceptor()
               );
            } catch (IllegalAccessException | CJException | InstantiationException var8) {
               throw ExceptionFactory.createException(
                  Messages.getString(
                     "Connection.CantLoadCacheFactory",
                     new Object[]{this.getPropertySet().getStringProperty("parseInfoCacheFactory").getValue(), "parseInfoCacheFactory"}
                  ),
                  var8,
                  this.getExceptionInterceptor()
               );
            }
         }
      }
   }

   public void loadServerVariables(Object syncMutex, String version) {
      if (this.cacheServerConfiguration.getValue()) {
         this.createConfigCacheIfNeeded(syncMutex);
         Map<String, String> cachedVariableMap = this.serverConfigCache.get(this.hostInfo.getDatabaseUrl());
         if (cachedVariableMap != null) {
            String cachedServerVersion = cachedVariableMap.get("server_version_string");
            if (cachedServerVersion != null
               && this.getServerSession().getServerVersion() != null
               && cachedServerVersion.equals(this.getServerSession().getServerVersion().toString())) {
               this.protocol.getServerSession().setServerVariables(cachedVariableMap);
               return;
            }

            this.serverConfigCache.invalidate(this.hostInfo.getDatabaseUrl());
         }
      }

      try {
         if (version != null && version.indexOf(42) != -1) {
            StringBuilder buf = new StringBuilder(version.length() + 10);

            for(int i = 0; i < version.length(); ++i) {
               char c = version.charAt(i);
               buf.append(c == '*' ? "[star]" : c);
            }

            version = buf.toString();
         }

         String versionComment = !this.propertySet.getBooleanProperty("paranoid").getValue() && version != null ? "/* " + version + " */" : "";
         this.protocol.getServerSession().setServerVariables(new HashMap<>());
         if (!this.versionMeetsMinimum(5, 1, 0)) {
            NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, versionComment + "SHOW VARIABLES"), false, 0);
            Resultset rs = ((NativeProtocol)this.protocol)
               .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            ValueFactory<String> vf = new StringValueFactory(rs.getColumnDefinition().getFields()[0].getEncoding());

            Row r;
            while((r = rs.getRows().next()) != null) {
               this.protocol.getServerSession().getServerVariables().put(r.getValue(0, vf), r.getValue(1, vf));
            }
         } else {
            StringBuilder queryBuf = new StringBuilder(versionComment).append("SELECT");
            queryBuf.append("  @@session.auto_increment_increment AS auto_increment_increment");
            queryBuf.append(", @@character_set_client AS character_set_client");
            queryBuf.append(", @@character_set_connection AS character_set_connection");
            queryBuf.append(", @@character_set_results AS character_set_results");
            queryBuf.append(", @@character_set_server AS character_set_server");
            queryBuf.append(", @@collation_server AS collation_server");
            queryBuf.append(", @@init_connect AS init_connect");
            queryBuf.append(", @@interactive_timeout AS interactive_timeout");
            if (!this.versionMeetsMinimum(5, 5, 0)) {
               queryBuf.append(", @@language AS language");
            }

            queryBuf.append(", @@license AS license");
            queryBuf.append(", @@lower_case_table_names AS lower_case_table_names");
            queryBuf.append(", @@max_allowed_packet AS max_allowed_packet");
            queryBuf.append(", @@net_write_timeout AS net_write_timeout");
            if (!this.versionMeetsMinimum(8, 0, 3)) {
               queryBuf.append(", @@query_cache_size AS query_cache_size");
               queryBuf.append(", @@query_cache_type AS query_cache_type");
            }

            queryBuf.append(", @@sql_mode AS sql_mode");
            queryBuf.append(", @@system_time_zone AS system_time_zone");
            queryBuf.append(", @@time_zone AS time_zone");
            if (!this.versionMeetsMinimum(8, 0, 3) && (!this.versionMeetsMinimum(5, 7, 20) || this.versionMeetsMinimum(8, 0, 0))) {
               queryBuf.append(", @@tx_isolation AS transaction_isolation");
            } else {
               queryBuf.append(", @@transaction_isolation AS transaction_isolation");
            }

            queryBuf.append(", @@wait_timeout AS wait_timeout");
            NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, queryBuf.toString()), false, 0);
            Resultset rs = ((NativeProtocol)this.protocol)
               .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            Field[] f = rs.getColumnDefinition().getFields();
            if (f.length > 0) {
               ValueFactory<String> vf = new StringValueFactory(f[0].getEncoding());
               Row r;
               if ((r = rs.getRows().next()) != null) {
                  for(int i = 0; i < f.length; ++i) {
                     this.protocol.getServerSession().getServerVariables().put(f[i].getColumnLabel(), r.getValue(i, vf));
                  }
               }
            }
         }
      } catch (PasswordExpiredException var11) {
         if (this.disconnectOnExpiredPasswords.getValue()) {
            throw var11;
         }
      } catch (IOException var12) {
         throw ExceptionFactory.createException(var12.getMessage(), var12);
      }

      if (this.cacheServerConfiguration.getValue()) {
         this.protocol.getServerSession().getServerVariables().put("server_version_string", this.getServerSession().getServerVersion().toString());
         this.serverConfigCache.put(this.hostInfo.getDatabaseUrl(), this.protocol.getServerSession().getServerVariables());
      }
   }

   public void setSessionVariables() {
      String sessionVariables = this.getPropertySet().getStringProperty("sessionVariables").getValue();
      if (sessionVariables != null) {
         List<String> variablesToSet = new ArrayList<>();

         for(String part : StringUtils.split(sessionVariables, ",", "\"'(", "\"')", "\"'", true)) {
            variablesToSet.addAll(StringUtils.split(part, ";", "\"'(", "\"')", "\"'", true));
         }

         if (!variablesToSet.isEmpty()) {
            StringBuilder query = new StringBuilder("SET ");
            String separator = "";

            for(String variableToSet : variablesToSet) {
               if (variableToSet.length() > 0) {
                  query.append(separator);
                  if (!variableToSet.startsWith("@")) {
                     query.append("SESSION ");
                  }

                  query.append(variableToSet);
                  separator = ",";
               }
            }

            this.sendCommand(this.commandBuilder.buildComQuery(null, query.toString()), false, 0);
         }
      }
   }

   public void buildCollationMapping() {
      Map<Integer, String> customCharset = null;
      Map<String, Integer> customMblen = null;
      String databaseURL = this.hostInfo.getDatabaseUrl();
      if (this.cacheServerConfiguration.getValue()) {
         synchronized(customIndexToCharsetMapByUrl) {
            customCharset = customIndexToCharsetMapByUrl.get(databaseURL);
            customMblen = customCharsetToMblenMapByUrl.get(databaseURL);
         }
      }

      if (customCharset == null && this.getPropertySet().getBooleanProperty("detectCustomCollations").getValue()) {
         customCharset = new HashMap<>();
         customMblen = new HashMap<>();
         ValueFactory<Integer> ivf = new IntegerValueFactory();

         try {
            NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, "SHOW COLLATION"), false, 0);
            Resultset rs = ((NativeProtocol)this.protocol)
               .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            ValueFactory<String> svf = new StringValueFactory(rs.getColumnDefinition().getFields()[1].getEncoding());

            Row r;
            while((r = rs.getRows().next()) != null) {
               int collationIndex = r.getValue(2, ivf).intValue();
               String charsetName = r.getValue(1, svf);
               if (collationIndex >= 2048 || !charsetName.equals(CharsetMapping.getMysqlCharsetNameForCollationIndex(collationIndex))) {
                  customCharset.put(collationIndex, charsetName);
               }

               if (!CharsetMapping.CHARSET_NAME_TO_CHARSET.containsKey(charsetName)) {
                  customMblen.put(charsetName, null);
               }
            }
         } catch (PasswordExpiredException var17) {
            if (this.disconnectOnExpiredPasswords.getValue()) {
               throw var17;
            }
         } catch (IOException var18) {
            throw ExceptionFactory.createException(var18.getMessage(), var18, this.exceptionInterceptor);
         }

         if (customMblen.size() > 0) {
            try {
               NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, "SHOW CHARACTER SET"), false, 0);
               Resultset rs = ((NativeProtocol)this.protocol)
                  .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
               int charsetColumn = rs.getColumnDefinition().getColumnNameToIndex().get("Charset");
               int maxlenColumn = rs.getColumnDefinition().getColumnNameToIndex().get("Maxlen");
               ValueFactory<String> svf = new StringValueFactory(rs.getColumnDefinition().getFields()[1].getEncoding());

               Row r;
               while((r = rs.getRows().next()) != null) {
                  String charsetName = r.getValue(charsetColumn, svf);
                  if (customMblen.containsKey(charsetName)) {
                     customMblen.put(charsetName, r.getValue(maxlenColumn, ivf));
                  }
               }
            } catch (PasswordExpiredException var15) {
               if (this.disconnectOnExpiredPasswords.getValue()) {
                  throw var15;
               }
            } catch (IOException var16) {
               throw ExceptionFactory.createException(var16.getMessage(), var16, this.exceptionInterceptor);
            }
         }

         if (this.cacheServerConfiguration.getValue()) {
            synchronized(customIndexToCharsetMapByUrl) {
               customIndexToCharsetMapByUrl.put(databaseURL, customCharset);
               customCharsetToMblenMapByUrl.put(databaseURL, customMblen);
            }
         }
      }

      if (customCharset != null) {
         ((NativeServerSession)this.protocol.getServerSession()).indexToCustomMysqlCharset = Collections.unmodifiableMap(customCharset);
      }

      if (customMblen != null) {
         ((NativeServerSession)this.protocol.getServerSession()).mysqlCharsetToCustomMblen = Collections.unmodifiableMap(customMblen);
      }

      if (this.protocol.getServerSession().getServerDefaultCollationIndex() == 0) {
         String collationServer = this.protocol.getServerSession().getServerVariable("collation_server");
         if (collationServer != null) {
            for(int i = 1; i < CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME.length; ++i) {
               if (CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME[i].equals(collationServer)) {
                  this.protocol.getServerSession().setServerDefaultCollationIndex(i);
                  break;
               }
            }
         } else {
            this.protocol.getServerSession().setServerDefaultCollationIndex(45);
         }
      }
   }

   @Override
   public String getProcessHost() {
      try {
         long threadId = this.getThreadId();
         String processHost = this.findProcessHost(threadId);
         if (processHost == null) {
            this.log
               .logWarn(
                  String.format("Connection id %d not found in \"SHOW PROCESSLIST\", assuming 32-bit overflow, using SELECT CONNECTION_ID() instead", threadId)
               );
            NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, "SELECT CONNECTION_ID()"), false, 0);
            Resultset rs = ((NativeProtocol)this.protocol)
               .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            ValueFactory<Long> lvf = new LongValueFactory();
            Row r;
            if ((r = rs.getRows().next()) != null) {
               threadId = r.getValue(0, lvf);
               processHost = this.findProcessHost(threadId);
            } else {
               this.log.logError("No rows returned for statement \"SELECT CONNECTION_ID()\", local connection check will most likely be incorrect");
            }
         }

         if (processHost == null) {
            this.log
               .logWarn(
                  String.format("Cannot find process listing for connection %d in SHOW PROCESSLIST output, unable to determine if locally connected", threadId)
               );
         }

         return processHost;
      } catch (IOException var8) {
         throw ExceptionFactory.createException(var8.getMessage(), var8);
      }
   }

   private String findProcessHost(long threadId) {
      try {
         String processHost = null;
         NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, "SHOW PROCESSLIST"), false, 0);
         Resultset rs = ((NativeProtocol)this.protocol)
            .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
         ValueFactory<Long> lvf = new LongValueFactory();
         ValueFactory<String> svf = new StringValueFactory(rs.getColumnDefinition().getFields()[2].getEncoding());

         Row r;
         while((r = rs.getRows().next()) != null) {
            long id = r.getValue(0, lvf);
            if (threadId == id) {
               processHost = r.getValue(2, svf);
               break;
            }
         }

         return processHost;
      } catch (IOException var11) {
         throw ExceptionFactory.createException(var11.getMessage(), var11);
      }
   }

   public String queryServerVariable(String varName) {
      try {
         NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(null, "SELECT " + varName), false, 0);
         Resultset rs = ((NativeProtocol)this.protocol)
            .readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
         ValueFactory<String> svf = new StringValueFactory(rs.getColumnDefinition().getFields()[0].getEncoding());
         Row r;
         if ((r = rs.getRows().next()) != null) {
            String s = r.getValue(0, svf);
            if (s != null) {
               return s;
            }
         }

         return null;
      } catch (IOException var7) {
         throw ExceptionFactory.createException(var7.getMessage(), var7);
      }
   }

   public <T extends Resultset> T execSQL(
      Query callingQuery,
      String query,
      int maxRows,
      NativePacketPayload packet,
      boolean streamResults,
      ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory,
      String catalog,
      ColumnDefinition cachedMetadata,
      boolean isBatch
   ) {
      long queryStartTime = 0L;
      int endOfQueryPacketPosition = 0;
      if (packet != null) {
         endOfQueryPacketPosition = packet.getPosition();
      }

      if (this.gatherPerfMetrics.getValue()) {
         queryStartTime = System.currentTimeMillis();
      }

      this.lastQueryFinishedTime = 0L;
      if (this.autoReconnect.getValue() && (this.getServerSession().isAutoCommit() || this.autoReconnectForPools.getValue()) && this.needsPing && !isBatch) {
         try {
            this.ping(false, 0);
            this.needsPing = false;
         } catch (Exception var24) {
            this.invokeReconnectListeners();
         }
      }

      Resultset var29;
      try {
         if (packet != null) {
            return ((NativeProtocol)this.protocol)
               .sendQueryPacket(
                  callingQuery, packet, maxRows, streamResults, catalog, cachedMetadata, this::getProfilerEventHandlerInstanceFunction, resultSetFactory
               );
         }

         String encoding = this.characterEncoding.getValue();
         var29 = ((NativeProtocol)this.protocol)
            .sendQueryString(
               callingQuery, query, encoding, maxRows, streamResults, catalog, cachedMetadata, this::getProfilerEventHandlerInstanceFunction, resultSetFactory
            );
      } catch (CJException var25) {
         if (this.getPropertySet().getBooleanProperty("dumpQueriesOnException").getValue()) {
            String extractedSql = NativePacketPayload.extractSqlFromPacket(
               query, packet, endOfQueryPacketPosition, this.getPropertySet().getIntegerProperty("maxQuerySizeToLog").getValue()
            );
            StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
            messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
            messageBuf.append(extractedSql);
            messageBuf.append("\n\n");
            var25.appendMessage(messageBuf.toString());
         }

         if (this.autoReconnect.getValue()) {
            if (var25 instanceof CJCommunicationsException) {
               this.protocol.getSocketConnection().forceClose();
            }

            this.needsPing = true;
         } else if (var25 instanceof CJCommunicationsException) {
            this.invokeCleanupListeners(var25);
         }

         throw var25;
      } catch (Throwable var26) {
         if (this.autoReconnect.getValue()) {
            if (var26 instanceof IOException) {
               this.protocol.getSocketConnection().forceClose();
            } else if (var26 instanceof IOException) {
               this.invokeCleanupListeners(var26);
            }

            this.needsPing = true;
         }

         throw ExceptionFactory.createException(var26.getMessage(), var26, this.exceptionInterceptor);
      } finally {
         if (this.maintainTimeStats.getValue()) {
            this.lastQueryFinishedTime = System.currentTimeMillis();
         }

         if (this.gatherPerfMetrics.getValue()) {
            long queryTime = System.currentTimeMillis() - queryStartTime;
            this.registerQueryExecutionTime(queryTime);
         }
      }

      return (T)var29;
   }

   public long getIdleFor() {
      if (this.lastQueryFinishedTime == 0L) {
         return 0L;
      } else {
         long now = System.currentTimeMillis();
         return now - this.lastQueryFinishedTime;
      }
   }

   public boolean isNeedsPing() {
      return this.needsPing;
   }

   public void setNeedsPing(boolean needsPing) {
      this.needsPing = needsPing;
   }

   public void ping(boolean checkForClosedConnection, int timeoutMillis) {
      if (checkForClosedConnection) {
         this.checkClosed();
      }

      long pingMillisLifetime = (long)this.getPropertySet().getIntegerProperty("selfDestructOnPingSecondsLifetime").getValue().intValue();
      int pingMaxOperations = this.getPropertySet().getIntegerProperty("selfDestructOnPingMaxOperations").getValue();
      if ((pingMillisLifetime <= 0L || System.currentTimeMillis() - this.connectionCreationTimeMillis <= pingMillisLifetime)
         && (pingMaxOperations <= 0 || pingMaxOperations > this.getCommandCount())) {
         this.sendCommand(this.commandBuilder.buildComPing(null), false, timeoutMillis);
      } else {
         this.invokeNormalCloseListeners();
         throw ExceptionFactory.createException(
            Messages.getString("Connection.exceededConnectionLifetime"), "08S01", 0, false, null, this.exceptionInterceptor
         );
      }
   }

   public long getConnectionCreationTimeMillis() {
      return this.connectionCreationTimeMillis;
   }

   public void setConnectionCreationTimeMillis(long connectionCreationTimeMillis) {
      this.connectionCreationTimeMillis = connectionCreationTimeMillis;
   }

   @Override
   public boolean isClosed() {
      return this.isClosed;
   }

   public void checkClosed() {
      if (this.isClosed) {
         if (this.forceClosedReason != null && this.forceClosedReason.getClass().equals(OperationCancelledException.class)) {
            throw (OperationCancelledException)this.forceClosedReason;
         } else {
            throw (ConnectionIsClosedException)ExceptionFactory.createException(
               ConnectionIsClosedException.class, Messages.getString("Connection.2"), this.forceClosedReason, this.getExceptionInterceptor()
            );
         }
      }
   }

   public Throwable getForceClosedReason() {
      return this.forceClosedReason;
   }

   public void setForceClosedReason(Throwable forceClosedReason) {
      this.forceClosedReason = forceClosedReason;
   }

   @Override
   public void addListener(Session.SessionEventListener l) {
      this.listeners.addIfAbsent(new WeakReference<>(l));
   }

   @Override
   public void removeListener(Session.SessionEventListener listener) {
      for(WeakReference<Session.SessionEventListener> wr : this.listeners) {
         Session.SessionEventListener l = wr.get();
         if (l == listener) {
            this.listeners.remove(wr);
            break;
         }
      }
   }

   protected void invokeNormalCloseListeners() {
      for(WeakReference<Session.SessionEventListener> wr : this.listeners) {
         Session.SessionEventListener l = wr.get();
         if (l != null) {
            l.handleNormalClose();
         } else {
            this.listeners.remove(wr);
         }
      }
   }

   protected void invokeReconnectListeners() {
      for(WeakReference<Session.SessionEventListener> wr : this.listeners) {
         Session.SessionEventListener l = wr.get();
         if (l != null) {
            l.handleReconnect();
         } else {
            this.listeners.remove(wr);
         }
      }
   }

   public void invokeCleanupListeners(Throwable whyCleanedUp) {
      for(WeakReference<Session.SessionEventListener> wr : this.listeners) {
         Session.SessionEventListener l = wr.get();
         if (l != null) {
            l.handleCleanup(whyCleanedUp);
         } else {
            this.listeners.remove(wr);
         }
      }
   }

   @Override
   public String getIdentifierQuoteString() {
      return this.protocol != null && this.protocol.getServerSession().useAnsiQuotedIdentifiers() ? "\"" : "`";
   }

   public synchronized Timer getCancelTimer() {
      if (this.cancelTimer == null) {
         this.cancelTimer = new Timer("MySQL Statement Cancellation Timer", Boolean.TRUE);
      }

      return this.cancelTimer;
   }

   @Override
   public <M extends Message, RES_T, R> RES_T query(M message, Predicate<Row> filterRow, Function<Row, R> mapRow, Collector<R, ?, RES_T> collector) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }
}
