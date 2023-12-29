package org.apache.commons.math.stat.descriptive;

import java.io.Serializable;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class StatisticalSummaryValues implements Serializable, StatisticalSummary {
   private static final long serialVersionUID = -5108854841843722536L;
   private final double mean;
   private final double variance;
   private final long n;
   private final double max;
   private final double min;
   private final double sum;

   public StatisticalSummaryValues(double mean, double variance, long n, double max, double min, double sum) {
      this.mean = mean;
      this.variance = variance;
      this.n = n;
      this.max = max;
      this.min = min;
      this.sum = sum;
   }

   @Override
   public double getMax() {
      return this.max;
   }

   @Override
   public double getMean() {
      return this.mean;
   }

   @Override
   public double getMin() {
      return this.min;
   }

   @Override
   public long getN() {
      return this.n;
   }

   @Override
   public double getSum() {
      return this.sum;
   }

   @Override
   public double getStandardDeviation() {
      return FastMath.sqrt(this.variance);
   }

   @Override
   public double getVariance() {
      return this.variance;
   }

   @Override
   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof StatisticalSummaryValues)) {
         return false;
      } else {
         StatisticalSummaryValues stat = (StatisticalSummaryValues)object;
         return MathUtils.equalsIncludingNaN(stat.getMax(), this.getMax())
            && MathUtils.equalsIncludingNaN(stat.getMean(), this.getMean())
            && MathUtils.equalsIncludingNaN(stat.getMin(), this.getMin())
            && MathUtils.equalsIncludingNaN((float)stat.getN(), (float)this.getN())
            && MathUtils.equalsIncludingNaN(stat.getSum(), this.getSum())
            && MathUtils.equalsIncludingNaN(stat.getVariance(), this.getVariance());
      }
   }

   @Override
   public int hashCode() {
      int result = 31 + MathUtils.hash(this.getMax());
      result = result * 31 + MathUtils.hash(this.getMean());
      result = result * 31 + MathUtils.hash(this.getMin());
      result = result * 31 + MathUtils.hash((double)this.getN());
      result = result * 31 + MathUtils.hash(this.getSum());
      return result * 31 + MathUtils.hash(this.getVariance());
   }

   @Override
   public String toString() {
      StringBuilder outBuffer = new StringBuilder();
      String endl = "\n";
      outBuffer.append("StatisticalSummaryValues:").append(endl);
      outBuffer.append("n: ").append(this.getN()).append(endl);
      outBuffer.append("min: ").append(this.getMin()).append(endl);
      outBuffer.append("max: ").append(this.getMax()).append(endl);
      outBuffer.append("mean: ").append(this.getMean()).append(endl);
      outBuffer.append("std dev: ").append(this.getStandardDeviation()).append(endl);
      outBuffer.append("variance: ").append(this.getVariance()).append(endl);
      outBuffer.append("sum: ").append(this.getSum()).append(endl);
      return outBuffer.toString();
   }
}
