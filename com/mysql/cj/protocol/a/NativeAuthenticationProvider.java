package com.mysql.cj.protocol.a;

import com.mysql.cj.Constants;
import com.mysql.cj.Messages;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.UnableToConnectException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.AuthenticationProvider;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.a.authentication.CachingSha2PasswordPlugin;
import com.mysql.cj.protocol.a.authentication.MysqlClearPasswordPlugin;
import com.mysql.cj.protocol.a.authentication.MysqlNativePasswordPlugin;
import com.mysql.cj.protocol.a.authentication.MysqlOldPasswordPlugin;
import com.mysql.cj.protocol.a.authentication.Sha256PasswordPlugin;
import com.mysql.cj.protocol.a.result.OkPacket;
import com.mysql.cj.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class NativeAuthenticationProvider implements AuthenticationProvider<NativePacketPayload> {
   protected static final int AUTH_411_OVERHEAD = 33;
   private static final String NONE = "none";
   protected String seed;
   private boolean useConnectWithDb;
   private ExceptionInterceptor exceptionInterceptor;
   private PropertySet propertySet;
   private Protocol<NativePacketPayload> protocol;
   private Log log;
   private Map<String, AuthenticationPlugin<NativePacketPayload>> authenticationPlugins = null;
   private List<String> disabledAuthenticationPlugins = null;
   private String clientDefaultAuthenticationPlugin = null;
   private String clientDefaultAuthenticationPluginName = null;
   private String serverDefaultAuthenticationPluginName = null;

   public NativeAuthenticationProvider(Log log) {
      this.log = log;
   }

   @Override
   public void init(Protocol<NativePacketPayload> prot, PropertySet propSet, ExceptionInterceptor excInterceptor) {
      this.protocol = prot;
      this.propertySet = propSet;
      this.exceptionInterceptor = excInterceptor;
   }

   @Override
   public void connect(ServerSession sessState, String user, String password, String database) {
      long clientParam = sessState.getClientParam();
      NativeCapabilities capabilities = (NativeCapabilities)sessState.getCapabilities();
      NativePacketPayload buf = capabilities.getInitialHandshakePacket();
      this.seed = capabilities.getSeed();
      sessState.setServerDefaultCollationIndex(capabilities.getServerDefaultCollationIndex());
      sessState.setStatusFlags(capabilities.getStatusFlags());
      int capabilityFlags = capabilities.getCapabilityFlags();
      if ((capabilityFlags & 32768) != 0) {
         clientParam |= 32768L;
         int authPluginDataLength = capabilities.getAuthPluginDataLength();
         String seedPart2;
         StringBuilder newSeed;
         if (authPluginDataLength > 0) {
            seedPart2 = buf.readString(NativeConstants.StringLengthDataType.STRING_FIXED, "ASCII", authPluginDataLength - 8);
            newSeed = new StringBuilder(authPluginDataLength);
         } else {
            seedPart2 = buf.readString(NativeConstants.StringSelfDataType.STRING_TERM, "ASCII");
            newSeed = new StringBuilder(20);
         }

         newSeed.append(this.seed);
         newSeed.append(seedPart2);
         this.seed = newSeed.toString();
         if ((capabilityFlags & 32) != 0 && this.propertySet.getBooleanProperty("useCompression").getValue()) {
            clientParam |= 32L;
         }

         this.useConnectWithDb = database != null && database.length() > 0 && !this.propertySet.getBooleanProperty("createDatabaseIfNotExist").getValue();
         if (this.useConnectWithDb) {
            clientParam |= 8L;
         }

         RuntimeProperty<Boolean> useSSL = this.propertySet.getProperty("useSSL");
         if (this.protocol.versionMeetsMinimum(5, 7, 0) && !useSSL.getValue() && !useSSL.isExplicitlySet()) {
            useSSL.setValue(true);
            this.propertySet.<Boolean>getProperty("verifyServerCertificate").setValue(false);
            if (this.log != null) {
               this.log.logWarn(Messages.getString("MysqlIO.SSLWarning"));
            }
         }

         RuntimeProperty<Boolean> useInformationSchema = this.propertySet.getProperty("useInformationSchema");
         if (this.protocol.versionMeetsMinimum(8, 0, 3) && !useInformationSchema.getValue() && !useInformationSchema.isExplicitlySet()) {
            useInformationSchema.setValue(true);
         }

         if ((capabilityFlags & 2048) == 0 && useSSL.getValue()) {
            if (this.propertySet.getBooleanProperty("requireSSL").getValue()) {
               throw (UnableToConnectException)ExceptionFactory.createException(
                  UnableToConnectException.class, Messages.getString("MysqlIO.15"), this.getExceptionInterceptor()
               );
            }

            useSSL.setValue(false);
         }

         if ((capabilityFlags & 4) != 0) {
            clientParam |= 4L;
            sessState.setHasLongColumnInfo(true);
         }

         if (!this.propertySet.getBooleanProperty("useAffectedRows").getValue()) {
            clientParam |= 2L;
         }

         if (this.propertySet.getBooleanProperty("allowLoadLocalInfile").getValue()) {
            clientParam |= 128L;
         }

         if (this.propertySet.getBooleanProperty("interactiveClient").getValue()) {
            clientParam |= 1024L;
         }

         if ((capabilityFlags & 8388608) != 0) {
         }

         if ((capabilityFlags & 16777216) != 0) {
            clientParam |= 16777216L;
         }

         if ((capabilityFlags & 524288) != 0) {
            sessState.setClientParam(clientParam);
            this.proceedHandshakeWithPluggableAuthentication(sessState, user, password, database, buf);
         } else {
            throw (UnableToConnectException)ExceptionFactory.createException(
               UnableToConnectException.class, "CLIENT_PLUGIN_AUTH is required", this.getExceptionInterceptor()
            );
         }
      } else {
         throw (UnableToConnectException)ExceptionFactory.createException(
            UnableToConnectException.class, "CLIENT_SECURE_CONNECTION is required", this.getExceptionInterceptor()
         );
      }
   }

   private void loadAuthenticationPlugins() {
      this.clientDefaultAuthenticationPlugin = this.propertySet.getStringProperty("defaultAuthenticationPlugin").getValue();
      if (this.clientDefaultAuthenticationPlugin != null && !"".equals(this.clientDefaultAuthenticationPlugin.trim())) {
         String disabledPlugins = this.propertySet.getStringProperty("disabledAuthenticationPlugins").getValue();
         if (disabledPlugins != null && !"".equals(disabledPlugins)) {
            this.disabledAuthenticationPlugins = new ArrayList<>();
            List<String> pluginsToDisable = StringUtils.split(disabledPlugins, ",", true);
            Iterator<String> iter = pluginsToDisable.iterator();

            while(iter.hasNext()) {
               this.disabledAuthenticationPlugins.add(iter.next());
            }
         }

         this.authenticationPlugins = new HashMap<>();
         boolean defaultIsFound = false;
         List<AuthenticationPlugin<NativePacketPayload>> pluginsToInit = new LinkedList<>();
         pluginsToInit.add(new MysqlNativePasswordPlugin());
         pluginsToInit.add(new MysqlClearPasswordPlugin());
         pluginsToInit.add(new Sha256PasswordPlugin());
         pluginsToInit.add(new CachingSha2PasswordPlugin());
         pluginsToInit.add(new MysqlOldPasswordPlugin());
         String authenticationPluginClasses = this.propertySet.getStringProperty("authenticationPlugins").getValue();
         if (authenticationPluginClasses != null && !"".equals(authenticationPluginClasses)) {
            List<String> pluginsToCreate = StringUtils.split(authenticationPluginClasses, ",", true);
            String className = null;

            try {
               int i = 0;

               for(int s = pluginsToCreate.size(); i < s; ++i) {
                  className = pluginsToCreate.get(i);
                  pluginsToInit.add((AuthenticationPlugin<NativePacketPayload>)Class.forName(className).newInstance());
               }
            } catch (Throwable var9) {
               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("AuthenticationProvider.BadAuthenticationPlugin", new Object[]{className}),
                  var9,
                  this.exceptionInterceptor
               );
            }
         }

         for(AuthenticationPlugin<NativePacketPayload> plugin : pluginsToInit) {
            plugin.init(this.protocol);
            if (this.addAuthenticationPlugin(plugin)) {
               defaultIsFound = true;
            }
         }

         if (!defaultIsFound) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("AuthenticationProvider.DefaultAuthenticationPluginIsNotListed", new Object[]{this.clientDefaultAuthenticationPlugin}),
               this.getExceptionInterceptor()
            );
         }
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            Messages.getString("AuthenticationProvider.BadDefaultAuthenticationPlugin", new Object[]{this.clientDefaultAuthenticationPlugin}),
            this.getExceptionInterceptor()
         );
      }
   }

   private boolean addAuthenticationPlugin(AuthenticationPlugin<NativePacketPayload> plugin) {
      boolean isDefault = false;
      String pluginClassName = plugin.getClass().getName();
      String pluginProtocolName = plugin.getProtocolPluginName();
      boolean disabledByClassName = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginClassName);
      boolean disabledByMechanism = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginProtocolName);
      if (!disabledByClassName && !disabledByMechanism) {
         this.authenticationPlugins.put(pluginProtocolName, plugin);
         if (this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
            this.clientDefaultAuthenticationPluginName = pluginProtocolName;
            isDefault = true;
         }
      } else if (this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            Messages.getString(
               "AuthenticationProvider.BadDisabledAuthenticationPlugin", new Object[]{disabledByClassName ? pluginClassName : pluginProtocolName}
            ),
            this.getExceptionInterceptor()
         );
      }

      return isDefault;
   }

   private AuthenticationPlugin<NativePacketPayload> getAuthenticationPlugin(String pluginName) {
      AuthenticationPlugin<NativePacketPayload> plugin = this.authenticationPlugins.get(pluginName);
      if (plugin != null && !plugin.isReusable()) {
         try {
            plugin = (AuthenticationPlugin)plugin.getClass().newInstance();
            plugin.init(this.protocol);
         } catch (Throwable var4) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("AuthenticationProvider.BadAuthenticationPlugin", new Object[]{plugin.getClass().getName()}),
               var4,
               this.getExceptionInterceptor()
            );
         }
      }

      return plugin;
   }

   private void checkConfidentiality(AuthenticationPlugin<?> plugin) {
      if (plugin.requiresConfidentiality() && !this.protocol.getSocketConnection().isSSLEstablished()) {
         throw ExceptionFactory.createException(
            Messages.getString("AuthenticationProvider.AuthenticationPluginRequiresSSL", new Object[]{plugin.getProtocolPluginName()}),
            this.getExceptionInterceptor()
         );
      }
   }

   private void proceedHandshakeWithPluggableAuthentication(
      ServerSession sessState, String user, String password, String database, NativePacketPayload challenge
   ) {
      if (this.authenticationPlugins == null) {
         this.loadAuthenticationPlugins();
      }

      boolean skipPassword = false;
      int passwordLength = 16;
      int userLength = user != null ? user.length() : 0;
      int databaseLength = database != null ? database.length() : 0;
      int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 33;
      long clientParam = sessState.getClientParam();
      int serverCapabilities = sessState.getCapabilities().getCapabilityFlags();
      AuthenticationPlugin<NativePacketPayload> plugin = null;
      NativePacketPayload fromServer = null;
      ArrayList<NativePacketPayload> toServer = new ArrayList<>();
      boolean done = false;
      NativePacketPayload last_sent = null;
      boolean old_raw_challenge = false;
      int counter = 100;

      while(0 < counter--) {
         if (!done) {
            if (challenge != null) {
               if (challenge.isOKPacket()) {
                  throw ExceptionFactory.createException(
                     Messages.getString("AuthenticationProvider.UnexpectedAuthenticationApproval", new Object[]{plugin.getProtocolPluginName()}),
                     this.getExceptionInterceptor()
                  );
               }

               clientParam |= 958977L;
               if (this.propertySet.getBooleanProperty("allowMultiQueries").getValue()) {
                  clientParam |= 65536L;
               }

               if ((serverCapabilities & 4194304) != 0 && !this.propertySet.getBooleanProperty("disconnectOnExpiredPasswords").getValue()) {
                  clientParam |= 4194304L;
               }

               if ((serverCapabilities & 1048576) != 0 && !"none".equals(this.propertySet.getStringProperty("connectionAttributes").getValue())) {
                  clientParam |= 1048576L;
               }

               if ((serverCapabilities & 2097152) != 0) {
                  clientParam |= 2097152L;
               }

               sessState.setClientParam(clientParam);
               if (this.propertySet.getBooleanProperty("useSSL").getValue()) {
                  this.negotiateSSLConnection(packLength);
               }

               String pluginName = null;
               if ((serverCapabilities & 524288) != 0) {
                  if (this.protocol.versionMeetsMinimum(5, 5, 10)
                     && (!this.protocol.versionMeetsMinimum(5, 6, 0) || this.protocol.versionMeetsMinimum(5, 6, 2))) {
                     pluginName = challenge.readString(NativeConstants.StringSelfDataType.STRING_TERM, "ASCII");
                  } else {
                     pluginName = challenge.readString(
                        NativeConstants.StringLengthDataType.STRING_FIXED,
                        "ASCII",
                        ((NativeCapabilities)sessState.getCapabilities()).getAuthPluginDataLength()
                     );
                  }
               }

               plugin = this.getAuthenticationPlugin(pluginName);
               if (plugin == null) {
                  plugin = this.getAuthenticationPlugin(this.clientDefaultAuthenticationPluginName);
               } else if (pluginName.equals(Sha256PasswordPlugin.PLUGIN_NAME)
                  && !this.protocol.getSocketConnection().isSSLEstablished()
                  && this.propertySet.getStringProperty("serverRSAPublicKeyFile").getValue() == null
                  && !this.propertySet.getBooleanProperty("allowPublicKeyRetrieval").getValue()) {
                  plugin = this.getAuthenticationPlugin(this.clientDefaultAuthenticationPluginName);
                  skipPassword = !this.clientDefaultAuthenticationPluginName.equals(pluginName);
               }

               this.serverDefaultAuthenticationPluginName = plugin.getProtocolPluginName();
               this.checkConfidentiality(plugin);
               fromServer = new NativePacketPayload(StringUtils.getBytes(this.seed));
            } else {
               plugin = this.getAuthenticationPlugin(
                  this.serverDefaultAuthenticationPluginName == null ? this.clientDefaultAuthenticationPluginName : this.serverDefaultAuthenticationPluginName
               );
               this.checkConfidentiality(plugin);
               fromServer = new NativePacketPayload(StringUtils.getBytes(this.seed));
            }
         } else {
            challenge = this.protocol.checkErrorMessage();
            old_raw_challenge = false;
            if (plugin == null) {
               plugin = this.getAuthenticationPlugin(
                  this.serverDefaultAuthenticationPluginName == null ? this.clientDefaultAuthenticationPluginName : this.serverDefaultAuthenticationPluginName
               );
            }

            if (challenge.isOKPacket()) {
               OkPacket ok = OkPacket.parse(challenge, null);
               sessState.setStatusFlags(ok.getStatusFlags(), true);
               plugin.destroy();
               break;
            }

            if (challenge.isAuthMethodSwitchRequestPacket()) {
               skipPassword = false;
               String pluginName = challenge.readString(NativeConstants.StringSelfDataType.STRING_TERM, "ASCII");
               if (!plugin.getProtocolPluginName().equals(pluginName)) {
                  plugin.destroy();
                  plugin = this.getAuthenticationPlugin(pluginName);
                  if (plugin == null) {
                     throw (WrongArgumentException)ExceptionFactory.createException(
                        WrongArgumentException.class,
                        Messages.getString("AuthenticationProvider.BadAuthenticationPlugin", new Object[]{pluginName}),
                        this.getExceptionInterceptor()
                     );
                  }
               } else {
                  plugin.reset();
               }

               this.checkConfidentiality(plugin);
               fromServer = new NativePacketPayload(StringUtils.getBytes(challenge.readString(NativeConstants.StringSelfDataType.STRING_TERM, "ASCII")));
            } else {
               if (!this.protocol.versionMeetsMinimum(5, 5, 16)) {
                  old_raw_challenge = true;
                  challenge.setPosition(challenge.getPosition() - 1);
               }

               fromServer = new NativePacketPayload(challenge.readBytes(NativeConstants.StringSelfDataType.STRING_EOF));
            }
         }

         plugin.setAuthenticationParameters(user, skipPassword ? null : password);
         done = plugin.nextAuthenticationStep(fromServer, toServer);
         if (toServer.size() > 0) {
            if (challenge == null) {
               String enc = this.getEncodingForHandshake();
               last_sent = new NativePacketPayload(packLength + 1);
               last_sent.writeInteger(NativeConstants.IntegerDataType.INT1, 17L);
               last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_TERM, StringUtils.getBytes(user, enc));
               if (toServer.get(0).getPayloadLength() < 256) {
                  last_sent.writeInteger(NativeConstants.IntegerDataType.INT1, (long)toServer.get(0).getPayloadLength());
                  last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_EOF, toServer.get(0).getByteBuffer());
               } else {
                  last_sent.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
               }

               if (this.useConnectWithDb) {
                  last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_TERM, StringUtils.getBytes(database, enc));
               } else {
                  last_sent.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
               }

               last_sent.writeInteger(
                  NativeConstants.IntegerDataType.INT1,
                  (long)AuthenticationProvider.getCharsetForHandshake(enc, sessState.getCapabilities().getServerVersion())
               );
               last_sent.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
               if ((serverCapabilities & 524288) != 0) {
                  last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_TERM, StringUtils.getBytes(plugin.getProtocolPluginName(), enc));
               }

               if ((clientParam & 1048576L) != 0L) {
                  this.appendConnectionAttributes(last_sent, this.propertySet.getStringProperty("connectionAttributes").getValue(), enc);
               }

               this.protocol.send(last_sent, last_sent.getPosition());
            } else if (challenge.isAuthMethodSwitchRequestPacket()) {
               this.protocol.send(toServer.get(0), toServer.get(0).getPayloadLength());
            } else if (!challenge.isAuthMoreData() && !old_raw_challenge) {
               String enc = this.getEncodingForHandshake();
               last_sent = new NativePacketPayload(packLength);
               last_sent.writeInteger(NativeConstants.IntegerDataType.INT4, clientParam);
               last_sent.writeInteger(NativeConstants.IntegerDataType.INT4, 16777215L);
               last_sent.writeInteger(
                  NativeConstants.IntegerDataType.INT1,
                  (long)AuthenticationProvider.getCharsetForHandshake(enc, sessState.getCapabilities().getServerVersion())
               );
               last_sent.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, new byte[23]);
               last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_TERM, StringUtils.getBytes(user, enc));
               if ((serverCapabilities & 2097152) != 0) {
                  last_sent.writeBytes(
                     NativeConstants.StringSelfDataType.STRING_LENENC, toServer.get(0).readBytes(NativeConstants.StringSelfDataType.STRING_EOF)
                  );
               } else {
                  last_sent.writeInteger(NativeConstants.IntegerDataType.INT1, (long)toServer.get(0).getPayloadLength());
                  last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_EOF, toServer.get(0).getByteBuffer());
               }

               if (this.useConnectWithDb) {
                  last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_TERM, StringUtils.getBytes(database, enc));
               }

               if ((serverCapabilities & 524288) != 0) {
                  last_sent.writeBytes(NativeConstants.StringSelfDataType.STRING_TERM, StringUtils.getBytes(plugin.getProtocolPluginName(), enc));
               }

               if ((clientParam & 1048576L) != 0L) {
                  this.appendConnectionAttributes(last_sent, this.propertySet.getStringProperty("connectionAttributes").getValue(), enc);
               }

               this.protocol.send(last_sent, last_sent.getPosition());
            } else {
               for(NativePacketPayload buffer : toServer) {
                  this.protocol.send(buffer, buffer.getPayloadLength());
               }
            }
         }
      }

      if (counter == 0) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            Messages.getString("CommunicationsException.TooManyAuthenticationPluginNegotiations"),
            this.getExceptionInterceptor()
         );
      } else {
         this.protocol.afterHandshake();
         if (!this.useConnectWithDb) {
            this.protocol.changeDatabase(database);
         }
      }
   }

   private Properties getConnectionAttributesAsProperties(String atts) {
      Properties props = new Properties();
      if (atts != null) {
         String[] pairs = atts.split(",");

         for(String pair : pairs) {
            int keyEnd = pair.indexOf(":");
            if (keyEnd > 0 && keyEnd + 1 < pair.length()) {
               props.setProperty(pair.substring(0, keyEnd), pair.substring(keyEnd + 1));
            }
         }
      }

      props.setProperty("_client_name", "MySQL Connector/J");
      props.setProperty("_client_version", "8.0.12");
      props.setProperty("_runtime_vendor", Constants.JVM_VENDOR);
      props.setProperty("_runtime_version", Constants.JVM_VERSION);
      props.setProperty("_client_license", "GPL");
      return props;
   }

   private void appendConnectionAttributes(NativePacketPayload buf, String attributes, String enc) {
      NativePacketPayload lb = new NativePacketPayload(100);
      Properties props = this.getConnectionAttributesAsProperties(attributes);

      for(Object key : props.keySet()) {
         lb.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes((String)key, enc));
         lb.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(props.getProperty((String)key), enc));
      }

      buf.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, (long)lb.getPosition());
      buf.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, lb.getByteBuffer(), 0, lb.getPosition());
   }

   @Override
   public String getEncodingForHandshake() {
      String enc = this.propertySet.getStringProperty("characterEncoding").getValue();
      if (enc == null) {
         enc = "UTF-8";
      }

      return enc;
   }

   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   private void negotiateSSLConnection(int packLength) {
      this.protocol.negotiateSSLConnection(packLength);
   }

   @Override
   public void changeUser(ServerSession serverSession, String userName, String password, String database) {
      this.proceedHandshakeWithPluggableAuthentication(serverSession, userName, password, database, null);
   }
}
