package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;

public class SecondMoment extends FirstMoment implements Serializable {
   private static final long serialVersionUID = 3942403127395076445L;
   protected double m2;

   public SecondMoment() {
      this.m2 = Double.NaN;
   }

   public SecondMoment(SecondMoment original) {
      super(original);
      this.m2 = original.m2;
   }

   @Override
   public void increment(double d) {
      if (this.n < 1L) {
         this.m1 = this.m2 = 0.0;
      }

      super.increment(d);
      this.m2 += ((double)this.n - 1.0) * this.dev * this.nDev;
   }

   @Override
   public void clear() {
      super.clear();
      this.m2 = Double.NaN;
   }

   @Override
   public double getResult() {
      return this.m2;
   }

   public SecondMoment copy() {
      SecondMoment result = new SecondMoment();
      copy(this, result);
      return result;
   }

   public static void copy(SecondMoment source, SecondMoment dest) {
      FirstMoment.copy(source, dest);
      dest.m2 = source.m2;
   }
}
