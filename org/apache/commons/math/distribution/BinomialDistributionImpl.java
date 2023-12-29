package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Beta;
import org.apache.commons.math.util.FastMath;

public class BinomialDistributionImpl extends AbstractIntegerDistribution implements BinomialDistribution, Serializable {
   private static final long serialVersionUID = 6751309484392813623L;
   private int numberOfTrials;
   private double probabilityOfSuccess;

   public BinomialDistributionImpl(int trials, double p) {
      this.setNumberOfTrialsInternal(trials);
      this.setProbabilityOfSuccessInternal(p);
   }

   @Override
   public int getNumberOfTrials() {
      return this.numberOfTrials;
   }

   @Override
   public double getProbabilityOfSuccess() {
      return this.probabilityOfSuccess;
   }

   @Deprecated
   @Override
   public void setNumberOfTrials(int trials) {
      this.setNumberOfTrialsInternal(trials);
   }

   private void setNumberOfTrialsInternal(int trials) {
      if (trials < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NEGATIVE_NUMBER_OF_TRIALS, trials);
      } else {
         this.numberOfTrials = trials;
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
      return this.numberOfTrials;
   }

   @Override
   public double cumulativeProbability(int x) throws MathException {
      double ret;
      if (x < 0) {
         ret = 0.0;
      } else if (x >= this.numberOfTrials) {
         ret = 1.0;
      } else {
         ret = 1.0 - Beta.regularizedBeta(this.getProbabilityOfSuccess(), (double)x + 1.0, (double)(this.numberOfTrials - x));
      }

      return ret;
   }

   @Override
   public double probability(int x) {
      double ret;
      if (x >= 0 && x <= this.numberOfTrials) {
         ret = FastMath.exp(SaddlePointExpansion.logBinomialProbability(x, this.numberOfTrials, this.probabilityOfSuccess, 1.0 - this.probabilityOfSuccess));
      } else {
         ret = 0.0;
      }

      return ret;
   }

   @Override
   public int inverseCumulativeProbability(double p) throws MathException {
      if (p == 0.0) {
         return -1;
      } else {
         return p == 1.0 ? Integer.MAX_VALUE : super.inverseCumulativeProbability(p);
      }
   }

   public int getSupportLowerBound() {
      return 0;
   }

   public int getSupportUpperBound() {
      return this.getNumberOfTrials();
   }

   public double getNumericalMean() {
      return (double)this.getNumberOfTrials() * this.getProbabilityOfSuccess();
   }

   public double getNumericalVariance() {
      double p = this.getProbabilityOfSuccess();
      return (double)this.getNumberOfTrials() * p * (1.0 - p);
   }
}
