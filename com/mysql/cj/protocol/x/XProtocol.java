package com.mysql.cj.protocol.x;

import com.mysql.cj.CharsetMapping;
import com.mysql.cj.QueryResult;
import com.mysql.cj.Session;
import com.mysql.cj.TransactionEventHandler;
import com.mysql.cj.conf.AbstractRuntimeProperty;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.AbstractProtocol;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ExportControlled;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.protocol.MessageSender;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ResultListener;
import com.mysql.cj.protocol.ResultStreamer;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerCapabilities;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.SocketConnection;
import com.mysql.cj.protocol.a.NativeSocketConnection;
import com.mysql.cj.result.DefaultColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.LongValueFactory;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.x.protobuf.MysqlxConnection;
import com.mysql.cj.x.protobuf.MysqlxResultset;
import com.mysql.cj.x.protobuf.MysqlxSession;
import com.mysql.cj.xdevapi.FilterParams;
import com.mysql.cj.xdevapi.SqlResult;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class XProtocol extends AbstractProtocol<XMessage> implements Protocol<XMessage> {
   private MessageReader<XMessageHeader, XMessage> reader;
   private MessageSender<XMessage> sender;
   private Closeable managedResource;
   private ProtocolEntityFactory<Field, XMessage> fieldFactory;
   private ProtocolEntityFactory<Notice, XMessage> noticeFactory;
   private String metadataCharacterSet;
   private ResultStreamer currentResultStreamer;
   XServerSession serverSession = null;
   public static Map<String, Integer> COLLATION_NAME_TO_COLLATION_INDEX = new HashMap<>();

   public static XProtocol getInstance(String host, int port, PropertySet propertySet) {
      SocketConnection socketConnection = (SocketConnection)(propertySet.getBooleanProperty("xdevapi.useAsyncProtocol").getValue()
         ? new XAsyncSocketConnection()
         : new NativeSocketConnection());
      socketConnection.connect(host, port, propertySet, null, null, 0);
      XProtocol protocol = new XProtocol();
      protocol.init(null, socketConnection, propertySet, null);
      return protocol;
   }

   @Override
   public void init(Session sess, SocketConnection socketConn, PropertySet propSet, TransactionEventHandler transactionManager) {
      this.socketConnection = socketConn;
      this.propertySet = propSet;
      this.messageBuilder = new XMessageBuilder();
      this.authProvider = new XAuthenticationProvider();
      this.authProvider.init(this, propSet, null);
      this.metadataCharacterSet = "latin1";
      this.fieldFactory = new FieldFactory(this.metadataCharacterSet);
      this.noticeFactory = new NoticeFactory();
   }

   @Override
   public ServerSession getServerSession() {
      return this.serverSession;
   }

   public void setCapability(String name, Object value) {
      ((XServerCapabilities)this.getServerSession().getCapabilities()).setCapability(name, value);
      this.sender.send(((XMessageBuilder)this.messageBuilder).buildCapabilitiesSet(name, value));
      this.readOk();
   }

   @Override
   public void negotiateSSLConnection(int packLength) {
      if (!ExportControlled.enabled()) {
         throw new CJConnectionFeatureNotAvailableException();
      } else if (!((XServerCapabilities)this.serverSession.getCapabilities()).hasCapability("tls")) {
         throw new CJCommunicationsException("A secure connection is required but the server is not configured with SSL.");
      } else {
         this.reader.stopAfterNextMessage();
         this.setCapability("tls", true);

         try {
            this.socketConnection.performTlsHandshake(null);
         } catch (FeatureNotAvailableException | IOException | SSLParamsException var3) {
            throw new CJCommunicationsException(var3);
         }

         if (this.socketConnection.isSynchronous()) {
            this.sender = new SyncMessageSender(this.socketConnection.getMysqlOutput());
            this.reader = new SyncMessageReader(this.socketConnection.getMysqlInput());
         } else {
            ((AsyncMessageSender)this.sender).setChannel(this.socketConnection.getAsynchronousSocketChannel());
            this.reader.start();
         }
      }
   }

   @Override
   public void beforeHandshake() {
      this.serverSession = new XServerSession();
      if (this.socketConnection.isSynchronous()) {
         this.sender = new SyncMessageSender(this.socketConnection.getMysqlOutput());
         this.reader = new SyncMessageReader(this.socketConnection.getMysqlInput());
         this.managedResource = this.socketConnection.getMysqlSocket();
      } else {
         this.sender = new AsyncMessageSender(this.socketConnection.getAsynchronousSocketChannel());
         this.reader = new AsyncMessageReader(this.propertySet, this.socketConnection);
         this.reader.start();
         this.managedResource = this.socketConnection.getAsynchronousSocketChannel();
      }

      this.serverSession.setCapabilities(this.readServerCapabilities());
      PropertyDefinitions.SslMode sslMode = this.propertySet.<PropertyDefinitions.SslMode>getEnumProperty("xdevapi.ssl-mode").getValue();
      boolean verifyServerCert = sslMode == PropertyDefinitions.SslMode.VERIFY_CA || sslMode == PropertyDefinitions.SslMode.VERIFY_IDENTITY;
      String trustStoreUrl = this.propertySet.getStringProperty("xdevapi.ssl-truststore").getValue();
      if (!verifyServerCert && !StringUtils.isNullOrEmpty(trustStoreUrl)) {
         StringBuilder msg = new StringBuilder("Incompatible security settings. The property '");
         msg.append("xdevapi.ssl-truststore").append("' requires '");
         msg.append("xdevapi.ssl-mode").append("' as '");
         msg.append(PropertyDefinitions.SslMode.VERIFY_CA).append("' or '");
         msg.append(PropertyDefinitions.SslMode.VERIFY_IDENTITY).append("'.");
         throw new CJCommunicationsException(msg.toString());
      } else {
         if (sslMode != PropertyDefinitions.SslMode.DISABLED) {
            if (this.socketConnection.isSynchronous()) {
               this.propertySet.getBooleanProperty("useSSL").setValue(true);
               this.propertySet.getBooleanProperty("verifyServerCertificate").setValue(sslMode != PropertyDefinitions.SslMode.REQUIRED);
               ((AbstractRuntimeProperty)this.propertySet.getStringProperty("trustCertificateKeyStoreUrl")).setValueInternal(trustStoreUrl, null);
               ((AbstractRuntimeProperty)this.propertySet.getStringProperty("trustCertificateKeyStorePassword"))
                  .setValueInternal(this.propertySet.getStringProperty("xdevapi.ssl-truststore-password").getValue(), null);
               ((AbstractRuntimeProperty)this.propertySet.getStringProperty("trustCertificateKeyStoreType"))
                  .setValueInternal(this.propertySet.getStringProperty("xdevapi.ssl-truststore-type").getValue(), null);
            }

            this.negotiateSSLConnection(0);
         }
      }
   }

   @Override
   public void connect(String user, String password, String database) {
      this.beforeHandshake();
      this.authProvider.connect(null, user, password, database);
   }

   @Override
   public void changeUser(String user, String password, String database) {
      this.authProvider.changeUser(null, user, password, database);
   }

   @Override
   public void afterHandshake() {
      this.initServerSession();
   }

   @Override
   public void configureTimezone() {
   }

   @Override
   public void initServerSession() {
      this.configureTimezone();
      this.send(this.messageBuilder.buildSqlStatement("select @@mysqlx_max_allowed_packet"), 0);
      ColumnDefinition metadata = this.readMetadata();
      long count = this.getRowInputStream(metadata).next().getValue(0, new LongValueFactory());
      this.readQueryResult();
      this.setMaxAllowedPacket((int)count);
   }

   public void readOk() {
      try {
         XMessageHeader header;
         while((header = this.reader.readHeader()).getMessageType() == 11) {
            this.reader.readMessage(null, header);
         }

         this.reader.readMessage(null, 0);
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   public void readAuthenticateOk() {
      try {
         XMessageHeader header;
         while((header = this.reader.readHeader()).getMessageType() == 11) {
            Notice notice = this.noticeFactory.createFromMessage(this.reader.readMessage(null, header));
            if (notice.getType() != 3) {
               throw new WrongArgumentException("Unknown notice received during authentication: " + notice);
            }

            switch(notice.getParamType()) {
               case 2:
               default:
                  throw new WrongArgumentException("Unknown SessionStateChanged notice received during authentication: " + notice.getParamType());
               case 11:
                  this.getServerSession().setThreadId(notice.getValue().getVUnsignedInt());
            }
         }

         this.reader.readMessage(null, 4);
      } catch (IOException var3) {
         throw new XProtocolError(var3.getMessage(), var3);
      }
   }

   public byte[] readAuthenticateContinue() {
      try {
         MysqlxSession.AuthenticateContinue msg = (MysqlxSession.AuthenticateContinue)this.reader.readMessage(null, 3).getMessage();
         byte[] data = msg.getAuthData().toByteArray();
         if (data.length != 20) {
            throw AssertionFailedException.shouldNotHappen("Salt length should be 20, but is " + data.length);
         } else {
            return data;
         }
      } catch (IOException var3) {
         throw new XProtocolError(var3.getMessage(), var3);
      }
   }

   public boolean hasMoreResults() {
      try {
         XMessageHeader header;
         if ((header = this.reader.readHeader()).getMessageType() == 16) {
            this.reader.readMessage(null, header);
            return this.reader.readHeader().getMessageType() != 14;
         } else {
            return false;
         }
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   @Override
   public <QR extends QueryResult> QR readQueryResult() {
      try {
         StatementExecuteOkBuilder builder = new StatementExecuteOkBuilder();
         XMessageHeader header;
         if ((header = this.reader.readHeader()).getMessageType() == 14) {
            this.reader.readMessage(null, header);
         }

         while((header = this.reader.readHeader()).getMessageType() == 11) {
            builder.addNotice(this.noticeFactory.createFromMessage(this.reader.readMessage(null, header)));
         }

         this.reader.readMessage(null, 17);
         return (QR)builder.build();
      } catch (IOException var3) {
         throw new XProtocolError(var3.getMessage(), var3);
      }
   }

   public boolean hasResults() {
      try {
         return this.reader.readHeader().getMessageType() == 12;
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   public void drainRows() {
      try {
         XMessageHeader header;
         while((header = this.reader.readHeader()).getMessageType() == 13) {
            this.reader.readMessage(null, header);
         }
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   @Override
   public ColumnDefinition readMetadata() {
      try {
         XMessageHeader header;
         while((header = this.reader.readHeader()).getMessageType() == 11) {
            this.reader.readMessage(null, header);
         }

         List<MysqlxResultset.ColumnMetaData> fromServer = new LinkedList<>();

         do {
            fromServer.add((MysqlxResultset.ColumnMetaData)this.reader.readMessage(null, 12).getMessage());
         } while(this.reader.readHeader().getMessageType() == 12);

         ArrayList<Field> metadata = new ArrayList<>(fromServer.size());
         fromServer.forEach(col -> metadata.add(this.fieldFactory.createFromMessage(new XMessage(col))));
         return new DefaultColumnDefinition(metadata.toArray(new Field[0]));
      } catch (IOException var4) {
         throw new XProtocolError(var4.getMessage(), var4);
      }
   }

   public XProtocolRow readRowOrNull(ColumnDefinition metadata) {
      try {
         XMessageHeader header;
         if ((header = this.reader.readHeader()).getMessageType() == 13) {
            MysqlxResultset.Row r = (MysqlxResultset.Row)this.reader.readMessage(null, header).getMessage();
            return new XProtocolRow(metadata, r);
         } else {
            return null;
         }
      } catch (IOException var4) {
         throw new XProtocolError(var4.getMessage(), var4);
      }
   }

   public XProtocolRowInputStream getRowInputStream(ColumnDefinition metadata) {
      return new XProtocolRowInputStream(metadata, this);
   }

   protected void newCommand() {
      if (this.currentResultStreamer != null) {
         try {
            this.currentResultStreamer.finishStreaming();
         } finally {
            this.currentResultStreamer = null;
         }
      }
   }

   @Override
   public void setCurrentResultStreamer(ResultStreamer currentResultStreamer) {
      this.currentResultStreamer = currentResultStreamer;
   }

   public CompletableFuture<SqlResult> asyncExecuteSql(String sql, List<Object> args) {
      this.newCommand();
      CompletableFuture<SqlResult> f = new CompletableFuture<>();
      MessageListener<XMessage> l = new SqlResultMessageListener(f, this.fieldFactory, this.noticeFactory, this.serverSession.getDefaultTimeZone());
      CompletionHandler<Long, Void> resultHandler = new ErrorToFutureCompletionHandler<>(f, () -> this.reader.pushMessageListener(l));
      this.sender.send(this.messageBuilder.buildSqlStatement(sql, args), resultHandler);
      return f;
   }

   public void asyncFind(FilterParams filterParams, ResultListener<StatementExecuteOk> callbacks, CompletableFuture<?> errorFuture) {
      this.newCommand();
      MessageListener<XMessage> l = new ResultMessageListener(this.fieldFactory, this.noticeFactory, callbacks);
      CompletionHandler<Long, Void> resultHandler = new ErrorToFutureCompletionHandler<>(errorFuture, () -> this.reader.pushMessageListener(l));
      this.sender.send(((XMessageBuilder)this.messageBuilder).buildFind(filterParams), resultHandler);
   }

   public boolean isOpen() {
      return this.managedResource != null;
   }

   @Override
   public void close() throws IOException {
      if (this.managedResource == null) {
         throw new ConnectionIsClosedException();
      } else {
         this.managedResource.close();
         this.managedResource = null;
      }
   }

   public boolean isSqlResultPending() {
      try {
         XMessageHeader header;
         switch((header = this.reader.readHeader()).getMessageType()) {
            case 12:
               return true;
            case 16:
               this.reader.readMessage(null, header);
            default:
               return false;
         }
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   public void setMaxAllowedPacket(int maxAllowedPacket) {
      this.sender.setMaxAllowedPacket(maxAllowedPacket);
   }

   @Override
   public void send(Message message, int packetLen) {
      this.newCommand();
      this.sender.send((XMessage)message);
   }

   @Override
   public <RES extends QueryResult> CompletableFuture<RES> sendAsync(Message message) {
      this.newCommand();
      CompletableFuture<StatementExecuteOk> f = new CompletableFuture<>();
      StatementExecuteOkMessageListener l = new StatementExecuteOkMessageListener(f, this.noticeFactory);
      CompletionHandler<Long, Void> resultHandler = new ErrorToFutureCompletionHandler<>(f, () -> this.reader.pushMessageListener(l));
      this.sender.send((XMessage)message, resultHandler);
      return f;
   }

   @Override
   public ServerCapabilities readServerCapabilities() {
      try {
         this.sender.send(((XMessageBuilder)this.messageBuilder).buildCapabilitiesGet());
         return new XServerCapabilities(
            ((MysqlxConnection.Capabilities)this.reader.readMessage(null, 2).getMessage())
               .getCapabilitiesList()
               .stream()
               .collect(Collectors.toMap(MysqlxConnection.Capability::getName, MysqlxConnection.Capability::getValue))
         );
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void changeDatabase(String database) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getPasswordCharacterEncoding() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean versionMeetsMinimum(int major, int minor, int subminor) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public XMessage readMessage(XMessage reuse) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public XMessage checkErrorMessage() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public XMessage sendCommand(Message queryPacket, boolean skipCheck, int timeoutMillis) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public <T extends ProtocolEntity> T read(Class<T> requiredClass, ProtocolEntityFactory<T, XMessage> protocolEntityFactory) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public <T extends ProtocolEntity> T read(
      Class<Resultset> requiredClass,
      int maxRows,
      boolean streamResults,
      XMessage resultPacket,
      boolean isBinaryEncoded,
      ColumnDefinition metadata,
      ProtocolEntityFactory<T, XMessage> protocolEntityFactory
   ) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setLocalInfileInputStream(InputStream stream) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getQueryComment() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setQueryComment(String comment) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   static {
      for(int i = 0; i < CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME.length; ++i) {
         COLLATION_NAME_TO_COLLATION_INDEX.put(CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME[i], i);
      }
   }
}
