package com.mchange.v2.sql.filter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public abstract class FilterStatement implements Statement {
   protected Statement inner;

   private void __setInner(Statement var1) {
      this.inner = var1;
   }

   public FilterStatement(Statement var1) {
      this.__setInner(var1);
   }

   public FilterStatement() {
   }

   public void setInner(Statement var1) {
      this.__setInner(var1);
   }

   public Statement getInner() {
      return this.inner;
   }

   @Override
   public boolean execute(String var1, int var2) throws SQLException {
      return this.inner.execute(var1, var2);
   }

   @Override
   public boolean execute(String var1, String[] var2) throws SQLException {
      return this.inner.execute(var1, var2);
   }

   @Override
   public boolean execute(String var1) throws SQLException {
      return this.inner.execute(var1);
   }

   @Override
   public boolean execute(String var1, int[] var2) throws SQLException {
      return this.inner.execute(var1, var2);
   }

   @Override
   public void clearWarnings() throws SQLException {
      this.inner.clearWarnings();
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      return this.inner.getWarnings();
   }

   @Override
   public boolean isClosed() throws SQLException {
      return this.inner.isClosed();
   }

   @Override
   public int getFetchDirection() throws SQLException {
      return this.inner.getFetchDirection();
   }

   @Override
   public int getFetchSize() throws SQLException {
      return this.inner.getFetchSize();
   }

   @Override
   public void setFetchDirection(int var1) throws SQLException {
      this.inner.setFetchDirection(var1);
   }

   @Override
   public void setFetchSize(int var1) throws SQLException {
      this.inner.setFetchSize(var1);
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this.inner.getConnection();
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      return this.inner.getResultSetHoldability();
   }

   @Override
   public void addBatch(String var1) throws SQLException {
      this.inner.addBatch(var1);
   }

   @Override
   public void cancel() throws SQLException {
      this.inner.cancel();
   }

   @Override
   public void clearBatch() throws SQLException {
      this.inner.clearBatch();
   }

   @Override
   public void closeOnCompletion() throws SQLException {
      this.inner.closeOnCompletion();
   }

   @Override
   public int[] executeBatch() throws SQLException {
      return this.inner.executeBatch();
   }

   @Override
   public ResultSet executeQuery(String var1) throws SQLException {
      return this.inner.executeQuery(var1);
   }

   @Override
   public int executeUpdate(String var1, int[] var2) throws SQLException {
      return this.inner.executeUpdate(var1, var2);
   }

   @Override
   public int executeUpdate(String var1, String[] var2) throws SQLException {
      return this.inner.executeUpdate(var1, var2);
   }

   @Override
   public int executeUpdate(String var1) throws SQLException {
      return this.inner.executeUpdate(var1);
   }

   @Override
   public int executeUpdate(String var1, int var2) throws SQLException {
      return this.inner.executeUpdate(var1, var2);
   }

   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      return this.inner.getGeneratedKeys();
   }

   @Override
   public int getMaxFieldSize() throws SQLException {
      return this.inner.getMaxFieldSize();
   }

   @Override
   public int getMaxRows() throws SQLException {
      return this.inner.getMaxRows();
   }

   @Override
   public boolean getMoreResults() throws SQLException {
      return this.inner.getMoreResults();
   }

   @Override
   public boolean getMoreResults(int var1) throws SQLException {
      return this.inner.getMoreResults(var1);
   }

   @Override
   public int getQueryTimeout() throws SQLException {
      return this.inner.getQueryTimeout();
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      return this.inner.getResultSet();
   }

   @Override
   public int getResultSetConcurrency() throws SQLException {
      return this.inner.getResultSetConcurrency();
   }

   @Override
   public int getResultSetType() throws SQLException {
      return this.inner.getResultSetType();
   }

   @Override
   public int getUpdateCount() throws SQLException {
      return this.inner.getUpdateCount();
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException {
      return this.inner.isCloseOnCompletion();
   }

   @Override
   public boolean isPoolable() throws SQLException {
      return this.inner.isPoolable();
   }

   @Override
   public void setCursorName(String var1) throws SQLException {
      this.inner.setCursorName(var1);
   }

   @Override
   public void setEscapeProcessing(boolean var1) throws SQLException {
      this.inner.setEscapeProcessing(var1);
   }

   @Override
   public void setMaxFieldSize(int var1) throws SQLException {
      this.inner.setMaxFieldSize(var1);
   }

   @Override
   public void setMaxRows(int var1) throws SQLException {
      this.inner.setMaxRows(var1);
   }

   @Override
   public void setPoolable(boolean var1) throws SQLException {
      this.inner.setPoolable(var1);
   }

   @Override
   public void setQueryTimeout(int var1) throws SQLException {
      this.inner.setQueryTimeout(var1);
   }

   @Override
   public void close() throws SQLException {
      this.inner.close();
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
