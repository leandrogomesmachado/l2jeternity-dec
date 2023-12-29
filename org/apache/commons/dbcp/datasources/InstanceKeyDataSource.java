package org.apache.commons.dbcp.datasources;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import org.apache.commons.dbcp.SQLNestedException;

public abstract class InstanceKeyDataSource implements DataSource, Referenceable, Serializable {
   private static final long serialVersionUID = -4243533936955098795L;
   private static final String GET_CONNECTION_CALLED = "A Connection was already requested from this source, further initialization is not allowed.";
   private static final String BAD_TRANSACTION_ISOLATION = "The requested TransactionIsolation level is invalid.";
   protected static final int UNKNOWN_TRANSACTIONISOLATION = -1;
   private volatile boolean getConnectionCalled = false;
   private ConnectionPoolDataSource dataSource = null;
   private String dataSourceName = null;
   private boolean defaultAutoCommit = false;
   private int defaultTransactionIsolation = -1;
   private boolean defaultReadOnly = false;
   private String description = null;
   Properties jndiEnvironment = null;
   private int loginTimeout = 0;
   private PrintWriter logWriter = null;
   private boolean _testOnBorrow = false;
   private boolean _testOnReturn = false;
   private int _timeBetweenEvictionRunsMillis = (int)Math.min(2147483647L, -1L);
   private int _numTestsPerEvictionRun = 3;
   private int _minEvictableIdleTimeMillis = (int)Math.min(2147483647L, 1800000L);
   private boolean _testWhileIdle = false;
   private String validationQuery = null;
   private boolean rollbackAfterValidation = false;
   private boolean testPositionSet = false;
   protected String instanceKey = null;

   public InstanceKeyDataSource() {
      this.defaultAutoCommit = true;
   }

   protected void assertInitializationAllowed() throws IllegalStateException {
      if (this.getConnectionCalled) {
         throw new IllegalStateException("A Connection was already requested from this source, further initialization is not allowed.");
      }
   }

   public abstract void close() throws Exception;

