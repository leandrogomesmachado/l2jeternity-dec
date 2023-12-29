package org.apache.commons.dbcp;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class BasicDataSource implements DataSource {
   protected volatile boolean defaultAutoCommit = true;
   protected transient Boolean defaultReadOnly = null;
   protected volatile int defaultTransactionIsolation = -1;
   protected volatile String defaultCatalog = null;
   protected String driverClassName = null;
   protected ClassLoader driverClassLoader = null;
   protected int maxActive = 8;
   protected int maxIdle = 8;
   protected int minIdle = 0;
   protected int initialSize = 0;
   protected long maxWait = -1L;
   protected boolean poolPreparedStatements = false;
   protected int maxOpenPreparedStatements = -1;
   protected boolean testOnBorrow = true;
   protected boolean testOnReturn = false;
   protected long timeBetweenEvictionRunsMillis = -1L;
   protected int numTestsPerEvictionRun = 3;
   protected long minEvictableIdleTimeMillis = 1800000L;
   protected boolean testWhileIdle = false;
   protected volatile String password = null;
   protected String url = null;
   protected String username = null;
   protected volatile String validationQuery = null;
   protected volatile int validationQueryTimeout = -1;
   protected volatile List connectionInitSqls;
   private boolean accessToUnderlyingConnectionAllowed = false;
   private volatile boolean restartNeeded = false;
   protected volatile GenericObjectPool connectionPool = null;
   protected Properties connectionProperties = new Properties();
   protected volatile DataSource dataSource = null;
   protected PrintWriter logWriter = new PrintWriter(System.out);
   private AbandonedConfig abandonedConfig;
   protected boolean closed;

   public boolean getDefaultAutoCommit() {
      return this.defaultAutoCommit;
   }

   public void setDefaultAutoCommit(boolean defaultAutoCommit) {
      this.defaultAutoCommit = defaultAutoCommit;
      this.restartNeeded = true;
   }

   public boolean getDefaultReadOnly() {
      Boolean val = this.defaultReadOnly;
      return val != null ? val : false;
   }

   public void setDefaultReadOnly(boolean defaultReadOnly) {
      this.defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this.restartNeeded = true;
   }

   public int getDefaultTransactionIsolation() {
      return this.defaultTransactionIsolation;
   }

   public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
      this.defaultTransactionIsolation = defaultTransactionIsolation;
      this.restartNeeded = true;
   }

   public String getDefaultCatalog() {
      return this.defaultCatalog;
   }

   public void setDefaultCatalog(String defaultCatalog) {
      if (defaultCatalog != null && defaultCatalog.trim().length() > 0) {
         this.defaultCatalog = defaultCatalog;
      } else {
         this.defaultCatalog = null;
      }

      this.restartNeeded = true;
   }

   public synchronized String getDriverClassName() {
      return this.driverClassName;
   }

   public synchronized void setDriverClassName(String driverClassName) {
      if (driverClassName != null && driverClassName.trim().length() > 0) {
         this.driverClassName = driverClassName;
      } else {
         this.driverClassName = null;
      }

      this.restartNeeded = true;
   }

   public synchronized ClassLoader getDriverClassLoader() {
      return this.driverClassLoader;
   }

   public synchronized void setDriverClassLoader(ClassLoader driverClassLoader) {
      this.driverClassLoader = driverClassLoader;
      this.restartNeeded = true;
   }

   public synchronized int getMaxActive() {
      return this.maxActive;
   }

   public synchronized void setMaxActive(int maxActive) {
      this.maxActive = maxActive;
      if (this.connectionPool != null) {
         this.connectionPool.setMaxActive(maxActive);
      }
   }

   public synchronized int getMaxIdle() {
      return this.maxIdle;
   }

   public synchronized void setMaxIdle(int maxIdle) {
      this.maxIdle = maxIdle;
      if (this.connectionPool != null) {
         this.connectionPool.setMaxIdle(maxIdle);
      }
   }

   public synchronized int getMinIdle() {
      return this.minIdle;
   }

   public synchronized void setMinIdle(int minIdle) {
      this.minIdle = minIdle;
      if (this.connectionPool != null) {
         this.connectionPool.setMinIdle(minIdle);
      }
   }

   public synchronized int getInitialSize() {
      return this.initialSize;
   }

   public synchronized void setInitialSize(int initialSize) {
      this.initialSize = initialSize;
      this.restartNeeded = true;
   }

   public synchronized long getMaxWait() {
      return this.maxWait;
   }

   public synchronized void setMaxWait(long maxWait) {
      this.maxWait = maxWait;
      if (this.connectionPool != null) {
         this.connectionPool.setMaxWait(maxWait);
      }
   }

   public synchronized boolean isPoolPreparedStatements() {
      return this.poolPreparedStatements;
   }

   public synchronized void setPoolPreparedStatements(boolean poolingStatements) {
      this.poolPreparedStatements = poolingStatements;
      this.restartNeeded = true;
   }

   public synchronized int getMaxOpenPreparedStatements() {
      return this.maxOpenPreparedStatements;
   }

   public synchronized void setMaxOpenPreparedStatements(int maxOpenStatements) {
      this.maxOpenPreparedStatements = maxOpenStatements;
      this.restartNeeded = true;
   }

   public synchronized boolean getTestOnBorrow() {
      return this.testOnBorrow;
   }

   public synchronized void setTestOnBorrow(boolean testOnBorrow) {
      this.testOnBorrow = testOnBorrow;
      if (this.connectionPool != null) {
         this.connectionPool.setTestOnBorrow(testOnBorrow);
      }
   }

   public synchronized boolean getTestOnReturn() {
      return this.testOnReturn;
   }

   public synchronized void setTestOnReturn(boolean testOnReturn) {
      this.testOnReturn = testOnReturn;
      if (this.connectionPool != null) {
         this.connectionPool.setTestOnReturn(testOnReturn);
      }
   }

   public synchronized long getTimeBetweenEvictionRunsMillis() {
      return this.timeBetweenEvictionRunsMillis;
   }

   public synchronized void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
      this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
      if (this.connectionPool != null) {
         this.connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
      }
   }

   public synchronized int getNumTestsPerEvictionRun() {
      return this.numTestsPerEvictionRun;
   }

   public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
      this.numTestsPerEvictionRun = numTestsPerEvictionRun;
      if (this.connectionPool != null) {
         this.connectionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
      }
   }

   public synchronized long getMinEvictableIdleTimeMillis() {
      return this.minEvictableIdleTimeMillis;
   }

   public synchronized void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
      this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
      if (this.connectionPool != null) {
         this.connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
      }
   }

   public synchronized boolean getTestWhileIdle() {
      return this.testWhileIdle;
   }

   public synchronized void setTestWhileIdle(boolean testWhileIdle) {
      this.testWhileIdle = testWhileIdle;
      if (this.connectionPool != null) {
         this.connectionPool.setTestWhileIdle(testWhileIdle);
      }
   }

   public synchronized int getNumActive() {
      return this.connectionPool != null ? this.connectionPool.getNumActive() : 0;
   }

   public synchronized int getNumIdle() {
      return this.connectionPool != null ? this.connectionPool.getNumIdle() : 0;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
      this.restartNeeded = true;
   }

   public synchronized String getUrl() {
      return this.url;
   }

   public synchronized void setUrl(String url) {
      this.url = url;
      this.restartNeeded = true;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
      this.restartNeeded = true;
   }

   public String getValidationQuery() {
      return this.validationQuery;
   }

   public void setValidationQuery(String validationQuery) {
      if (validationQuery != null && validationQuery.trim().length() > 0) {
         this.validationQuery = validationQuery;
      } else {
         this.validationQuery = null;
      }

      this.restartNeeded = true;
   }

   public int getValidationQueryTimeout() {
      return this.validationQueryTimeout;
   }

   public void setValidationQueryTimeout(int timeout) {
      this.validationQueryTimeout = timeout;
      this.restartNeeded = true;
   }

   public Collection getConnectionInitSqls() {
      Collection result = this.connectionInitSqls;
      return (Collection)(result == null ? Collections.EMPTY_LIST : result);
   }

   public void setConnectionInitSqls(Collection connectionInitSqls) {
      if (connectionInitSqls != null && connectionInitSqls.size() > 0) {
         ArrayList newVal = null;

         for(Object o : connectionInitSqls) {
            if (o != null) {
               String s = o.toString();
               if (s.trim().length() > 0) {
                  if (newVal == null) {
                     newVal = new ArrayList();
                  }

                  newVal.add(s);
               }
            }
         }

         this.connectionInitSqls = newVal;
      } else {
         this.connectionInitSqls = null;
      }

      this.restartNeeded = true;
   }

   public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
      return this.accessToUnderlyingConnectionAllowed;
   }

   public synchronized void setAccessToUnderlyingConnectionAllowed(boolean allow) {
      this.accessToUnderlyingConnectionAllowed = allow;
      this.restartNeeded = true;
   }

   private boolean isRestartNeeded() {
      return this.restartNeeded;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this.createDataSource().getConnection();
   }

   @Override
   public Connection getConnection(String user, String pass) throws SQLException {
      throw new UnsupportedOperationException("Not supported by BasicDataSource");
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      throw new UnsupportedOperationException("Not supported by BasicDataSource");
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return this.createDataSource().getLogWriter();
   }

   @Override
   public void setLoginTimeout(int loginTimeout) throws SQLException {
      throw new UnsupportedOperationException("Not supported by BasicDataSource");
   }

   @Override
   public void setLogWriter(PrintWriter logWriter) throws SQLException {
      this.createDataSource().setLogWriter(logWriter);
      this.logWriter = logWriter;
   }

   public boolean getRemoveAbandoned() {
      return this.abandonedConfig != null ? this.abandonedConfig.getRemoveAbandoned() : false;
   }

   public void setRemoveAbandoned(boolean removeAbandoned) {
      if (this.abandonedConfig == null) {
         this.abandonedConfig = new AbandonedConfig();
      }

      this.abandonedConfig.setRemoveAbandoned(removeAbandoned);
      this.restartNeeded = true;
   }

   public int getRemoveAbandonedTimeout() {
      return this.abandonedConfig != null ? this.abandonedConfig.getRemoveAbandonedTimeout() : 300;
   }

   public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
      if (this.abandonedConfig == null) {
         this.abandonedConfig = new AbandonedConfig();
      }

      this.abandonedConfig.setRemoveAbandonedTimeout(removeAbandonedTimeout);
      this.restartNeeded = true;
   }

   public boolean getLogAbandoned() {
      return this.abandonedConfig != null ? this.abandonedConfig.getLogAbandoned() : false;
   }

   public void setLogAbandoned(boolean logAbandoned) {
      if (this.abandonedConfig == null) {
         this.abandonedConfig = new AbandonedConfig();
      }

      this.abandonedConfig.setLogAbandoned(logAbandoned);
      this.restartNeeded = true;
   }

   public void addConnectionProperty(String name, String value) {
      this.connectionProperties.put(name, value);
      this.restartNeeded = true;
   }

   public void removeConnectionProperty(String name) {
      this.connectionProperties.remove(name);
      this.restartNeeded = true;
   }

   public void setConnectionProperties(String connectionProperties) {
      if (connectionProperties == null) {
         throw new NullPointerException("connectionProperties is null");
      } else {
         String[] entries = connectionProperties.split(";");
         Properties properties = new Properties();

         for(int i = 0; i < entries.length; ++i) {
            String entry = entries[i];
            if (entry.length() > 0) {
               int index = entry.indexOf(61);
               if (index > 0) {
                  String name = entry.substring(0, index);
                  String value = entry.substring(index + 1);
                  properties.setProperty(name, value);
               } else {
                  properties.setProperty(entry, "");
               }
            }
         }

         this.connectionProperties = properties;
         this.restartNeeded = true;
      }
   }

   public synchronized void close() throws SQLException {
      this.closed = true;
      GenericObjectPool oldpool = this.connectionPool;
      this.connectionPool = null;
      this.dataSource = null;

      try {
         if (oldpool != null) {
            oldpool.close();
         }
      } catch (SQLException var3) {
         throw var3;
      } catch (RuntimeException var4) {
         throw var4;
      } catch (Exception var5) {
         throw new SQLNestedException("Cannot close connection pool", var5);
      }
   }

   public synchronized boolean isClosed() {
      return this.closed;
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLException("BasicDataSource is not a wrapper.");
   }

   protected synchronized DataSource createDataSource() throws SQLException {
      if (this.closed) {
         throw new SQLException("Data source is closed");
      } else if (this.dataSource != null) {
         return this.dataSource;
      } else {
         ConnectionFactory driverConnectionFactory = this.createConnectionFactory();
         this.createConnectionPool();
         GenericKeyedObjectPoolFactory statementPoolFactory = null;
         if (this.isPoolPreparedStatements()) {
            statementPoolFactory = new GenericKeyedObjectPoolFactory(null, -1, (byte)0, 0L, 1, this.maxOpenPreparedStatements);
         }

         this.createPoolableConnectionFactory(driverConnectionFactory, statementPoolFactory, this.abandonedConfig);
         this.createDataSourceInstance();

         try {
            for(int i = 0; i < this.initialSize; ++i) {
               this.connectionPool.addObject();
            }
         } catch (Exception var4) {
            throw new SQLNestedException("Error preloading the connection pool", var4);
         }

         return this.dataSource;
      }
   }

   protected ConnectionFactory createConnectionFactory() throws SQLException {
      Class driverFromCCL = null;
      if (this.driverClassName != null) {
         try {
            try {
               if (this.driverClassLoader == null) {
                  Class.forName(this.driverClassName);
               } else {
                  Class.forName(this.driverClassName, true, this.driverClassLoader);
               }
            } catch (ClassNotFoundException var6) {
               driverFromCCL = Thread.currentThread().getContextClassLoader().loadClass(this.driverClassName);
            }
         } catch (Throwable var7) {
            String message = "Cannot load JDBC driver class '" + this.driverClassName + "'";
            this.logWriter.println(message);
            var7.printStackTrace(this.logWriter);
            throw new SQLNestedException(message, var7);
         }
      }

      Driver driver = null;

      try {
         if (driverFromCCL == null) {
            driver = DriverManager.getDriver(this.url);
         } else {
            driver = (Driver)driverFromCCL.newInstance();
            if (!driver.acceptsURL(this.url)) {
               throw new SQLException("No suitable driver", "08001");
            }
         }
      } catch (Throwable var8) {
         String message = "Cannot create JDBC driver of class '"
            + (this.driverClassName != null ? this.driverClassName : "")
            + "' for connect URL '"
            + this.url
            + "'";
         this.logWriter.println(message);
         var8.printStackTrace(this.logWriter);
         throw new SQLNestedException(message, var8);
      }

      if (this.validationQuery == null) {
         this.setTestOnBorrow(false);
         this.setTestOnReturn(false);
         this.setTestWhileIdle(false);
      }

      String user = this.username;
      if (user != null) {
         this.connectionProperties.put("user", user);
      } else {
         this.log("DBCP DataSource configured without a 'username'");
      }

      String pwd = this.password;
      if (pwd != null) {
         this.connectionProperties.put("password", pwd);
      } else {
         this.log("DBCP DataSource configured without a 'password'");
      }

      ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, this.url, this.connectionProperties);
      return driverConnectionFactory;
   }

   protected void createConnectionPool() {
      GenericObjectPool gop;
      if (this.abandonedConfig != null && this.abandonedConfig.getRemoveAbandoned()) {
         gop = new AbandonedObjectPool(null, this.abandonedConfig);
      } else {
         gop = new GenericObjectPool();
      }

      gop.setMaxActive(this.maxActive);
      gop.setMaxIdle(this.maxIdle);
      gop.setMinIdle(this.minIdle);
      gop.setMaxWait(this.maxWait);
      gop.setTestOnBorrow(this.testOnBorrow);
      gop.setTestOnReturn(this.testOnReturn);
      gop.setTimeBetweenEvictionRunsMillis(this.timeBetweenEvictionRunsMillis);
      gop.setNumTestsPerEvictionRun(this.numTestsPerEvictionRun);
      gop.setMinEvictableIdleTimeMillis(this.minEvictableIdleTimeMillis);
      gop.setTestWhileIdle(this.testWhileIdle);
      this.connectionPool = gop;
   }

   protected void createDataSourceInstance() throws SQLException {
      PoolingDataSource pds = new PoolingDataSource(this.connectionPool);
      pds.setAccessToUnderlyingConnectionAllowed(this.isAccessToUnderlyingConnectionAllowed());
      pds.setLogWriter(this.logWriter);
      this.dataSource = pds;
   }

   protected void createPoolableConnectionFactory(
      ConnectionFactory driverConnectionFactory, KeyedObjectPoolFactory statementPoolFactory, AbandonedConfig configuration
   ) throws SQLException {
      PoolableConnectionFactory connectionFactory = null;

      try {
         connectionFactory = new PoolableConnectionFactory(
            driverConnectionFactory,
            this.connectionPool,
            statementPoolFactory,
            this.validationQuery,
            this.validationQueryTimeout,
            this.connectionInitSqls,
            this.defaultReadOnly,
            this.defaultAutoCommit,
            this.defaultTransactionIsolation,
            this.defaultCatalog,
            configuration
         );
         validateConnectionFactory(connectionFactory);
      } catch (RuntimeException var6) {
         throw var6;
      } catch (Exception var7) {
         throw new SQLNestedException("Cannot create PoolableConnectionFactory (" + var7.getMessage() + ")", var7);
      }
   }

   protected static void validateConnectionFactory(PoolableConnectionFactory connectionFactory) throws Exception {
      Connection conn = null;

      try {
         conn = (Connection)connectionFactory.makeObject();
         connectionFactory.activateObject(conn);
         connectionFactory.validateConnection(conn);
         connectionFactory.passivateObject(conn);
      } finally {
         connectionFactory.destroyObject(conn);
      }
   }

   private void restart() {
      try {
         this.close();
      } catch (SQLException var2) {
         this.log("Could not restart DataSource, cause: " + var2.getMessage());
      }
   }

   protected void log(String message) {
      if (this.logWriter != null) {
         this.logWriter.println(message);
      }
   }

   static {
      DriverManager.getDrivers();
   }
}
