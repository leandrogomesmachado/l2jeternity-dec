package com.mysql.cj.result;

import com.mysql.cj.util.DataTypeUtil;
import java.math.BigDecimal;
import java.math.BigInteger;

public class LongValueFactory extends DefaultValueFactory<Long> {
   public Long createFromBigInteger(BigInteger i) {
      return i.longValue();
   }

   public Long createFromLong(long l) {
      return l;
   }

   public Long createFromBigDecimal(BigDecimal d) {
      return d.longValue();
   }

   public Long createFromDouble(double d) {
      return (long)d;
   }

   public Long createFromBit(byte[] bytes, int offset, int length) {
      return DataTypeUtil.bitToLong(bytes, offset, length);
   }

   public Long createFromNull() {
      return 0L;
   }

   @Override
   public String getTargetTypeName() {
      return Long.class.getName();
   }
}
