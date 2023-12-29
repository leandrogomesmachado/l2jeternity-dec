package com.mchange.v1.db.sql;

import com.mchange.v1.util.BrokenObjectException;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionBundlePoolBean implements ConnectionBundlePool {
   ConnectionBundlePool inner;

   public void init(String var1, String var2, String var3, String var4, int var5, int var6, int var7) throws SQLException, ClassNotFoundException {
      Class.forName(var1);
      this.init(var2, var3, var4, var5, var6, var7);
   }

   public void init(String var1, String var2, String var3, int var4, int var5, int var6) throws SQLException {
      this.inner = new ConnectionBundlePoolBean.InnerPool(var1, var2, var3, var4, var5, var6);
   }

   @Override
   public ConnectionBundle checkoutBundle() throws SQLException, InterruptedException, BrokenObjectException {
      return this.inner.checkoutBundle();
   }

   @Override
   public void checkinBundle(ConnectionBundle var1) throws SQLException, BrokenObjectException {
      this.inner.checkinBundle(var1);
   }

   @Override
   public void close() throws SQLException {
      this.inner.close();
   }

   protected void setConnectionOptions(Connection var1) throws SQLException {
      var1.setAutoCommit(false);
   }

   class InnerPool extends ConnectionBundlePoolImpl {
      InnerPool(String var2, String var3, String var4, int var5, int var6, int var7) throws SQLException {
         super(var5, var6, var7);
         this.init(var2, var3, var4);
      }

      @Override
      protected void setConnectionOptions(Connection var1) throws SQLException {
         ConnectionBundlePoolBean.this.setConnectionOptions(var1);
      }
   }
}
