package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.pool.ObjectPool;

public class PoolableConnection extends DelegatingConnection {
   protected ObjectPool _pool = null;

   public PoolableConnection(Connection conn, ObjectPool pool) {
      super(conn);
      this._pool = pool;
   }

   public PoolableConnection(Connection conn, ObjectPool pool, AbandonedConfig config) {
      super(conn, config);
      this._pool = pool;
   }

   @Override
   public synchronized void close() throws SQLException {
      if (!this._closed) {
         boolean isUnderlyingConectionClosed;
         try {
            isUnderlyingConectionClosed = this._conn.isClosed();
         } catch (SQLException var12) {
            try {
               this._pool.invalidateObject(this);
            } catch (IllegalStateException var4) {
               this.passivate();
               this.getInnermostDelegate().close();
            } catch (Exception var5) {
            }

            throw (SQLException)new SQLException("Cannot close connection (isClosed check failed)").initCause(var12);
         }

         if (!isUnderlyingConectionClosed) {
            try {
               this._pool.returnObject(this);
            } catch (IllegalStateException var6) {
               this.passivate();
               this.getInnermostDelegate().close();
            } catch (SQLException var7) {
               throw var7;
            } catch (RuntimeException var8) {
               throw var8;
            } catch (Exception var9) {
               throw (SQLException)new SQLException("Cannot close connection (return to pool failed)").initCause(var9);
            }
         } else {
            try {
               this._pool.invalidateObject(this);
            } catch (IllegalStateException var10) {
               this.passivate();
               this.getInnermostDelegate().close();
            } catch (Exception var11) {
            }

            throw new SQLException("Already closed.");
         }
      }
   }

   public void reallyClose() throws SQLException {
      super.close();
   }
}
