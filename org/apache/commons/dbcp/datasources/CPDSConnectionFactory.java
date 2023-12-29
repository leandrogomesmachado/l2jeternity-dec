package org.apache.commons.dbcp.datasources;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

class CPDSConnectionFactory implements PoolableObjectFactory, ConnectionEventListener, PooledConnectionManager {
   private static final String NO_KEY_MESSAGE = "close() was called on a Connection, but I have no record of the underlying PooledConnection.";
   private final ConnectionPoolDataSource _cpds;
   private final String _validationQuery;
   private final boolean _rollbackAfterValidation;
   private final ObjectPool _pool;
   private String _username = null;
   private String _password = null;
   private final Map validatingMap = new HashMap();
   private final WeakHashMap pcMap = new WeakHashMap();

   public CPDSConnectionFactory(ConnectionPoolDataSource cpds, ObjectPool pool, String validationQuery, String username, String password) {
      this(cpds, pool, validationQuery, false, username, password);
   }

   public CPDSConnectionFactory(
      ConnectionPoolDataSource cpds, ObjectPool pool, String validationQuery, boolean rollbackAfterValidation, String username, String password
   ) {
      this._cpds = cpds;
      this._pool = pool;
      pool.setFactory(this);
      this._validationQuery = validationQuery;
      this._username = username;
      this._password = password;
      this._rollbackAfterValidation = rollbackAfterValidation;
   }

   public ObjectPool getPool() {
      return this._pool;
   }

   @Override
   public synchronized Object makeObject() {
      try {
         PooledConnection pc = null;
         if (this._username == null) {
            pc = this._cpds.getPooledConnection();
         } else {
            pc = this._cpds.getPooledConnection(this._username, this._password);
         }

         if (pc == null) {
            throw new IllegalStateException("Connection pool data source returned null from getPooledConnection");
         } else {
            pc.addConnectionEventListener(this);
            Object obj = new PooledConnectionAndInfo(pc, this._username, this._password);
            this.pcMap.put(pc, obj);
            return obj;
         }
      } catch (SQLException var3) {
         throw new RuntimeException(var3.getMessage());
      }
   }

   @Override
   public void destroyObject(Object obj) throws Exception {
      if (obj instanceof PooledConnectionAndInfo) {
         PooledConnection pc = ((PooledConnectionAndInfo)obj).getPooledConnection();
         pc.removeConnectionEventListener(this);
         this.pcMap.remove(pc);
         pc.close();
      }
   }

   @Override
   public boolean validateObject(Object obj) {
      boolean valid = false;
      if (obj instanceof PooledConnectionAndInfo) {
         PooledConnection pconn = ((PooledConnectionAndInfo)obj).getPooledConnection();
         String query = this._validationQuery;
         if (null != query) {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;
            this.validatingMap.put(pconn, null);

            try {
               conn = pconn.getConnection();
               stmt = conn.createStatement();
               rset = stmt.executeQuery(query);
               if (rset.next()) {
                  valid = true;
               } else {
                  valid = false;
               }

               if (this._rollbackAfterValidation) {
                  conn.rollback();
               }
            } catch (Exception var25) {
               valid = false;
            } finally {
               if (rset != null) {
                  try {
                     rset.close();
                  } catch (Throwable var24) {
                  }
               }

               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var23) {
                  }
               }

               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var22) {
                  }
               }

               this.validatingMap.remove(pconn);
            }
         } else {
            valid = true;
         }
      } else {
         valid = false;
      }

      return valid;
   }

   @Override
   public void passivateObject(Object obj) {
   }

   @Override
   public void activateObject(Object obj) {
   }

   @Override
   public void connectionClosed(ConnectionEvent event) {
      PooledConnection pc = (PooledConnection)event.getSource();
      if (!this.validatingMap.containsKey(pc)) {
         Object info = this.pcMap.get(pc);
         if (info == null) {
            throw new IllegalStateException("close() was called on a Connection, but I have no record of the underlying PooledConnection.");
         }

         try {
            this._pool.returnObject(info);
         } catch (Exception var7) {
            System.err.println("CLOSING DOWN CONNECTION AS IT COULD NOT BE RETURNED TO THE POOL");
            pc.removeConnectionEventListener(this);

            try {
               this.destroyObject(info);
            } catch (Exception var6) {
               System.err.println("EXCEPTION WHILE DESTROYING OBJECT " + info);
               var6.printStackTrace();
            }
         }
      }
   }

   @Override
   public void connectionErrorOccurred(ConnectionEvent event) {
      PooledConnection pc = (PooledConnection)event.getSource();
      if (null != event.getSQLException()) {
         System.err.println("CLOSING DOWN CONNECTION DUE TO INTERNAL ERROR (" + event.getSQLException() + ")");
      }

      pc.removeConnectionEventListener(this);
      Object info = this.pcMap.get(pc);
      if (info == null) {
         throw new IllegalStateException("close() was called on a Connection, but I have no record of the underlying PooledConnection.");
      } else {
         try {
            this._pool.invalidateObject(info);
         } catch (Exception var5) {
            System.err.println("EXCEPTION WHILE DESTROYING OBJECT " + info);
            var5.printStackTrace();
         }
      }
   }

   @Override
   public void invalidate(PooledConnection pc) throws SQLException {
      Object info = this.pcMap.get(pc);
      if (info == null) {
         throw new IllegalStateException("close() was called on a Connection, but I have no record of the underlying PooledConnection.");
      } else {
         try {
            this._pool.invalidateObject(info);
            this._pool.close();
         } catch (Exception var4) {
            throw (SQLException)new SQLException("Error invalidating connection").initCause(var4);
         }
      }
   }

   @Override
   public synchronized void setPassword(String password) {
      this._password = password;
   }

   @Override
   public void closePool(String username) throws SQLException {
      synchronized(this) {
         if (username == null || !username.equals(this._username)) {
            return;
         }
      }

      try {
         this._pool.close();
      } catch (Exception var4) {
         throw (SQLException)new SQLException("Error closing connection pool").initCause(var4);
      }
   }
}
