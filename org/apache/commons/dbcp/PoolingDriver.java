package org.apache.commons.dbcp;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.jocl.JOCLContentHandler;
import org.apache.commons.pool.ObjectPool;
import org.xml.sax.SAXException;

public class PoolingDriver implements Driver {
   protected static final HashMap _pools;
   private static boolean accessToUnderlyingConnectionAllowed;
   protected static final String URL_PREFIX = "jdbc:apache:commons:dbcp:";
   protected static final int URL_PREFIX_LEN;
   protected static final int MAJOR_VERSION = 1;
   protected static final int MINOR_VERSION = 0;

   public static synchronized boolean isAccessToUnderlyingConnectionAllowed() {
      return accessToUnderlyingConnectionAllowed;
   }

   public static synchronized void setAccessToUnderlyingConnectionAllowed(boolean allow) {
      accessToUnderlyingConnectionAllowed = allow;
   }

   /** @deprecated */
   public synchronized ObjectPool getPool(String name) {
      try {
         return this.getConnectionPool(name);
      } catch (Exception var3) {
         throw new DbcpException(var3);
      }
   }

   public synchronized ObjectPool getConnectionPool(String name) throws SQLException {
      ObjectPool pool = (ObjectPool)_pools.get(name);
      if (null == pool) {
         InputStream in = this.getClass().getResourceAsStream(name + ".jocl");
         if (in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name + ".jocl");
         }

         if (null == in) {
            throw new SQLException("Configuration file not found");
         }

         JOCLContentHandler jocl = null;

         try {
            jocl = JOCLContentHandler.parse(in);
         } catch (SAXException var6) {
            throw (SQLException)new SQLException("Could not parse configuration file").initCause(var6);
         } catch (IOException var7) {
            throw (SQLException)new SQLException("Could not load configuration file").initCause(var7);
         }

         if (jocl.getType(0).equals(String.class)) {
            pool = this.getPool((String)jocl.getValue(0));
            if (null != pool) {
               this.registerPool(name, pool);
            }
         } else {
            pool = ((PoolableConnectionFactory)jocl.getValue(0)).getPool();
            if (null != pool) {
               this.registerPool(name, pool);
            }
         }
      }

