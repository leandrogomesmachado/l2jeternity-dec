package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;

public class FourthMoment extends ThirdMoment implements Serializable {
   private static final long serialVersionUID = 4763990447117157611L;
   protected double m4;

   public FourthMoment() {
      this.m4 = Double.NaN;
   }

   public FourthMoment(FourthMoment original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (this.n < 1L) {
         this.m4 = 0.0;
         this.m3 = 0.0;
         this.m2 = 0.0;
         this.m1 = 0.0;
      }

      double prevM3 = this.m3;
      double prevM2 = this.m2;
      super.increment(d);
      double n0 = (double)this.n;
      this.m4 = this.m4 - 4.0 * this.nDev * prevM3 + 6.0 * this.nDevSq * prevM2 + (n0 * n0 - 3.0 * (n0 - 1.0)) * this.nDevSq * this.nDevSq * (n0 - 1.0) * n0;
   }

   @Override
   public double getResult() {
      return this.m4;
   }

   @Override
   public void clear() {
      super.clear();
      this.m4 = Double.NaN;
   }

   public FourthMoment copy() {
      FourthMoment result = new FourthMoment();
      copy(this, result);
      return result;
   }

   public static void copy(FourthMoment source, FourthMoment dest) {
      ThirdMoment.copy(source, dest);
      dest.m4 = source.m4;
   }
}
