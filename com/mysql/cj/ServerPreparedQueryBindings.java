package com.mysql.cj;

import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerPreparedQueryBindings extends AbstractQueryBindings<ServerPreparedQueryBindValue> {
   private AtomicBoolean sendTypesToServer = new AtomicBoolean(false);
   private boolean longParameterSwitchDetected = false;

   public ServerPreparedQueryBindings(int parameterCount, Session sess) {
      super(parameterCount, sess);
   }

   @Override
   protected void initBindValues(int parameterCount) {
      this.bindValues = new ServerPreparedQueryBindValue[parameterCount];

      for(int i = 0; i < parameterCount; ++i) {
         this.bindValues[i] = new ServerPreparedQueryBindValue();
      }
   }

   public ServerPreparedQueryBindings clone() {
      ServerPreparedQueryBindings newBindings = new ServerPreparedQueryBindings(((ServerPreparedQueryBindValue[])this.bindValues).length, this.session);
      ServerPreparedQueryBindValue[] bvs = new ServerPreparedQueryBindValue[((ServerPreparedQueryBindValue[])this.bindValues).length];

      for(int i = 0; i < ((ServerPreparedQueryBindValue[])this.bindValues).length; ++i) {
         bvs[i] = this.bindValues[i].clone();
      }

      newBindings.bindValues = bvs;
      newBindings.sendTypesToServer = this.sendTypesToServer;
      newBindings.longParameterSwitchDetected = this.longParameterSwitchDetected;
      newBindings.isLoadDataQuery = this.isLoadDataQuery;
      return newBindings;
   }

   public ServerPreparedQueryBindValue getBinding(int parameterIndex, boolean forLongData) {
      if (((ServerPreparedQueryBindValue[])this.bindValues)[parameterIndex] != null && this.bindValues[parameterIndex].isLongData && !forLongData) {
         this.longParameterSwitchDetected = true;
      }

      return this.bindValues[parameterIndex];
   }

   @Override
   public void checkParameterSet(int columnIndex) {
      if (!this.bindValues[columnIndex].isSet()) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            Messages.getString("ServerPreparedStatement.13") + (columnIndex + 1) + Messages.getString("ServerPreparedStatement.14")
         );
      }
   }

   public AtomicBoolean getSendTypesToServer() {
      return this.sendTypesToServer;
   }

   public boolean isLongParameterSwitchDetected() {
      return this.longParameterSwitchDetected;
   }

   public void setLongParameterSwitchDetected(boolean longParameterSwitchDetected) {
      this.longParameterSwitchDetected = longParameterSwitchDetected;
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x) {
      this.setAsciiStream(parameterIndex, x, -1);
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, int length) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, true);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(252, (long)this.numberOfExecutions));
         binding.value = x;
         binding.isLongData = true;
         binding.bindLength = this.useStreamLengthsInPrepStmts.getValue() ? (long)length : -1L;
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, long length) {
      this.setAsciiStream(parameterIndex, x, (int)length);
      this.bindValues[parameterIndex].setMysqlType(MysqlType.TEXT);
   }

   @Override
   public void setBigDecimal(int parameterIndex, BigDecimal x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(246, (long)this.numberOfExecutions));
         binding.value = StringUtils.fixDecimalExponent(x.toPlainString());
      }
   }

   @Override
   public void setBigInteger(int parameterIndex, BigInteger x) {
      this.setValue(parameterIndex, x.toString(), MysqlType.BIGINT_UNSIGNED);
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x) {
      this.setBinaryStream(parameterIndex, x, -1);
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, int length) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, true);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(252, (long)this.numberOfExecutions));
         binding.value = x;
         binding.isLongData = true;
         binding.bindLength = this.useStreamLengthsInPrepStmts.getValue() ? (long)length : -1L;
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, long length) {
      this.setBinaryStream(parameterIndex, x, (int)length);
   }

   @Override
   public void setBlob(int parameterIndex, InputStream inputStream) {
      this.setBinaryStream(parameterIndex, inputStream);
   }

   @Override
   public void setBlob(int parameterIndex, InputStream inputStream, long length) {
      this.setBinaryStream(parameterIndex, inputStream, (int)length);
   }

   @Override
   public void setBlob(int parameterIndex, Blob x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         try {
            ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, true);
            this.sendTypesToServer.compareAndSet(false, binding.resetToType(252, (long)this.numberOfExecutions));
            binding.value = x;
            binding.isLongData = true;
            binding.bindLength = this.useStreamLengthsInPrepStmts.getValue() ? x.length() : -1L;
         } catch (Throwable var4) {
            throw ExceptionFactory.createException(var4.getMessage(), var4);
         }
      }
   }

   @Override
   public void setBoolean(int parameterIndex, boolean x) {
      this.setByte(parameterIndex, (byte)(x ? 1 : 0));
   }

   @Override
   public void setByte(int parameterIndex, byte x) {
      ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
      this.sendTypesToServer.compareAndSet(false, binding.resetToType(1, (long)this.numberOfExecutions));
      binding.longBinding = (long)x;
   }

   @Override
   public void setBytes(int parameterIndex, byte[] x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(253, (long)this.numberOfExecutions));
         binding.value = x;
      }
   }

   @Override
   public void setBytes(int parameterIndex, byte[] x, boolean checkForIntroducer, boolean escapeForMBChars) {
      this.setBytes(parameterIndex, x);
   }

   @Override
   public void setBytesNoEscape(int parameterIndex, byte[] parameterAsBytes) {
      byte[] parameterWithQuotes = new byte[parameterAsBytes.length + 2];
      parameterWithQuotes[0] = 39;
      System.arraycopy(parameterAsBytes, 0, parameterWithQuotes, 1, parameterAsBytes.length);
      parameterWithQuotes[parameterAsBytes.length + 1] = 39;
      this.setValue(parameterIndex, parameterWithQuotes);
   }

   @Override
   public void setBytesNoEscapeNoQuotes(int parameterIndex, byte[] parameterAsBytes) {
      this.setBytes(parameterIndex, parameterAsBytes);
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader) {
      this.setCharacterStream(parameterIndex, reader, -1);
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, int length) {
      if (reader == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, true);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(252, (long)this.numberOfExecutions));
         binding.value = reader;
         binding.isLongData = true;
         binding.bindLength = this.useStreamLengthsInPrepStmts.getValue() ? (long)length : -1L;
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, long length) {
      this.setCharacterStream(parameterIndex, reader, (int)length);
   }

   @Override
   public void setClob(int parameterIndex, Reader reader) {
      this.setCharacterStream(parameterIndex, reader);
   }

   @Override
   public void setClob(int parameterIndex, Reader reader, long length) {
      this.setCharacterStream(parameterIndex, reader, length);
   }

   @Override
   public void setClob(int parameterIndex, Clob x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         try {
            ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, true);
            this.sendTypesToServer.compareAndSet(false, binding.resetToType(252, (long)this.numberOfExecutions));
            binding.value = x.getCharacterStream();
            binding.isLongData = true;
            binding.bindLength = this.useStreamLengthsInPrepStmts.getValue() ? x.length() : -1L;
         } catch (Throwable var4) {
            throw ExceptionFactory.createException(var4.getMessage(), var4);
         }
      }
   }

   @Override
   public void setDate(int parameterIndex, Date x) {
      this.setDate(parameterIndex, x, this.session.getServerSession().getDefaultTimeZone());
   }

   @Override
   public void setDate(int parameterIndex, Date x, Calendar cal) {
      this.setDate(parameterIndex, x, cal.getTimeZone());
   }

   @Override
   public void setDate(int parameterIndex, Date x, TimeZone tz) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(10, (long)this.numberOfExecutions));
         binding.value = x;
         binding.tz = tz;
      }
   }

   @Override
   public void setDouble(int parameterIndex, double x) {
      if (this.session.getPropertySet().getBooleanProperty("allowNanAndInf").getValue()
         || x != Double.POSITIVE_INFINITY && x != Double.NEGATIVE_INFINITY && !Double.isNaN(x)) {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(5, (long)this.numberOfExecutions));
         binding.doubleBinding = x;
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("PreparedStatement.64", new Object[]{x}), this.session.getExceptionInterceptor()
         );
      }
   }

   @Override
   public void setFloat(int parameterIndex, float x) {
      ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
      this.sendTypesToServer.compareAndSet(false, binding.resetToType(4, (long)this.numberOfExecutions));
      binding.floatBinding = x;
   }

   @Override
   public void setInt(int parameterIndex, int x) {
      ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
      this.sendTypesToServer.compareAndSet(false, binding.resetToType(3, (long)this.numberOfExecutions));
      binding.longBinding = (long)x;
   }

   @Override
   public void setLong(int parameterIndex, long x) {
      ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
      this.sendTypesToServer.compareAndSet(false, binding.resetToType(8, (long)this.numberOfExecutions));
      binding.longBinding = x;
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader value) {
      this.setNCharacterStream(parameterIndex, value, -1L);
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader reader, long length) {
      if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
         throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.28"), this.session.getExceptionInterceptor());
      } else {
         if (reader == null) {
            this.setNull(parameterIndex);
         } else {
            ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, true);
            this.sendTypesToServer.compareAndSet(false, binding.resetToType(252, (long)this.numberOfExecutions));
            binding.value = reader;
            binding.isLongData = true;
            binding.bindLength = this.useStreamLengthsInPrepStmts.getValue() ? length : -1L;
         }
      }
   }

   @Override
   public void setNClob(int parameterIndex, Reader reader) {
      this.setNCharacterStream(parameterIndex, reader);
   }

   @Override
   public void setNClob(int parameterIndex, Reader reader, long length) {
      if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
         throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.29"), this.session.getExceptionInterceptor());
      } else {
         this.setNCharacterStream(parameterIndex, reader, length);
      }
   }

   @Override
   public void setNClob(int parameterIndex, NClob value) {
      try {
         this.setNClob(parameterIndex, value.getCharacterStream(), this.useStreamLengthsInPrepStmts.getValue() ? value.length() : -1L);
      } catch (Throwable var4) {
         throw ExceptionFactory.createException(var4.getMessage(), var4, this.session.getExceptionInterceptor());
      }
   }

   @Override
   public void setNString(int parameterIndex, String x) {
      if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
         throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.30"), this.session.getExceptionInterceptor());
      } else {
         this.setString(parameterIndex, x);
      }
   }

   @Override
   public void setNull(int parameterIndex) {
      ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
      this.sendTypesToServer.compareAndSet(false, binding.resetToType(6, (long)this.numberOfExecutions));
      binding.setNull(true);
   }

   @Override
   public void setShort(int parameterIndex, short x) {
      ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
      this.sendTypesToServer.compareAndSet(false, binding.resetToType(2, (long)this.numberOfExecutions));
      binding.longBinding = (long)x;
   }

   @Override
   public void setString(int parameterIndex, String x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(253, (long)this.numberOfExecutions));
         binding.value = x;
      }
   }

   @Override
   public void setTime(int parameterIndex, Time x, Calendar cal) {
      this.setTime(parameterIndex, x, cal.getTimeZone());
   }

   @Override
   public void setTime(int parameterIndex, Time x) {
      this.setTime(parameterIndex, x, this.session.getServerSession().getDefaultTimeZone());
   }

   @Override
   public void setTime(int parameterIndex, Time x, TimeZone tz) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(11, (long)this.numberOfExecutions));
         binding.value = x;
         binding.tz = tz;
      }
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x) {
      this.setTimestamp(parameterIndex, x, this.session.getServerSession().getDefaultTimeZone());
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) {
      this.setTimestamp(parameterIndex, x, cal.getTimeZone());
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x, TimeZone tz) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         ServerPreparedQueryBindValue binding = this.getBinding(parameterIndex, false);
         this.sendTypesToServer.compareAndSet(false, binding.resetToType(12, (long)this.numberOfExecutions));
         if (!this.sendFractionalSeconds.getValue()) {
            x = TimeUtil.truncateFractionalSeconds(x);
         }

         binding.value = x;
         binding.tz = tz;
      }
   }
}
