package com.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class DoubleValueFactory extends DefaultValueFactory<Double> {
   public Double createFromBigInteger(BigInteger i) {
      return i.doubleValue();
   }

   public Double createFromLong(long l) {
      return (double)l;
   }

   public Double createFromBigDecimal(BigDecimal d) {
      return d.doubleValue();
   }

   public Double createFromDouble(double d) {
      return d;
   }

   public Double createFromBit(byte[] bytes, int offset, int length) {
      return new BigInteger(ByteBuffer.allocate(length + 1).put((byte)0).put(bytes, offset, length).array()).doubleValue();
   }

   public Double createFromNull() {
      return 0.0;
   }

   @Override
   public String getTargetTypeName() {
      return Double.class.getName();
   }
}
