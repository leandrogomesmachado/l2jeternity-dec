package com.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface ValueFactory<T> {
   T createFromDate(int var1, int var2, int var3);

   T createFromTime(int var1, int var2, int var3, int var4);

   T createFromTimestamp(int var1, int var2, int var3, int var4, int var5, int var6, int var7);

   T createFromLong(long var1);

   T createFromBigInteger(BigInteger var1);

   T createFromDouble(double var1);

   T createFromBigDecimal(BigDecimal var1);

   T createFromBytes(byte[] var1, int var2, int var3);

   T createFromBit(byte[] var1, int var2, int var3);

   T createFromNull();

   String getTargetTypeName();
}
