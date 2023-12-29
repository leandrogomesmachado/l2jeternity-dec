package com.mysql.cj.conf;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.InvalidConnectionAttributeException;
import com.mysql.cj.exceptions.UnsupportedConnectionStringException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.util.LRUCache;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public abstract class ConnectionUrl implements DatabaseUrlContainer {
   private static final String DEFAULT_HOST = "localhost";
   private static final int DEFAULT_PORT = 3306;
   private static final LRUCache<String, ConnectionUrl> connectionUrlCache = new LRUCache<>(100);
   private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
   protected ConnectionUrl.Type type;
   protected String originalConnStr;
   protected String originalDatabase;
   protected List<HostInfo> hosts = new ArrayList<>();
   protected Map<String, String> properties = new HashMap<>();
   ConnectionPropertiesTransform propertiesTransformer;

   public static ConnectionUrl getConnectionUrlInstance(String connString, Properties info) {
      if (connString == null) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("ConnectionString.0"));
      } else {
         String connStringCacheKey = buildConnectionStringCacheKey(connString, info);
         rwLock.readLock().lock();
         ConnectionUrl connectionString = connectionUrlCache.get(connStringCacheKey);
         if (connectionString == null) {
            rwLock.readLock().unlock();
            rwLock.writeLock().lock();

            try {
               connectionString = connectionUrlCache.get(connStringCacheKey);
               if (connectionString == null) {
                  ConnectionUrlParser connStrParser = ConnectionUrlParser.parseConnectionString(connString);
                  switch(ConnectionUrl.Type.fromValue(connStrParser.getScheme(), connStrParser.getHosts().size())) {
                     case SINGLE_CONNECTION:
                        connectionString = (ConnectionUrl)Util.getInstance(
                           "com.mysql.cj.conf.url.SingleConnectionUrl",
                           new Class[]{ConnectionUrlParser.class, Properties.class},
                           new Object[]{connStrParser, info},
                           null
                        );
                        break;
                     case FAILOVER_CONNECTION:
                        connectionString = (ConnectionUrl)Util.getInstance(
                           "com.mysql.cj.conf.url.FailoverConnectionUrl",
                           new Class[]{ConnectionUrlParser.class, Properties.class},
                           new Object[]{connStrParser, info},
                           null
                        );
                        break;
                     case LOADBALANCE_CONNECTION:
                        connectionString = (ConnectionUrl)Util.getInstance(
                           "com.mysql.cj.conf.url.LoadbalanceConnectionUrl",
                           new Class[]{ConnectionUrlParser.class, Properties.class},
                           new Object[]{connStrParser, info},
                           null
                        );
                        break;
                     case REPLICATION_CONNECTION:
                        connectionString = (ConnectionUrl)Util.getInstance(
                           "com.mysql.cj.conf.url.ReplicationConnectionUrl",
                           new Class[]{ConnectionUrlParser.class, Properties.class},
                           new Object[]{connStrParser, info},
                           null
                        );
                        break;
                     case XDEVAPI_SESSION:
                        connectionString = (ConnectionUrl)Util.getInstance(
                           "com.mysql.cj.conf.url.XDevAPIConnectionUrl",
                           new Class[]{ConnectionUrlParser.class, Properties.class},
                           new Object[]{connStrParser, info},
                           null
                        );
                        break;
                     default:
                        return null;
                  }

                  connectionUrlCache.put(connStringCacheKey, connectionString);
               }

               rwLock.readLock().lock();
            } finally {
               rwLock.writeLock().unlock();
            }
         }

         rwLock.readLock().unlock();
         return connectionString;
      }
   }

   private static String buildConnectionStringCacheKey(String connString, Properties info) {
      StringBuilder sbKey = new StringBuilder(connString);
      sbKey.append("??");
      sbKey.append(
         info == null ? null : info.stringPropertyNames().stream().map(k -> k + "=" + info.getProperty(k)).collect(Collectors.joining(", ", "{", "}"))
      );
      return sbKey.toString();
   }

   public static boolean acceptsUrl(String connString) {
      return ConnectionUrlParser.isConnectionStringSupported(connString);
   }

   protected ConnectionUrl() {
   }

   public ConnectionUrl(String origUrl) {
      this.originalConnStr = origUrl;
   }

   protected ConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      this.originalConnStr = connStrParser.getDatabaseUrl();
      this.originalDatabase = connStrParser.getPath() == null ? "" : connStrParser.getPath();
      this.collectProperties(connStrParser, info);
      this.collectHostsInfo(connStrParser);
   }

   protected void collectProperties(ConnectionUrlParser connStrParser, Properties info) {
      connStrParser.getProperties().entrySet().stream().forEach(e -> PropertyDefinitions.PropertyKey.normalizeCase(e.getKey()));
      if (info != null) {
         info.stringPropertyNames().stream().forEach(k -> PropertyDefinitions.PropertyKey.normalizeCase(k));
      }

      this.processColdFusionAutoConfiguration();
      this.setupPropertiesTransformer();
      this.expandPropertiesFromConfigFiles(this.properties);
      this.injectPerTypeProperties(this.properties);
   }

   protected void processColdFusionAutoConfiguration() {
      if (Util.isColdFusion()) {
         String autoConfigCf = this.properties.get("autoConfigureForColdFusion");
         if (autoConfigCf == null || autoConfigCf.equalsIgnoreCase("TRUE") || autoConfigCf.equalsIgnoreCase("YES")) {
            String currentConfigFiles = this.properties.get("useConfigs");
            StringBuilder newConfigFiles = new StringBuilder();
            if (currentConfigFiles != null) {
               newConfigFiles.append(currentConfigFiles).append(",");
            }

            newConfigFiles.append("coldFusion");
            this.properties.put("useConfigs", newConfigFiles.toString());
         }
      }
   }

   protected void setupPropertiesTransformer() {
      String propertiesTransformClassName = this.properties.get("propertiesTransform");
      if (!StringUtils.isNullOrEmpty(propertiesTransformClassName)) {
         try {
            this.propertiesTransformer = (ConnectionPropertiesTransform)Class.forName(propertiesTransformClassName).newInstance();
         } catch (IllegalAccessException | ClassNotFoundException | CJException | InstantiationException var3) {
            throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
               InvalidConnectionAttributeException.class,
               Messages.getString("ConnectionString.9", new Object[]{propertiesTransformClassName, var3.toString()}),
               var3
            );
         }
      }
   }

   protected void expandPropertiesFromConfigFiles(Map<String, String> props) {
      String configFiles = props.get("useConfigs");
      if (!StringUtils.isNullOrEmpty(configFiles)) {
         Properties configProps = getPropertiesFromConfigFiles(configFiles);
         configProps.stringPropertyNames().stream().map(PropertyDefinitions.PropertyKey::normalizeCase).filter(k -> !props.containsKey(k)).forEach(k -> {
         });
      }
   }

   public static Properties getPropertiesFromConfigFiles(String configFiles) {
      Properties configProps = new Properties();

      for(String configFile : configFiles.split(",")) {
         try (InputStream configAsStream = ConnectionUrl.class.getResourceAsStream("/com/mysql/cj/configurations/" + configFile + ".properties")) {
            if (configAsStream == null) {
               throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
                  InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.10", new Object[]{configFile})
               );
            }

            configProps.load(configAsStream);
         } catch (IOException var19) {
            throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
               InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.11", new Object[]{configFile}), var19
            );
         }
      }

      return configProps;
   }

   protected void injectPerTypeProperties(Map<String, String> props) {
   }

   protected void collectHostsInfo(ConnectionUrlParser connStrParser) {
      connStrParser.getHosts().stream().map(this::fixHostInfo).forEach(this.hosts::add);
   }

   protected HostInfo fixHostInfo(HostInfo hi) {
      Map<String, String> hostProps = new HashMap<>();
      hostProps.putAll(this.properties);
      hi.getHostProperties().entrySet().stream().forEach(e -> PropertyDefinitions.PropertyKey.normalizeCase(e.getKey()));
      hostProps.put(PropertyDefinitions.PropertyKey.DBNAME.getKeyName(), this.getDatabase());
      this.preprocessPerTypeHostProperties(hostProps);
      String host = hostProps.remove(PropertyDefinitions.PropertyKey.HOST.getKeyName());
      if (!StringUtils.isNullOrEmpty(hi.getHost())) {
         host = hi.getHost();
      } else if (StringUtils.isNullOrEmpty(host)) {
         host = this.getDefaultHost();
      }

      String portAsString = hostProps.remove(PropertyDefinitions.PropertyKey.PORT.getKeyName());
      int port = hi.getPort();
      if (port == -1 && !StringUtils.isNullOrEmpty(portAsString)) {
         try {
            port = Integer.valueOf(portAsString);
         } catch (NumberFormatException var9) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("ConnectionString.7", new Object[]{hostProps.get(PropertyDefinitions.PropertyKey.PORT.getKeyName())}),
               var9
            );
         }
      }

      if (port == -1) {
         port = this.getDefaultPort();
      }

      String user = hostProps.remove(PropertyDefinitions.PropertyKey.USER.getKeyName());
      if (!StringUtils.isNullOrEmpty(hi.getUser())) {
         user = hi.getUser();
      } else if (StringUtils.isNullOrEmpty(user)) {
         user = this.getDefaultUser();
      }

      boolean isPasswordless = hi.isPasswordless();
      String password = hostProps.remove(PropertyDefinitions.PropertyKey.PASSWORD.getKeyName());
      if (!isPasswordless) {
         password = hi.getPassword();
      } else if (password == null) {
         password = this.getDefaultPassword();
         isPasswordless = true;
      } else {
         isPasswordless = false;
      }

      this.expandPropertiesFromConfigFiles(hostProps);
      this.fixProtocolDependencies(hostProps);
      return this.buildHostInfo(host, port, user, password, isPasswordless, hostProps);
   }

   protected void preprocessPerTypeHostProperties(Map<String, String> hostProps) {
   }

   public String getDefaultHost() {
      return "localhost";
   }

   public int getDefaultPort() {
      return 3306;
   }

   public String getDefaultUser() {
      String user = this.properties.get(PropertyDefinitions.PropertyKey.USER.getKeyName());
      return StringUtils.isNullOrEmpty(user) ? "" : user;
   }

   public String getDefaultPassword() {
      String password = this.properties.get(PropertyDefinitions.PropertyKey.PASSWORD.getKeyName());
      return StringUtils.isNullOrEmpty(password) ? "" : password;
   }

   protected void fixProtocolDependencies(Map<String, String> hostProps) {
      String protocol = hostProps.get(PropertyDefinitions.PropertyKey.PROTOCOL.getKeyName());
      if (!StringUtils.isNullOrEmpty(protocol) && protocol.equalsIgnoreCase("PIPE")) {
         if (!hostProps.containsKey("socketFactory")) {
            hostProps.put("socketFactory", "com.mysql.cj.protocol.NamedPipeSocketFactory");
         }

         if (hostProps.containsKey(PropertyDefinitions.PropertyKey.PATH.getKeyName()) && !hostProps.containsKey("namedPipePath")) {
            hostProps.put("namedPipePath", hostProps.get(PropertyDefinitions.PropertyKey.PATH.getKeyName()));
         }
      }
   }

   public ConnectionUrl.Type getType() {
      return this.type;
   }

   @Override
   public String getDatabaseUrl() {
      return this.originalConnStr;
   }

   public String getDatabase() {
      return this.properties.containsKey(PropertyDefinitions.PropertyKey.DBNAME.getKeyName())
         ? this.properties.get(PropertyDefinitions.PropertyKey.DBNAME.getKeyName())
         : this.originalDatabase;
   }

   public int hostsCount() {
      return this.hosts.size();
   }

   public HostInfo getMainHost() {
      return this.hosts.isEmpty() ? null : this.hosts.get(0);
   }

   public List<HostInfo> getHostsList() {
      return Collections.unmodifiableList(this.hosts);
   }

   public HostInfo getHostOrSpawnIsolated(String hostPortPair) {
      return this.getHostOrSpawnIsolated(hostPortPair, this.hosts);
   }

   public HostInfo getHostOrSpawnIsolated(String hostPortPair, List<HostInfo> hostsList) {
      for(HostInfo hi : hostsList) {
         if (hostPortPair.equals(hi.getHostPortPair())) {
            return hi;
         }
      }

      ConnectionUrlParser.Pair<String, Integer> hostAndPort = ConnectionUrlParser.parseHostPortPair(hostPortPair);
      String host = hostAndPort.left;
      Integer port = hostAndPort.right;
      String user = this.getDefaultUser();
      String password = this.getDefaultPassword();
      return this.buildHostInfo(host, port, user, password, true, this.properties);
   }

   private HostInfo buildHostInfo(String host, int port, String user, String password, boolean isDefaultPwd, Map<String, String> hostProps) {
      if (this.propertiesTransformer != null) {
         Properties props = new Properties();
         props.putAll(hostProps);
         props.setProperty(PropertyDefinitions.PropertyKey.HOST.getKeyName(), host);
         props.setProperty(PropertyDefinitions.PropertyKey.PORT.getKeyName(), String.valueOf(port));
         props.setProperty(PropertyDefinitions.PropertyKey.USER.getKeyName(), user);
         props.setProperty(PropertyDefinitions.PropertyKey.PASSWORD.getKeyName(), password);
         Properties transformedProps = this.propertiesTransformer.transformProperties(props);
         host = transformedProps.getProperty(PropertyDefinitions.PropertyKey.PORT.getKeyName());

         try {
            port = Integer.parseInt(transformedProps.getProperty(PropertyDefinitions.PropertyKey.PORT.getKeyName()));
         } catch (NumberFormatException var10) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString(
                  "ConnectionString.8",
                  new Object[]{
                     PropertyDefinitions.PropertyKey.PORT.getKeyName(), transformedProps.getProperty(PropertyDefinitions.PropertyKey.PORT.getKeyName())
                  }
               ),
               var10
            );
         }

         user = transformedProps.getProperty(PropertyDefinitions.PropertyKey.USER.getKeyName());
         password = transformedProps.getProperty(PropertyDefinitions.PropertyKey.PASSWORD.getKeyName());
         Map<String, String> transformedHostProps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         transformedProps.stringPropertyNames().stream().forEach(k -> {
         });
         transformedHostProps.remove(PropertyDefinitions.PropertyKey.HOST.getKeyName());
         transformedHostProps.remove(PropertyDefinitions.PropertyKey.PORT.getKeyName());
         transformedHostProps.remove(PropertyDefinitions.PropertyKey.USER.getKeyName());
         transformedHostProps.remove(PropertyDefinitions.PropertyKey.PASSWORD.getKeyName());
         hostProps = transformedHostProps;
      }

      return new HostInfo(this, host, port, user, password, isDefaultPwd, hostProps);
   }

   public Map<String, String> getOriginalProperties() {
      return Collections.unmodifiableMap(this.properties);
   }

   public Properties getConnectionArgumentsAsProperties() {
      Properties props = new Properties();
      if (this.properties != null) {
         props.putAll(this.properties);
      }

      return this.propertiesTransformer != null ? this.propertiesTransformer.transformProperties(props) : props;
   }

   @Override
   public String toString() {
      StringBuilder asStr = new StringBuilder(super.toString());
      asStr.append(
         String.format(
            " :: {type: \"%s\", hosts: %s, database: \"%s\", properties: %s, propertiesTransformer: %s}",
            this.type,
            this.hosts,
            this.originalDatabase,
            this.properties,
            this.propertiesTransformer
         )
      );
      return asStr.toString();
   }

   public static enum HostsCardinality {
      SINGLE {
         @Override
         public boolean assertSize(int n) {
            return n == 1;
         }
      },
      MULTIPLE {
         @Override
         public boolean assertSize(int n) {
            return n > 1;
         }
      },
      ONE_OR_MORE {
         @Override
         public boolean assertSize(int n) {
            return n >= 1;
         }
      };

      private HostsCardinality() {
      }

      public abstract boolean assertSize(int var1);
   }

   public static enum Type {
      SINGLE_CONNECTION("jdbc:mysql:", ConnectionUrl.HostsCardinality.SINGLE),
      FAILOVER_CONNECTION("jdbc:mysql:", ConnectionUrl.HostsCardinality.MULTIPLE),
      LOADBALANCE_CONNECTION("jdbc:mysql:loadbalance:", ConnectionUrl.HostsCardinality.ONE_OR_MORE),
      REPLICATION_CONNECTION("jdbc:mysql:replication:", ConnectionUrl.HostsCardinality.ONE_OR_MORE),
      XDEVAPI_SESSION("mysqlx:", ConnectionUrl.HostsCardinality.ONE_OR_MORE);

      private String scheme;
      private ConnectionUrl.HostsCardinality cardinality;

      private Type(String scheme, ConnectionUrl.HostsCardinality cardinality) {
         this.scheme = scheme;
         this.cardinality = cardinality;
      }

      public String getScheme() {
         return this.scheme;
      }

      public ConnectionUrl.HostsCardinality getCardinality() {
         return this.cardinality;
      }

      public static ConnectionUrl.Type fromValue(String scheme, int n) {
         for(ConnectionUrl.Type t : values()) {
            if (t.getScheme().equalsIgnoreCase(scheme) && (n < 0 || t.getCardinality().assertSize(n))) {
               return t;
            }
         }

         if (n < 0) {
            throw (UnsupportedConnectionStringException)ExceptionFactory.createException(
               UnsupportedConnectionStringException.class, Messages.getString("ConnectionString.5", new Object[]{scheme})
            );
         } else {
            throw (UnsupportedConnectionStringException)ExceptionFactory.createException(
               UnsupportedConnectionStringException.class, Messages.getString("ConnectionString.6", new Object[]{scheme, n})
            );
         }
      }

      public static boolean isSupported(String scheme) {
         for(ConnectionUrl.Type t : values()) {
            if (t.getScheme().equalsIgnoreCase(scheme)) {
               return true;
            }
         }

         return false;
      }
   }
}
