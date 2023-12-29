package com.mchange.v1.db.sql;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

/** @deprecated */
public class ConnectionEventSupport {
   PooledConnection source;
   Set mlisteners = new HashSet();

   public ConnectionEventSupport(PooledConnection var1) {
      this.source = var1;
   }

   public synchronized void addConnectionEventListener(ConnectionEventListener var1) {
      this.mlisteners.add(var1);
   }

   public synchronized void removeConnectionEventListener(ConnectionEventListener var1) {
      this.mlisteners.remove(var1);
   }

   public synchronized void fireConnectionClosed() {
      ConnectionEvent var1 = new ConnectionEvent(this.source);

      for(ConnectionEventListener var3 : this.mlisteners) {
         var3.connectionClosed(var1);
      }
   }

   public synchronized void fireConnectionErrorOccurred(SQLException var1) {
      ConnectionEvent var2 = new ConnectionEvent(this.source, var1);

      for(ConnectionEventListener var4 : this.mlisteners) {
         var4.connectionErrorOccurred(var2);
      }
   }
}
