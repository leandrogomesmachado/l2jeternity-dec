package com.mchange.v1.db.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionBundleImpl implements ConnectionBundle {
   Connection con;
   Map map = new HashMap();

   public ConnectionBundleImpl(Connection var1) {
      this.con = var1;
   }

   @Override
   public Connection getConnection() {
      return this.con;
   }

   @Override
   public PreparedStatement getStatement(String var1) {
      return (PreparedStatement)this.map.get(var1);
   }

   @Override
   public void putStatement(String var1, PreparedStatement var2) {
      this.map.put(var1, var2);
   }

   @Override
   public void close() throws SQLException {
      this.con.close();
   }

   @Override
   public void finalize() throws Exception {
      if (!this.con.isClosed()) {
         this.close();
      }
   }
}
