package com.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ByteValueFactory extends DefaultValueFactory<Byte> {
   public Byte createFromBigInteger(BigInteger i) {
      return (byte)i.intValue();
   }

   public Byte createFromLong(long l) {
      return (byte)((int)l);
   }

   public Byte createFromBigDecimal(BigDecimal d) {
      return (byte)((int)d.longValue());
   }

   public Byte createFromDouble(double d) {
      return (byte)((int)d);
   }

   public Byte createFromBit(byte[] bytes, int offset, int length) {
      return bytes[offset + length - 1];
   }

   public Byte createFromNull() {
      return (byte)0;
   }

   @Override
   public String getTargetTypeName() {
      return Byte.class.getName();
   }
}
