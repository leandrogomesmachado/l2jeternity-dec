package org.apache.commons.dbcp.managed;

import java.sql.Connection;
import java.util.Collection;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingConnection;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;

public class PoolableManagedConnectionFactory extends PoolableConnectionFactory {
   private final TransactionRegistry transactionRegistry;

   public PoolableManagedConnectionFactory(
      XAConnectionFactory connFactory,
      ObjectPool pool,
      KeyedObjectPoolFactory stmtPoolFactory,
      String validationQuery,
      boolean defaultReadOnly,
      boolean defaultAutoCommit
   ) {
      super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit);
      this.transactionRegistry = connFactory.getTransactionRegistry();
   }

   public PoolableManagedConnectionFactory(
      XAConnectionFactory connFactory,
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
      super(
         connFactory,
         pool,
         stmtPoolFactory,
         validationQuery,
         validationQueryTimeout,
         connectionInitSqls,
         defaultReadOnly,
         defaultAutoCommit,
         defaultTransactionIsolation,
         defaultCatalog,
         config
      );
      this.transactionRegistry = connFactory.getTransactionRegistry();
   }

   @Override
   public synchronized Object makeObject() throws Exception {
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

         return new PoolableManagedConnection(this.transactionRegistry, conn, this._pool, this._config);
      }
   }
}
