package org.apache.commons.dbcp.managed;

import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.apache.commons.dbcp.ConnectionFactory;

public class LocalXAConnectionFactory implements XAConnectionFactory {
   protected TransactionRegistry transactionRegistry;
   protected ConnectionFactory connectionFactory;

   public LocalXAConnectionFactory(TransactionManager transactionManager, ConnectionFactory connectionFactory) {
      if (transactionManager == null) {
         throw new NullPointerException("transactionManager is null");
      } else if (connectionFactory == null) {
         throw new NullPointerException("connectionFactory is null");
      } else {
         this.transactionRegistry = new TransactionRegistry(transactionManager);
         this.connectionFactory = connectionFactory;
      }
   }

   @Override
   public TransactionRegistry getTransactionRegistry() {
      return this.transactionRegistry;
   }

   @Override
   public Connection createConnection() throws SQLException {
      Connection connection = this.connectionFactory.createConnection();
      XAResource xaResource = new LocalXAConnectionFactory.LocalXAResource(connection);
      this.transactionRegistry.registerConnection(connection, xaResource);
      return connection;
   }

   protected static class LocalXAResource implements XAResource {
      private final Connection connection;
      private Xid currentXid;
      private boolean originalAutoCommit;

      public LocalXAResource(Connection localTransaction) {
         this.connection = localTransaction;
      }

      public synchronized Xid getXid() {
         return this.currentXid;
      }

      @Override
      public synchronized void start(Xid xid, int flag) throws XAException {
         if (flag == 0) {
            if (this.currentXid != null) {
               throw new XAException("Already enlisted in another transaction with xid " + xid);
            }

            try {
               this.originalAutoCommit = this.connection.getAutoCommit();
            } catch (SQLException var5) {
               this.originalAutoCommit = true;
            }

            try {
               this.connection.setAutoCommit(false);
            } catch (SQLException var4) {
               throw (XAException)new XAException("Count not turn off auto commit for a XA transaction").initCause(var4);
            }

            this.currentXid = xid;
         } else {
            if (flag != 134217728) {
               throw new XAException("Unknown start flag " + flag);
            }

            if (xid != this.currentXid) {
               throw new XAException("Attempting to resume in different transaction: expected " + this.currentXid + ", but was " + xid);
            }
         }
      }

      @Override
      public synchronized void end(Xid xid, int flag) throws XAException {
         if (xid == null) {
            throw new NullPointerException("xid is null");
         } else if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
         }
      }

      @Override
      public synchronized int prepare(Xid xid) {
         try {
            if (this.connection.isReadOnly()) {
               this.connection.setAutoCommit(this.originalAutoCommit);
               return 3;
            }
         } catch (SQLException var3) {
         }

         return 0;
      }

      @Override
      public synchronized void commit(Xid xid, boolean flag) throws XAException {
         if (xid == null) {
            throw new NullPointerException("xid is null");
         } else if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
         } else {
            try {
               if (this.connection.isClosed()) {
                  throw new XAException("Conection is closed");
               }

               if (!this.connection.isReadOnly()) {
                  this.connection.commit();
               }
            } catch (SQLException var11) {
               throw (XAException)new XAException().initCause(var11);
            } finally {
               try {
                  this.connection.setAutoCommit(this.originalAutoCommit);
               } catch (SQLException var10) {
               }

               this.currentXid = null;
            }
         }
      }

      @Override
      public synchronized void rollback(Xid xid) throws XAException {
         if (xid == null) {
            throw new NullPointerException("xid is null");
         } else if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
         } else {
            try {
               this.connection.rollback();
            } catch (SQLException var10) {
               throw (XAException)new XAException().initCause(var10);
            } finally {
               try {
                  this.connection.setAutoCommit(this.originalAutoCommit);
               } catch (SQLException var9) {
               }

               this.currentXid = null;
            }
         }
      }

      @Override
      public boolean isSameRM(XAResource xaResource) {
         return this == xaResource;
      }

      @Override
      public synchronized void forget(Xid xid) {
         if (xid != null && this.currentXid.equals(xid)) {
            this.currentXid = null;
         }
      }

      @Override
      public Xid[] recover(int flag) {
         return new Xid[0];
      }

      @Override
      public int getTransactionTimeout() {
         return 0;
      }

      @Override
      public boolean setTransactionTimeout(int transactionTimeout) {
         return false;
      }
   }
}