   protected abstract PooledConnectionManager getConnectionManager(UserPassKey var1);

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLException("InstanceKeyDataSource is not a wrapper.");
   }

   public ConnectionPoolDataSource getConnectionPoolDataSource() {
      return this.dataSource;
   }

   public void setConnectionPoolDataSource(ConnectionPoolDataSource v) {
      this.assertInitializationAllowed();
      if (this.dataSourceName != null) {
         throw new IllegalStateException("Cannot set the DataSource, if JNDI is used.");
      } else if (this.dataSource != null) {
         throw new IllegalStateException("The CPDS has already been set. It cannot be altered.");
      } else {
         this.dataSource = v;
         this.instanceKey = InstanceKeyObjectFactory.registerNewInstance(this);
      }
   }

   public String getDataSourceName() {
      return this.dataSourceName;
   }

   public void setDataSourceName(String v) {
      this.assertInitializationAllowed();
      if (this.dataSource != null) {
         throw new IllegalStateException("Cannot set the JNDI name for the DataSource, if already set using setConnectionPoolDataSource.");
      } else if (this.dataSourceName != null) {
         throw new IllegalStateException("The DataSourceName has already been set. It cannot be altered.");
      } else {
         this.dataSourceName = v;
         this.instanceKey = InstanceKeyObjectFactory.registerNewInstance(this);
      }
   }

   public boolean isDefaultAutoCommit() {
      return this.defaultAutoCommit;
   }

   public void setDefaultAutoCommit(boolean v) {
      this.assertInitializationAllowed();
      this.defaultAutoCommit = v;
   }

   public boolean isDefaultReadOnly() {
      return this.defaultReadOnly;
   }

   public void setDefaultReadOnly(boolean v) {
      this.assertInitializationAllowed();
      this.defaultReadOnly = v;
   }

   public int getDefaultTransactionIsolation() {
      return this.defaultTransactionIsolation;
   }

   public void setDefaultTransactionIsolation(int v) {
      this.assertInitializationAllowed();
      switch(v) {
         case 0:
         case 1:
         case 2:
         case 4:
         case 8:
            this.defaultTransactionIsolation = v;
            return;
         case 3:
         case 5:
         case 6:
         case 7:
         default:
            throw new IllegalArgumentException("The requested TransactionIsolation level is invalid.");
      }
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String v) {
      this.description = v;
   }

   public String getJndiEnvironment(String key) {
      String value = null;
      if (this.jndiEnvironment != null) {
         value = this.jndiEnvironment.getProperty(key);
      }

      return value;
   }

   public void setJndiEnvironment(String key, String value) {
      if (this.jndiEnvironment == null) {
         this.jndiEnvironment = new Properties();
      }

      this.jndiEnvironment.setProperty(key, value);
   }

   @Override
   public int getLoginTimeout() {
      return this.loginTimeout;
   }

   @Override
   public void setLoginTimeout(int v) {
      this.loginTimeout = v;
   }

   @Override
   public PrintWriter getLogWriter() {
      if (this.logWriter == null) {
         this.logWriter = new PrintWriter(System.out);
      }

      return this.logWriter;
   }

   @Override
   public void setLogWriter(PrintWriter v) {
      this.logWriter = v;
   }

   public final boolean isTestOnBorrow() {
      return this.getTestOnBorrow();
   }

   public boolean getTestOnBorrow() {
      return this._testOnBorrow;
   }

   public void setTestOnBorrow(boolean testOnBorrow) {
      this.assertInitializationAllowed();
      this._testOnBorrow = testOnBorrow;
      this.testPositionSet = true;
   }

   public final boolean isTestOnReturn() {
      return this.getTestOnReturn();
   }

   public boolean getTestOnReturn() {
      return this._testOnReturn;
   }

   public void setTestOnReturn(boolean testOnReturn) {
      this.assertInitializationAllowed();
      this._testOnReturn = testOnReturn;
      this.testPositionSet = true;
   }

   public int getTimeBetweenEvictionRunsMillis() {
      return this._timeBetweenEvictionRunsMillis;
   }

   public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
      this.assertInitializationAllowed();
      this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
   }

   public int getNumTestsPerEvictionRun() {
      return this._numTestsPerEvictionRun;
   }

   public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
      this.assertInitializationAllowed();
      this._numTestsPerEvictionRun = numTestsPerEvictionRun;
   }

   public int getMinEvictableIdleTimeMillis() {
      return this._minEvictableIdleTimeMillis;
   }

   public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
      this.assertInitializationAllowed();
      this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
   }

   public final boolean isTestWhileIdle() {
      return this.getTestWhileIdle();
   }

   public boolean getTestWhileIdle() {
      return this._testWhileIdle;
   }

   public void setTestWhileIdle(boolean testWhileIdle) {
      this.assertInitializationAllowed();
      this._testWhileIdle = testWhileIdle;
      this.testPositionSet = true;
   }

   public String getValidationQuery() {
      return this.validationQuery;
   }

   public void setValidationQuery(String validationQuery) {
      this.assertInitializationAllowed();
      this.validationQuery = validationQuery;
      if (!this.testPositionSet) {
         this.setTestOnBorrow(true);
      }
   }

   public boolean isRollbackAfterValidation() {
      return this.rollbackAfterValidation;
   }

   public void setRollbackAfterValidation(boolean rollbackAfterValidation) {
      this.assertInitializationAllowed();
      this.rollbackAfterValidation = rollbackAfterValidation;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this.getConnection(null, null);
   }

   @Override
   public Connection getConnection(String username, String password) throws SQLException {
      if (this.instanceKey == null) {
         throw new SQLException("Must set the ConnectionPoolDataSource through setDataSourceName or setConnectionPoolDataSource before calling getConnection.");
      } else {
         this.getConnectionCalled = true;
         PooledConnectionAndInfo info = null;

         try {
            info = this.getPooledConnectionAndInfo(username, password);
         } catch (NoSuchElementException var16) {
            this.closeDueToException(info);
            throw new SQLNestedException("Cannot borrow connection from pool", var16);
         } catch (RuntimeException var17) {
            this.closeDueToException(info);
            throw var17;
         } catch (SQLException var18) {
            this.closeDueToException(info);
            throw var18;
         } catch (Exception var19) {
            this.closeDueToException(info);
            throw new SQLNestedException("Cannot borrow connection from pool", var19);
         }

         if (null == password ? null != info.getPassword() : !password.equals(info.getPassword())) {
            try {
               this.testCPDS(username, password);
            } catch (SQLException var14) {
               this.closeDueToException(info);
               throw new SQLException("Given password did not match password used to create the PooledConnection.");
            } catch (NamingException var15) {
               throw (SQLException)new SQLException("NamingException encountered connecting to database").initCause(var15);
            }

            UserPassKey upkey = info.getUserPassKey();
            PooledConnectionManager manager = this.getConnectionManager(upkey);
            manager.invalidate(info.getPooledConnection());
            manager.setPassword(upkey.getPassword());
            info = null;

            for(int i = 0; i < 10; ++i) {
               try {
                  info = this.getPooledConnectionAndInfo(username, password);
               } catch (NoSuchElementException var10) {
                  this.closeDueToException(info);
                  throw new SQLNestedException("Cannot borrow connection from pool", var10);
               } catch (RuntimeException var11) {
                  this.closeDueToException(info);
                  throw var11;
               } catch (SQLException var12) {
                  this.closeDueToException(info);
                  throw var12;
               } catch (Exception var13) {
                  this.closeDueToException(info);
                  throw new SQLNestedException("Cannot borrow connection from pool", var13);
               }

               if (info != null && password.equals(info.getPassword())) {
                  break;
               }

               if (info != null) {
                  manager.invalidate(info.getPooledConnection());
               }

               info = null;
            }

            if (info == null) {
               throw new SQLException("Cannot borrow connection from pool - password change failure.");
            }
         }

         Connection con = info.getPooledConnection().getConnection();

         try {
            this.setupDefaults(con, username);
            con.clearWarnings();
            return con;
         } catch (SQLException var9) {
            try {
               con.close();
            } catch (Exception var8) {
               this.getLogWriter().println("ignoring exception during close: " + var8);
            }

            throw var9;
         }
      }
   }

   protected abstract PooledConnectionAndInfo getPooledConnectionAndInfo(String var1, String var2) throws SQLException;

   protected abstract void setupDefaults(Connection var1, String var2) throws SQLException;

   private void closeDueToException(PooledConnectionAndInfo info) {
      if (info != null) {
         try {
            info.getPooledConnection().getConnection().close();
         } catch (Exception var3) {
            this.getLogWriter().println("[ERROR] Could not return connection to pool during exception handling. " + var3.getMessage());
         }
      }
   }

   protected ConnectionPoolDataSource testCPDS(String username, String password) throws NamingException, SQLException {
      ConnectionPoolDataSource cpds = this.dataSource;
      if (cpds == null) {
         Context ctx = null;
         InitialContext var13;
         if (this.jndiEnvironment == null) {
            var13 = new InitialContext();
         } else {
            var13 = new InitialContext(this.jndiEnvironment);
         }

         Object ds = var13.lookup(this.dataSourceName);
         if (!(ds instanceof ConnectionPoolDataSource)) {
            throw new SQLException(
               "Illegal configuration: DataSource "
                  + this.dataSourceName
                  + " ("
                  + ds.getClass().getName()
                  + ")"
                  + " doesn't implement javax.sql.ConnectionPoolDataSource"
            );
         }

         cpds = (ConnectionPoolDataSource)ds;
      }

      PooledConnection conn = null;

      try {
         if (username != null) {
            conn = cpds.getPooledConnection(username, password);
         } else {
            conn = cpds.getPooledConnection();
         }

         if (conn == null) {
            throw new SQLException("Cannot connect using the supplied username/password");
         }
      } finally {
         if (conn != null) {
            try {
               conn.close();
            } catch (SQLException var11) {
            }
         }
      }

      return cpds;
   }

   protected byte whenExhaustedAction(int maxActive, int maxWait) {
      byte whenExhausted = 1;
      if (maxActive <= 0) {
         whenExhausted = 2;
      } else if (maxWait == 0) {
         whenExhausted = 0;
      }

      return whenExhausted;
   }

   @Override
   public Reference getReference() throws NamingException {
      String className = this.getClass().getName();
      String factoryName = className + "Factory";
      Reference ref = new Reference(className, factoryName, null);
      ref.add(new StringRefAddr("instanceKey", this.instanceKey));
      return ref;
   }
}
