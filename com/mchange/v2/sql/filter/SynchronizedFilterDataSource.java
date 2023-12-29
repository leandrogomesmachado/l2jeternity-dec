package com.mchange.v2.sql.filter;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public abstract class SynchronizedFilterDataSource implements DataSource {
   protected DataSource inner;

   private void __setInner(DataSource var1) {
      this.inner = var1;
   }

   public SynchronizedFilterDataSource(DataSource var1) {
      this.__setInner(var1);
   }

   public SynchronizedFilterDataSource() {
   }

   public synchronized void setInner(DataSource var1) {
      this.__setInner(var1);
   }

   public synchronized DataSource getInner() {
      return this.inner;
   }

   @Override
   public synchronized Connection getConnection() throws SQLException {
      return this.inner.getConnection();
   }

   @Override
   public synchronized Connection getConnection(String var1, String var2) throws SQLException {
      return this.inner.getConnection(var1, var2);
   }

   @Override
   public synchronized PrintWriter getLogWriter() throws SQLException {
      return this.inner.getLogWriter();
   }

   @Override
   public synchronized int getLoginTimeout() throws SQLException {
      return this.inner.getLoginTimeout();
   }

   @Override
   public synchronized Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return this.inner.getParentLogger();
   }

   @Override
   public synchronized void setLogWriter(PrintWriter var1) throws SQLException {
      this.inner.setLogWriter(var1);
   }

   @Override
   public synchronized void setLoginTimeout(int var1) throws SQLException {
      this.inner.setLoginTimeout(var1);
   }

   @Override
   public synchronized boolean isWrapperFor(Class var1) throws SQLException {
      return this.inner.isWrapperFor(var1);
   }

   @Override
   public synchronized Object unwrap(Class var1) throws SQLException {
      return this.inner.unwrap(var1);
   }
}
