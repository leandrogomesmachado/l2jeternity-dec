package org.apache.commons.dbcp.managed;

import java.sql.SQLException;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;

public class BasicManagedDataSource extends BasicDataSource {
   private TransactionRegistry transactionRegistry;
   private transient TransactionManager transactionManager;
   private String xaDataSource;
   private XADataSource xaDataSourceInstance;

   public synchronized XADataSource getXaDataSourceInstance() {
      return this.xaDataSourceInstance;
   }

   public synchronized void setXaDataSourceInstance(XADataSource xaDataSourceInstance) {
      this.xaDataSourceInstance = xaDataSourceInstance;
      this.xaDataSource = xaDataSourceInstance == null ? null : xaDataSourceInstance.getClass().getName();
   }

   public TransactionManager getTransactionManager() {
      return this.transactionManager;
   }

   protected synchronized TransactionRegistry getTransactionRegistry() {
      return this.transactionRegistry;
   }

   public void setTransactionManager(TransactionManager transactionManager) {
      this.transactionManager = transactionManager;
   }

   public synchronized String getXADataSource() {
      return this.xaDataSource;
   }

   public synchronized void setXADataSource(String xaDataSource) {
      this.xaDataSource = xaDataSource;
   }

   @Override
   protected ConnectionFactory createConnectionFactory() throws SQLException {
      if (this.transactionManager == null) {
         throw new SQLException("Transaction manager must be set before a connection can be created");
      } else if (this.xaDataSource == null) {
         ConnectionFactory connectionFactory = super.createConnectionFactory();
         XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(this.getTransactionManager(), connectionFactory);
         this.transactionRegistry = xaConnectionFactory.getTransactionRegistry();
         return xaConnectionFactory;
      } else {
         if (this.xaDataSourceInstance == null) {
            Class xaDataSourceClass = null;

            try {
               xaDataSourceClass = Class.forName(this.xaDataSource);
            } catch (Throwable var5) {
               String message = "Cannot load XA data source class '" + this.xaDataSource + "'";
               throw (SQLException)new SQLException(message).initCause(var5);
            }

            try {
               this.xaDataSourceInstance = (XADataSource)xaDataSourceClass.newInstance();
            } catch (Throwable var4) {
               String message = "Cannot create XA data source of class '" + this.xaDataSource + "'";
               throw (SQLException)new SQLException(message).initCause(var4);
            }
         }

         XAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(
            this.getTransactionManager(), this.xaDataSourceInstance, this.username, this.password
         );
         this.transactionRegistry = xaConnectionFactory.getTransactionRegistry();
         return xaConnectionFactory;
      }
   }

   @Override
   protected void createDataSourceInstance() throws SQLException {
      PoolingDataSource pds = new ManagedDataSource(this.connectionPool, this.transactionRegistry);
      pds.setAccessToUnderlyingConnectionAllowed(this.isAccessToUnderlyingConnectionAllowed());
      pds.setLogWriter(this.logWriter);
      this.dataSource = pds;
   }

   @Override
   protected void createPoolableConnectionFactory(
      ConnectionFactory driverConnectionFactory, KeyedObjectPoolFactory statementPoolFactory, AbandonedConfig abandonedConfig
   ) throws SQLException {
      PoolableConnectionFactory connectionFactory = null;

      try {
         PoolableConnectionFactory var8 = new PoolableManagedConnectionFactory(
            (XAConnectionFactory)driverConnectionFactory,
            this.connectionPool,
            statementPoolFactory,
            this.validationQuery,
            this.validationQueryTimeout,
            this.connectionInitSqls,
            this.defaultReadOnly,
            this.defaultAutoCommit,
            this.defaultTransactionIsolation,
            this.defaultCatalog,
            abandonedConfig
         );
         validateConnectionFactory(var8);
      } catch (RuntimeException var6) {
         throw var6;
      } catch (Exception var7) {
         throw (SQLException)new SQLException("Cannot create PoolableConnectionFactory (" + var7.getMessage() + ")").initCause(var7);
      }
   }
}
