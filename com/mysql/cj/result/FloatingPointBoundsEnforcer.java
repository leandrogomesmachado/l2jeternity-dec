package com.mysql.cj.result;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.NumberOutOfRange;
import java.math.BigDecimal;
import java.math.BigInteger;

public class FloatingPointBoundsEnforcer<T> extends BaseDecoratingValueFactory<T> {
   private double min;
   private double max;

   public FloatingPointBoundsEnforcer(ValueFactory<T> targetVf, double min, double max) {
      super(targetVf);
      this.min = min;
      this.max = max;
   }

   @Override
   public T createFromLong(long l) {
      if (!((double)l < this.min) && !((double)l > this.max)) {
         return this.targetVf.createFromLong(l);
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{l, this.targetVf.getTargetTypeName()}));
      }
   }

   @Override
   public T createFromBigInteger(BigInteger i) {
      if (new BigDecimal(i).compareTo(BigDecimal.valueOf(this.min)) >= 0 && new BigDecimal(i).compareTo(BigDecimal.valueOf(this.max)) <= 0) {
         return this.targetVf.createFromBigInteger(i);
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{i, this.targetVf.getTargetTypeName()}));
      }
   }

   @Override
   public T createFromDouble(double d) {
      if (!(d < this.min) && !(d > this.max)) {
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
