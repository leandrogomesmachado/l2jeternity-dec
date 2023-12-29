package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class HypergeometricDistributionImpl extends AbstractIntegerDistribution implements HypergeometricDistribution, Serializable {
   private static final long serialVersionUID = -436928820673516179L;
   private int numberOfSuccesses;
   private int populationSize;
   private int sampleSize;

   public HypergeometricDistributionImpl(int populationSize, int numberOfSuccesses, int sampleSize) {
      if (numberOfSuccesses > populationSize) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.NUMBER_OF_SUCCESS_LARGER_THAN_POPULATION_SIZE, numberOfSuccesses, populationSize
         );
      } else if (sampleSize > populationSize) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.SAMPLE_SIZE_LARGER_THAN_POPULATION_SIZE, sampleSize, populationSize);
      } else {
         this.setPopulationSizeInternal(populationSize);
         this.setSampleSizeInternal(sampleSize);
         this.setNumberOfSuccessesInternal(numberOfSuccesses);
      }
   }

   @Override
   public double cumulativeProbability(int x) {
      int[] domain = this.getDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
      double ret;
      if (x < domain[0]) {
         ret = 0.0;
      } else if (x >= domain[1]) {
         ret = 1.0;
      } else {
         ret = this.innerCumulativeProbability(domain[0], x, 1, this.populationSize, this.numberOfSuccesses, this.sampleSize);
      }

      return ret;
   }

   private int[] getDomain(int n, int m, int k) {
      return new int[]{this.getLowerDomain(n, m, k), this.getUpperDomain(m, k)};
   }

   @Override
   protected int getDomainLowerBound(double p) {
      return this.getLowerDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
   }

   @Override
   protected int getDomainUpperBound(double p) {
      return this.getUpperDomain(this.sampleSize, this.numberOfSuccesses);
   }

   private int getLowerDomain(int n, int m, int k) {
      return FastMath.max(0, m - (n - k));
   }

   @Override
   public int getNumberOfSuccesses() {
      return this.numberOfSuccesses;
   }

   @Override
   public int getPopulationSize() {
      return this.populationSize;
   }

   @Override
   public int getSampleSize() {
      return this.sampleSize;
   }

   private int getUpperDomain(int m, int k) {
      return FastMath.min(k, m);
   }

   @Override
   public double probability(int x) {
      int[] domain = this.getDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
      double ret;
      if (x >= domain[0] && x <= domain[1]) {
         double p = (double)this.sampleSize / (double)this.populationSize;
         double q = (double)(this.populationSize - this.sampleSize) / (double)this.populationSize;
         double p1 = SaddlePointExpansion.logBinomialProbability(x, this.numberOfSuccesses, p, q);
         double p2 = SaddlePointExpansion.logBinomialProbability(this.sampleSize - x, this.populationSize - this.numberOfSuccesses, p, q);
         double p3 = SaddlePointExpansion.logBinomialProbability(this.sampleSize, this.populationSize, p, q);
         ret = FastMath.exp(p1 + p2 - p3);
      } else {
         ret = 0.0;
      }

      return ret;
   }

   private double probability(int n, int m, int k, int x) {
      return FastMath.exp(MathUtils.binomialCoefficientLog(m, x) + MathUtils.binomialCoefficientLog(n - m, k - x) - MathUtils.binomialCoefficientLog(n, k));
   }

   @Deprecated
   @Override
   public void setNumberOfSuccesses(int num) {
      this.setNumberOfSuccessesInternal(num);
   }

   private void setNumberOfSuccessesInternal(int num) {
      if (num < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NEGATIVE_NUMBER_OF_SUCCESSES, num);
      } else {
         this.numberOfSuccesses = num;
      }
   }

   @Deprecated
   @Override
   public void setPopulationSize(int size) {
      this.setPopulationSizeInternal(size);
   }

   private void setPopulationSizeInternal(int size) {
      if (size <= 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POPULATION_SIZE, size);
      } else {
         this.populationSize = size;
      }
   }

   @Deprecated
   @Override
   public void setSampleSize(int size) {
      this.setSampleSizeInternal(size);
   }

   private void setSampleSizeInternal(int size) {
      if (size < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_SAMPLE_SIZE, size);
      } else {
         this.sampleSize = size;
      }
   }

   public double upperCumulativeProbability(int x) {
      int[] domain = this.getDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
      double ret;
      if (x < domain[0]) {
         ret = 1.0;
      } else if (x > domain[1]) {
         ret = 0.0;
      } else {
         ret = this.innerCumulativeProbability(domain[1], x, -1, this.populationSize, this.numberOfSuccesses, this.sampleSize);
      }

      return ret;
   }

   private double innerCumulativeProbability(int x0, int x1, int dx, int n, int m, int k) {
      double ret;
      for(ret = this.probability(n, m, k, x0); x0 != x1; ret += this.probability(n, m, k, x0)) {
         x0 += dx;
      }

      return ret;
   }

   public int getSupportLowerBound() {
      return FastMath.max(0, this.getSampleSize() + this.getNumberOfSuccesses() - this.getPopulationSize());
   }

   public int getSupportUpperBound() {
      return FastMath.min(this.getNumberOfSuccesses(), this.getSampleSize());
   }

   protected double getNumericalMean() {
      return (double)(this.getSampleSize() * this.getNumberOfSuccesses()) / (double)this.getPopulationSize();
   }

   public double getNumericalVariance() {
      double N = (double)this.getPopulationSize();
      double m = (double)this.getNumberOfSuccesses();
      double n = (double)this.getSampleSize();
      return n * m * (N - n) * (N - m) / (N * N * (N - 1.0));
   }
}
