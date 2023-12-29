package com.mchange.v2.sql.filter;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public abstract class SynchronizedFilterConnection implements Connection {
   protected Connection inner;

   private void __setInner(Connection var1) {
      this.inner = var1;
   }

   public SynchronizedFilterConnection(Connection var1) {
      this.__setInner(var1);
   }

   public SynchronizedFilterConnection() {
   }

   public synchronized void setInner(Connection var1) {
      this.__setInner(var1);
   }

   public synchronized Connection getInner() {
      return this.inner;
   }

   @Override
   public synchronized void commit() throws SQLException {
      this.inner.commit();
   }

   @Override
   public synchronized void clearWarnings() throws SQLException {
      this.inner.clearWarnings();
   }

   @Override
   public synchronized Array createArrayOf(String var1, Object[] var2) throws SQLException {
      return this.inner.createArrayOf(var1, var2);
   }

   @Override
   public synchronized Blob createBlob() throws SQLException {
      return this.inner.createBlob();
   }

   @Override
   public synchronized Clob createClob() throws SQLException {
      return this.inner.createClob();
   }

   @Override
   public synchronized NClob createNClob() throws SQLException {
      return this.inner.createNClob();
   }

   @Override
   public synchronized SQLXML createSQLXML() throws SQLException {
      return this.inner.createSQLXML();
   }

   @Override
   public synchronized Statement createStatement(int var1, int var2, int var3) throws SQLException {
      return this.inner.createStatement(var1, var2, var3);
   }

   @Override
   public synchronized Statement createStatement(int var1, int var2) throws SQLException {
      return this.inner.createStatement(var1, var2);
   }

   @Override
   public synchronized Statement createStatement() throws SQLException {
      return this.inner.createStatement();
   }

   @Override
   public synchronized Struct createStruct(String var1, Object[] var2) throws SQLException {
      return this.inner.createStruct(var1, var2);
   }

   @Override
   public synchronized boolean getAutoCommit() throws SQLException {
      return this.inner.getAutoCommit();
   }

   @Override
   public synchronized String getCatalog() throws SQLException {
      return this.inner.getCatalog();
   }

   @Override
   public synchronized String getClientInfo(String var1) throws SQLException {
      return this.inner.getClientInfo(var1);
   }

   @Override
   public synchronized Properties getClientInfo() throws SQLException {
      return this.inner.getClientInfo();
   }

   @Override
   public synchronized int getHoldability() throws SQLException {
      return this.inner.getHoldability();
   }

   @Override
   public synchronized DatabaseMetaData getMetaData() throws SQLException {
      return this.inner.getMetaData();
   }

   @Override
   public synchronized int getNetworkTimeout() throws SQLException {
      return this.inner.getNetworkTimeout();
   }

   @Override
   public synchronized String getSchema() throws SQLException {
      return this.inner.getSchema();
   }

   @Override
   public synchronized int getTransactionIsolation() throws SQLException {
      return this.inner.getTransactionIsolation();
   }

   @Override
   public synchronized Map getTypeMap() throws SQLException {
      return this.inner.getTypeMap();
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
   public synchronized String nativeSQL(String var1) throws SQLException {
      return this.inner.nativeSQL(var1);
   }

   @Override
   public synchronized CallableStatement prepareCall(String var1, int var2, int var3, int var4) throws SQLException {
      return this.inner.prepareCall(var1, var2, var3, var4);
   }

   @Override
   public synchronized CallableStatement prepareCall(String var1, int var2, int var3) throws SQLException {
      return this.inner.prepareCall(var1, var2, var3);
   }

   @Override
   public synchronized CallableStatement prepareCall(String var1) throws SQLException {
      return this.inner.prepareCall(var1);
   }

   @Override
   public synchronized PreparedStatement prepareStatement(String var1, int var2, int var3, int var4) throws SQLException {
      return this.inner.prepareStatement(var1, var2, var3, var4);
   }

   @Override
   public synchronized PreparedStatement prepareStatement(String var1, int var2) throws SQLException {
      return this.inner.prepareStatement(var1, var2);
   }

   @Override
   public synchronized PreparedStatement prepareStatement(String var1, int[] var2) throws SQLException {
      return this.inner.prepareStatement(var1, var2);
   }

   @Override
   public synchronized PreparedStatement prepareStatement(String var1, String[] var2) throws SQLException {
      return this.inner.prepareStatement(var1, var2);
   }

   @Override
   public synchronized PreparedStatement prepareStatement(String var1) throws SQLException {
      return this.inner.prepareStatement(var1);
   }

   @Override
   public synchronized PreparedStatement prepareStatement(String var1, int var2, int var3) throws SQLException {
      return this.inner.prepareStatement(var1, var2, var3);
   }

   @Override
   public synchronized void releaseSavepoint(Savepoint var1) throws SQLException {
      this.inner.releaseSavepoint(var1);
   }

   @Override
   public synchronized void rollback() throws SQLException {
      this.inner.rollback();
   }

   @Override
   public synchronized void rollback(Savepoint var1) throws SQLException {
      this.inner.rollback(var1);
   }

   @Override
   public synchronized void setAutoCommit(boolean var1) throws SQLException {
      this.inner.setAutoCommit(var1);
   }

   @Override
   public synchronized void setCatalog(String var1) throws SQLException {
      this.inner.setCatalog(var1);
   }

   @Override
   public synchronized void setClientInfo(String var1, String var2) throws SQLClientInfoException {
      this.inner.setClientInfo(var1, var2);
   }

   @Override
   public synchronized void setClientInfo(Properties var1) throws SQLClientInfoException {
      this.inner.setClientInfo(var1);
   }

   @Override
   public synchronized void setHoldability(int var1) throws SQLException {
      this.inner.setHoldability(var1);
   }

   @Override
   public synchronized void setNetworkTimeout(Executor var1, int var2) throws SQLException {
      this.inner.setNetworkTimeout(var1, var2);
   }

   @Override
   public synchronized Savepoint setSavepoint() throws SQLException {
      return this.inner.setSavepoint();
   }

   @Override
   public synchronized Savepoint setSavepoint(String var1) throws SQLException {
      return this.inner.setSavepoint(var1);
   }

   @Override
   public synchronized void setSchema(String var1) throws SQLException {
      this.inner.setSchema(var1);
   }

   @Override
   public synchronized void setTransactionIsolation(int var1) throws SQLException {
      this.inner.setTransactionIsolation(var1);
   }

   @Override
   public synchronized void setTypeMap(Map var1) throws SQLException {
      this.inner.setTypeMap(var1);
   }

   @Override
   public synchronized void setReadOnly(boolean var1) throws SQLException {
      this.inner.setReadOnly(var1);
   }

   @Override
   public synchronized void close() throws SQLException {
      this.inner.close();
   }

   @Override
   public synchronized boolean isValid(int var1) throws SQLException {
      return this.inner.isValid(var1);
   }

   @Override
   public synchronized boolean isReadOnly() throws SQLException {
      return this.inner.isReadOnly();
   }

   @Override
   public synchronized void abort(Executor var1) throws SQLException {
      this.inner.abort(var1);
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
