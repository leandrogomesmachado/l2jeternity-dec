package com.mchange.v2.sql.filter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public abstract class SynchronizedFilterResultSet implements ResultSet {
   protected ResultSet inner;

   private void __setInner(ResultSet var1) {
      this.inner = var1;
   }

   public SynchronizedFilterResultSet(ResultSet var1) {
      this.__setInner(var1);
   }

   public SynchronizedFilterResultSet() {
   }

   public synchronized void setInner(ResultSet var1) {
      this.__setInner(var1);
   }

   public synchronized ResultSet getInner() {
      return this.inner;
   }

   @Override
   public synchronized void clearWarnings() throws SQLException {
      this.inner.clearWarnings();
   }

   @Override
   public synchronized int getHoldability() throws SQLException {
      return this.inner.getHoldability();
   }

   @Override
   public synchronized ResultSetMetaData getMetaData() throws SQLException {
      return this.inner.getMetaData();
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
   public synchronized void updateBigDecimal(int var1, BigDecimal var2) throws SQLException {
      this.inner.updateBigDecimal(var1, var2);
   }

   @Override
   public synchronized void updateBigDecimal(String var1, BigDecimal var2) throws SQLException {
      this.inner.updateBigDecimal(var1, var2);
   }

   @Override
   public synchronized boolean absolute(int var1) throws SQLException {
      return this.inner.absolute(var1);
   }

   @Override
   public synchronized void afterLast() throws SQLException {
      this.inner.afterLast();
   }

   @Override
   public synchronized void beforeFirst() throws SQLException {
      this.inner.beforeFirst();
   }

   @Override
   public synchronized void cancelRowUpdates() throws SQLException {
      this.inner.cancelRowUpdates();
   }

   @Override
   public synchronized void deleteRow() throws SQLException {
      this.inner.deleteRow();
   }

   @Override
   public synchronized int findColumn(String var1) throws SQLException {
      return this.inner.findColumn(var1);
   }

   @Override
   public synchronized boolean first() throws SQLException {
      return this.inner.first();
   }

   @Override
   public synchronized InputStream getAsciiStream(int var1) throws SQLException {
      return this.inner.getAsciiStream(var1);
   }

   @Override
   public synchronized InputStream getAsciiStream(String var1) throws SQLException {
      return this.inner.getAsciiStream(var1);
   }

   @Override
   public synchronized BigDecimal getBigDecimal(String var1, int var2) throws SQLException {
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
   public synchronized BigDecimal getBigDecimal(int var1, int var2) throws SQLException {
      return this.inner.getBigDecimal(var1, var2);
   }

   @Override
   public synchronized InputStream getBinaryStream(String var1) throws SQLException {
      return this.inner.getBinaryStream(var1);
   }

   @Override
   public synchronized InputStream getBinaryStream(int var1) throws SQLException {
      return this.inner.getBinaryStream(var1);
   }

   @Override
   public synchronized Blob getBlob(String var1) throws SQLException {
      return this.inner.getBlob(var1);
   }

   @Override
   public synchronized Blob getBlob(int var1) throws SQLException {
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
   public synchronized int getConcurrency() throws SQLException {
      return this.inner.getConcurrency();
   }

   @Override
   public synchronized String getCursorName() throws SQLException {
      return this.inner.getCursorName();
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
   public synchronized int getRow() throws SQLException {
      return this.inner.getRow();
   }

   @Override
   public synchronized RowId getRowId(int var1) throws SQLException {
      return this.inner.getRowId(var1);
   }

   @Override
   public synchronized RowId getRowId(String var1) throws SQLException {
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
   public synchronized Statement getStatement() throws SQLException {
      return this.inner.getStatement();
   }

   @Override
   public synchronized InputStream getUnicodeStream(int var1) throws SQLException {
      return this.inner.getUnicodeStream(var1);
   }

   @Override
   public synchronized InputStream getUnicodeStream(String var1) throws SQLException {
      return this.inner.getUnicodeStream(var1);
   }

   @Override
   public synchronized void insertRow() throws SQLException {
      this.inner.insertRow();
   }

   @Override
   public synchronized boolean isAfterLast() throws SQLException {
      return this.inner.isAfterLast();
   }

   @Override
   public synchronized boolean isBeforeFirst() throws SQLException {
      return this.inner.isBeforeFirst();
   }

   @Override
   public synchronized boolean isFirst() throws SQLException {
      return this.inner.isFirst();
   }

   @Override
   public synchronized boolean isLast() throws SQLException {
      return this.inner.isLast();
   }

   @Override
   public synchronized boolean last() throws SQLException {
      return this.inner.last();
   }

   @Override
   public synchronized void moveToCurrentRow() throws SQLException {
      this.inner.moveToCurrentRow();
   }

   @Override
   public synchronized void moveToInsertRow() throws SQLException {
      this.inner.moveToInsertRow();
   }

   @Override
   public synchronized void refreshRow() throws SQLException {
      this.inner.refreshRow();
   }

   @Override
   public synchronized boolean relative(int var1) throws SQLException {
      return this.inner.relative(var1);
   }

   @Override
   public synchronized boolean rowDeleted() throws SQLException {
      return this.inner.rowDeleted();
   }

   @Override
   public synchronized boolean rowInserted() throws SQLException {
      return this.inner.rowInserted();
   }

   @Override
   public synchronized boolean rowUpdated() throws SQLException {
      return this.inner.rowUpdated();
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
   public synchronized void updateArray(String var1, Array var2) throws SQLException {
      this.inner.updateArray(var1, var2);
   }

   @Override
   public synchronized void updateArray(int var1, Array var2) throws SQLException {
      this.inner.updateArray(var1, var2);
   }

   @Override
   public synchronized void updateAsciiStream(int var1, InputStream var2) throws SQLException {
      this.inner.updateAsciiStream(var1, var2);
   }

   @Override
   public synchronized void updateAsciiStream(int var1, InputStream var2, int var3) throws SQLException {
      this.inner.updateAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateAsciiStream(String var1, InputStream var2) throws SQLException {
      this.inner.updateAsciiStream(var1, var2);
   }

   @Override
   public synchronized void updateAsciiStream(String var1, InputStream var2, long var3) throws SQLException {
      this.inner.updateAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateAsciiStream(String var1, InputStream var2, int var3) throws SQLException {
      this.inner.updateAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateAsciiStream(int var1, InputStream var2, long var3) throws SQLException {
      this.inner.updateAsciiStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateBinaryStream(int var1, InputStream var2, long var3) throws SQLException {
      this.inner.updateBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateBinaryStream(String var1, InputStream var2) throws SQLException {
      this.inner.updateBinaryStream(var1, var2);
   }

   @Override
   public synchronized void updateBinaryStream(int var1, InputStream var2) throws SQLException {
      this.inner.updateBinaryStream(var1, var2);
   }

   @Override
   public synchronized void updateBinaryStream(String var1, InputStream var2, long var3) throws SQLException {
      this.inner.updateBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateBinaryStream(int var1, InputStream var2, int var3) throws SQLException {
      this.inner.updateBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateBinaryStream(String var1, InputStream var2, int var3) throws SQLException {
      this.inner.updateBinaryStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateBlob(int var1, Blob var2) throws SQLException {
      this.inner.updateBlob(var1, var2);
   }

   @Override
   public synchronized void updateBlob(String var1, Blob var2) throws SQLException {
      this.inner.updateBlob(var1, var2);
   }

   @Override
   public synchronized void updateBlob(String var1, InputStream var2) throws SQLException {
      this.inner.updateBlob(var1, var2);
   }

   @Override
   public synchronized void updateBlob(String var1, InputStream var2, long var3) throws SQLException {
      this.inner.updateBlob(var1, var2, var3);
   }

   @Override
   public synchronized void updateBlob(int var1, InputStream var2, long var3) throws SQLException {
      this.inner.updateBlob(var1, var2, var3);
   }

   @Override
   public synchronized void updateBlob(int var1, InputStream var2) throws SQLException {
      this.inner.updateBlob(var1, var2);
   }

   @Override
   public synchronized void updateBoolean(String var1, boolean var2) throws SQLException {
      this.inner.updateBoolean(var1, var2);
   }

   @Override
   public synchronized void updateBoolean(int var1, boolean var2) throws SQLException {
      this.inner.updateBoolean(var1, var2);
   }

   @Override
   public synchronized void updateByte(String var1, byte var2) throws SQLException {
      this.inner.updateByte(var1, var2);
   }

   @Override
   public synchronized void updateByte(int var1, byte var2) throws SQLException {
      this.inner.updateByte(var1, var2);
   }

   @Override
   public synchronized void updateBytes(String var1, byte[] var2) throws SQLException {
      this.inner.updateBytes(var1, var2);
   }

   @Override
   public synchronized void updateBytes(int var1, byte[] var2) throws SQLException {
      this.inner.updateBytes(var1, var2);
   }

   @Override
   public synchronized void updateCharacterStream(String var1, Reader var2) throws SQLException {
      this.inner.updateCharacterStream(var1, var2);
   }

   @Override
   public synchronized void updateCharacterStream(String var1, Reader var2, int var3) throws SQLException {
      this.inner.updateCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateCharacterStream(int var1, Reader var2, long var3) throws SQLException {
      this.inner.updateCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateCharacterStream(String var1, Reader var2, long var3) throws SQLException {
      this.inner.updateCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateCharacterStream(int var1, Reader var2) throws SQLException {
      this.inner.updateCharacterStream(var1, var2);
   }

   @Override
   public synchronized void updateCharacterStream(int var1, Reader var2, int var3) throws SQLException {
      this.inner.updateCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateClob(String var1, Reader var2, long var3) throws SQLException {
      this.inner.updateClob(var1, var2, var3);
   }

   @Override
   public synchronized void updateClob(int var1, Reader var2, long var3) throws SQLException {
      this.inner.updateClob(var1, var2, var3);
   }

   @Override
   public synchronized void updateClob(String var1, Reader var2) throws SQLException {
      this.inner.updateClob(var1, var2);
   }

   @Override
   public synchronized void updateClob(int var1, Reader var2) throws SQLException {
      this.inner.updateClob(var1, var2);
   }

   @Override
   public synchronized void updateClob(int var1, Clob var2) throws SQLException {
      this.inner.updateClob(var1, var2);
   }

   @Override
   public synchronized void updateClob(String var1, Clob var2) throws SQLException {
      this.inner.updateClob(var1, var2);
   }

   @Override
   public synchronized void updateDate(int var1, Date var2) throws SQLException {
      this.inner.updateDate(var1, var2);
   }

   @Override
   public synchronized void updateDate(String var1, Date var2) throws SQLException {
      this.inner.updateDate(var1, var2);
   }

   @Override
   public synchronized void updateDouble(int var1, double var2) throws SQLException {
      this.inner.updateDouble(var1, var2);
   }

   @Override
   public synchronized void updateDouble(String var1, double var2) throws SQLException {
      this.inner.updateDouble(var1, var2);
   }

   @Override
   public synchronized void updateFloat(String var1, float var2) throws SQLException {
      this.inner.updateFloat(var1, var2);
   }

   @Override
   public synchronized void updateFloat(int var1, float var2) throws SQLException {
      this.inner.updateFloat(var1, var2);
   }

   @Override
   public synchronized void updateInt(String var1, int var2) throws SQLException {
      this.inner.updateInt(var1, var2);
   }

   @Override
   public synchronized void updateInt(int var1, int var2) throws SQLException {
      this.inner.updateInt(var1, var2);
   }

   @Override
   public synchronized void updateLong(String var1, long var2) throws SQLException {
      this.inner.updateLong(var1, var2);
   }

   @Override
   public synchronized void updateLong(int var1, long var2) throws SQLException {
      this.inner.updateLong(var1, var2);
   }

   @Override
   public synchronized void updateNCharacterStream(int var1, Reader var2) throws SQLException {
      this.inner.updateNCharacterStream(var1, var2);
   }

   @Override
   public synchronized void updateNCharacterStream(String var1, Reader var2) throws SQLException {
      this.inner.updateNCharacterStream(var1, var2);
   }

   @Override
   public synchronized void updateNCharacterStream(String var1, Reader var2, long var3) throws SQLException {
      this.inner.updateNCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateNCharacterStream(int var1, Reader var2, long var3) throws SQLException {
      this.inner.updateNCharacterStream(var1, var2, var3);
   }

   @Override
   public synchronized void updateNClob(int var1, Reader var2) throws SQLException {
      this.inner.updateNClob(var1, var2);
   }

   @Override
   public synchronized void updateNClob(String var1, Reader var2) throws SQLException {
      this.inner.updateNClob(var1, var2);
   }

   @Override
   public synchronized void updateNClob(int var1, Reader var2, long var3) throws SQLException {
      this.inner.updateNClob(var1, var2, var3);
   }

   @Override
   public synchronized void updateNClob(int var1, NClob var2) throws SQLException {
      this.inner.updateNClob(var1, var2);
   }

   @Override
   public synchronized void updateNClob(String var1, Reader var2, long var3) throws SQLException {
      this.inner.updateNClob(var1, var2, var3);
   }

   @Override
   public synchronized void updateNClob(String var1, NClob var2) throws SQLException {
      this.inner.updateNClob(var1, var2);
   }

   @Override
   public synchronized void updateNString(String var1, String var2) throws SQLException {
      this.inner.updateNString(var1, var2);
   }

   @Override
   public synchronized void updateNString(int var1, String var2) throws SQLException {
      this.inner.updateNString(var1, var2);
   }

   @Override
   public synchronized void updateNull(int var1) throws SQLException {
      this.inner.updateNull(var1);
   }

   @Override
   public synchronized void updateNull(String var1) throws SQLException {
      this.inner.updateNull(var1);
   }

   @Override
   public synchronized void updateObject(int var1, Object var2) throws SQLException {
      this.inner.updateObject(var1, var2);
   }

   @Override
   public synchronized void updateObject(String var1, Object var2) throws SQLException {
      this.inner.updateObject(var1, var2);
   }

   @Override
   public synchronized void updateObject(String var1, Object var2, int var3) throws SQLException {
      this.inner.updateObject(var1, var2, var3);
   }

   @Override
   public synchronized void updateObject(int var1, Object var2, int var3) throws SQLException {
      this.inner.updateObject(var1, var2, var3);
   }

   @Override
   public synchronized void updateRef(int var1, Ref var2) throws SQLException {
      this.inner.updateRef(var1, var2);
   }

   @Override
   public synchronized void updateRef(String var1, Ref var2) throws SQLException {
      this.inner.updateRef(var1, var2);
   }

   @Override
   public synchronized void updateRow() throws SQLException {
      this.inner.updateRow();
   }

   @Override
   public synchronized void updateRowId(int var1, RowId var2) throws SQLException {
      this.inner.updateRowId(var1, var2);
   }

   @Override
   public synchronized void updateRowId(String var1, RowId var2) throws SQLException {
      this.inner.updateRowId(var1, var2);
   }

   @Override
   public synchronized void updateSQLXML(int var1, SQLXML var2) throws SQLException {
      this.inner.updateSQLXML(var1, var2);
   }

   @Override
   public synchronized void updateSQLXML(String var1, SQLXML var2) throws SQLException {
      this.inner.updateSQLXML(var1, var2);
   }

   @Override
   public synchronized void updateShort(String var1, short var2) throws SQLException {
      this.inner.updateShort(var1, var2);
   }

   @Override
   public synchronized void updateShort(int var1, short var2) throws SQLException {
      this.inner.updateShort(var1, var2);
   }

   @Override
   public synchronized void updateString(String var1, String var2) throws SQLException {
      this.inner.updateString(var1, var2);
   }

   @Override
   public synchronized void updateString(int var1, String var2) throws SQLException {
      this.inner.updateString(var1, var2);
   }

   @Override
   public synchronized void updateTime(String var1, Time var2) throws SQLException {
      this.inner.updateTime(var1, var2);
   }

   @Override
   public synchronized void updateTime(int var1, Time var2) throws SQLException {
      this.inner.updateTime(var1, var2);
   }

   @Override
   public synchronized void updateTimestamp(String var1, Timestamp var2) throws SQLException {
      this.inner.updateTimestamp(var1, var2);
   }

   @Override
   public synchronized void updateTimestamp(int var1, Timestamp var2) throws SQLException {
      this.inner.updateTimestamp(var1, var2);
   }

   @Override
   public synchronized boolean wasNull() throws SQLException {
      return this.inner.wasNull();
   }

   @Override
   public synchronized Object getObject(int var1, Class var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(String var1) throws SQLException {
      return this.inner.getObject(var1);
   }

   @Override
   public synchronized Object getObject(String var1, Class var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(int var1, Map var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(String var1, Map var2) throws SQLException {
      return this.inner.getObject(var1, var2);
   }

   @Override
   public synchronized Object getObject(int var1) throws SQLException {
      return this.inner.getObject(var1);
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
   public synchronized byte getByte(int var1) throws SQLException {
      return this.inner.getByte(var1);
   }

   @Override
   public synchronized byte getByte(String var1) throws SQLException {
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
   public synchronized int getInt(String var1) throws SQLException {
      return this.inner.getInt(var1);
   }

   @Override
   public synchronized int getInt(int var1) throws SQLException {
      return this.inner.getInt(var1);
   }

   @Override
   public synchronized long getLong(String var1) throws SQLException {
      return this.inner.getLong(var1);
   }

   @Override
   public synchronized long getLong(int var1) throws SQLException {
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
   public synchronized double getDouble(int var1) throws SQLException {
      return this.inner.getDouble(var1);
   }

   @Override
   public synchronized double getDouble(String var1) throws SQLException {
      return this.inner.getDouble(var1);
   }

   @Override
   public synchronized byte[] getBytes(String var1) throws SQLException {
      return this.inner.getBytes(var1);
   }

   @Override
   public synchronized byte[] getBytes(int var1) throws SQLException {
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
   public synchronized boolean next() throws SQLException {
      return this.inner.next();
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
   public synchronized void close() throws SQLException {
      this.inner.close();
   }

   @Override
   public synchronized int getType() throws SQLException {
      return this.inner.getType();
   }

   @Override
   public synchronized boolean previous() throws SQLException {
      return this.inner.previous();
   }

   @Override
   public synchronized Ref getRef(String var1) throws SQLException {
      return this.inner.getRef(var1);
   }

   @Override
   public synchronized Ref getRef(int var1) throws SQLException {
      return this.inner.getRef(var1);
   }

   @Override
   public synchronized String getString(int var1) throws SQLException {
      return this.inner.getString(var1);
   }

   @Override
   public synchronized String getString(String var1) throws SQLException {
      return this.inner.getString(var1);
   }

   @Override
   public synchronized Date getDate(int var1, Calendar var2) throws SQLException {
      return this.inner.getDate(var1, var2);
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
   public synchronized Date getDate(int var1) throws SQLException {
      return this.inner.getDate(var1);
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
   public synchronized Time getTime(String var1) throws SQLException {
      return this.inner.getTime(var1);
   }

   @Override
   public synchronized Time getTime(int var1, Calendar var2) throws SQLException {
      return this.inner.getTime(var1, var2);
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
   public synchronized Timestamp getTimestamp(String var1, Calendar var2) throws SQLException {
      return this.inner.getTimestamp(var1, var2);
   }

   @Override
   public synchronized Timestamp getTimestamp(int var1, Calendar var2) throws SQLException {
      return this.inner.getTimestamp(var1, var2);
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
