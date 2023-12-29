package com.mchange.v1.db.sql;

import com.mchange.v1.util.AbstractResourcePool;
import com.mchange.v1.util.BrokenObjectException;
import com.mchange.v1.util.UnexpectedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ConnectionBundlePoolImpl extends AbstractResourcePool implements ConnectionBundlePool {
   String jdbcUrl;
   String username;
   String pwd;

   public ConnectionBundlePoolImpl(String var1, String var2, String var3, int var4, int var5, int var6) throws SQLException {
      super(var4, var5, var6);
      this.init(var1, var2, var3);
   }

   protected ConnectionBundlePoolImpl(int var1, int var2, int var3) {
      super(var1, var2, var3);
   }

   protected void init(String var1, String var2, String var3) throws SQLException {
      this.jdbcUrl = var1;
      this.username = var2;
      this.pwd = var3;

      try {
         this.init();
      } catch (SQLException var5) {
         throw var5;
      } catch (Exception var6) {
         throw new UnexpectedException(var6, "Unexpected exception while initializing ConnectionBundlePool");
      }
   }

   @Override
   public ConnectionBundle checkoutBundle() throws SQLException, BrokenObjectException, InterruptedException {
      try {
         return (ConnectionBundle)this.checkoutResource();
      } catch (BrokenObjectException var2) {
         throw var2;
      } catch (InterruptedException var3) {
         throw var3;
      } catch (SQLException var4) {
         throw var4;
      } catch (Exception var5) {
         throw new UnexpectedException(var5, "Unexpected exception while checking out ConnectionBundle");
      }
   }

   @Override
   public void checkinBundle(ConnectionBundle var1) throws BrokenObjectException {
      this.checkinResource(var1);
   }

   @Override
   public void close() throws SQLException {
      try {
         super.close();
      } catch (SQLException var2) {
         throw var2;
      } catch (Exception var3) {
         throw new UnexpectedException(var3, "Unexpected exception while closing pool.");
      }
   }

   @Override
   protected Object acquireResource() throws Exception {
      Connection var1 = DriverManager.getConnection(this.jdbcUrl, this.username, this.pwd);
      this.setConnectionOptions(var1);
      return new ConnectionBundleImpl(var1);
   }

   @Override
   protected void refurbishResource(Object var1) throws BrokenObjectException {
      boolean var2;
      try {
         Connection var3 = ((ConnectionBundle)var1).getConnection();
         var3.rollback();
         var2 = var3.isClosed();
         this.setConnectionOptions(var3);
      } catch (SQLException var4) {
         var2 = true;
      }

      if (var2) {
         throw new BrokenObjectException(var1);
      }
   }

   @Override
   protected void destroyResource(Object var1) throws Exception {
      ((ConnectionBundle)var1).close();
   }

   protected abstract void setConnectionOptions(Connection var1) throws SQLException;
}
