package org.apache.commons.dbcp.managed;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.pool.ObjectPool;

public class PoolableManagedConnection extends PoolableConnection {
   private final TransactionRegistry transactionRegistry;

   public PoolableManagedConnection(TransactionRegistry transactionRegistry, Connection conn, ObjectPool pool, AbandonedConfig config) {
      super(conn, pool, config);
      this.transactionRegistry = transactionRegistry;
   }

   public PoolableManagedConnection(TransactionRegistry transactionRegistry, Connection conn, ObjectPool pool) {
      super(conn, pool);
      this.transactionRegistry = transactionRegistry;
   }

   @Override
   public void reallyClose() throws SQLException {
      try {
         super.reallyClose();
      } finally {
         this.transactionRegistry.unregisterConnection(this);
      }
   }
}
