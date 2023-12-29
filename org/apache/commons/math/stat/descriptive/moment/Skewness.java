package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.util.FastMath;

public class Skewness extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = 7101857578996691352L;
   protected ThirdMoment moment = null;
   protected boolean incMoment;

   public Skewness() {
      this.incMoment = true;
      this.moment = new ThirdMoment();
   }

   public Skewness(ThirdMoment m3) {
      this.incMoment = false;
      this.moment = m3;
   }

   public Skewness(Skewness original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (this.incMoment) {
         this.moment.increment(d);
      }
   }

   @Override
   public double getResult() {
      if (this.moment.n < 3L) {
         return Double.NaN;
      } else {
         double variance = this.moment.m2 / (double)(this.moment.n - 1L);
         if (variance < 1.0E-19) {
            return 0.0;
         } else {
            double n0 = (double)this.moment.getN();
            return n0 * this.moment.m3 / ((n0 - 1.0) * (n0 - 2.0) * FastMath.sqrt(variance) * variance);
         }
      }
   }

   @Override
   public long getN() {
      return this.moment.getN();
   }

   @Override
   public void clear() {
      if (this.incMoment) {
         this.moment.clear();
      }
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      double skew = Double.NaN;
      if (this.test(values, begin, length) && length > 2) {
         Mean mean = new Mean();
         double m = mean.evaluate(values, begin, length);
         double accum = 0.0;
         double accum2 = 0.0;

         for(int i = begin; i < begin + length; ++i) {
            double d = values[i] - m;
            accum += d * d;
            accum2 += d;
         }

         double variance = (accum - accum2 * accum2 / (double)length) / (double)(length - 1);
         double accum3 = 0.0;

         for(int i = begin; i < begin + length; ++i) {
            double d = values[i] - m;
            accum3 += d * d * d;
         }

         accum3 /= variance * FastMath.sqrt(variance);
         double n0 = (double)length;
         skew = n0 / ((n0 - 1.0) * (n0 - 2.0)) * accum3;
      }

      return skew;
   }

   public Skewness copy() {
      Skewness result = new Skewness();
      copy(this, result);
      return result;
   }

   public static void copy(Skewness source, Skewness dest) {
      dest.setData(source.getDataRef());
      dest.moment = new ThirdMoment(source.moment.copy());
      dest.incMoment = source.incMoment;
   }
}
