package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;

public class ThirdMoment extends SecondMoment implements Serializable {
   private static final long serialVersionUID = -7818711964045118679L;
   protected double m3;
   protected double nDevSq;

   public ThirdMoment() {
      this.m3 = Double.NaN;
      this.nDevSq = Double.NaN;
   }

   public ThirdMoment(ThirdMoment original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (this.n < 1L) {
         this.m3 = this.m2 = this.m1 = 0.0;
      }

      double prevM2 = this.m2;
      super.increment(d);
      this.nDevSq = this.nDev * this.nDev;
      double n0 = (double)this.n;
      this.m3 = this.m3 - 3.0 * this.nDev * prevM2 + (n0 - 1.0) * (n0 - 2.0) * this.nDevSq * this.dev;
   }

   @Override
   public double getResult() {
      return this.m3;
   }

   @Override
   public void clear() {
      super.clear();
      this.m3 = Double.NaN;
      this.nDevSq = Double.NaN;
   }

   public ThirdMoment copy() {
      ThirdMoment result = new ThirdMoment();
      copy(this, result);
      return result;
   }

   public static void copy(ThirdMoment source, ThirdMoment dest) {
      SecondMoment.copy(source, dest);
      dest.m3 = source.m3;
      dest.nDevSq = source.nDevSq;
   }
}
