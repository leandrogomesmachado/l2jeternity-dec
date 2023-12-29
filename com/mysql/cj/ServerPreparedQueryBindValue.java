package com.mysql.cj;

import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.StringUtils;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ServerPreparedQueryBindValue extends ClientPreparedQueryBindValue implements BindValue {
   public long boundBeforeExecutionNum = 0L;
   public long bindLength;
   public int bufferType;
   public double doubleBinding;
   public float floatBinding;
   public boolean isLongData;
   private boolean isSet = false;
   public long longBinding;
   public Object value;
   public TimeZone tz;

   public ServerPreparedQueryBindValue() {
   }

   public ServerPreparedQueryBindValue clone() {
      return new ServerPreparedQueryBindValue(this);
   }

   private ServerPreparedQueryBindValue(ServerPreparedQueryBindValue copyMe) {
      super(copyMe);
      this.value = copyMe.value;
      this.isSet = copyMe.isSet;
      this.isLongData = copyMe.isLongData;
      this.bufferType = copyMe.bufferType;
      this.bindLength = copyMe.bindLength;
      this.longBinding = copyMe.longBinding;
      this.floatBinding = copyMe.floatBinding;
      this.doubleBinding = copyMe.doubleBinding;
      this.tz = copyMe.tz;
   }

   @Override
   public void reset() {
      super.reset();
      this.isSet = false;
      this.value = null;
      this.isLongData = false;
      this.longBinding = 0L;
      this.floatBinding = 0.0F;
      this.doubleBinding = 0.0;
      this.tz = null;
   }

   public boolean resetToType(int bufType, long numberOfExecutions) {
      boolean sendTypesToServer = false;
      this.reset();
      if ((bufType != 6 || this.bufferType == 0) && this.bufferType != bufType) {
         sendTypesToServer = true;
         this.bufferType = bufType;
      }

      this.isSet = true;
      this.boundBeforeExecutionNum = numberOfExecutions;
      return sendTypesToServer;
   }

   @Override
   public String toString() {
      return this.toString(false);
   }

   public String toString(boolean quoteIfNeeded) {
      if (this.isLongData) {
         return "' STREAM DATA '";
      } else if (this.isNull) {
         return "NULL";
      } else {
         switch(this.bufferType) {
            case 1:
            case 2:
            case 3:
            case 8:
               return String.valueOf(this.longBinding);
            case 4:
               return String.valueOf(this.floatBinding);
            case 5:
               return String.valueOf(this.doubleBinding);
            case 7:
            case 10:
            case 11:
            case 12:
            case 15:
            case 253:
            case 254:
               if (quoteIfNeeded) {
                  return "'" + String.valueOf(this.value) + "'";
               }

               return String.valueOf(this.value);
            default:
               if (this.value instanceof byte[]) {
                  return "byte data";
               } else {
                  return quoteIfNeeded ? "'" + String.valueOf(this.value) + "'" : String.valueOf(this.value);
               }
         }
      }
   }

   public long getBoundLength() {
      if (this.isNull) {
         return 0L;
      } else if (this.isLongData) {
         return this.bindLength;
      } else {
         switch(this.bufferType) {
            case 0:
            case 15:
            case 246:
            case 253:
            case 254:
               if (this.value instanceof byte[]) {
                  return (long)((byte[])this.value).length;
               }

               return (long)((String)this.value).length();
            case 1:
               return 1L;
            case 2:
               return 2L;
            case 3:
               return 4L;
            case 4:
               return 4L;
            case 5:
               return 8L;
            case 7:
            case 12:
               return 11L;
            case 8:
               return 8L;
            case 10:
               return 7L;
            case 11:
               return 9L;
            default:
               return 0L;
         }
      }
   }

   @Override
   public boolean isSet() {
      return this.isSet;
   }

   public void storeBinding(NativePacketPayload intoPacket, boolean isLoadDataQuery, String characterEncoding, ExceptionInterceptor interceptor) {
      synchronized(this) {
         try {
            switch(this.bufferType) {
               case 0:
               case 15:
               case 246:
               case 253:
               case 254:
                  if (this.value instanceof byte[]) {
                     intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, (byte[])this.value);
                     return;
                  } else {
                     if (!isLoadDataQuery) {
                        intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes((String)this.value, characterEncoding));
                     } else {
                        intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes((String)this.value));
                     }

                     return;
                  }
               case 1:
                  intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, this.longBinding);
                  return;
               case 2:
                  intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, this.longBinding);
                  return;
               case 3:
                  intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, this.longBinding);
                  return;
               case 4:
                  intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)Float.floatToIntBits(this.floatBinding));
                  return;
               case 5:
                  intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, Double.doubleToLongBits(this.doubleBinding));
                  return;
               case 7:
               case 10:
               case 12:
                  this.storeDateTime(intoPacket);
                  return;
               case 8:
                  intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, this.longBinding);
                  return;
               case 11:
                  this.storeTime(intoPacket);
                  return;
            }
         } catch (CJException var8) {
            throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.22") + characterEncoding + "'", var8, interceptor);
         }
      }
   }

   private void storeTime(NativePacketPayload intoPacket) {
      intoPacket.ensureCapacity(9);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 8L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, 0L);
      Calendar cal = Calendar.getInstance(this.tz);
      cal.setTime((Date)this.value);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)cal.get(11));
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)cal.get(12));
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)cal.get(13));
   }

   private void storeDateTime(NativePacketPayload intoPacket) {
      synchronized(this) {
         Calendar cal = Calendar.getInstance(this.tz);
         cal.setTime((Date)this.value);
         if (this.value instanceof java.sql.Date) {
            cal.set(11, 0);
            cal.set(12, 0);
            cal.set(13, 0);
         }

         byte length = 7;
         if (this.value instanceof Timestamp) {
            length = 11;
         }

         intoPacket.ensureCapacity(length);
         intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)length);
         int year = cal.get(1);
         int month = cal.get(2) + 1;
         int date = cal.get(5);
         intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, (long)year);
         intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)month);
         intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)date);
         if (this.value instanceof java.sql.Date) {
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
         } else {
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)cal.get(11));
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)cal.get(12));
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)cal.get(13));
         }

         if (length == 11) {
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)(((Timestamp)this.value).getNanos() / 1000));
         }
      }
   }
}
