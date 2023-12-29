package org.apache.commons.dbcp.managed;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class TransactionContext {
   private final TransactionRegistry transactionRegistry;
   private final WeakReference transactionRef;
   private Connection sharedConnection;

   public TransactionContext(TransactionRegistry transactionRegistry, Transaction transaction) {
      if (transactionRegistry == null) {
         throw new NullPointerException("transactionRegistry is null");
      } else if (transaction == null) {
         throw new NullPointerException("transaction is null");
      } else {
         this.transactionRegistry = transactionRegistry;
         this.transactionRef = new WeakReference(transaction);
      }
   }

   public Connection getSharedConnection() {
      return this.sharedConnection;
   }

   public void setSharedConnection(Connection sharedConnection) throws SQLException {
      if (this.sharedConnection != null) {
         throw new IllegalStateException("A shared connection is alredy set");
      } else {
         Transaction transaction = this.getTransaction();

         try {
            XAResource xaResource = this.transactionRegistry.getXAResource(sharedConnection);
            transaction.enlistResource(xaResource);
         } catch (RollbackException var4) {
         } catch (SystemException var5) {
            throw (SQLException)new SQLException("Unable to enlist connection the transaction").initCause(var5);
         }

         this.sharedConnection = sharedConnection;
      }
   }

   public void addTransactionContextListener(final TransactionContextListener listener) throws SQLException {
      try {
         this.getTransaction().registerSynchronization(new Synchronization() {
            public void beforeCompletion() {
            }

            public void afterCompletion(int status) {
               listener.afterCompletion(TransactionContext.this, status == 3);
            }
         });
      } catch (RollbackException var3) {
      } catch (Exception var4) {
         throw (SQLException)new SQLException("Unable to register transaction context listener").initCause(var4);
      }
   }

   public boolean isActive() throws SQLException {
      try {
         Transaction transaction = (Transaction)this.transactionRef.get();
         if (transaction == null) {
            return false;
         } else {
            int status = transaction.getStatus();
            return status == 0 || status == 1;
         }
      } catch (SystemException var3) {
         throw (SQLException)new SQLException("Unable to get transaction status").initCause(var3);
      }
   }

   private Transaction getTransaction() throws SQLException {
      Transaction transaction = (Transaction)this.transactionRef.get();
      if (transaction == null) {
         throw new SQLException("Unable to enlist connection because the transaction has been garbage collected");
      } else {
         return transaction;
      }
   }
}
