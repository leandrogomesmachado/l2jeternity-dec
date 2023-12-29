package com.mysql.cj.protocol.a;

import com.mysql.cj.Constants;
import com.mysql.cj.MessageBuilder;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.Query;
import com.mysql.cj.QueryResult;
import com.mysql.cj.ServerVersion;
import com.mysql.cj.Session;
import com.mysql.cj.TransactionEventHandler;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.CJPacketTooBigException;
import com.mysql.cj.exceptions.ClosedOnExpiredPasswordException;
import com.mysql.cj.exceptions.DataTruncationException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.MysqlErrorNumbers;
import com.mysql.cj.exceptions.PasswordExpiredException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import com.mysql.cj.log.BaseMetricsHolder;
import com.mysql.cj.log.Log;
import com.mysql.cj.log.ProfilerEventHandler;
import com.mysql.cj.log.ProfilerEventImpl;
import com.mysql.cj.protocol.AbstractProtocol;
import com.mysql.cj.protocol.AuthenticationProvider;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ExportControlled;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.protocol.MessageSender;
import com.mysql.cj.protocol.PacketReceivedTimeHolder;
import com.mysql.cj.protocol.PacketSentTimeHolder;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ProtocolEntityReader;
import com.mysql.cj.protocol.ResultStreamer;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.protocol.SocketConnection;
import com.mysql.cj.protocol.a.result.OkPacket;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.IntegerValueFactory;
import com.mysql.cj.result.Row;
import com.mysql.cj.result.RowList;
import com.mysql.cj.result.StringValueFactory;
import com.mysql.cj.result.ValueFactory;
import com.mysql.cj.util.LazyString;
import com.mysql.cj.util.LogUtils;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TestUtils;
import com.mysql.cj.util.TimeUtil;
import com.mysql.cj.util.Util;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.sql.DataTruncation;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class NativeProtocol extends AbstractProtocol<NativePacketPayload> implements Protocol<NativePacketPayload>, RuntimeProperty.RuntimePropertyListener {
   protected static final int INITIAL_PACKET_SIZE = 1024;
   protected static final int COMP_HEADER_LENGTH = 3;
   protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 1048576;
   private static final String EXPLAINABLE_STATEMENT = "SELECT";
   private static final String[] EXPLAINABLE_STATEMENT_EXTENSION = new String[]{"INSERT", "UPDATE", "REPLACE", "DELETE"};
   protected MessageSender<NativePacketPayload> packetSender;
   protected MessageReader<NativePacketHeader, NativePacketPayload> packetReader;
   protected NativeServerSession serverSession;
   protected CompressedPacketSender compressedPacketSender;
   protected NativePacketPayload sharedSendPacket = null;
   protected NativePacketPayload reusablePacket = null;
   private SoftReference<NativePacketPayload> loadFileBufRef;
   protected byte packetSequence = 0;
   protected boolean useCompression = false;
   private RuntimeProperty<Integer> maxAllowedPacket;
   private RuntimeProperty<Boolean> useServerPrepStmts;
   private boolean autoGenerateTestcaseScript;
   private boolean logSlowQueries = false;
   private boolean useAutoSlowLog;
   private boolean profileSQL = false;
   private boolean useNanosForElapsedTime;
   private long slowQueryThreshold;
   private String queryTimingUnits;
   private int commandCount = 0;
   protected boolean hadWarnings = false;
   private int warningCount = 0;
   protected Map<Class<? extends ProtocolEntity>, ProtocolEntityReader<? extends ProtocolEntity, ? extends Message>> PROTOCOL_ENTITY_CLASS_TO_TEXT_READER;
   protected Map<Class<? extends ProtocolEntity>, ProtocolEntityReader<? extends ProtocolEntity, ? extends Message>> PROTOCOL_ENTITY_CLASS_TO_BINARY_READER;
   protected boolean platformDbCharsetMatches = true;
   private int statementExecutionDepth = 0;
   private List<QueryInterceptor> queryInterceptors;
   private RuntimeProperty<Boolean> maintainTimeStats;
   private RuntimeProperty<Integer> maxQuerySizeToLog;
   private InputStream localInfileInputStream;
   private BaseMetricsHolder metricsHolder;
   private TransactionEventHandler transactionManager;
   private String queryComment = null;
   private static String jvmPlatformCharset = null;
   private NativeMessageBuilder commandBuilder = new NativeMessageBuilder();
   private ResultsetRows streamingData = null;

   public static NativeProtocol getInstance(
      Session session, SocketConnection socketConnection, PropertySet propertySet, Log log, TransactionEventHandler transactionManager
   ) {
      NativeProtocol protocol = new NativeProtocol(log);
      protocol.init(session, socketConnection, propertySet, transactionManager);
      return protocol;
   }

   public NativeProtocol(Log logger) {
      this.log = logger;
      this.metricsHolder = new BaseMetricsHolder();
   }

   @Override
   public void init(Session sess, SocketConnection phConnection, PropertySet propSet, TransactionEventHandler trManager) {
      this.session = sess;
      this.propertySet = propSet;
      this.socketConnection = phConnection;
      this.exceptionInterceptor = this.socketConnection.getExceptionInterceptor();
      this.transactionManager = trManager;
      this.maintainTimeStats = this.propertySet.getBooleanProperty("maintainTimeStats");
      this.maxQuerySizeToLog = this.propertySet.getIntegerProperty("maxQuerySizeToLog");
      this.useAutoSlowLog = this.propertySet.getBooleanProperty("autoSlowLog").getValue();
      this.logSlowQueries = this.propertySet.getBooleanProperty("logSlowQueries").getValue();
      this.maxAllowedPacket = this.propertySet.getIntegerProperty("maxAllowedPacket");
      this.profileSQL = this.propertySet.getBooleanProperty("profileSQL").getValue();
      this.autoGenerateTestcaseScript = this.propertySet.getBooleanProperty("autoGenerateTestcaseScript").getValue();
      this.useServerPrepStmts = this.propertySet.getBooleanProperty("useServerPrepStmts");
      this.reusablePacket = new NativePacketPayload(1024);
      this.packetSender = new SimplePacketSender(this.socketConnection.getMysqlOutput());
      this.packetReader = new SimplePacketReader(this.socketConnection, this.maxAllowedPacket);
      if (this.propertySet.getBooleanProperty("useNanosForElapsedTime").getValue() && TimeUtil.nanoTimeAvailable()) {
         this.useNanosForElapsedTime = true;
         this.queryTimingUnits = Messages.getString("Nanoseconds");
      } else {
         this.queryTimingUnits = Messages.getString("Milliseconds");
      }

      if (this.propertySet.getBooleanProperty("logSlowQueries").getValue()) {
         this.calculateSlowQueryThreshold();
      }

      this.authProvider = new NativeAuthenticationProvider(this.log);
      this.authProvider.init(this, this.getPropertySet(), this.socketConnection.getExceptionInterceptor());
      Map<Class<? extends ProtocolEntity>, ProtocolEntityReader<? extends ProtocolEntity, NativePacketPayload>> protocolEntityClassToTextReader = new HashMap<>(
         
      );
      protocolEntityClassToTextReader.put(ColumnDefinition.class, new ColumnDefinitionReader(this));
      protocolEntityClassToTextReader.put(ResultsetRow.class, new ResultsetRowReader(this));
      protocolEntityClassToTextReader.put(Resultset.class, new TextResultsetReader(this));
      this.PROTOCOL_ENTITY_CLASS_TO_TEXT_READER = Collections.unmodifiableMap(protocolEntityClassToTextReader);
      Map<Class<? extends ProtocolEntity>, ProtocolEntityReader<? extends ProtocolEntity, NativePacketPayload>> protocolEntityClassToBinaryReader = new HashMap<>(
         
      );
      protocolEntityClassToBinaryReader.put(ColumnDefinition.class, new ColumnDefinitionReader(this));
      protocolEntityClassToBinaryReader.put(Resultset.class, new BinaryResultsetReader(this));
      this.PROTOCOL_ENTITY_CLASS_TO_BINARY_READER = Collections.unmodifiableMap(protocolEntityClassToBinaryReader);
   }

   @Override
   public MessageBuilder<NativePacketPayload> getMessageBuilder() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public MessageSender<NativePacketPayload> getPacketSender() {
      return this.packetSender;
   }

   public MessageReader<NativePacketHeader, NativePacketPayload> getPacketReader() {
      return this.packetReader;
   }

   @Override
   public void negotiateSSLConnection(int packLength) {
      if (!ExportControlled.enabled()) {
         throw new CJConnectionFeatureNotAvailableException(this.getPropertySet(), this.serverSession, this.getPacketSentTimeHolder(), null);
      } else {
         long clientParam = this.serverSession.getClientParam();
         clientParam |= 2048L;
         this.serverSession.setClientParam(clientParam);
         NativePacketPayload packet = new NativePacketPayload(packLength);
         packet.writeInteger(NativeConstants.IntegerDataType.INT4, clientParam);
         packet.writeInteger(NativeConstants.IntegerDataType.INT4, 16777215L);
         packet.writeInteger(
            NativeConstants.IntegerDataType.INT1,
            (long)AuthenticationProvider.getCharsetForHandshake(
               this.authProvider.getEncodingForHandshake(), this.serverSession.getCapabilities().getServerVersion()
            )
         );
         packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, new byte[23]);
         this.send(packet, packet.getPosition());

         try {
            this.socketConnection.performTlsHandshake(this.serverSession);
         } catch (FeatureNotAvailableException var6) {
            throw new CJConnectionFeatureNotAvailableException(this.getPropertySet(), this.serverSession, this.getPacketSentTimeHolder(), var6);
         } catch (IOException var7) {
            throw ExceptionFactory.createCommunicationsException(
               this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var7, this.getExceptionInterceptor()
            );
         }

         this.packetSender = new SimplePacketSender(this.socketConnection.getMysqlOutput());
         this.packetReader = new SimplePacketReader(this.socketConnection, this.maxAllowedPacket);
      }
   }

   public void rejectProtocol(NativePacketPayload msg) {
      try {
         this.socketConnection.getMysqlSocket().close();
      } catch (Exception var8) {
      }

      int errno = 2000;
      NativePacketPayload buf = msg;
      msg.setPosition(1);
      errno = (int)msg.readInteger(NativeConstants.IntegerDataType.INT2);
      String serverErrorMessage = "";

      try {
         serverErrorMessage = buf.readString(NativeConstants.StringSelfDataType.STRING_TERM, "ASCII");
      } catch (Exception var7) {
      }

      StringBuilder errorBuf = new StringBuilder(Messages.getString("Protocol.0"));
      errorBuf.append(serverErrorMessage);
      errorBuf.append("\"");
      String xOpen = MysqlErrorNumbers.mysqlToSqlState(errno);
      throw ExceptionFactory.createException(
         MysqlErrorNumbers.get(xOpen) + ", " + errorBuf.toString(), xOpen, errno, false, null, this.getExceptionInterceptor()
      );
   }

   @Override
   public void beforeHandshake() {
      this.packetReader.resetMessageSequence();
      this.serverSession = new NativeServerSession(this.propertySet);
      NativeCapabilities capabilities = this.readServerCapabilities();
      this.serverSession.setCapabilities(capabilities);
   }

   @Override
   public void afterHandshake() {
      this.checkTransactionState();
      PropertySet pset = this.getPropertySet();
      if ((this.serverSession.getCapabilities().getCapabilityFlags() & 32) != 0
         && pset.getBooleanProperty("useCompression").getValue()
         && !(this.socketConnection.getMysqlInput().getUnderlyingStream() instanceof CompressedInputStream)) {
         this.useCompression = true;
         this.socketConnection
            .setMysqlInput(new CompressedInputStream(this.socketConnection.getMysqlInput(), pset.getBooleanProperty("traceProtocol"), this.log));
         this.compressedPacketSender = new CompressedPacketSender(this.socketConnection.getMysqlOutput());
         this.packetSender = this.compressedPacketSender;
      }

      this.applyPacketDecorators(this.packetSender, this.packetReader);

      try {
         this.socketConnection.getSocketFactory().afterHandshake();
      } catch (IOException var3) {
         throw ExceptionFactory.createCommunicationsException(
            this.getPropertySet(),
            this.serverSession,
            this.getPacketSentTimeHolder(),
            this.getPacketReceivedTimeHolder(),
            var3,
            this.getExceptionInterceptor()
         );
      }

      this.maintainTimeStats.addListener(this);
      pset.getBooleanProperty("traceProtocol").addListener(this);
      pset.getBooleanProperty("enablePacketDebug").addListener(this);
   }

   @Override
   public void handlePropertyChange(RuntimeProperty<?> prop) {
      String var2 = prop.getPropertyDefinition().getName();
      switch(var2) {
         case "maintainTimeStats":
         case "traceProtocol":
         case "enablePacketDebug":
            this.applyPacketDecorators(this.packetSender.undecorateAll(), this.packetReader.undecorateAll());
      }
   }

   public void applyPacketDecorators(MessageSender<NativePacketPayload> sender, MessageReader<NativePacketHeader, NativePacketPayload> messageReader) {
      TimeTrackingPacketSender ttSender = null;
      TimeTrackingPacketReader ttReader = null;
      LinkedList<StringBuilder> debugRingBuffer = null;
      if (this.maintainTimeStats.getValue()) {
         ttSender = new TimeTrackingPacketSender(sender);
         sender = ttSender;
         ttReader = new TimeTrackingPacketReader(messageReader);
         messageReader = ttReader;
      }

      if (this.propertySet.getBooleanProperty("traceProtocol").getValue()) {
         sender = new TracingPacketSender(sender, this.log, this.socketConnection.getHost(), this.getServerSession().getCapabilities().getThreadId());
         messageReader = new TracingPacketReader(messageReader, this.log);
      }

      if (this.getPropertySet().getBooleanProperty("enablePacketDebug").getValue()) {
         debugRingBuffer = new LinkedList<>();
         sender = new DebugBufferingPacketSender(sender, debugRingBuffer, this.propertySet.getIntegerProperty("packetDebugBufferSize"));
         messageReader = new DebugBufferingPacketReader(messageReader, debugRingBuffer, this.propertySet.getIntegerProperty("packetDebugBufferSize"));
      }

      messageReader = new MultiPacketReader(messageReader);
      synchronized(this.packetReader) {
         this.packetReader = messageReader;
         this.packetDebugRingBuffer = debugRingBuffer;
         this.setPacketSentTimeHolder((PacketSentTimeHolder)(ttSender != null ? ttSender : new PacketSentTimeHolder() {
         }));
      }

      synchronized(this.packetSender) {
         this.packetSender = sender;
         this.setPacketReceivedTimeHolder((PacketReceivedTimeHolder)(ttReader != null ? ttReader : new PacketReceivedTimeHolder() {
         }));
      }
   }

   public NativeCapabilities readServerCapabilities() {
      NativePacketPayload buf = this.readMessage(null);
      if (buf.isErrorPacket()) {
         this.rejectProtocol(buf);
      }

      NativeCapabilities serverCapabilities = new NativeCapabilities();
      serverCapabilities.setInitialHandshakePacket(buf);
      return serverCapabilities;
   }

   public NativeServerSession getServerSession() {
      return this.serverSession;
   }

   @Override
   public void changeDatabase(String database) {
      if (database != null && database.length() != 0) {
         try {
            this.sendCommand(this.commandBuilder.buildComInitDb(this.getSharedSendPacket(), database), false, 0);
         } catch (CJException var3) {
            if (!this.getPropertySet().getBooleanProperty("createDatabaseIfNotExist").getValue()) {
               throw ExceptionFactory.createCommunicationsException(
                  this.getPropertySet(),
                  this.serverSession,
                  this.getPacketSentTimeHolder(),
                  this.getPacketReceivedTimeHolder(),
                  var3,
                  this.getExceptionInterceptor()
               );
            }

            this.sendCommand(this.commandBuilder.buildComQuery(this.getSharedSendPacket(), "CREATE DATABASE IF NOT EXISTS " + database), false, 0);
            this.sendCommand(this.commandBuilder.buildComInitDb(this.getSharedSendPacket(), database), false, 0);
         }
      }
   }

   public final NativePacketPayload readMessage(NativePacketPayload reuse) {
      try {
         NativePacketHeader header = this.packetReader.readHeader();
         NativePacketPayload buf = this.packetReader.readMessage(Optional.ofNullable(reuse), header);
         this.packetSequence = header.getMessageSequence();
         return buf;
      } catch (IOException var4) {
         throw ExceptionFactory.createCommunicationsException(
            this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var4, this.getExceptionInterceptor()
         );
      } catch (OutOfMemoryError var5) {
         throw ExceptionFactory.createException(var5.getMessage(), "HY001", 0, false, var5, this.exceptionInterceptor);
      }
   }

   @Override
   public final void send(Message packet, int packetLen) {
      try {
         if (this.maxAllowedPacket.getValue() > 0 && packetLen > this.maxAllowedPacket.getValue()) {
            throw new CJPacketTooBigException((long)packetLen, (long)this.maxAllowedPacket.getValue().intValue());
         } else {
            ++this.packetSequence;
            this.packetSender.send(packet.getByteBuffer(), packetLen, this.packetSequence);
            if (packet == this.sharedSendPacket) {
               this.reclaimLargeSharedSendPacket();
            }
         }
      } catch (IOException var4) {
         throw ExceptionFactory.createCommunicationsException(
            this.getPropertySet(),
            this.serverSession,
            this.getPacketSentTimeHolder(),
            this.getPacketReceivedTimeHolder(),
            var4,
            this.getExceptionInterceptor()
         );
      }
   }

   @Override
   public <RES extends QueryResult> CompletableFuture<RES> sendAsync(Message message) {
      return null;
   }

   public final NativePacketPayload sendCommand(Message queryPacket, boolean skipCheck, int timeoutMillis) {
      int command = queryPacket.getByteBuffer()[0];
      ++this.commandCount;
      if (this.queryInterceptors != null) {
         NativePacketPayload interceptedPacketPayload = this.invokeQueryInterceptorsPre(queryPacket, false);
         if (interceptedPacketPayload != null) {
            return interceptedPacketPayload;
         }
      }

      this.packetReader.resetMessageSequence();
      int oldTimeout = 0;
      if (timeoutMillis != 0) {
         try {
            oldTimeout = this.socketConnection.getMysqlSocket().getSoTimeout();
            this.socketConnection.getMysqlSocket().setSoTimeout(timeoutMillis);
         } catch (SocketException var22) {
            throw ExceptionFactory.createCommunicationsException(
               this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var22, this.getExceptionInterceptor()
            );
         }
      }

      NativePacketPayload var7;
      try {
         this.checkForOutstandingStreamingData();
         this.serverSession.setStatusFlags(0, true);
         this.hadWarnings = false;
         this.setWarningCount(0);
         if (this.useCompression) {
            int bytesLeft = this.socketConnection.getMysqlInput().available();
            if (bytesLeft > 0) {
               this.socketConnection.getMysqlInput().skip((long)bytesLeft);
            }
         }

         try {
            this.clearInputStream();
            this.packetSequence = -1;
            this.send(queryPacket, queryPacket.getPosition());
         } catch (CJException var20) {
            throw var20;
         } catch (Exception var21) {
            throw ExceptionFactory.createCommunicationsException(
               this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var21, this.getExceptionInterceptor()
            );
         }

         NativePacketPayload returnPacket = null;
         if (!skipCheck) {
            if (command == 23 || command == 26) {
               this.packetReader.resetMessageSequence();
            }

            returnPacket = this.checkErrorMessage(command);
            if (this.queryInterceptors != null) {
               returnPacket = this.invokeQueryInterceptorsPost(queryPacket, returnPacket, false);
            }
         }

         var7 = returnPacket;
      } catch (IOException var23) {
         this.serverSession.preserveOldTransactionState();
         throw ExceptionFactory.createCommunicationsException(
            this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var23, this.getExceptionInterceptor()
         );
      } catch (CJException var24) {
         this.serverSession.preserveOldTransactionState();
         throw var24;
      } finally {
         if (timeoutMillis != 0) {
            try {
               this.socketConnection.getMysqlSocket().setSoTimeout(oldTimeout);
            } catch (SocketException var19) {
               throw ExceptionFactory.createCommunicationsException(
                  this.propertySet,
                  this.serverSession,
                  this.getPacketSentTimeHolder(),
                  this.getPacketReceivedTimeHolder(),
                  var19,
                  this.getExceptionInterceptor()
               );
            }
         }
      }

      return var7;
   }

   public void checkTransactionState() {
      int transState = this.serverSession.getTransactionState();
      if (transState == 3) {
         this.transactionManager.transactionCompleted();
      } else if (transState == 2) {
         this.transactionManager.transactionBegun();
      }
   }

   public NativePacketPayload checkErrorMessage() {
      return this.checkErrorMessage(-1);
   }

   private NativePacketPayload checkErrorMessage(int command) {
      NativePacketPayload resultPacket = null;
      this.serverSession.setStatusFlags(0);

      try {
         resultPacket = this.readMessage(this.reusablePacket);
      } catch (CJException var4) {
         throw var4;
      } catch (Exception var5) {
         throw ExceptionFactory.createCommunicationsException(
            this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var5, this.getExceptionInterceptor()
         );
      }

      this.checkErrorMessage(resultPacket);
      return resultPacket;
   }

   public void checkErrorMessage(NativePacketPayload resultPacket) {
      resultPacket.setPosition(0);
      byte statusCode = (byte)((int)resultPacket.readInteger(NativeConstants.IntegerDataType.INT1));
      if (statusCode == -1) {
         int errno = 2000;
         errno = (int)resultPacket.readInteger(NativeConstants.IntegerDataType.INT2);
         String xOpen = null;
         String serverErrorMessage = resultPacket.readString(NativeConstants.StringSelfDataType.STRING_TERM, this.serverSession.getErrorMessageEncoding());
         if (serverErrorMessage.charAt(0) == '#') {
            if (serverErrorMessage.length() > 6) {
               xOpen = serverErrorMessage.substring(1, 6);
               serverErrorMessage = serverErrorMessage.substring(6);
               if (xOpen.equals("HY000")) {
                  xOpen = MysqlErrorNumbers.mysqlToSqlState(errno);
               }
            } else {
               xOpen = MysqlErrorNumbers.mysqlToSqlState(errno);
            }
         } else {
            xOpen = MysqlErrorNumbers.mysqlToSqlState(errno);
         }

         this.clearInputStream();
         StringBuilder errorBuf = new StringBuilder();
         String xOpenErrorMessage = MysqlErrorNumbers.get(xOpen);
         boolean useOnlyServerErrorMessages = this.propertySet.getBooleanProperty("useOnlyServerErrorMessages").getValue();
         if (!useOnlyServerErrorMessages && xOpenErrorMessage != null) {
            errorBuf.append(xOpenErrorMessage);
            errorBuf.append(Messages.getString("Protocol.0"));
         }

         errorBuf.append(serverErrorMessage);
         if (!useOnlyServerErrorMessages && xOpenErrorMessage != null) {
            errorBuf.append("\"");
         }

         this.appendDeadlockStatusInformation(this.session, xOpen, errorBuf);
         if (xOpen != null) {
            if (xOpen.startsWith("22")) {
               throw new DataTruncationException(errorBuf.toString(), 0, true, false, 0, 0, errno);
            }

            if (errno == 1820) {
               throw (PasswordExpiredException)ExceptionFactory.createException(
                  PasswordExpiredException.class, errorBuf.toString(), this.getExceptionInterceptor()
               );
            }

            if (errno == 1862) {
               throw (ClosedOnExpiredPasswordException)ExceptionFactory.createException(
                  ClosedOnExpiredPasswordException.class, errorBuf.toString(), this.getExceptionInterceptor()
               );
            }
         }

         throw ExceptionFactory.createException(errorBuf.toString(), xOpen, errno, false, null, this.getExceptionInterceptor());
      }
   }

   private void reclaimLargeSharedSendPacket() {
      if (this.sharedSendPacket != null && this.sharedSendPacket.getCapacity() > 1048576) {
         this.sharedSendPacket = new NativePacketPayload(1024);
      }
   }

   public void clearInputStream() {
      try {
         int len;
         while((len = this.socketConnection.getMysqlInput().available()) > 0 && this.socketConnection.getMysqlInput().skip((long)len) > 0L) {
         }
      } catch (IOException var2) {
         throw ExceptionFactory.createCommunicationsException(
            this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var2, this.getExceptionInterceptor()
         );
      }
   }

   public void reclaimLargeReusablePacket() {
      if (this.reusablePacket != null && this.reusablePacket.getCapacity() > 1048576) {
         this.reusablePacket = new NativePacketPayload(1024);
      }
   }

   public final <T extends Resultset> T sendQueryString(
      Query callingQuery,
      String query,
      String characterEncoding,
      int maxRows,
      boolean streamResults,
      String catalog,
      ColumnDefinition cachedMetadata,
      Protocol.GetProfilerEventHandlerInstanceFunction getProfilerEventHandlerInstanceFunction,
      ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) throws IOException {
      String statementComment = this.queryComment;
      if (this.propertySet.getBooleanProperty("includeThreadNamesAsStatementComment").getValue()) {
         statementComment = (statementComment != null ? statementComment + ", " : "") + "java thread: " + Thread.currentThread().getName();
      }

      int packLength = 1 + query.length() * 4 + 2;
      byte[] commentAsBytes = null;
      if (statementComment != null) {
         commentAsBytes = StringUtils.getBytes(statementComment, characterEncoding);
         packLength += commentAsBytes.length;
         packLength += 6;
      }

      NativePacketPayload sendPacket = new NativePacketPayload(packLength);
      sendPacket.setPosition(0);
      sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 3L);
      if (commentAsBytes != null) {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SLASH_STAR_SPACE_AS_BYTES);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, commentAsBytes);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
      }

      if (!this.platformDbCharsetMatches && StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, StringUtils.getBytes(query));
      } else {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, StringUtils.getBytes(query, characterEncoding));
      }

      return this.sendQueryPacket(
         callingQuery, sendPacket, maxRows, streamResults, catalog, cachedMetadata, getProfilerEventHandlerInstanceFunction, resultSetFactory
      );
   }

   public final <T extends Resultset> T sendQueryPacket(
      Query callingQuery,
      NativePacketPayload queryPacket,
      int maxRows,
      boolean streamResults,
      String catalog,
      ColumnDefinition cachedMetadata,
      Protocol.GetProfilerEventHandlerInstanceFunction getProfilerEventHandlerInstanceFunction,
      ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) throws IOException {
      ++this.statementExecutionDepth;
      byte[] queryBuf = null;
      int oldPacketPosition = 0;
      long queryStartTime = 0L;
      long queryEndTime = 0L;
      queryBuf = queryPacket.getByteBuffer();
      oldPacketPosition = queryPacket.getPosition();
      queryStartTime = this.getCurrentTimeNanosOrMillis();
      LazyString query = new LazyString(queryBuf, 1, oldPacketPosition - 1);

      try {
         if (this.queryInterceptors != null) {
            T interceptedResults = this.invokeQueryInterceptorsPre(query, callingQuery, false);
            if (interceptedResults != null) {
               return interceptedResults;
            }
         }

         if (this.autoGenerateTestcaseScript) {
            StringBuilder debugBuf = new StringBuilder(query.length() + 32);
            this.generateQueryCommentBlock(debugBuf);
            debugBuf.append(query);
            debugBuf.append(';');
            TestUtils.dumpTestcaseQuery(debugBuf.toString());
         }

         NativePacketPayload resultPacket = this.sendCommand(queryPacket, false, 0);
         long fetchBeginTime = 0L;
         long fetchEndTime = 0L;
         String profileQueryToLog = null;
         boolean queryWasSlow = false;
         if (this.profileSQL || this.logSlowQueries) {
            queryEndTime = this.getCurrentTimeNanosOrMillis();
            boolean shouldExtractQuery = false;
            if (this.profileSQL) {
               shouldExtractQuery = true;
            } else if (this.logSlowQueries) {
               long queryTime = queryEndTime - queryStartTime;
               boolean logSlow = false;
               if (!this.useAutoSlowLog) {
                  logSlow = queryTime > (long)this.propertySet.getIntegerProperty("slowQueryThresholdMillis").getValue().intValue();
               } else {
                  logSlow = this.metricsHolder.isAbonormallyLongQuery(queryTime);
                  this.metricsHolder.reportQueryTime(queryTime);
               }

               if (logSlow) {
                  shouldExtractQuery = true;
                  queryWasSlow = true;
               }
            }

            if (shouldExtractQuery) {
               boolean truncated = false;
               int extractPosition = oldPacketPosition;
               if (oldPacketPosition > this.maxQuerySizeToLog.getValue()) {
                  extractPosition = this.maxQuerySizeToLog.getValue() + 1;
                  truncated = true;
               }

               profileQueryToLog = StringUtils.toString(queryBuf, 1, extractPosition - 1);
               if (truncated) {
                  profileQueryToLog = profileQueryToLog + Messages.getString("Protocol.2");
               }
            }

            fetchBeginTime = queryEndTime;
         }

         T rs = this.readAllResults(maxRows, streamResults, resultPacket, false, cachedMetadata, resultSetFactory);
         long threadId = this.getServerSession().getCapabilities().getThreadId();
         int queryId = callingQuery != null ? callingQuery.getId() : 999;
         int resultSetId = rs.getResultId();
         long eventDuration = queryEndTime - queryStartTime;
         if (queryWasSlow && !this.serverSession.queryWasSlow()) {
            StringBuilder mesgBuf = new StringBuilder(48 + profileQueryToLog.length());
            mesgBuf.append(
               Messages.getString(
                  "Protocol.SlowQuery",
                  new Object[]{
                     String.valueOf(this.useAutoSlowLog ? " 95% of all queries " : this.slowQueryThreshold),
                     this.queryTimingUnits,
                     queryEndTime - queryStartTime
                  }
               )
            );
            mesgBuf.append(profileQueryToLog);
            ProfilerEventHandler eventSink = getProfilerEventHandlerInstanceFunction.apply();
            eventSink.consumeEvent(
               new ProfilerEventImpl(
                  (byte)6,
                  "",
                  catalog,
                  threadId,
                  queryId,
                  resultSetId,
                  System.currentTimeMillis(),
                  eventDuration,
                  this.queryTimingUnits,
                  null,
                  LogUtils.findCallingClassAndMethod(new Throwable()),
                  mesgBuf.toString()
               )
            );
            if (this.propertySet.getBooleanProperty("explainSlowQueries").getValue()) {
               if (oldPacketPosition < 1048576) {
                  queryPacket.setPosition(1);
                  this.explainSlowQuery(query.toString(), profileQueryToLog);
               } else {
                  this.log.logWarn(Messages.getString("Protocol.3", new Object[]{1048576}));
               }
            }
         }

         if (this.profileSQL || this.logSlowQueries) {
            ProfilerEventHandler eventSink = getProfilerEventHandlerInstanceFunction.apply();
            String eventCreationPoint = LogUtils.findCallingClassAndMethod(new Throwable());
            if (this.logSlowQueries) {
               if (this.serverSession.noGoodIndexUsed()) {
                  eventSink.consumeEvent(
                     new ProfilerEventImpl(
                        (byte)6,
                        "",
                        catalog,
                        threadId,
                        queryId,
                        resultSetId,
                        System.currentTimeMillis(),
                        eventDuration,
                        this.queryTimingUnits,
                        null,
                        eventCreationPoint,
                        Messages.getString("Protocol.4") + profileQueryToLog
                     )
                  );
               }

               if (this.serverSession.noIndexUsed()) {
                  eventSink.consumeEvent(
                     new ProfilerEventImpl(
                        (byte)6,
                        "",
                        catalog,
                        threadId,
                        queryId,
                        resultSetId,
                        System.currentTimeMillis(),
                        eventDuration,
                        this.queryTimingUnits,
                        null,
                        eventCreationPoint,
                        Messages.getString("Protocol.5") + profileQueryToLog
                     )
                  );
               }

               if (this.serverSession.queryWasSlow()) {
                  eventSink.consumeEvent(
                     new ProfilerEventImpl(
                        (byte)6,
                        "",
                        catalog,
                        threadId,
                        queryId,
                        resultSetId,
                        System.currentTimeMillis(),
                        eventDuration,
                        this.queryTimingUnits,
                        null,
                        eventCreationPoint,
                        Messages.getString("Protocol.ServerSlowQuery") + profileQueryToLog
                     )
                  );
               }
            }

            fetchEndTime = this.getCurrentTimeNanosOrMillis();
            eventSink.consumeEvent(
               new ProfilerEventImpl(
                  (byte)3,
                  "",
                  catalog,
                  threadId,
                  queryId,
                  resultSetId,
                  System.currentTimeMillis(),
                  eventDuration,
                  this.queryTimingUnits,
                  null,
                  eventCreationPoint,
                  profileQueryToLog
               )
            );
            eventSink.consumeEvent(
               new ProfilerEventImpl(
                  (byte)5,
                  "",
                  catalog,
                  threadId,
                  queryId,
                  resultSetId,
                  System.currentTimeMillis(),
                  fetchEndTime - fetchBeginTime,
                  this.queryTimingUnits,
                  null,
                  eventCreationPoint,
                  null
               )
            );
         }

         if (this.hadWarnings) {
            this.scanForAndThrowDataTruncation();
         }

         if (this.queryInterceptors != null) {
            T interceptedResults = this.invokeQueryInterceptorsPost(query, callingQuery, rs, false);
            if (interceptedResults != null) {
               rs = interceptedResults;
            }
         }

         return rs;
      } catch (CJException var35) {
         if (this.queryInterceptors != null) {
            this.invokeQueryInterceptorsPost(query, callingQuery, (T)null, false);
         }

         if (callingQuery != null) {
            callingQuery.checkCancelTimeout();
         }

         throw var35;
      } finally {
         --this.statementExecutionDepth;
      }
   }

   public <T extends Resultset> T invokeQueryInterceptorsPre(Supplier<String> sql, Query interceptedQuery, boolean forceExecute) {
      T previousResultSet = null;
      int i = 0;

      for(int s = this.queryInterceptors.size(); i < s; ++i) {
         QueryInterceptor interceptor = this.queryInterceptors.get(i);
         boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
         boolean shouldExecute = executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute) || !executeTopLevelOnly;
         if (shouldExecute) {
            T interceptedResultSet = interceptor.preProcess(sql, interceptedQuery);
            if (interceptedResultSet != null) {
               previousResultSet = interceptedResultSet;
            }
         }
      }

      return previousResultSet;
   }

   public <M extends Message> M invokeQueryInterceptorsPre(M queryPacket, boolean forceExecute) {
      M previousPacketPayload = null;
      int i = 0;

      for(int s = this.queryInterceptors.size(); i < s; ++i) {
         QueryInterceptor interceptor = this.queryInterceptors.get(i);
         M interceptedPacketPayload = interceptor.preProcess(queryPacket);
         if (interceptedPacketPayload != null) {
            previousPacketPayload = interceptedPacketPayload;
         }
      }

      return previousPacketPayload;
   }

   public <T extends Resultset> T invokeQueryInterceptorsPost(Supplier<String> sql, Query interceptedQuery, T originalResultSet, boolean forceExecute) {
      int i = 0;

      for(int s = this.queryInterceptors.size(); i < s; ++i) {
         QueryInterceptor interceptor = this.queryInterceptors.get(i);
         boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
         boolean shouldExecute = executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute) || !executeTopLevelOnly;
         if (shouldExecute) {
            T interceptedResultSet = interceptor.postProcess(sql, interceptedQuery, originalResultSet, this.serverSession);
            if (interceptedResultSet != null) {
               originalResultSet = interceptedResultSet;
            }
         }
      }

      return originalResultSet;
   }

   public <M extends Message> M invokeQueryInterceptorsPost(M queryPacket, M originalResponsePacket, boolean forceExecute) {
      int i = 0;

      for(int s = this.queryInterceptors.size(); i < s; ++i) {
         QueryInterceptor interceptor = this.queryInterceptors.get(i);
         M interceptedPacketPayload = interceptor.postProcess(queryPacket, originalResponsePacket);
         if (interceptedPacketPayload != null) {
            originalResponsePacket = interceptedPacketPayload;
         }
      }

      return originalResponsePacket;
   }

   public long getCurrentTimeNanosOrMillis() {
      return this.useNanosForElapsedTime ? TimeUtil.getCurrentTimeNanosOrMillis() : System.currentTimeMillis();
   }

   public boolean hadWarnings() {
      return this.hadWarnings;
   }

   public void setHadWarnings(boolean hadWarnings) {
      this.hadWarnings = hadWarnings;
   }

   public void explainSlowQuery(String query, String truncatedQuery) {
      if (StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, "SELECT")
         || this.versionMeetsMinimum(5, 6, 3) && StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, EXPLAINABLE_STATEMENT_EXTENSION) != -1) {
         try {
            NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(this.getSharedSendPacket(), "EXPLAIN " + query), false, 0);
            Resultset rs = this.readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            StringBuilder explainResults = new StringBuilder(Messages.getString("Protocol.6"));
            explainResults.append(truncatedQuery);
            explainResults.append(Messages.getString("Protocol.7"));
            this.appendResultSetSlashGStyle(explainResults, rs);
            this.log.logWarn(explainResults.toString());
         } catch (CJException var6) {
            throw var6;
         } catch (Exception var7) {
            throw ExceptionFactory.createException(var7.getMessage(), var7, this.getExceptionInterceptor());
         }
      }
   }

   public final void skipPacket() {
      try {
         int packetLength = this.packetReader.readHeader().getMessageSize();
         this.socketConnection.getMysqlInput().skipFully((long)packetLength);
      } catch (IOException var2) {
         throw ExceptionFactory.createCommunicationsException(
            this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var2, this.getExceptionInterceptor()
         );
      }
   }

   public final void quit() {
      try {
         try {
            if (!this.socketConnection.getMysqlSocket().isClosed()) {
               try {
                  this.socketConnection.getMysqlSocket().shutdownInput();
               } catch (UnsupportedOperationException var6) {
               }
            }
         } catch (IOException var7) {
            this.log.logWarn("Caught while disconnecting...", var7);
         }

         this.packetSequence = -1;
         NativePacketPayload packet = new NativePacketPayload(1);
         this.send(this.commandBuilder.buildComQuit(packet), packet.getPosition());
      } finally {
         this.socketConnection.forceClose();
         this.localInfileInputStream = null;
      }
   }

   public NativePacketPayload getSharedSendPacket() {
      if (this.sharedSendPacket == null) {
         this.sharedSendPacket = new NativePacketPayload(1024);
      }

      this.sharedSendPacket.setPosition(0);
      return this.sharedSendPacket;
   }

   private void calculateSlowQueryThreshold() {
      this.slowQueryThreshold = (long)this.propertySet.getIntegerProperty("slowQueryThresholdMillis").getValue().intValue();
      if (this.propertySet.getBooleanProperty("useNanosForElapsedTime").getValue()) {
         long nanosThreshold = this.propertySet.getLongProperty("slowQueryThresholdNanos").getValue();
         if (nanosThreshold != 0L) {
            this.slowQueryThreshold = nanosThreshold;
         } else {
            this.slowQueryThreshold *= 1000000L;
         }
      }
   }

   @Override
   public void changeUser(String user, String password, String database) {
      this.packetSequence = -1;
      this.authProvider.changeUser(this.serverSession, user, password, database);
   }

   public void checkForCharsetMismatch() {
      String characterEncoding = this.propertySet.getStringProperty("characterEncoding").getValue();
      if (characterEncoding != null) {
         String encodingToCheck = jvmPlatformCharset;
         if (encodingToCheck == null) {
            encodingToCheck = Constants.PLATFORM_ENCODING;
         }

         if (encodingToCheck == null) {
            this.platformDbCharsetMatches = false;
         } else {
            this.platformDbCharsetMatches = encodingToCheck.equals(characterEncoding);
         }
      }
   }

   protected boolean useNanosForElapsedTime() {
      return this.useNanosForElapsedTime;
   }

   public long getSlowQueryThreshold() {
      return this.slowQueryThreshold;
   }

   public String getQueryTimingUnits() {
      return this.queryTimingUnits;
   }

   public int getCommandCount() {
      return this.commandCount;
   }

   public void setQueryInterceptors(List<QueryInterceptor> queryInterceptors) {
      this.queryInterceptors = queryInterceptors.isEmpty() ? null : queryInterceptors;
   }

   public List<QueryInterceptor> getQueryInterceptors() {
      return this.queryInterceptors;
   }

   public void setSocketTimeout(int milliseconds) {
      try {
         Socket soc = this.socketConnection.getMysqlSocket();
         if (soc != null) {
            soc.setSoTimeout(milliseconds);
         }
      } catch (SocketException var3) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("Protocol.8"), var3, this.getExceptionInterceptor()
         );
      }
   }

   public void releaseResources() {
      if (this.compressedPacketSender != null) {
         this.compressedPacketSender.stop();
      }
   }

   @Override
   public void connect(String user, String password, String database) {
      this.beforeHandshake();
      this.authProvider.connect(this.serverSession, user, password, database);
   }

   protected boolean isDataAvailable() {
      try {
         return this.socketConnection.getMysqlInput().available() > 0;
      } catch (IOException var2) {
         throw ExceptionFactory.createCommunicationsException(
            this.propertySet, this.serverSession, this.getPacketSentTimeHolder(), this.getPacketReceivedTimeHolder(), var2, this.getExceptionInterceptor()
         );
      }
   }

   public NativePacketPayload getReusablePacket() {
      return this.reusablePacket;
   }

   public int getWarningCount() {
      return this.warningCount;
   }

   public void setWarningCount(int warningCount) {
      this.warningCount = warningCount;
   }

   public void dumpPacketRingBuffer() {
      LinkedList<StringBuilder> localPacketDebugRingBuffer = this.packetDebugRingBuffer;
      if (localPacketDebugRingBuffer != null) {
         StringBuilder dumpBuffer = new StringBuilder();
         dumpBuffer.append("Last " + localPacketDebugRingBuffer.size() + " packets received from server, from oldest->newest:\n");
         dumpBuffer.append("\n");
         Iterator<StringBuilder> ringBufIter = localPacketDebugRingBuffer.iterator();

         while(ringBufIter.hasNext()) {
            dumpBuffer.append(ringBufIter.next());
            dumpBuffer.append("\n");
         }

         this.log.logTrace(dumpBuffer.toString());
      }
   }

   public boolean doesPlatformDbCharsetMatches() {
      return this.platformDbCharsetMatches;
   }

   @Override
   public String getPasswordCharacterEncoding() {
      String encoding;
      if ((encoding = this.propertySet.getStringProperty("passwordCharacterEncoding").getStringValue()) != null) {
         return encoding;
      } else {
         return (encoding = this.propertySet.getStringProperty("characterEncoding").getValue()) != null ? encoding : "UTF-8";
      }
   }

   @Override
   public boolean versionMeetsMinimum(int major, int minor, int subminor) {
      return this.serverSession.getServerVersion().meetsMinimum(new ServerVersion(major, minor, subminor));
   }

   public static MysqlType findMysqlType(
      PropertySet propertySet,
      int mysqlTypeId,
      short colFlag,
      long length,
      LazyString tableName,
      LazyString originalTableName,
      int collationIndex,
      String encoding
   ) {
      boolean isUnsigned = (colFlag & 32) > 0;
      boolean isFromFunction = originalTableName.length() == 0;
      boolean isBinary = (colFlag & 128) > 0;
      boolean isImplicitTemporaryTable = tableName.length() > 0 && tableName.toString().startsWith("#sql_");
      boolean isOpaqueBinary = isBinary && collationIndex == 63 && (mysqlTypeId == 254 || mysqlTypeId == 253 || mysqlTypeId == 15)
         ? !isImplicitTemporaryTable
         : "binary".equalsIgnoreCase(encoding);
      switch(mysqlTypeId) {
         case 0:
         case 246:
            return isUnsigned ? MysqlType.DECIMAL_UNSIGNED : MysqlType.DECIMAL;
         case 1:
            if (length == 1L) {
               if (propertySet.getBooleanProperty("transformedBitIsBoolean").getValue()) {
                  return MysqlType.BOOLEAN;
               }

               if (propertySet.getBooleanProperty("tinyInt1isBit").getValue()) {
                  return MysqlType.BIT;
               }
            }

            return isUnsigned ? MysqlType.TINYINT_UNSIGNED : MysqlType.TINYINT;
         case 2:
            return isUnsigned ? MysqlType.SMALLINT_UNSIGNED : MysqlType.SMALLINT;
         case 3:
            return isUnsigned ? MysqlType.INT_UNSIGNED : MysqlType.INT;
         case 4:
            return isUnsigned ? MysqlType.FLOAT_UNSIGNED : MysqlType.FLOAT;
         case 5:
            return isUnsigned ? MysqlType.DOUBLE_UNSIGNED : MysqlType.DOUBLE;
         case 6:
            return MysqlType.NULL;
         case 7:
            return MysqlType.TIMESTAMP;
         case 8:
            return isUnsigned ? MysqlType.BIGINT_UNSIGNED : MysqlType.BIGINT;
         case 9:
            return isUnsigned ? MysqlType.MEDIUMINT_UNSIGNED : MysqlType.MEDIUMINT;
         case 10:
            return MysqlType.DATE;
         case 11:
            return MysqlType.TIME;
         case 12:
            return MysqlType.DATETIME;
         case 13:
            return MysqlType.YEAR;
         case 15:
         case 253:
            return !isOpaqueBinary || isFromFunction && propertySet.getBooleanProperty("functionsNeverReturnBlobs").getValue()
               ? MysqlType.VARCHAR
               : MysqlType.VARBINARY;
         case 16:
            return MysqlType.BIT;
         case 245:
            return MysqlType.JSON;
         case 247:
            return MysqlType.ENUM;
         case 248:
            return MysqlType.SET;
         case 249:
            if (isBinary
               && collationIndex == 63
               && !propertySet.getBooleanProperty("blobsAreStrings").getValue()
               && (!isFromFunction || !propertySet.getBooleanProperty("functionsNeverReturnBlobs").getValue())) {
               return MysqlType.TINYBLOB;
            }

            return MysqlType.TINYTEXT;
         case 250:
            if (isBinary
               && collationIndex == 63
               && !propertySet.getBooleanProperty("blobsAreStrings").getValue()
               && (!isFromFunction || !propertySet.getBooleanProperty("functionsNeverReturnBlobs").getValue())) {
               return MysqlType.MEDIUMBLOB;
            }

            return MysqlType.MEDIUMTEXT;
         case 251:
            if (isBinary
               && collationIndex == 63
               && !propertySet.getBooleanProperty("blobsAreStrings").getValue()
               && (!isFromFunction || !propertySet.getBooleanProperty("functionsNeverReturnBlobs").getValue())) {
               return MysqlType.LONGBLOB;
            }

            return MysqlType.LONGTEXT;
         case 252:
            short newMysqlTypeId;
            if (length <= MysqlType.TINYBLOB.getPrecision()) {
               newMysqlTypeId = 249;
            } else {
               if (length <= MysqlType.BLOB.getPrecision()) {
                  if (isBinary
                     && collationIndex == 63
                     && !propertySet.getBooleanProperty("blobsAreStrings").getValue()
                     && (!isFromFunction || !propertySet.getBooleanProperty("functionsNeverReturnBlobs").getValue())) {
                     return MysqlType.BLOB;
                  }

                  newMysqlTypeId = 15;
                  return MysqlType.TEXT;
               }

               if (length <= MysqlType.MEDIUMBLOB.getPrecision()) {
                  newMysqlTypeId = 250;
               } else {
                  newMysqlTypeId = 251;
               }
            }

            return findMysqlType(propertySet, newMysqlTypeId, colFlag, length, tableName, originalTableName, collationIndex, encoding);
         case 254:
            if (isOpaqueBinary && !propertySet.getBooleanProperty("blobsAreStrings").getValue()) {
               return MysqlType.BINARY;
            }

            return MysqlType.CHAR;
         case 255:
            return MysqlType.GEOMETRY;
         default:
            return MysqlType.UNKNOWN;
      }
   }

   @Override
   public <T extends ProtocolEntity> T read(Class<T> requiredClass, ProtocolEntityFactory<T, NativePacketPayload> protocolEntityFactory) throws IOException {
      ProtocolEntityReader<T, NativePacketPayload> sr = this.PROTOCOL_ENTITY_CLASS_TO_TEXT_READER.get(requiredClass);
      if (sr == null) {
         throw (FeatureNotAvailableException)ExceptionFactory.createException(
            FeatureNotAvailableException.class, "ProtocolEntityReader isn't available for class " + requiredClass
         );
      } else {
         return sr.read(protocolEntityFactory);
      }
   }

   public <T extends ProtocolEntity> T read(
      Class<Resultset> requiredClass,
      int maxRows,
      boolean streamResults,
      NativePacketPayload resultPacket,
      boolean isBinaryEncoded,
      ColumnDefinition metadata,
      ProtocolEntityFactory<T, NativePacketPayload> protocolEntityFactory
   ) throws IOException {
      ProtocolEntityReader<T, NativePacketPayload> sr = isBinaryEncoded
         ? this.PROTOCOL_ENTITY_CLASS_TO_BINARY_READER.get(requiredClass)
         : this.PROTOCOL_ENTITY_CLASS_TO_TEXT_READER.get(requiredClass);
      if (sr == null) {
         throw (FeatureNotAvailableException)ExceptionFactory.createException(
            FeatureNotAvailableException.class, "ProtocolEntityReader isn't available for class " + requiredClass
         );
      } else {
         return sr.read(maxRows, streamResults, resultPacket, metadata, protocolEntityFactory);
      }
   }

   public <T extends ProtocolEntity> T readNextResultset(
      T currentProtocolEntity, int maxRows, boolean streamResults, boolean isBinaryEncoded, ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) throws IOException {
      T result = null;
      if (Resultset.class.isAssignableFrom(currentProtocolEntity.getClass()) && this.serverSession.useMultiResults() && this.serverSession.hasMoreResults()) {
         T currentResultSet = currentProtocolEntity;

         T newResultSet;
         do {
            NativePacketPayload fieldPacket = this.checkErrorMessage();
            fieldPacket.setPosition(0);
            newResultSet = this.read(Resultset.class, maxRows, streamResults, fieldPacket, isBinaryEncoded, null, resultSetFactory);
            ((Resultset)currentResultSet).setNextResultset((Resultset)newResultSet);
            currentResultSet = newResultSet;
            if (result == null) {
               result = newResultSet;
            }
         } while(streamResults && this.serverSession.hasMoreResults() && !((Resultset)newResultSet).hasRows());
      }

      return result;
   }

   public <T extends Resultset> T readAllResults(
      int maxRows,
      boolean streamResults,
      NativePacketPayload resultPacket,
      boolean isBinaryEncoded,
      ColumnDefinition metadata,
      ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) throws IOException {
      resultPacket.setPosition(0);
      T topLevelResultSet = this.read(Resultset.class, maxRows, streamResults, resultPacket, isBinaryEncoded, metadata, resultSetFactory);
      if (this.serverSession.hasMoreResults()) {
         T currentResultSet = topLevelResultSet;
         if (streamResults) {
            currentResultSet = this.readNextResultset(topLevelResultSet, maxRows, true, isBinaryEncoded, resultSetFactory);
         } else {
            while(this.serverSession.hasMoreResults()) {
               currentResultSet = this.readNextResultset(currentResultSet, maxRows, false, isBinaryEncoded, resultSetFactory);
            }

            this.clearInputStream();
         }
      }

      if (this.hadWarnings) {
         this.scanForAndThrowDataTruncation();
      }

      this.reclaimLargeReusablePacket();
      return topLevelResultSet;
   }

   public final <T> T readServerStatusForResultSets(NativePacketPayload rowPacket, boolean saveOldStatus) {
      T result = null;
      if (rowPacket.isEOFPacket()) {
         rowPacket.readInteger(NativeConstants.IntegerDataType.INT1);
         this.warningCount = (int)rowPacket.readInteger(NativeConstants.IntegerDataType.INT2);
         if (this.warningCount > 0) {
            this.hadWarnings = true;
         }

         this.serverSession.setStatusFlags((int)rowPacket.readInteger(NativeConstants.IntegerDataType.INT2), saveOldStatus);
         this.checkTransactionState();
      } else {
         OkPacket ok = OkPacket.parse(rowPacket, this.serverSession.getErrorMessageEncoding());
         result = ok;
         this.serverSession.setStatusFlags(ok.getStatusFlags(), saveOldStatus);
         this.checkTransactionState();
         this.warningCount = ok.getWarningCount();
         if (this.warningCount > 0) {
            this.hadWarnings = true;
         }
      }

      return result;
   }

   @Override
   public <QR extends QueryResult> QR readQueryResult() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return this.localInfileInputStream;
   }

   @Override
   public void setLocalInfileInputStream(InputStream stream) {
      this.localInfileInputStream = stream;
   }

   public final NativePacketPayload sendFileToServer(String fileName) {
      NativePacketPayload filePacket = this.loadFileBufRef == null ? null : this.loadFileBufRef.get();
      int bigPacketLength = Math.min(this.maxAllowedPacket.getValue() - 12, this.alignPacketSize(this.maxAllowedPacket.getValue() - 16, 4096) - 12);
      int oneMeg = 1048576;
      int smallerPacketSizeAligned = Math.min(oneMeg - 12, this.alignPacketSize(oneMeg - 16, 4096) - 12);
      int packetLength = Math.min(smallerPacketSizeAligned, bigPacketLength);
      if (filePacket == null) {
         try {
            filePacket = new NativePacketPayload(packetLength);
            this.loadFileBufRef = new SoftReference<>(filePacket);
         } catch (OutOfMemoryError var22) {
            throw ExceptionFactory.createException(
               Messages.getString("MysqlIO.111", new Object[]{packetLength}), "HY001", 0, false, var22, this.exceptionInterceptor
            );
         }
      }

      filePacket.setPosition(0);
      byte[] fileBuf = new byte[packetLength];
      BufferedInputStream fileIn = null;

      try {
         if (!this.propertySet.getBooleanProperty("allowLoadLocalInfile").getValue()) {
            throw ExceptionFactory.createException(Messages.getString("MysqlIO.LoadDataLocalNotAllowed"), this.exceptionInterceptor);
         }

         InputStream hookedStream = null;
         hookedStream = this.getLocalInfileInputStream();
         if (hookedStream != null) {
            fileIn = new BufferedInputStream(hookedStream);
         } else if (!this.propertySet.getBooleanProperty("allowUrlInLocalInfile").getValue()) {
            fileIn = new BufferedInputStream(new FileInputStream(fileName));
         } else if (fileName.indexOf(58) != -1) {
            try {
               URL urlFromFileName = new URL(fileName);
               fileIn = new BufferedInputStream(urlFromFileName.openStream());
            } catch (MalformedURLException var21) {
               fileIn = new BufferedInputStream(new FileInputStream(fileName));
            }
         } else {
            fileIn = new BufferedInputStream(new FileInputStream(fileName));
         }

         int bytesRead = 0;

         while((bytesRead = fileIn.read(fileBuf)) != -1) {
            filePacket.setPosition(0);
            filePacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, fileBuf, 0, bytesRead);
            this.send(filePacket, filePacket.getPosition());
         }
      } catch (IOException var23) {
         StringBuilder messageBuf = new StringBuilder(Messages.getString("MysqlIO.60"));
         boolean isParanoid = this.propertySet.getBooleanProperty("paranoid").getValue();
         if (fileName != null && !isParanoid) {
            messageBuf.append("'");
            messageBuf.append(fileName);
            messageBuf.append("'");
         }

         messageBuf.append(Messages.getString("MysqlIO.63"));
         if (!isParanoid) {
            messageBuf.append(Messages.getString("MysqlIO.64"));
            messageBuf.append(Util.stackTraceToString(var23));
         }

         throw ExceptionFactory.createException(messageBuf.toString(), var23, this.exceptionInterceptor);
      } finally {
         if (fileIn != null) {
            try {
               fileIn.close();
            } catch (Exception var20) {
               throw ExceptionFactory.createException(Messages.getString("MysqlIO.65"), var20, this.exceptionInterceptor);
            }

            fileIn = null;
         } else {
            filePacket.setPosition(0);
            this.send(filePacket, filePacket.getPosition());
            this.checkErrorMessage();
         }
      }

      filePacket.setPosition(0);
      this.send(filePacket, filePacket.getPosition());
      return this.checkErrorMessage();
   }

   private int alignPacketSize(int a, int l) {
      return a + l - 1 & ~(l - 1);
   }

   public ResultsetRows getStreamingData() {
      return this.streamingData;
   }

   public void setStreamingData(ResultsetRows streamingData) {
      this.streamingData = streamingData;
   }

   public void checkForOutstandingStreamingData() {
      if (this.streamingData != null) {
         boolean shouldClobber = this.propertySet.getBooleanProperty("clobberStreamingResults").getValue();
         if (!shouldClobber) {
            throw ExceptionFactory.createException(
               Messages.getString("MysqlIO.39")
                  + this.streamingData
                  + Messages.getString("MysqlIO.40")
                  + Messages.getString("MysqlIO.41")
                  + Messages.getString("MysqlIO.42"),
               this.exceptionInterceptor
            );
         }

         this.streamingData.getOwner().closeOwner(false);
         this.clearInputStream();
      }
   }

   public void closeStreamer(ResultsetRows streamer) {
      if (this.streamingData == null) {
         throw ExceptionFactory.createException(Messages.getString("MysqlIO.17") + streamer + Messages.getString("MysqlIO.18"), this.exceptionInterceptor);
      } else if (streamer != this.streamingData) {
         throw ExceptionFactory.createException(
            Messages.getString("MysqlIO.19")
               + streamer
               + Messages.getString("MysqlIO.20")
               + Messages.getString("MysqlIO.21")
               + Messages.getString("MysqlIO.22"),
            this.exceptionInterceptor
         );
      } else {
         this.streamingData = null;
      }
   }

   public void scanForAndThrowDataTruncation() {
      if (this.streamingData == null && this.propertySet.getBooleanProperty("jdbcCompliantTruncation").getValue() && this.getWarningCount() > 0) {
         int warningCountOld = this.getWarningCount();
         this.convertShowWarningsToSQLWarnings(this.getWarningCount(), true);
         this.setWarningCount(warningCountOld);
      }
   }

   public StringBuilder generateQueryCommentBlock(StringBuilder buf) {
      buf.append("/* conn id ");
      buf.append(this.getServerSession().getCapabilities().getThreadId());
      buf.append(" clock: ");
      buf.append(System.currentTimeMillis());
      buf.append(" */ ");
      return buf;
   }

   public BaseMetricsHolder getMetricsHolder() {
      return this.metricsHolder;
   }

   @Override
   public String getQueryComment() {
      return this.queryComment;
   }

   @Override
   public void setQueryComment(String comment) {
      this.queryComment = comment;
   }

   private void appendDeadlockStatusInformation(Session sess, String xOpen, StringBuilder errorBuf) {
      if (sess.getPropertySet().getBooleanProperty("includeInnodbStatusInDeadlockExceptions").getValue()
         && xOpen != null
         && (xOpen.startsWith("40") || xOpen.startsWith("41"))
         && this.getStreamingData() == null) {
         try {
            NativePacketPayload resultPacket = this.sendCommand(
               this.commandBuilder.buildComQuery(this.getSharedSendPacket(), "SHOW ENGINE INNODB STATUS"), false, 0
            );
            Resultset rs = this.readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            int colIndex = 0;
            Field f = null;

            for(int i = 0; i < rs.getColumnDefinition().getFields().length; ++i) {
               f = rs.getColumnDefinition().getFields()[i];
               if ("Status".equals(f.getName())) {
                  colIndex = i;
                  break;
               }
            }

            ValueFactory<String> vf = new StringValueFactory(f.getEncoding());
            Row r;
            if ((r = rs.getRows().next()) != null) {
               errorBuf.append("\n\n").append(r.getValue(colIndex, vf));
            } else {
               errorBuf.append("\n\n").append(Messages.getString("MysqlIO.NoInnoDBStatusFound"));
            }
         } catch (CJException | IOException var13) {
            errorBuf.append("\n\n").append(Messages.getString("MysqlIO.InnoDBStatusFailed")).append("\n\n").append(Util.stackTraceToString(var13));
         }
      }

      if (sess.getPropertySet().getBooleanProperty("includeThreadDumpInDeadlockExceptions").getValue()) {
         errorBuf.append("\n\n*** Java threads running at time of deadlock ***\n\n");
         ThreadMXBean threadMBean = ManagementFactory.getThreadMXBean();
         long[] threadIds = threadMBean.getAllThreadIds();
         ThreadInfo[] threads = threadMBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
         List<ThreadInfo> activeThreads = new ArrayList<>();

         for(ThreadInfo info : threads) {
            if (info != null) {
               activeThreads.add(info);
            }
         }

         for(ThreadInfo threadInfo : activeThreads) {
            errorBuf.append('"')
               .append(threadInfo.getThreadName())
               .append("\" tid=")
               .append(threadInfo.getThreadId())
               .append(" ")
               .append(threadInfo.getThreadState());
            if (threadInfo.getLockName() != null) {
               errorBuf.append(" on lock=").append(threadInfo.getLockName());
            }

            if (threadInfo.isSuspended()) {
               errorBuf.append(" (suspended)");
            }

            if (threadInfo.isInNative()) {
               errorBuf.append(" (running in native)");
            }

            StackTraceElement[] stackTrace = threadInfo.getStackTrace();
            if (stackTrace.length > 0) {
               errorBuf.append(" in ");
               errorBuf.append(stackTrace[0].getClassName()).append(".");
               errorBuf.append(stackTrace[0].getMethodName()).append("()");
            }

            errorBuf.append("\n");
            if (threadInfo.getLockOwnerName() != null) {
               errorBuf.append("\t owned by ").append(threadInfo.getLockOwnerName()).append(" Id=").append(threadInfo.getLockOwnerId()).append("\n");
            }

            for(int j = 0; j < stackTrace.length; ++j) {
               StackTraceElement ste = stackTrace[j];
               errorBuf.append("\tat ").append(ste.toString()).append("\n");
            }
         }
      }
   }

   private StringBuilder appendResultSetSlashGStyle(StringBuilder appendTo, Resultset rs) {
      Field[] fields = rs.getColumnDefinition().getFields();
      int maxWidth = 0;

      for(int i = 0; i < fields.length; ++i) {
         if (fields[i].getColumnLabel().length() > maxWidth) {
            maxWidth = fields[i].getColumnLabel().length();
         }
      }

      int rowCount = 1;

      Row r;
      while((r = rs.getRows().next()) != null) {
         appendTo.append("*************************** ");
         appendTo.append(rowCount++);
         appendTo.append(". row ***************************\n");

         for(int i = 0; i < fields.length; ++i) {
            int leftPad = maxWidth - fields[i].getColumnLabel().length();

            for(int j = 0; j < leftPad; ++j) {
               appendTo.append(" ");
            }

            appendTo.append(fields[i].getColumnLabel()).append(": ");
            String stringVal = r.getValue(i, new StringValueFactory(fields[i].getEncoding()));
            appendTo.append(stringVal != null ? stringVal : "NULL").append("\n");
         }

         appendTo.append("\n");
      }

      return appendTo;
   }

   public SQLWarning convertShowWarningsToSQLWarnings(int warningCountIfKnown, boolean forTruncationOnly) {
      SQLWarning currentWarning = null;
      ResultsetRows rows = null;

      Object var21;
      try {
         NativePacketPayload resultPacket = this.sendCommand(this.commandBuilder.buildComQuery(this.getSharedSendPacket(), "SHOW WARNINGS"), false, 0);
         Resultset warnRs = this.readAllResults(
            -1, warningCountIfKnown > 99, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, Resultset.Concurrency.READ_ONLY)
         );
         int codeFieldIndex = warnRs.getColumnDefinition().findColumn("Code", false, 1) - 1;
         int messageFieldIndex = warnRs.getColumnDefinition().findColumn("Message", false, 1) - 1;
         String enc = warnRs.getColumnDefinition().getFields()[messageFieldIndex].getEncoding();
         ValueFactory<String> svf = new StringValueFactory(enc);
         ValueFactory<Integer> ivf = new IntegerValueFactory();
         rows = warnRs.getRows();

         Row r;
         while((r = rows.next()) != null) {
            int code = r.getValue(codeFieldIndex, ivf);
            if (forTruncationOnly) {
               if (code == 1265 || code == 1264) {
                  DataTruncation newTruncation = new MysqlDataTruncation(r.getValue(messageFieldIndex, svf), 0, false, false, 0, 0, code);
                  if (currentWarning == null) {
                     currentWarning = newTruncation;
                  } else {
                     currentWarning.setNextWarning(newTruncation);
                  }
               }
            } else {
               String message = r.getValue(messageFieldIndex, svf);
               SQLWarning newWarning = new SQLWarning(message, MysqlErrorNumbers.mysqlToSqlState(code), code);
               if (currentWarning == null) {
                  currentWarning = newWarning;
               } else {
                  currentWarning.setNextWarning(newWarning);
               }
            }
         }

         if (forTruncationOnly && currentWarning != null) {
            throw ExceptionFactory.createException(currentWarning.getMessage(), currentWarning);
         }

         var21 = currentWarning;
      } catch (IOException var19) {
         throw ExceptionFactory.createException(var19.getMessage(), var19);
      } finally {
         if (rows != null) {
            rows.close();
         }
      }

      return (SQLWarning)var21;
   }

   @Override
   public ColumnDefinition readMetadata() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public RowList getRowInputStream(ColumnDefinition metadata) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void close() throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setCurrentResultStreamer(ResultStreamer currentResultStreamer) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void configureTimezone() {
      String configuredTimeZoneOnServer = this.serverSession.getServerVariable("time_zone");
      if ("SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer)) {
         configuredTimeZoneOnServer = this.serverSession.getServerVariable("system_time_zone");
      }

      String canonicalTimezone = this.getPropertySet().getStringProperty("serverTimezone").getValue();
      if (configuredTimeZoneOnServer != null && (canonicalTimezone == null || StringUtils.isEmptyOrWhitespaceOnly(canonicalTimezone))) {
         try {
            canonicalTimezone = TimeUtil.getCanonicalTimezone(configuredTimeZoneOnServer, this.getExceptionInterceptor());
         } catch (IllegalArgumentException var4) {
            throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var4.getMessage(), this.getExceptionInterceptor());
         }
      }

      if (canonicalTimezone != null && canonicalTimezone.length() > 0) {
         this.serverSession.setServerTimeZone(TimeZone.getTimeZone(canonicalTimezone));
         if (!canonicalTimezone.equalsIgnoreCase("GMT") && this.serverSession.getServerTimeZone().getID().equals("GMT")) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("Connection.9", new Object[]{canonicalTimezone}), this.getExceptionInterceptor()
            );
         }
      }

      this.serverSession.setDefaultTimeZone(this.serverSession.getServerTimeZone());
   }

   @Override
   public void initServerSession() {
      this.configureTimezone();
      if (this.session.getServerSession().getServerVariables().containsKey("max_allowed_packet")) {
         int serverMaxAllowedPacket = this.session.getServerSession().getServerVariable("max_allowed_packet", -1);
         if (serverMaxAllowedPacket != -1 && (!this.maxAllowedPacket.isExplicitlySet() || serverMaxAllowedPacket < this.maxAllowedPacket.getValue())) {
            this.maxAllowedPacket.setValue(serverMaxAllowedPacket);
         }

         if (this.useServerPrepStmts.getValue()) {
            RuntimeProperty<Integer> blobSendChunkSize = this.propertySet.getProperty("blobSendChunkSize");
            int preferredBlobSendChunkSize = blobSendChunkSize.getValue();
            int packetHeaderSize = 8203;
            int allowedBlobSendChunkSize = Math.min(preferredBlobSendChunkSize, this.maxAllowedPacket.getValue()) - packetHeaderSize;
            if (allowedBlobSendChunkSize <= 0) {
               throw ExceptionFactory.createException(
                  Messages.getString("Connection.15", new Object[]{packetHeaderSize}), "01S00", 0, false, null, this.exceptionInterceptor
               );
            }

            blobSendChunkSize.setValue(allowedBlobSendChunkSize);
         }
      }
   }

   static {
      OutputStreamWriter outWriter = null;

      try {
         outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
         jvmPlatformCharset = outWriter.getEncoding();
      } finally {
         try {
            if (outWriter != null) {
               outWriter.close();
            }
         } catch (IOException var7) {
         }
      }
   }
}
