package com.mchange.v2.sql.filter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public abstract class SynchronizedFilterCallableStatement implements CallableStatement {
   protected CallableStatement inner;

   private void __setInner(CallableStatement var1) {
      this.inner = var1;
   }

   public SynchronizedFilterCallableStatement(CallableStatement var1) {
      this.__setInner(var1);
   }

   public SynchronizedFilterCallableStatement() {
   }

   public synchronized void setInner(CallableStatement var1) {
      this.__setInner(var1);
   }

   public synchronized CallableStatement getInner() {
      return this.inner;
   }

   @Override
   public synchronized BigDecimal getBigDecimal(int var1, int var2) throws SQLException {
      return this.inner.getBigDecimal(var1, var2);
   }

   @Override
   public synchronized BigDecimal getBigDecimal(String var1) throws SQLException {
      return this.inner.getBigDecimal(var1);
   }

   @Override
   public synchronized BigDecimal getBigDecimal(int var1) throws SQLException {
      return this.inner.getBigDecimal(var1);
   }

   @Override
   public synchronized Blob getBlob(int var1) throws SQLException {
      return this.inner.getBlob(var1);
   }

   @Override
   public synchronized Blob getBlob(String var1) throws SQLException {
      return this.inner.getBlob(var1);
   }

   @Override
   public synchronized Reader getCharacterStream(int var1) throws SQLException {
      return this.inner.getCharacterStream(var1);
   }

   @Override
   public synchronized Reader getCharacterStream(String var1) throws SQLException {
      return this.inner.getCharacterStream(var1);
   }

   @Override
   public synchronized Clob getClob(int var1) throws SQLException {
      return this.inner.getClob(var1);
   }

   @Override
   public synchronized Clob getClob(String var1) throws SQLException {
      return this.inner.getClob(var1);
   }

   @Override
   public synchronized Reader getNCharacterStream(int var1) throws SQLException {
      return this.inner.getNCharacterStream(var1);
   }

   @Override
   public synchronized Reader getNCharacterStream(String var1) throws SQLException {
      return this.inner.getNCharacterStream(var1);
   }

   @Override
   public synchronized NClob getNClob(String var1) throws SQLException {
      return this.inner.getNClob(var1);
   }

   @Override
   public synchronized NClob getNClob(int var1) throws SQLException {
      return this.inner.getNClob(var1);
   }

   @Override
   public synchronized String getNString(int var1) throws SQLException {
      return this.inner.getNString(var1);
   }

   @Override
   public synchronized String getNString(String var1) throws SQLException {
      return this.inner.getNString(var1);
   }

   @Override
   public synchronized RowId getRowId(String var1) throws SQLException {
      return this.inner.getRowId(var1);
   }

   @Override
   public synchronized RowId getRowId(int var1) throws SQLException {
      return this.inner.getRowId(var1);
   }

   @Override
   public synchronized SQLXML getSQLXML(String var1) throws SQLException {
      return this.inner.getSQLXML(var1);
   }

   @Override
   public synchronized SQLXML getSQLXML(int var1) throws SQLException {
      return this.inner.getSQLXML(var1);
   }

   @Override
   public synchronized boolean wasNull() throws SQLException {
      return this.inner.wasNull();
   }

   @Override
   public synchronized void registerOutParameter(String var1, int var2, String var3) throws SQLException {
      this.inner.registerOutParameter(var1, var2, var3);
   }

   @Override
   public synchronized void registerOutParameter(int var1, int var2) throws SQLException {
      this.inner.registerOutParameter(var1, var2);
   }

   @Override
   public synchronized void registerOutParameter(int var1, int var2, String var3) throws SQLException {
      this.inner.registerOutParameter(var1, var2, var3);
   }

   @Override
   public synchronized void registerOutParameter(String var1, int var2, int var3) throws SQLException {
      this.inner.registerOutParameter(var1, var2, var3);
   }

   @Override
   public synchronized void registerOutParameter(String var1, int var2) throws SQLException {
      this.inner.registerOutParameter(var1, var2);
   }

   @Override
   public synchronized void registerOutParameter(int var1, int var2, int var3) throws SQLException {
      this.inner.registerOutParameter(var1, var2, var3);
   }

   @Override
   public synchronized void setAsciiStream(String var1, InputStream var2) throws SQLException {
      this.inner.setAsciiStream(var1, var2);
   }

   @Override
   public synchronized void setAsciiStream(String var1, InputStream var2, long var3) throws SQLException {
      this.inner.setAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void setAsciiStream(String var1, InputStream var2, int var3) throws SQLException {
      this.inner.setAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBigDecimal(String var1, BigDecimal var2) throws SQLException {
      this.inner.setBigDecimal(var1, var2);
   }

   @Override
   public synchronized void setBinaryStream(String var1, InputStream var2) throws SQLException {
      this.inner.setBinaryStream(var1, var2);
   }

   @Override
   public synchronized void setBinaryStream(String var1, InputStream var2, int var3) throws SQLException {
      this.inner.setBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBinaryStream(String var1, InputStream var2, long var3) throws SQLException {
      this.inner.setBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBlob(String var1, Blob var2) throws SQLException {
      this.inner.setBlob(var1, var2);
   }

   @Override
   public synchronized void setBlob(String var1, InputStream var2, long var3) throws SQLException {
      this.inner.setBlob(var1, var2, var3);
   }

   @Override
   public synchronized void setBlob(String var1, InputStream var2) throws SQLException {
      this.inner.setBlob(var1, var2);
   }

   @Override
   public synchronized void setBytes(String var1, byte[] var2) throws SQLException {
      this.inner.setBytes(var1, var2);
   }

   @Override
   public synchronized void setCharacterStream(String var1, Reader var2, long var3) throws SQLException {
      this.inner.setCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void setCharacterStream(String var1, Reader var2) throws SQLException {
      this.inner.setCharacterStream(var1, var2);
   }

   @Override
   public synchronized void setCharacterStream(String var1, Reader var2, int var3) throws SQLException {
      this.inner.setCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void setClob(String var1, Reader var2) throws SQLException {
      this.inner.setClob(var1, var2);
   }

   @Override
   public synchronized void setClob(String var1, Clob var2) throws SQLException {
      this.inner.setClob(var1, var2);
   }

   @Override
   public synchronized void setClob(String var1, Reader var2, long var3) throws SQLException {
      this.inner.setClob(var1, var2, var3);
   }

   @Override
   public synchronized void setDate(String var1, Date var2, Calendar var3) throws SQLException {
      this.inner.setDate(var1, var2, var3);
   }

   @Override
   public synchronized void setDate(String var1, Date var2) throws SQLException {
      this.inner.setDate(var1, var2);
   }

   @Override
   public synchronized void setNCharacterStream(String var1, Reader var2) throws SQLException {
      this.inner.setNCharacterStream(var1, var2);
   }

   @Override
   public synchronized void setNCharacterStream(String var1, Reader var2, long var3) throws SQLException {
      this.inner.setNCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void setNClob(String var1, Reader var2, long var3) throws SQLException {
      this.inner.setNClob(var1, var2, var3);
   }

   @Override
   public synchronized void setNClob(String var1, NClob var2) throws SQLException {
      this.inner.setNClob(var1, var2);
   }

   @Override
   public synchronized void setNClob(String var1, Reader var2) throws SQLException {
      this.inner.setNClob(var1, var2);
   }

   @Override
   public synchronized void setNString(String var1, String var2) throws SQLException {
      this.inner.setNString(var1, var2);
   }

   @Override
   public synchronized void setNull(String var1, int var2, String var3) throws SQLException {
      this.inner.setNull(var1, var2, var3);
   }

   @Override
   public synchronized void setNull(String var1, int var2) throws SQLException {
      this.inner.setNull(var1, var2);
   }

   @Override
   public synchronized void setObject(String var1, Object var2) throws SQLException {
      this.inner.setObject(var1, var2);
   }

   @Override
   public synchronized void setObject(String var1, Object var2, int var3, int var4) throws SQLException {
      this.inner.setObject(var1, var2, var3, var4);
   }

   @Override
   public synchronized void setObject(String var1, Object var2, int var3) throws SQLException {
      this.inner.setObject(var1, var2, var3);
   }

   @Override
   public synchronized void setRowId(String var1, RowId var2) throws SQLException {
      this.inner.setRowId(var1, var2);
   }

   @Override
   public synchronized void setSQLXML(String var1, SQLXML var2) throws SQLException {
      this.inner.setSQLXML(var1, var2);
   }

   @Override
   public synchronized void setString(String var1, String var2) throws SQLException {
      this.inner.setString(var1, var2);
   }

   @Override
   public synchronized Object getObject(String var1) throws SQLException {
      return this.inner.getObject(var1);
   }

   @Override
   public synchronized Object getObject(String var1, Map var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(int var1, Class var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(int var1) throws SQLException {
      return this.inner.getObject(var1);
   }

   @Override
   public synchronized Object getObject(int var1, Map var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(String var1, Class var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized boolean getBoolean(String var1) throws SQLException {
      return this.inner.getBoolean(var1);
   }

   @Override
   public synchronized boolean getBoolean(int var1) throws SQLException {
      return this.inner.getBoolean(var1);
   }

   @Override
   public synchronized byte getByte(String var1) throws SQLException {
      return this.inner.getByte(var1);
   }

   @Override
   public synchronized byte getByte(int var1) throws SQLException {
      return this.inner.getByte(var1);
   }

   @Override
   public synchronized short getShort(String var1) throws SQLException {
      return this.inner.getShort(var1);
   }

   @Override
   public synchronized short getShort(int var1) throws SQLException {
      return this.inner.getShort(var1);
   }

   @Override
   public synchronized int getInt(int var1) throws SQLException {
      return this.inner.getInt(var1);
   }

   @Override
   public synchronized int getInt(String var1) throws SQLException {
      return this.inner.getInt(var1);
   }

   @Override
   public synchronized long getLong(int var1) throws SQLException {
      return this.inner.getLong(var1);
   }

   @Override
   public synchronized long getLong(String var1) throws SQLException {
      return this.inner.getLong(var1);
   }

   @Override
   public synchronized float getFloat(int var1) throws SQLException {
      return this.inner.getFloat(var1);
   }

   @Override
   public synchronized float getFloat(String var1) throws SQLException {
      return this.inner.getFloat(var1);
   }

   @Override
   public synchronized double getDouble(String var1) throws SQLException {
      return this.inner.getDouble(var1);
   }

   @Override
   public synchronized double getDouble(int var1) throws SQLException {
      return this.inner.getDouble(var1);
   }

   @Override
   public synchronized byte[] getBytes(int var1) throws SQLException {
      return this.inner.getBytes(var1);
   }

   @Override
   public synchronized byte[] getBytes(String var1) throws SQLException {
      return this.inner.getBytes(var1);
   }

   @Override
   public synchronized Array getArray(int var1) throws SQLException {
      return this.inner.getArray(var1);
   }

   @Override
   public synchronized Array getArray(String var1) throws SQLException {
      return this.inner.getArray(var1);
   }

   @Override
   public synchronized URL getURL(int var1) throws SQLException {
      return this.inner.getURL(var1);
   }

   @Override
   public synchronized URL getURL(String var1) throws SQLException {
      return this.inner.getURL(var1);
   }

   @Override
   public synchronized void setBoolean(String var1, boolean var2) throws SQLException {
      this.inner.setBoolean(var1, var2);
   }

   @Override
   public synchronized void setByte(String var1, byte var2) throws SQLException {
      this.inner.setByte(var1, var2);
   }

   @Override
   public synchronized void setDouble(String var1, double var2) throws SQLException {
      this.inner.setDouble(var1, var2);
   }

   @Override
   public synchronized void setFloat(String var1, float var2) throws SQLException {
      this.inner.setFloat(var1, var2);
   }

   @Override
   public synchronized void setInt(String var1, int var2) throws SQLException {
      this.inner.setInt(var1, var2);
   }

   @Override
   public synchronized void setLong(String var1, long var2) throws SQLException {
      this.inner.setLong(var1, var2);
   }

   @Override
   public synchronized void setShort(String var1, short var2) throws SQLException {
      this.inner.setShort(var1, var2);
   }

   @Override
   public synchronized void setTimestamp(String var1, Timestamp var2) throws SQLException {
      this.inner.setTimestamp(var1, var2);
   }

   @Override
   public synchronized void setTimestamp(String var1, Timestamp var2, Calendar var3) throws SQLException {
      this.inner.setTimestamp(var1, var2, var3);
   }

   @Override
   public synchronized Ref getRef(int var1) throws SQLException {
      return this.inner.getRef(var1);
   }

   @Override
   public synchronized Ref getRef(String var1) throws SQLException {
      return this.inner.getRef(var1);
   }

   @Override
   public synchronized String getString(String var1) throws SQLException {
      return this.inner.getString(var1);
   }

   @Override
   public synchronized String getString(int var1) throws SQLException {
      return this.inner.getString(var1);
   }

   @Override
   public synchronized void setURL(String var1, URL var2) throws SQLException {
      this.inner.setURL(var1, var2);
   }

   @Override
   public synchronized Date getDate(int var1, Calendar var2) throws SQLException {
      return this.inner.getDate(var1, var2);
   }

   @Override
   public synchronized Date getDate(int var1) throws SQLException {
      return this.inner.getDate(var1);
   }

   @Override
   public synchronized Date getDate(String var1, Calendar var2) throws SQLException {
      return this.inner.getDate(var1, var2);
   }

   @Override
   public synchronized Date getDate(String var1) throws SQLException {
      return this.inner.getDate(var1);
   }

   @Override
   public synchronized Time getTime(String var1) throws SQLException {
      return this.inner.getTime(var1);
   }

   @Override
   public synchronized Time getTime(int var1) throws SQLException {
      return this.inner.getTime(var1);
   }

   @Override
   public synchronized Time getTime(String var1, Calendar var2) throws SQLException {
      return this.inner.getTime(var1, var2);
   }

   @Override
   public synchronized Time getTime(int var1, Calendar var2) throws SQLException {
      return this.inner.getTime(var1, var2);
   }

   @Override
   public synchronized void setTime(String var1, Time var2, Calendar var3) throws SQLException {
      this.inner.setTime(var1, var2, var3);
   }

   @Override
   public synchronized void setTime(String var1, Time var2) throws SQLException {
      this.inner.setTime(var1, var2);
   }

   @Override
   public synchronized Timestamp getTimestamp(String var1, Calendar var2) throws SQLException {
      return this.inner.getTimestamp(var1, var2);
   }

   @Override
   public synchronized Timestamp getTimestamp(int var1) throws SQLException {
      return this.inner.getTimestamp(var1);
   }

   @Override
   public synchronized Timestamp getTimestamp(String var1) throws SQLException {
      return this.inner.getTimestamp(var1);
   }

   @Override
   public synchronized Timestamp getTimestamp(int var1, Calendar var2) throws SQLException {
      return this.inner.getTimestamp(var1, var2);
   }

   @Override
   public synchronized boolean execute() throws SQLException {
      return this.inner.execute();
   }

   @Override
   public synchronized ResultSetMetaData getMetaData() throws SQLException {
      return this.inner.getMetaData();
   }

   @Override
   public synchronized void setArray(int var1, Array var2) throws SQLException {
      this.inner.setArray(var1, var2);
   }

   @Override
   public synchronized void addBatch() throws SQLException {
      this.inner.addBatch();
   }

   @Override
   public synchronized ResultSet executeQuery() throws SQLException {
      return this.inner.executeQuery();
   }

   @Override
   public synchronized int executeUpdate() throws SQLException {
      return this.inner.executeUpdate();
   }

   @Override
   public synchronized void clearParameters() throws SQLException {
      this.inner.clearParameters();
   }

   @Override
   public synchronized ParameterMetaData getParameterMetaData() throws SQLException {
      return this.inner.getParameterMetaData();
   }

   @Override
   public synchronized void setAsciiStream(int var1, InputStream var2, long var3) throws SQLException {
      this.inner.setAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void setAsciiStream(int var1, InputStream var2) throws SQLException {
      this.inner.setAsciiStream(var1, var2);
   }

   @Override
   public synchronized void setAsciiStream(int var1, InputStream var2, int var3) throws SQLException {
      this.inner.setAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBigDecimal(int var1, BigDecimal var2) throws SQLException {
      this.inner.setBigDecimal(var1, var2);
   }

   @Override
   public synchronized void setBinaryStream(int var1, InputStream var2, int var3) throws SQLException {
      this.inner.setBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBinaryStream(int var1, InputStream var2, long var3) throws SQLException {
      this.inner.setBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBinaryStream(int var1, InputStream var2) throws SQLException {
      this.inner.setBinaryStream(var1, var2);
   }

   @Override
   public synchronized void setBlob(int var1, InputStream var2, long var3) throws SQLException {
      this.inner.setBlob(var1, var2, var3);
   }

   @Override
   public synchronized void setBlob(int var1, Blob var2) throws SQLException {
      this.inner.setBlob(var1, var2);
   }

   @Override
   public synchronized void setBlob(int var1, InputStream var2) throws SQLException {
      this.inner.setBlob(var1, var2);
   }

   @Override
   public synchronized void setBytes(int var1, byte[] var2) throws SQLException {
      this.inner.setBytes(var1, var2);
   }

   @Override
   public synchronized void setCharacterStream(int var1, Reader var2) throws SQLException {
      this.inner.setCharacterStream(var1, var2);
   }

   @Override
   public synchronized void setCharacterStream(int var1, Reader var2, int var3) throws SQLException {
      this.inner.setCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void setCharacterStream(int var1, Reader var2, long var3) throws SQLException {
      this.inner.setCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void setClob(int var1, Reader var2, long var3) throws SQLException {
      this.inner.setClob(var1, var2, var3);
   }

   @Override
   public synchronized void setClob(int var1, Reader var2) throws SQLException {
      this.inner.setClob(var1, var2);
   }

   @Override
   public synchronized void setClob(int var1, Clob var2) throws SQLException {
      this.inner.setClob(var1, var2);
   }

   @Override
   public synchronized void setDate(int var1, Date var2) throws SQLException {
      this.inner.setDate(var1, var2);
   }

   @Override
   public synchronized void setDate(int var1, Date var2, Calendar var3) throws SQLException {
      this.inner.setDate(var1, var2, var3);
   }

   @Override
   public synchronized void setNCharacterStream(int var1, Reader var2, long var3) throws SQLException {
      this.inner.setNCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void setNCharacterStream(int var1, Reader var2) throws SQLException {
      this.inner.setNCharacterStream(var1, var2);
   }

   @Override
   public synchronized void setNClob(int var1, Reader var2) throws SQLException {
      this.inner.setNClob(var1, var2);
   }

   @Override
   public synchronized void setNClob(int var1, Reader var2, long var3) throws SQLException {
      this.inner.setNClob(var1, var2, var3);
   }

   @Override
   public synchronized void setNClob(int var1, NClob var2) throws SQLException {
      this.inner.setNClob(var1, var2);
   }

   @Override
   public synchronized void setNString(int var1, String var2) throws SQLException {
      this.inner.setNString(var1, var2);
   }

   @Override
   public synchronized void setNull(int var1, int var2) throws SQLException {
      this.inner.setNull(var1, var2);
   }

   @Override
   public synchronized void setNull(int var1, int var2, String var3) throws SQLException {
      this.inner.setNull(var1, var2, var3);
   }

   @Override
   public synchronized void setObject(int var1, Object var2, int var3) throws SQLException {
      this.inner.setObject(var1, var2, var3);
   }

   @Override
   public synchronized void setObject(int var1, Object var2, int var3, int var4) throws SQLException {
      this.inner.setObject(var1, var2, var3, var4);
   }

   @Override
   public synchronized void setObject(int var1, Object var2) throws SQLException {
      this.inner.setObject(var1, var2);
   }

   @Override
   public synchronized void setRef(int var1, Ref var2) throws SQLException {
      this.inner.setRef(var1, var2);
   }

   @Override
   public synchronized void setRowId(int var1, RowId var2) throws SQLException {
      this.inner.setRowId(var1, var2);
   }

   @Override
   public synchronized void setSQLXML(int var1, SQLXML var2) throws SQLException {
      this.inner.setSQLXML(var1, var2);
   }

   @Override
   public synchronized void setString(int var1, String var2) throws SQLException {
      this.inner.setString(var1, var2);
   }

   @Override
   public synchronized void setUnicodeStream(int var1, InputStream var2, int var3) throws SQLException {
      this.inner.setUnicodeStream(var1, var2, var3);
   }

   @Override
   public synchronized void setBoolean(int var1, boolean var2) throws SQLException {
      this.inner.setBoolean(var1, var2);
   }

   @Override
   public synchronized void setByte(int var1, byte var2) throws SQLException {
      this.inner.setByte(var1, var2);
   }

   @Override
   public synchronized void setDouble(int var1, double var2) throws SQLException {
      this.inner.setDouble(var1, var2);
   }

   @Override
   public synchronized void setFloat(int var1, float var2) throws SQLException {
      this.inner.setFloat(var1, var2);
   }

   @Override
   public synchronized void setInt(int var1, int var2) throws SQLException {
      this.inner.setInt(var1, var2);
   }

   @Override
   public synchronized void setLong(int var1, long var2) throws SQLException {
      this.inner.setLong(var1, var2);
   }

   @Override
   public synchronized void setShort(int var1, short var2) throws SQLException {
      this.inner.setShort(var1, var2);
   }

   @Override
   public synchronized void setTimestamp(int var1, Timestamp var2, Calendar var3) throws SQLException {
      this.inner.setTimestamp(var1, var2, var3);
   }

   @Override
   public synchronized void setTimestamp(int var1, Timestamp var2) throws SQLException {
      this.inner.setTimestamp(var1, var2);
   }

   @Override
   public synchronized void setURL(int var1, URL var2) throws SQLException {
      this.inner.setURL(var1, var2);
   }

   @Override
   public synchronized void setTime(int var1, Time var2, Calendar var3) throws SQLException {
      this.inner.setTime(var1, var2, var3);
   }

   @Override
   public synchronized void setTime(int var1, Time var2) throws SQLException {
      this.inner.setTime(var1, var2);
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
