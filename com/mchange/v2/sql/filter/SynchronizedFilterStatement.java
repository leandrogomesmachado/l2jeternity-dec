package com.mchange.v2.sql.filter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public abstract class SynchronizedFilterStatement implements Statement {
   protected Statement inner;

   private void __setInner(Statement var1) {
      this.inner = var1;
   }

   public SynchronizedFilterStatement(Statement var1) {
      this.__setInner(var1);
   }

   public SynchronizedFilterStatement() {
   }

   public synchronized void setInner(Statement var1) {
      this.__setInner(var1);
   }

   public synchronized Statement getInner() {
      return this.inner;
   }

   @Override
   public synchronized boolean execute(String var1, int var2) throws SQLException {
      return this.inner.execute(var1, var2);
   }

   @Override
   public synchronized boolean execute(String var1, String[] var2) throws SQLException {
      return this.inner.execute(var1, var2);
   }

   @Override
   public synchronized boolean execute(String var1) throws SQLException {
      return this.inner.execute(var1);
   }

   @Override
   public synchronized boolean execute(String var1, int[] var2) throws SQLException {
      return this.inner.execute(var1, var2);
   }

   @Override
   public synchronized void clearWarnings() throws SQLException {
      this.inner.clearWarnings();
   }

   @Override
   public synchronized SQLWarning getWarnings() throws SQLException {
      return this.inner.getWarnings();
   }

   @Override
   public synchronized boolean isClosed() throws SQLException {
      return this.inner.isClosed();
   }

   @Override
   public synchronized int getFetchDirection() throws SQLException {
      return this.inner.getFetchDirection();
   }

   @Override
   public synchronized int getFetchSize() throws SQLException {
      return this.inner.getFetchSize();
   }

   @Override
   public synchronized void setFetchDirection(int var1) throws SQLException {
      this.inner.setFetchDirection(var1);
   }

   @Override
   public synchronized void setFetchSize(int var1) throws SQLException {
      this.inner.setFetchSize(var1);
   }

   @Override
   public synchronized Connection getConnection() throws SQLException {
      return this.inner.getConnection();
   }

   @Override
   public synchronized int getResultSetHoldability() throws SQLException {
      return this.inner.getResultSetHoldability();
   }

   @Override
   public synchronized void addBatch(String var1) throws SQLException {
      this.inner.addBatch(var1);
   }

   @Override
   public synchronized void cancel() throws SQLException {
      this.inner.cancel();
   }

   @Override
   public synchronized void clearBatch() throws SQLException {
      this.inner.clearBatch();
   }

   @Override
   public synchronized void closeOnCompletion() throws SQLException {
      this.inner.closeOnCompletion();
   }

   @Override
   public synchronized int[] executeBatch() throws SQLException {
      return this.inner.executeBatch();
   }

   @Override
   public synchronized ResultSet executeQuery(String var1) throws SQLException {
      return this.inner.executeQuery(var1);
   }

   @Override
   public synchronized int executeUpdate(String var1, int[] var2) throws SQLException {
      return this.inner.executeUpdate(var1, var2);
   }

   @Override
   public synchronized int executeUpdate(String var1, String[] var2) throws SQLException {
      return this.inner.executeUpdate(var1, var2);
   }

   @Override
   public synchronized int executeUpdate(String var1) throws SQLException {
      return this.inner.executeUpdate(var1);
   }

   @Override
   public synchronized int executeUpdate(String var1, int var2) throws SQLException {
      return this.inner.executeUpdate(var1, var2);
   }

   @Override
   public synchronized ResultSet getGeneratedKeys() throws SQLException {
      return this.inner.getGeneratedKeys();
   }

   @Override
   public synchronized int getMaxFieldSize() throws SQLException {
      return this.inner.getMaxFieldSize();
   }

   @Override
   public synchronized int getMaxRows() throws SQLException {
      return this.inner.getMaxRows();
   }

   @Override
   public synchronized boolean getMoreResults() throws SQLException {
      return this.inner.getMoreResults();
   }

   @Override
   public synchronized boolean getMoreResults(int var1) throws SQLException {
      return this.inner.getMoreResults(var1);
   }

   @Override
   public synchronized int getQueryTimeout() throws SQLException {
      return this.inner.getQueryTimeout();
   }

   @Override
   public synchronized ResultSet getResultSet() throws SQLException {
      return this.inner.getResultSet();
   }

   @Override
   public synchronized int getResultSetConcurrency() throws SQLException {
      return this.inner.getResultSetConcurrency();
   }

   @Override
   public synchronized int getResultSetType() throws SQLException {
      return this.inner.getResultSetType();
   }

   @Override
   public synchronized int getUpdateCount() throws SQLException {
      return this.inner.getUpdateCount();
   }

   @Override
   public synchronized boolean isCloseOnCompletion() throws SQLException {
      return this.inner.isCloseOnCompletion();
   }

   @Override
   public synchronized boolean isPoolable() throws SQLException {
      return this.inner.isPoolable();
   }

   @Override
   public synchronized void setCursorName(String var1) throws SQLException {
      this.inner.setCursorName(var1);
   }

   @Override
   public synchronized void setEscapeProcessing(boolean var1) throws SQLException {
      this.inner.setEscapeProcessing(var1);
   }

   @Override
   public synchronized void setMaxFieldSize(int var1) throws SQLException {
      this.inner.setMaxFieldSize(var1);
   }

   @Override
   public synchronized void setMaxRows(int var1) throws SQLException {
      this.inner.setMaxRows(var1);
   }

   @Override
   public synchronized void setPoolable(boolean var1) throws SQLException {
      this.inner.setPoolable(var1);
   }

   @Override
   public synchronized void setQueryTimeout(int var1) throws SQLException {
      this.inner.setQueryTimeout(var1);
   }

   @Override
   public synchronized void close() throws SQLException {
      this.inner.close();
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
