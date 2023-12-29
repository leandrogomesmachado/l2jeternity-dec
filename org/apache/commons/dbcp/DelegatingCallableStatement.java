package org.apache.commons.dbcp;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class DelegatingCallableStatement extends DelegatingPreparedStatement implements CallableStatement {
   public DelegatingCallableStatement(DelegatingConnection c, CallableStatement s) {
      super(c, s);
   }

   @Override
   public boolean equals(Object obj) {
      CallableStatement delegate = (CallableStatement)this.getInnermostDelegate();
      if (delegate == null) {
         return false;
      } else if (obj instanceof DelegatingCallableStatement) {
         DelegatingCallableStatement s = (DelegatingCallableStatement)obj;
         return delegate.equals(s.getInnermostDelegate());
      } else {
         return delegate.equals(obj);
      }
   }

   public void setDelegate(CallableStatement s) {
      super.setDelegate(s);
      this._stmt = s;
   }

   @Override
   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).registerOutParameter(parameterIndex, sqlType);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).registerOutParameter(parameterIndex, sqlType, scale);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public boolean wasNull() throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).wasNull();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public String getString(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getString(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public boolean getBoolean(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBoolean(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public byte getByte(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getByte(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public short getShort(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getShort(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public int getInt(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getInt(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public long getLong(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getLong(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0L;
      }
   }

   @Override
   public float getFloat(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getFloat(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0.0F;
      }
   }

   @Override
   public double getDouble(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getDouble(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0.0;
      }
   }

   /** @deprecated */
   @Override
   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBigDecimal(parameterIndex, scale);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public byte[] getBytes(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBytes(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Date getDate(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getDate(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Time getTime(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTime(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Timestamp getTimestamp(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTimestamp(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Object getObject(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getObject(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBigDecimal(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Object getObject(int i, Map map) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getObject(i, map);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public Ref getRef(int i) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getRef(i);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Blob getBlob(int i) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBlob(i);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Clob getClob(int i) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getClob(i);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Array getArray(int i) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getArray(i);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getDate(parameterIndex, cal);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTime(parameterIndex, cal);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTimestamp(parameterIndex, cal);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).registerOutParameter(paramIndex, sqlType, typeName);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).registerOutParameter(parameterName, sqlType);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).registerOutParameter(parameterName, sqlType, scale);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).registerOutParameter(parameterName, sqlType, typeName);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public URL getURL(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getURL(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public void setURL(String parameterName, URL val) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setURL(parameterName, val);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNull(String parameterName, int sqlType) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNull(parameterName, sqlType);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBoolean(String parameterName, boolean x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBoolean(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setByte(String parameterName, byte x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setByte(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setShort(String parameterName, short x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setShort(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setInt(String parameterName, int x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setInt(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setLong(String parameterName, long x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setLong(parameterName, x);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setFloat(String parameterName, float x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setFloat(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setDouble(String parameterName, double x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setDouble(parameterName, x);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBigDecimal(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setString(String parameterName, String x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setString(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBytes(String parameterName, byte[] x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBytes(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setDate(String parameterName, Date x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setDate(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setTime(String parameterName, Time x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setTime(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setTimestamp(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setAsciiStream(parameterName, x, length);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBinaryStream(parameterName, x, length);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setObject(parameterName, x, targetSqlType, scale);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setObject(parameterName, x, targetSqlType);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setObject(String parameterName, Object x) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setObject(parameterName, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
      this.checkOpen();
      ((CallableStatement)this._stmt).setCharacterStream(parameterName, reader, length);
   }

   @Override
   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setDate(parameterName, x, cal);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setTime(parameterName, x, cal);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setTimestamp(parameterName, x, cal);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNull(parameterName, sqlType, typeName);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public String getString(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getString(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public boolean getBoolean(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBoolean(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public byte getByte(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getByte(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public short getShort(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getShort(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public int getInt(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getInt(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0;
      }
   }

   @Override
   public long getLong(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getLong(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0L;
      }
   }

   @Override
   public float getFloat(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getFloat(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0.0F;
      }
   }

   @Override
   public double getDouble(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getDouble(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return 0.0;
      }
   }

   @Override
   public byte[] getBytes(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBytes(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Date getDate(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getDate(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Time getTime(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTime(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Timestamp getTimestamp(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTimestamp(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Object getObject(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getObject(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public BigDecimal getBigDecimal(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBigDecimal(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Object getObject(String parameterName, Map map) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getObject(parameterName, map);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public Ref getRef(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getRef(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Blob getBlob(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getBlob(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Clob getClob(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getClob(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Array getArray(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getArray(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Date getDate(String parameterName, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getDate(parameterName, cal);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public Time getTime(String parameterName, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTime(parameterName, cal);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getTimestamp(parameterName, cal);
      } catch (SQLException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Override
   public URL getURL(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getURL(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public RowId getRowId(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getRowId(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public RowId getRowId(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getRowId(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public void setRowId(String parameterName, RowId value) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setRowId(parameterName, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNString(String parameterName, String value) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNString(parameterName, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNCharacterStream(parameterName, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setNClob(String parameterName, NClob value) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNClob(parameterName, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setClob(String parameterName, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setClob(parameterName, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBlob(parameterName, inputStream, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNClob(parameterName, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public NClob getNClob(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getNClob(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public NClob getNClob(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getNClob(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public void setSQLXML(String parameterName, SQLXML value) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setSQLXML(parameterName, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public SQLXML getSQLXML(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getSQLXML(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public SQLXML getSQLXML(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getSQLXML(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public String getNString(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getNString(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public String getNString(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getNString(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Reader getNCharacterStream(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getNCharacterStream(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Reader getNCharacterStream(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getNCharacterStream(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Reader getCharacterStream(int parameterIndex) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getCharacterStream(parameterIndex);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public Reader getCharacterStream(String parameterName) throws SQLException {
      this.checkOpen();

      try {
         return ((CallableStatement)this._stmt).getCharacterStream(parameterName);
      } catch (SQLException var3) {
         this.handleException(var3);
         return null;
      }
   }

   @Override
   public void setBlob(String parameterName, Blob blob) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBlob(parameterName, blob);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setClob(String parameterName, Clob clob) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setClob(parameterName, clob);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setAsciiStream(String parameterName, InputStream inputStream, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setAsciiStream(parameterName, inputStream, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setBinaryStream(String parameterName, InputStream inputStream, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBinaryStream(parameterName, inputStream, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setCharacterStream(parameterName, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setAsciiStream(String parameterName, InputStream inputStream) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setAsciiStream(parameterName, inputStream);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBinaryStream(String parameterName, InputStream inputStream) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBinaryStream(parameterName, inputStream);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setCharacterStream(parameterName, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNCharacterStream(String parameterName, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNCharacterStream(parameterName, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setClob(String parameterName, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setClob(parameterName, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setBlob(parameterName, inputStream);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNClob(String parameterName, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((CallableStatement)this._stmt).setNClob(parameterName, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }
}
