package com.mchange.v2.sql.filter;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public abstract class FilterDataSource implements DataSource {
   protected DataSource inner;

   private void __setInner(DataSource var1) {
      this.inner = var1;
   }

   public FilterDataSource(DataSource var1) {
      this.__setInner(var1);
   }

   public FilterDataSource() {
   }

   public void setInner(DataSource var1) {
      this.__setInner(var1);
   }

   public DataSource getInner() {
      return this.inner;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this.inner.getConnection();
   }

   @Override
   public Connection getConnection(String var1, String var2) throws SQLException {
      return this.inner.getConnection(var1, var2);
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return this.inner.getLogWriter();
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      return this.inner.getLoginTimeout();
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return this.inner.getParentLogger();
   }

   @Override
   public void setLogWriter(PrintWriter var1) throws SQLException {
      this.inner.setLogWriter(var1);
   }

   @Override
   public void setLoginTimeout(int var1) throws SQLException {
      this.inner.setLoginTimeout(var1);
   }

   @Override
   public boolean isWrapperFor(Class var1) throws SQLException {
      return this.inner.isWrapperFor(var1);
   }

   @Override
   public Object unwrap(Class var1) throws SQLException {
      return this.inner.unwrap(var1);
   }
}
