package org.apache.commons.dbcp.datasources;

import javax.sql.PooledConnection;

final class PooledConnectionAndInfo {
   private final PooledConnection pooledConnection;
   private final String password;
   private final String username;
   private final UserPassKey upkey;

   PooledConnectionAndInfo(PooledConnection pc, String username, String password) {
      this.pooledConnection = pc;
      this.username = username;
      this.password = password;
      this.upkey = new UserPassKey(username, password);
   }

   final PooledConnection getPooledConnection() {
      return this.pooledConnection;
   }

   final UserPassKey getUserPassKey() {
      return this.upkey;
   }

   final String getPassword() {
      return this.password;
   }

   final String getUsername() {
      return this.username;
   }
}
