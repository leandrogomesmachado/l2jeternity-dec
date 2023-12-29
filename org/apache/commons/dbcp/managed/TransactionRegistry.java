package org.apache.commons.dbcp.managed;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public class TransactionRegistry {
   private final TransactionManager transactionManager;
   private final Map caches = new WeakHashMap();
   private final Map xaResources = new WeakHashMap();

   public TransactionRegistry(TransactionManager transactionManager) {
      this.transactionManager = transactionManager;
   }

   public synchronized void registerConnection(Connection connection, XAResource xaResource) {
      if (connection == null) {
         throw new NullPointerException("connection is null");
      } else if (xaResource == null) {
         throw new NullPointerException("xaResource is null");
      } else {
         this.xaResources.put(connection, xaResource);
      }
   }

   public synchronized XAResource getXAResource(Connection connection) throws SQLException {
      if (connection == null) {
         throw new NullPointerException("connection is null");
      } else {
         XAResource xaResource = (XAResource)this.xaResources.get(connection);
         if (xaResource == null) {
            throw new SQLException("Connection does not have a registered XAResource " + connection);
         } else {
            return xaResource;
         }
      }
   }

   public TransactionContext getActiveTransactionContext() throws SQLException {
      Transaction transaction = null;

      try {
         transaction = this.transactionManager.getTransaction();
         if (transaction == null) {
            return null;
         }

         int status = transaction.getStatus();
         if (status != 0 && status != 1) {
            return null;
         }
      } catch (SystemException var6) {
         throw (SQLException)new SQLException("Unable to determine current transaction ").initCause(var6);
      }

      synchronized(this) {
         TransactionContext cache = (TransactionContext)this.caches.get(transaction);
         if (cache == null) {
            cache = new TransactionContext(this, transaction);
            this.caches.put(transaction, cache);
         }

         return cache;
      }
   }

   public synchronized void unregisterConnection(Connection connection) {
      this.xaResources.remove(connection);
   }
}
