package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Beta;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class PascalDistributionImpl extends AbstractIntegerDistribution implements PascalDistribution, Serializable {
   private static final long serialVersionUID = 6751309484392813623L;
   private int numberOfSuccesses;
   private double probabilityOfSuccess;

   public PascalDistributionImpl(int r, double p) {
      this.setNumberOfSuccessesInternal(r);
      this.setProbabilityOfSuccessInternal(p);
   }

   @Override
   public int getNumberOfSuccesses() {
      return this.numberOfSuccesses;
   }

   @Override
   public double getProbabilityOfSuccess() {
      return this.probabilityOfSuccess;
   }

   @Deprecated
   @Override
   public void setNumberOfSuccesses(int successes) {
      this.setNumberOfSuccessesInternal(successes);
   }

   private void setNumberOfSuccessesInternal(int successes) {
      if (successes < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NEGATIVE_NUMBER_OF_SUCCESSES, successes);
      } else {
         this.numberOfSuccesses = successes;
      }
   }

   @Deprecated
   @Override
   public void setProbabilityOfSuccess(double p) {
      this.setProbabilityOfSuccessInternal(p);
   }

   private void setProbabilityOfSuccessInternal(double p) {
      if (!(p < 0.0) && !(p > 1.0)) {
         this.probabilityOfSuccess = p;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, p, 0.0, 1.0);
      }
   }

   @Override
   protected int getDomainLowerBound(double p) {
      return -1;
   }

   @Override
   protected int getDomainUpperBound(double p) {
      return 2147483646;
   }

   @Override
   public double cumulativeProbability(int x) throws MathException {
      double ret;
      if (x < 0) {
         ret = 0.0;
      } else {
         ret = Beta.regularizedBeta(this.probabilityOfSuccess, (double)this.numberOfSuccesses, (double)(x + 1));
      }

      return ret;
   }

   @Override
   public double probability(int x) {
      double ret;
      if (x < 0) {
         ret = 0.0;
      } else {
         ret = MathUtils.binomialCoefficientDouble(x + this.numberOfSuccesses - 1, this.numberOfSuccesses - 1)
            * FastMath.pow(this.probabilityOfSuccess, (double)this.numberOfSuccesses)
            * FastMath.pow(1.0 - this.probabilityOfSuccess, (double)x);
      }

      return ret;
   }

   @Override
   public int inverseCumulativeProbability(double p) throws MathException {
      int ret;
      if (p == 0.0) {
         ret = -1;
      } else if (p == 1.0) {
         ret = Integer.MAX_VALUE;
      } else {
         ret = super.inverseCumulativeProbability(p);
      }

      return ret;
   }

   public int getSupportLowerBound() {
      return 0;
   }

   public int getSupportUpperBound() {
      return Integer.MAX_VALUE;
   }

   public double getNumericalMean() {
      double p = this.getProbabilityOfSuccess();
      double r = (double)this.getNumberOfSuccesses();
      return r * p / (1.0 - p);
   }

   public double getNumericalVariance() {
      double p = this.getProbabilityOfSuccess();
      double r = (double)this.getNumberOfSuccesses();
      double pInv = 1.0 - p;
      return r * p / (pInv * pInv);
   }
}
