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

public abstract class FilterConnection implements Connection {
   protected Connection inner;

   private void __setInner(Connection var1) {
      this.inner = var1;
   }

   public FilterConnection(Connection var1) {
      this.__setInner(var1);
   }

   public FilterConnection() {
   }

   public void setInner(Connection var1) {
      this.__setInner(var1);
   }

   public Connection getInner() {
      return this.inner;
   }

   @Override
   public void commit() throws SQLException {
      this.inner.commit();
   }

   @Override
   public void clearWarnings() throws SQLException {
      this.inner.clearWarnings();
   }

   @Override
   public Array createArrayOf(String var1, Object[] var2) throws SQLException {
      return this.inner.createArrayOf(var1, var2);
   }

   @Override
   public Blob createBlob() throws SQLException {
      return this.inner.createBlob();
   }

   @Override
   public Clob createClob() throws SQLException {
      return this.inner.createClob();
   }

   @Override
   public NClob createNClob() throws SQLException {
      return this.inner.createNClob();
   }

   @Override
   public SQLXML createSQLXML() throws SQLException {
      return this.inner.createSQLXML();
   }

   @Override
   public Statement createStatement(int var1, int var2, int var3) throws SQLException {
      return this.inner.createStatement(var1, var2, var3);
   }

   @Override
   public Statement createStatement(int var1, int var2) throws SQLException {
      return this.inner.createStatement(var1, var2);
   }

   @Override
   public Statement createStatement() throws SQLException {
      return this.inner.createStatement();
   }

   @Override
   public Struct createStruct(String var1, Object[] var2) throws SQLException {
      return this.inner.createStruct(var1, var2);
   }

   @Override
   public boolean getAutoCommit() throws SQLException {
      return this.inner.getAutoCommit();
   }

   @Override
   public String getCatalog() throws SQLException {
      return this.inner.getCatalog();
   }

   @Override
   public String getClientInfo(String var1) throws SQLException {
      return this.inner.getClientInfo(var1);
   }

   @Override
   public Properties getClientInfo() throws SQLException {
      return this.inner.getClientInfo();
   }

   @Override
   public int getHoldability() throws SQLException {
      return this.inner.getHoldability();
   }

   @Override
   public DatabaseMetaData getMetaData() throws SQLException {
      return this.inner.getMetaData();
   }

   @Override
   public int getNetworkTimeout() throws SQLException {
      return this.inner.getNetworkTimeout();
   }

   @Override
   public String getSchema() throws SQLException {
      return this.inner.getSchema();
   }

   @Override
   public int getTransactionIsolation() throws SQLException {
      return this.inner.getTransactionIsolation();
   }

   @Override
   public Map getTypeMap() throws SQLException {
      return this.inner.getTypeMap();
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
   public String nativeSQL(String var1) throws SQLException {
      return this.inner.nativeSQL(var1);
   }

   @Override
   public CallableStatement prepareCall(String var1, int var2, int var3, int var4) throws SQLException {
      return this.inner.prepareCall(var1, var2, var3, var4);
   }

   @Override
   public CallableStatement prepareCall(String var1, int var2, int var3) throws SQLException {
      return this.inner.prepareCall(var1, var2, var3);
   }

   @Override
   public CallableStatement prepareCall(String var1) throws SQLException {
      return this.inner.prepareCall(var1);
   }

   @Override
   public PreparedStatement prepareStatement(String var1, int var2, int var3, int var4) throws SQLException {
      return this.inner.prepareStatement(var1, var2, var3, var4);
   }

   @Override
   public PreparedStatement prepareStatement(String var1, int var2) throws SQLException {
      return this.inner.prepareStatement(var1, var2);
   }

   @Override
   public PreparedStatement prepareStatement(String var1, int[] var2) throws SQLException {
      return this.inner.prepareStatement(var1, var2);
   }

   @Override
   public PreparedStatement prepareStatement(String var1, String[] var2) throws SQLException {
      return this.inner.prepareStatement(var1, var2);
   }

   @Override
   public PreparedStatement prepareStatement(String var1) throws SQLException {
      return this.inner.prepareStatement(var1);
   }

   @Override
   public PreparedStatement prepareStatement(String var1, int var2, int var3) throws SQLException {
      return this.inner.prepareStatement(var1, var2, var3);
   }

   @Override
   public void releaseSavepoint(Savepoint var1) throws SQLException {
      this.inner.releaseSavepoint(var1);
   }

   @Override
   public void rollback() throws SQLException {
      this.inner.rollback();
   }

   @Override
   public void rollback(Savepoint var1) throws SQLException {
      this.inner.rollback(var1);
   }

   @Override
   public void setAutoCommit(boolean var1) throws SQLException {
      this.inner.setAutoCommit(var1);
   }

   @Override
   public void setCatalog(String var1) throws SQLException {
      this.inner.setCatalog(var1);
   }

   @Override
   public void setClientInfo(String var1, String var2) throws SQLClientInfoException {
      this.inner.setClientInfo(var1, var2);
   }

   @Override
   public void setClientInfo(Properties var1) throws SQLClientInfoException {
      this.inner.setClientInfo(var1);
   }

   @Override
   public void setHoldability(int var1) throws SQLException {
      this.inner.setHoldability(var1);
   }

   @Override
   public void setNetworkTimeout(Executor var1, int var2) throws SQLException {
      this.inner.setNetworkTimeout(var1, var2);
   }

   @Override
   public Savepoint setSavepoint() throws SQLException {
      return this.inner.setSavepoint();
   }

   @Override
   public Savepoint setSavepoint(String var1) throws SQLException {
      return this.inner.setSavepoint(var1);
   }

   @Override
   public void setSchema(String var1) throws SQLException {
      this.inner.setSchema(var1);
   }

   @Override
   public void setTransactionIsolation(int var1) throws SQLException {
      this.inner.setTransactionIsolation(var1);
   }

   @Override
   public void setTypeMap(Map var1) throws SQLException {
      this.inner.setTypeMap(var1);
   }

   @Override
   public void setReadOnly(boolean var1) throws SQLException {
      this.inner.setReadOnly(var1);
   }

   @Override
   public void close() throws SQLException {
      this.inner.close();
   }

   @Override
   public boolean isValid(int var1) throws SQLException {
      return this.inner.isValid(var1);
   }

   @Override
   public boolean isReadOnly() throws SQLException {
      return this.inner.isReadOnly();
   }

   @Override
   public void abort(Executor var1) throws SQLException {
      this.inner.abort(var1);
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