      return pool;
   }

   public synchronized void registerPool(String name, ObjectPool pool) {
      _pools.put(name, pool);
   }

   public synchronized void closePool(String name) throws SQLException {
      ObjectPool pool = (ObjectPool)_pools.get(name);
      if (pool != null) {
         _pools.remove(name);

         try {
            pool.close();
         } catch (Exception var4) {
            throw (SQLException)new SQLException("Error closing pool " + name).initCause(var4);
         }
      }
   }

   public synchronized String[] getPoolNames() {
      Set names = _pools.keySet();
      return names.toArray(new String[names.size()]);
   }

   @Override
   public boolean acceptsURL(String url) throws SQLException {
      try {
         return url.startsWith("jdbc:apache:commons:dbcp:");
      } catch (NullPointerException var3) {
         return false;
      }
   }

   @Override
   public Connection connect(String url, Properties info) throws SQLException {
      if (this.acceptsURL(url)) {
         ObjectPool pool = this.getConnectionPool(url.substring(URL_PREFIX_LEN));
         if (null == pool) {
            throw new SQLException("No pool found for " + url + ".");
         } else {
            try {
               Connection conn = (Connection)pool.borrowObject();
               if (conn != null) {
                  conn = new PoolingDriver.PoolGuardConnectionWrapper(pool, conn);
               }

               return conn;
            } catch (SQLException var5) {
               throw var5;
            } catch (NoSuchElementException var6) {
               throw (SQLException)new SQLException("Cannot get a connection, pool error: " + var6.getMessage()).initCause(var6);
            } catch (RuntimeException var7) {
               throw var7;
            } catch (Exception var8) {
               throw (SQLException)new SQLException("Cannot get a connection, general error: " + var8.getMessage()).initCause(var8);
            }
         }
      } else {
         return null;
      }
   }

   public void invalidateConnection(Connection conn) throws SQLException {
      if (conn instanceof PoolingDriver.PoolGuardConnectionWrapper) {
         PoolingDriver.PoolGuardConnectionWrapper pgconn = (PoolingDriver.PoolGuardConnectionWrapper)conn;
         ObjectPool pool = pgconn.pool;
         Connection delegate = pgconn.delegate;

         try {
            pool.invalidateObject(delegate);
         } catch (Exception var6) {
         }

         pgconn.delegate = null;
      } else {
         throw new SQLException("Invalid connection class");
      }
   }

   @Override
   public int getMajorVersion() {
      return 1;
   }

   @Override
   public int getMinorVersion() {
      return 0;
   }

   @Override
   public boolean jdbcCompliant() {
      return true;
   }

   @Override
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
      return new DriverPropertyInfo[0];
   }

   static {
      try {
         DriverManager.registerDriver(new PoolingDriver());
      } catch (Exception var1) {
      }

      _pools = new HashMap();
      accessToUnderlyingConnectionAllowed = false;
      URL_PREFIX_LEN = "jdbc:apache:commons:dbcp:".length();
   }

   private static class PoolGuardConnectionWrapper extends DelegatingConnection {
      private final ObjectPool pool;
      private Connection delegate;

      PoolGuardConnectionWrapper(ObjectPool pool, Connection delegate) {
         super(delegate);
         this.pool = pool;
         this.delegate = delegate;
      }

      @Override
      protected void checkOpen() throws SQLException {
         if (this.delegate == null) {
            throw new SQLException("Connection is closed.");
         }
      }

      @Override
      public void close() throws SQLException {
         if (this.delegate != null) {
            this.delegate.close();
            this.delegate = null;
            super.setDelegate(null);
         }
      }

      @Override
      public boolean isClosed() throws SQLException {
         return this.delegate == null ? true : this.delegate.isClosed();
      }

      @Override
      public void clearWarnings() throws SQLException {
         this.checkOpen();
         this.delegate.clearWarnings();
      }

      @Override
      public void commit() throws SQLException {
         this.checkOpen();
         this.delegate.commit();
      }

      @Override
      public Statement createStatement() throws SQLException {
         this.checkOpen();
         return new DelegatingStatement(this, this.delegate.createStatement());
      }

      @Override
      public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
         this.checkOpen();
         return new DelegatingStatement(this, this.delegate.createStatement(resultSetType, resultSetConcurrency));
      }

      @Override
      public boolean equals(Object obj) {
         return this.delegate == null ? false : this.delegate.equals(obj);
      }

      @Override
      public boolean getAutoCommit() throws SQLException {
         this.checkOpen();
         return this.delegate.getAutoCommit();
      }

      @Override
      public String getCatalog() throws SQLException {
         this.checkOpen();
         return this.delegate.getCatalog();
      }

      @Override
      public DatabaseMetaData getMetaData() throws SQLException {
         this.checkOpen();
         return this.delegate.getMetaData();
      }

      @Override
      public int getTransactionIsolation() throws SQLException {
         this.checkOpen();
         return this.delegate.getTransactionIsolation();
      }

      @Override
      public Map getTypeMap() throws SQLException {
         this.checkOpen();
         return this.delegate.getTypeMap();
      }

      @Override
      public SQLWarning getWarnings() throws SQLException {
         this.checkOpen();
         return this.delegate.getWarnings();
      }

      @Override
      public int hashCode() {
         return this.delegate == null ? 0 : this.delegate.hashCode();
      }

      @Override
      public boolean isReadOnly() throws SQLException {
         this.checkOpen();
         return this.delegate.isReadOnly();
      }

      @Override
      public String nativeSQL(String sql) throws SQLException {
         this.checkOpen();
         return this.delegate.nativeSQL(sql);
      }

      @Override
      public CallableStatement prepareCall(String sql) throws SQLException {
         this.checkOpen();
         return new DelegatingCallableStatement(this, this.delegate.prepareCall(sql));
      }

      @Override
      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
         this.checkOpen();
         return new DelegatingCallableStatement(this, this.delegate.prepareCall(sql, resultSetType, resultSetConcurrency));
      }

      @Override
      public PreparedStatement prepareStatement(String sql) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, resultSetType, resultSetConcurrency));
      }

      @Override
      public void rollback() throws SQLException {
         this.checkOpen();
         this.delegate.rollback();
      }

      @Override
      public void setAutoCommit(boolean autoCommit) throws SQLException {
         this.checkOpen();
         this.delegate.setAutoCommit(autoCommit);
      }

      @Override
      public void setCatalog(String catalog) throws SQLException {
         this.checkOpen();
         this.delegate.setCatalog(catalog);
      }

      @Override
      public void setReadOnly(boolean readOnly) throws SQLException {
         this.checkOpen();
         this.delegate.setReadOnly(readOnly);
      }

      @Override
      public void setTransactionIsolation(int level) throws SQLException {
         this.checkOpen();
         this.delegate.setTransactionIsolation(level);
      }

      @Override
      public void setTypeMap(Map map) throws SQLException {
         this.checkOpen();
         this.delegate.setTypeMap(map);
      }

      @Override
      public String toString() {
         return this.delegate == null ? "NULL" : this.delegate.toString();
      }

      @Override
      public int getHoldability() throws SQLException {
         this.checkOpen();
         return this.delegate.getHoldability();
      }

      @Override
      public void setHoldability(int holdability) throws SQLException {
         this.checkOpen();
         this.delegate.setHoldability(holdability);
      }

      @Override
      public Savepoint setSavepoint() throws SQLException {
         this.checkOpen();
         return this.delegate.setSavepoint();
      }

      @Override
      public Savepoint setSavepoint(String name) throws SQLException {
         this.checkOpen();
         return this.delegate.setSavepoint(name);
      }

      @Override
      public void releaseSavepoint(Savepoint savepoint) throws SQLException {
         this.checkOpen();
         this.delegate.releaseSavepoint(savepoint);
      }

      @Override
      public void rollback(Savepoint savepoint) throws SQLException {
         this.checkOpen();
         this.delegate.rollback(savepoint);
      }

      @Override
      public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
         this.checkOpen();
         return new DelegatingStatement(this, this.delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
      }

      @Override
      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
         this.checkOpen();
         return new DelegatingCallableStatement(this, this.delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, autoGeneratedKeys));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, columnIndexes));
      }

      @Override
      public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
         this.checkOpen();
         return new DelegatingPreparedStatement(this, this.delegate.prepareStatement(sql, columnNames));
      }

      @Override
      public Connection getDelegate() {
         return PoolingDriver.isAccessToUnderlyingConnectionAllowed() ? super.getDelegate() : null;
      }

      @Override
      public Connection getInnermostDelegate() {
         return PoolingDriver.isAccessToUnderlyingConnectionAllowed() ? super.getInnermostDelegate() : null;
      }
   }
}
