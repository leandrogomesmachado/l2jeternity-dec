package org.apache.commons.dbcp.datasources;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import org.apache.commons.dbcp.SQLNestedException;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class PerUserPoolDataSource extends InstanceKeyDataSource {
   private static final long serialVersionUID = -3104731034410444060L;
   private int defaultMaxActive = 8;
   private int defaultMaxIdle = 8;
   private int defaultMaxWait = (int)Math.min(2147483647L, -1L);
   Map perUserDefaultAutoCommit = null;
   Map perUserDefaultTransactionIsolation = null;
   Map perUserMaxActive = null;
   Map perUserMaxIdle = null;
   Map perUserMaxWait = null;
   Map perUserDefaultReadOnly = null;
   private transient Map managers = new HashMap();

   @Override
   public void close() {
      Iterator poolIter = this.managers.values().iterator();

      while(poolIter.hasNext()) {
         try {
            ((CPDSConnectionFactory)poolIter.next()).getPool().close();
         } catch (Exception var3) {
         }
      }

      InstanceKeyObjectFactory.removeInstance(this.instanceKey);
   }

   public int getDefaultMaxActive() {
      return this.defaultMaxActive;
   }

   public void setDefaultMaxActive(int maxActive) {
      this.assertInitializationAllowed();
      this.defaultMaxActive = maxActive;
   }

   public int getDefaultMaxIdle() {
      return this.defaultMaxIdle;
   }

   public void setDefaultMaxIdle(int defaultMaxIdle) {
      this.assertInitializationAllowed();
      this.defaultMaxIdle = defaultMaxIdle;
   }

   public int getDefaultMaxWait() {
      return this.defaultMaxWait;
   }

   public void setDefaultMaxWait(int defaultMaxWait) {
      this.assertInitializationAllowed();
      this.defaultMaxWait = defaultMaxWait;
   }

   public Boolean getPerUserDefaultAutoCommit(String key) {
      Boolean value = null;
      if (this.perUserDefaultAutoCommit != null) {
         value = (Boolean)this.perUserDefaultAutoCommit.get(key);
      }

      return value;
   }

   public void setPerUserDefaultAutoCommit(String username, Boolean value) {
      this.assertInitializationAllowed();
      if (this.perUserDefaultAutoCommit == null) {
         this.perUserDefaultAutoCommit = new HashMap();
      }

      this.perUserDefaultAutoCommit.put(username, value);
   }

   public Integer getPerUserDefaultTransactionIsolation(String username) {
      Integer value = null;
      if (this.perUserDefaultTransactionIsolation != null) {
         value = (Integer)this.perUserDefaultTransactionIsolation.get(username);
      }

      return value;
   }

   public void setPerUserDefaultTransactionIsolation(String username, Integer value) {
      this.assertInitializationAllowed();
      if (this.perUserDefaultTransactionIsolation == null) {
         this.perUserDefaultTransactionIsolation = new HashMap();
      }

      this.perUserDefaultTransactionIsolation.put(username, value);
   }

   public Integer getPerUserMaxActive(String username) {
      Integer value = null;
      if (this.perUserMaxActive != null) {
         value = (Integer)this.perUserMaxActive.get(username);
      }

      return value;
   }

   public void setPerUserMaxActive(String username, Integer value) {
      this.assertInitializationAllowed();
      if (this.perUserMaxActive == null) {
         this.perUserMaxActive = new HashMap();
      }

      this.perUserMaxActive.put(username, value);
   }

   public Integer getPerUserMaxIdle(String username) {
      Integer value = null;
      if (this.perUserMaxIdle != null) {
         value = (Integer)this.perUserMaxIdle.get(username);
      }

      return value;
   }

   public void setPerUserMaxIdle(String username, Integer value) {
      this.assertInitializationAllowed();
      if (this.perUserMaxIdle == null) {
         this.perUserMaxIdle = new HashMap();
      }

      this.perUserMaxIdle.put(username, value);
   }

   public Integer getPerUserMaxWait(String username) {
      Integer value = null;
      if (this.perUserMaxWait != null) {
         value = (Integer)this.perUserMaxWait.get(username);
      }

      return value;
   }

   public void setPerUserMaxWait(String username, Integer value) {
      this.assertInitializationAllowed();
      if (this.perUserMaxWait == null) {
         this.perUserMaxWait = new HashMap();
      }

      this.perUserMaxWait.put(username, value);
   }

   public Boolean getPerUserDefaultReadOnly(String username) {
      Boolean value = null;
      if (this.perUserDefaultReadOnly != null) {
         value = (Boolean)this.perUserDefaultReadOnly.get(username);
      }

      return value;
   }

   public void setPerUserDefaultReadOnly(String username, Boolean value) {
      this.assertInitializationAllowed();
      if (this.perUserDefaultReadOnly == null) {
         this.perUserDefaultReadOnly = new HashMap();
      }

      this.perUserDefaultReadOnly.put(username, value);
   }

   public int getNumActive() {
      return this.getNumActive(null, null);
   }

   public int getNumActive(String username, String password) {
      ObjectPool pool = this.getPool(this.getPoolKey(username, password));
      return pool == null ? 0 : pool.getNumActive();
   }

   public int getNumIdle() {
      return this.getNumIdle(null, null);
   }

   public int getNumIdle(String username, String password) {
      ObjectPool pool = this.getPool(this.getPoolKey(username, password));
      return pool == null ? 0 : pool.getNumIdle();
   }

   @Override
   protected PooledConnectionAndInfo getPooledConnectionAndInfo(String username, String password) throws SQLException {
      PoolKey key = this.getPoolKey(username, password);
      ObjectPool pool;
      PooledConnectionManager manager;
      synchronized(this) {
         manager = (PooledConnectionManager)this.managers.get(key);
         if (manager == null) {
            try {
               this.registerPool(username, password);
               manager = (PooledConnectionManager)this.managers.get(key);
            } catch (NamingException var16) {
               throw new SQLNestedException("RegisterPool failed", var16);
            }
         }

         pool = ((CPDSConnectionFactory)manager).getPool();
      }

      PooledConnectionAndInfo info = null;

      try {
         info = (PooledConnectionAndInfo)pool.borrowObject();
      } catch (NoSuchElementException var14) {
         throw new SQLNestedException("Could not retrieve connection info from pool", var14);
      } catch (Exception var15) {
         try {
            this.testCPDS(username, password);
         } catch (Exception var13) {
            throw (SQLException)new SQLException("Could not retrieve connection info from pool").initCause(var13);
         }

         manager.closePool(username);
         synchronized(this) {
            this.managers.remove(key);
         }

         try {
            this.registerPool(username, password);
            pool = this.getPool(key);
         } catch (NamingException var11) {
            throw new SQLNestedException("RegisterPool failed", var11);
         }

         try {
            info = (PooledConnectionAndInfo)pool.borrowObject();
         } catch (Exception var10) {
            throw (SQLException)new SQLException("Could not retrieve connection info from pool").initCause(var10);
         }
      }

      return info;
   }

   @Override
   protected void setupDefaults(Connection con, String username) throws SQLException {
      boolean defaultAutoCommit = this.isDefaultAutoCommit();
      if (username != null) {
         Boolean userMax = this.getPerUserDefaultAutoCommit(username);
         if (userMax != null) {
            defaultAutoCommit = userMax;
         }
      }

      boolean defaultReadOnly = this.isDefaultReadOnly();
      if (username != null) {
         Boolean userMax = this.getPerUserDefaultReadOnly(username);
         if (userMax != null) {
            defaultReadOnly = userMax;
         }
      }

      int defaultTransactionIsolation = this.getDefaultTransactionIsolation();
      if (username != null) {
         Integer userMax = this.getPerUserDefaultTransactionIsolation(username);
         if (userMax != null) {
            defaultTransactionIsolation = userMax;
         }
      }

      if (con.getAutoCommit() != defaultAutoCommit) {
         con.setAutoCommit(defaultAutoCommit);
      }

      if (defaultTransactionIsolation != -1) {
         con.setTransactionIsolation(defaultTransactionIsolation);
      }

      if (con.isReadOnly() != defaultReadOnly) {
         con.setReadOnly(defaultReadOnly);
      }
   }

   @Override
   protected PooledConnectionManager getConnectionManager(UserPassKey upkey) {
      return (PooledConnectionManager)this.managers.get(this.getPoolKey(upkey.getUsername(), upkey.getPassword()));
   }

   @Override
   public Reference getReference() throws NamingException {
      Reference ref = new Reference(this.getClass().getName(), PerUserPoolDataSourceFactory.class.getName(), null);
      ref.add(new StringRefAddr("instanceKey", this.instanceKey));
      return ref;
   }

   private PoolKey getPoolKey(String username, String password) {
      return new PoolKey(this.getDataSourceName(), username);
   }

   private synchronized void registerPool(String username, String password) throws NamingException, SQLException {
      ConnectionPoolDataSource cpds = this.testCPDS(username, password);
      Integer userMax = this.getPerUserMaxActive(username);
      int maxActive = userMax == null ? this.getDefaultMaxActive() : userMax;
      userMax = this.getPerUserMaxIdle(username);
      int maxIdle = userMax == null ? this.getDefaultMaxIdle() : userMax;
      userMax = this.getPerUserMaxWait(username);
      int maxWait = userMax == null ? this.getDefaultMaxWait() : userMax;
      GenericObjectPool pool = new GenericObjectPool(null);
      pool.setMaxActive(maxActive);
      pool.setMaxIdle(maxIdle);
      pool.setMaxWait((long)maxWait);
      pool.setWhenExhaustedAction(this.whenExhaustedAction(maxActive, maxWait));
      pool.setTestOnBorrow(this.getTestOnBorrow());
      pool.setTestOnReturn(this.getTestOnReturn());
      pool.setTimeBetweenEvictionRunsMillis((long)this.getTimeBetweenEvictionRunsMillis());
      pool.setNumTestsPerEvictionRun(this.getNumTestsPerEvictionRun());
      pool.setMinEvictableIdleTimeMillis((long)this.getMinEvictableIdleTimeMillis());
      pool.setTestWhileIdle(this.getTestWhileIdle());
      CPDSConnectionFactory factory = new CPDSConnectionFactory(cpds, pool, this.getValidationQuery(), this.isRollbackAfterValidation(), username, password);
      Object old = this.managers.put(this.getPoolKey(username, password), factory);
      if (old != null) {
         throw new IllegalStateException("Pool already contains an entry for this user/password: " + username);
      }
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      try {
         in.defaultReadObject();
         PerUserPoolDataSource oldDS = (PerUserPoolDataSource)new PerUserPoolDataSourceFactory().getObjectInstance(this.getReference(), null, null, null);
         this.managers = oldDS.managers;
      } catch (NamingException var3) {
         throw new IOException("NamingException: " + var3);
      }
   }

   private GenericObjectPool getPool(PoolKey key) {
      CPDSConnectionFactory mgr = (CPDSConnectionFactory)this.managers.get(key);
      return mgr == null ? null : (GenericObjectPool)mgr.getPool();
   }
}
