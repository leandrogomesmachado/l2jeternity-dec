package org.apache.commons.dbcp;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class DelegatingPreparedStatement extends DelegatingStatement implements PreparedStatement {
   public DelegatingPreparedStatement(DelegatingConnection c, PreparedStatement s) {
      super(c, s);
   }

   @Override
   public boolean equals(Object obj) {
      PreparedStatement delegate = (PreparedStatement)this.getInnermostDelegate();
      if (delegate == null) {
         return false;
      } else if (obj instanceof DelegatingPreparedStatement) {
         DelegatingPreparedStatement s = (DelegatingPreparedStatement)obj;
         return delegate.equals(s.getInnermostDelegate());
      } else {
         return delegate.equals(obj);
      }
   }

   public void setDelegate(PreparedStatement s) {
      super.setDelegate(s);
      this._stmt = s;
   }

   @Override
   public ResultSet executeQuery() throws SQLException {
      this.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this, ((PreparedStatement)this._stmt).executeQuery());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public int executeUpdate() throws SQLException {
      this.checkOpen();

      try {
         return ((PreparedStatement)this._stmt).executeUpdate();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public void setNull(int parameterIndex, int sqlType) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNull(parameterIndex, sqlType);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBoolean(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setByte(int parameterIndex, byte x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setByte(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setShort(int parameterIndex, short x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setShort(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setInt(int parameterIndex, int x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setInt(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setLong(int parameterIndex, long x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setLong(parameterIndex, x);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setFloat(int parameterIndex, float x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setFloat(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setDouble(int parameterIndex, double x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setDouble(parameterIndex, x);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBigDecimal(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setString(int parameterIndex, String x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setString(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBytes(int parameterIndex, byte[] x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBytes(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setDate(int parameterIndex, Date x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setDate(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setTime(int parameterIndex, Time x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setTime(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setTimestamp(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setAsciiStream(parameterIndex, x, length);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   /** @deprecated */
   @Override
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setUnicodeStream(parameterIndex, x, length);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBinaryStream(parameterIndex, x, length);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void clearParameters() throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).clearParameters();
      } catch (SQLException var2) {
         this.handleException(var2);
      }
   }

   @Override
   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setObject(parameterIndex, x, targetSqlType, scale);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setObject(parameterIndex, x, targetSqlType);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setObject(int parameterIndex, Object x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setObject(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public boolean execute() throws SQLException {
      this.checkOpen();

      try {
         return ((PreparedStatement)this._stmt).execute();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public void addBatch() throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).addBatch();
      } catch (SQLException var2) {
         this.handleException(var2);
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setCharacterStream(parameterIndex, reader, length);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setRef(int i, Ref x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setRef(i, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBlob(int i, Blob x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBlob(i, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setClob(int i, Clob x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setClob(i, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setArray(int i, Array x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setArray(i, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public ResultSetMetaData getMetaData() throws SQLException {
      this.checkOpen();

      try {
         return ((PreparedStatement)this._stmt).getMetaData();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setDate(parameterIndex, x, cal);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setTime(parameterIndex, x, cal);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setTimestamp(parameterIndex, x, cal);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNull(paramIndex, sqlType, typeName);
      } catch (SQLException var5) {
         this.handleException(var5);
      }
   }

   @Override
   public String toString() {
      return this._stmt.toString();
   }

   @Override
   public void setURL(int parameterIndex, URL x) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setURL(parameterIndex, x);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
      this.checkOpen();

      try {
         return ((PreparedStatement)this._stmt).getParameterMetaData();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public void setRowId(int parameterIndex, RowId value) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setRowId(parameterIndex, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNString(int parameterIndex, String value) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNString(parameterIndex, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNCharacterStream(parameterIndex, value, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setNClob(int parameterIndex, NClob value) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNClob(parameterIndex, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setClob(parameterIndex, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBlob(parameterIndex, inputStream, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNClob(parameterIndex, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setSQLXML(int parameterIndex, SQLXML value) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setSQLXML(parameterIndex, value);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setAsciiStream(parameterIndex, inputStream, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBinaryStream(parameterIndex, inputStream, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setCharacterStream(parameterIndex, reader, length);
      } catch (SQLException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream inputStream) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setAsciiStream(parameterIndex, inputStream);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBinaryStream(parameterIndex, inputStream);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setCharacterStream(parameterIndex, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNCharacterStream(parameterIndex, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setClob(int parameterIndex, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setClob(parameterIndex, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setBlob(parameterIndex, inputStream);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }

   @Override
   public void setNClob(int parameterIndex, Reader reader) throws SQLException {
      this.checkOpen();

      try {
         ((PreparedStatement)this._stmt).setNClob(parameterIndex, reader);
      } catch (SQLException var4) {
         this.handleException(var4);
      }
   }
}
