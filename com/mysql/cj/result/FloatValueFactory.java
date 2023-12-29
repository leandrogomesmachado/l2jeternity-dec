package com.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class FloatValueFactory extends DefaultValueFactory<Float> {
   public Float createFromBigInteger(BigInteger i) {
      return (float)i.doubleValue();
   }

   public Float createFromLong(long l) {
      return (float)l;
   }

   public Float createFromBigDecimal(BigDecimal d) {
      return (float)d.doubleValue();
   }

   public Float createFromDouble(double d) {
      return (float)d;
   }

   public Float createFromBit(byte[] bytes, int offset, int length) {
      return new BigInteger(ByteBuffer.allocate(length + 1).put((byte)0).put(bytes, offset, length).array()).floatValue();
   }

   public Float createFromNull() {
      return 0.0F;
   }

   @Override
   public String getTargetTypeName() {
      return Float.class.getName();
   }
}
