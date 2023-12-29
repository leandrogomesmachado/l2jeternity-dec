package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

public class PoolableConnectionFactory implements PoolableObjectFactory {
   protected volatile ConnectionFactory _connFactory = null;
   protected volatile String _validationQuery = null;
   protected volatile int _validationQueryTimeout = -1;
   protected Collection _connectionInitSqls = null;
   protected volatile ObjectPool _pool = null;
   protected volatile KeyedObjectPoolFactory _stmtPoolFactory = null;
   protected Boolean _defaultReadOnly = null;
   protected boolean _defaultAutoCommit = true;
   protected int _defaultTransactionIsolation = -1;
   protected String _defaultCatalog;
   protected AbandonedConfig _config = null;
   static final int UNKNOWN_TRANSACTIONISOLATION = -1;

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      boolean defaultReadOnly,
      boolean defaultAutoCommit
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      Collection connectionInitSqls,
      boolean defaultReadOnly,
      boolean defaultAutoCommit
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._connectionInitSqls = connectionInitSqls;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      int validationQueryTimeout,
      boolean defaultReadOnly,
      boolean defaultAutoCommit
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._validationQueryTimeout = validationQueryTimeout;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      int validationQueryTimeout,
      Collection connectionInitSqls,
      boolean defaultReadOnly,
      boolean defaultAutoCommit
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._validationQueryTimeout = validationQueryTimeout;
      this._connectionInitSqls = connectionInitSqls;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      Collection connectionInitSqls,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._connectionInitSqls = connectionInitSqls;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      int validationQueryTimeout,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._validationQueryTimeout = validationQueryTimeout;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      int validationQueryTimeout,
      Collection connectionInitSqls,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._validationQueryTimeout = validationQueryTimeout;
      this._connectionInitSqls = connectionInitSqls;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation,
      String defaultCatalog,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
      this._defaultCatalog = defaultCatalog;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      Boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation,
      String defaultCatalog,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._defaultReadOnly = defaultReadOnly;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
      this._defaultCatalog = defaultCatalog;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      Collection connectionInitSqls,
      Boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation,
      String defaultCatalog,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._connectionInitSqls = connectionInitSqls;
      this._defaultReadOnly = defaultReadOnly;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
      this._defaultCatalog = defaultCatalog;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      int validationQueryTimeout,
      Boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation,
      String defaultCatalog,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._validationQueryTimeout = validationQueryTimeout;
      this._defaultReadOnly = defaultReadOnly;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
      this._defaultCatalog = defaultCatalog;
   }

   public PoolableConnectionFactory(
      ConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      int validationQueryTimeout,
      Collection connectionInitSqls,
      Boolean defaultReadOnly,
      boolean defaultAutoCommit,
      int defaultTransactionIsolation,
      String defaultCatalog,
      AbandonedConfig config
   ) {
      this._connFactory = connFactory;
      this._pool = pool;
      this._config = config;
      this._pool.setFactory(this);
      this._stmtPoolFactory = stmtPoolFactory;
      this._validationQuery = validationQuery;
      this._validationQueryTimeout = validationQueryTimeout;
      this._connectionInitSqls = connectionInitSqls;
      this._defaultReadOnly = defaultReadOnly;
      this._defaultAutoCommit = defaultAutoCommit;
      this._defaultTransactionIsolation = defaultTransactionIsolation;
      this._defaultCatalog = defaultCatalog;
   }

   public void setConnectionFactory(ConnectionFactory connFactory) {
      this._connFactory = connFactory;
   }

   public void setValidationQuery(String validationQuery) {
      this._validationQuery = validationQuery;
   }

   public void setValidationQueryTimeout(int timeout) {
      this._validationQueryTimeout = timeout;
   }

   public synchronized void setConnectionInitSql(Collection connectionInitSqls) {
      this._connectionInitSqls = connectionInitSqls;
   }

   public synchronized void setPool(ObjectPool pool) {
      if (null != this._pool && pool != this._pool) {
         try {
            this._pool.close();
         } catch (Exception var3) {
         }
      }

      this._pool = pool;
   }

   public synchronized ObjectPool getPool() {
      return this._pool;
   }

   public void setStatementPoolFactory(KeyedObjectPoolFactory stmtPoolFactory) {
      this._stmtPoolFactory = stmtPoolFactory;
   }

   public void setDefaultReadOnly(boolean defaultReadOnly) {
      this._defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
   }

   public void setDefaultAutoCommit(boolean defaultAutoCommit) {
      this._defaultAutoCommit = defaultAutoCommit;
   }

   public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
      this._defaultTransactionIsolation = defaultTransactionIsolation;
   }

   public void setDefaultCatalog(String defaultCatalog) {
      this._defaultCatalog = defaultCatalog;
   }

   @Override
   public Object makeObject() throws Exception {
      Connection conn = this._connFactory.createConnection();
      if (conn == null) {
         throw new IllegalStateException("Connection factory returned null from createConnection");
      } else {
         this.initializeConnection(conn);
         if (null != this._stmtPoolFactory) {
            KeyedObjectPool stmtpool = this._stmtPoolFactory.createPool();
            conn = new PoolingConnection(conn, stmtpool);
            stmtpool.setFactory((PoolingConnection)conn);
         }

         return new PoolableConnection(conn, this._pool, this._config);
      }
   }

   protected void initializeConnection(Connection conn) throws SQLException {
      Collection sqls = this._connectionInitSqls;
      if (conn.isClosed()) {
         throw new SQLException("initializeConnection: connection closed");
      } else {
         if (null != sqls) {
            Statement stmt = null;

            try {
               stmt = conn.createStatement();

               for(Object o : sqls) {
                  if (o == null) {
                     throw new NullPointerException("null connectionInitSqls element");
                  }

                  String sql = o.toString();
                  stmt.execute(sql);
               }
            } finally {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Exception var12) {
                  }
               }
            }
         }
      }
   }

   @Override
   public void destroyObject(Object obj) throws Exception {
      if (obj instanceof PoolableConnection) {
         ((PoolableConnection)obj).reallyClose();
      }
   }

   @Override
   public boolean validateObject(Object obj) {
      if (obj instanceof Connection) {
         try {
            this.validateConnection((Connection)obj);
            return true;
         } catch (Exception var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public void validateConnection(Connection conn) throws SQLException {
      String query = this._validationQuery;
      if (conn.isClosed()) {
         throw new SQLException("validateConnection: connection closed");
      } else {
         if (null != query) {
            Statement stmt = null;
            ResultSet rset = null;

            try {
               stmt = conn.createStatement();
               if (this._validationQueryTimeout > 0) {
                  stmt.setQueryTimeout(this._validationQueryTimeout);
               }

               rset = stmt.executeQuery(query);
               if (!rset.next()) {
                  throw new SQLException("validationQuery didn't return a row");
               }
            } finally {
               if (rset != null) {
                  try {
                     rset.close();
                  } catch (Exception var14) {
                  }
               }

               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Exception var13) {
                  }
               }
            }
         }
      }
   }

   @Override
   public void passivateObject(Object obj) throws Exception {
      if (obj instanceof Connection) {
         Connection conn = (Connection)obj;
         if (!conn.getAutoCommit() && !conn.isReadOnly()) {
            conn.rollback();
         }

         conn.clearWarnings();
         if (!conn.getAutoCommit()) {
            conn.setAutoCommit(true);
         }
      }

      if (obj instanceof DelegatingConnection) {
         ((DelegatingConnection)obj).passivate();
      }
   }

   @Override
   public void activateObject(Object obj) throws Exception {
      if (obj instanceof DelegatingConnection) {
         ((DelegatingConnection)obj).activate();
      }

      if (obj instanceof Connection) {
         Connection conn = (Connection)obj;
         if (conn.getAutoCommit() != this._defaultAutoCommit) {
            conn.setAutoCommit(this._defaultAutoCommit);
         }

         if (this._defaultTransactionIsolation != -1 && conn.getTransactionIsolation() != this._defaultTransactionIsolation) {
            conn.setTransactionIsolation(this._defaultTransactionIsolation);
         }

         if (this._defaultReadOnly != null && conn.isReadOnly() != this._defaultReadOnly) {
            conn.setReadOnly(this._defaultReadOnly);
         }

         if (this._defaultCatalog != null && !this._defaultCatalog.equals(conn.getCatalog())) {
            conn.setCatalog(this._defaultCatalog);
         }
      }
   }
}
