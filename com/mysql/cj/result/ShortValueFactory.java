package com.mysql.cj.result;

import com.mysql.cj.util.DataTypeUtil;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ShortValueFactory extends DefaultValueFactory<Short> {
   public Short createFromBigInteger(BigInteger i) {
      return (short)i.intValue();
   }

   public Short createFromLong(long l) {
      return (short)((int)l);
   }

   public Short createFromBigDecimal(BigDecimal d) {
      return (short)((int)d.longValue());
   }

   public Short createFromDouble(double d) {
      return (short)((int)d);
   }

   public Short createFromBit(byte[] bytes, int offset, int length) {
      return this.createFromLong(DataTypeUtil.bitToLong(bytes, offset, length));
   }

   public Short createFromNull() {
      return (short)0;
   }

   @Override
   public String getTargetTypeName() {
      return Short.class.getName();
   }
}
