package com.mysql.cj.result;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.NumberOutOfRange;
import java.math.BigDecimal;
import java.math.BigInteger;

public class IntegerBoundsEnforcer<T> extends BaseDecoratingValueFactory<T> {
   private long min;
   private long max;

   public IntegerBoundsEnforcer(ValueFactory<T> targetVf, long min, long max) {
      super(targetVf);
      this.min = min;
      this.max = max;
   }

   @Override
   public T createFromLong(long l) {
      if (l >= this.min && l <= this.max) {
         return this.targetVf.createFromLong(l);
      } else {
         throw new NumberOutOfRange(
            Messages.getString("ResultSet.NumberOutOfRange", new Object[]{Long.valueOf(l).toString(), this.targetVf.getTargetTypeName()})
         );
      }
   }

   @Override
   public T createFromBigInteger(BigInteger i) {
      if (i.compareTo(BigInteger.valueOf(this.min)) >= 0 && i.compareTo(BigInteger.valueOf(this.max)) <= 0) {
         return this.targetVf.createFromBigInteger(i);
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{i, this.targetVf.getTargetTypeName()}));
      }
   }

   @Override
   public T createFromDouble(double d) {
      if (!(d < (double)this.min) && !(d > (double)this.max)) {
         return this.targetVf.createFromDouble(d);
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{d, this.targetVf.getTargetTypeName()}));
      }
   }

   @Override
   public T createFromBigDecimal(BigDecimal d) {
      if (d.compareTo(BigDecimal.valueOf(this.min)) >= 0 && d.compareTo(BigDecimal.valueOf(this.max)) <= 0) {
         return this.targetVf.createFromBigDecimal(d);
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{d, this.targetVf.getTargetTypeName()}));
      }
   }
}
