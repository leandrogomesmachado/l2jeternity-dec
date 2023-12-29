package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.util.FastMath;

public class TTestImpl implements TTest {
   @Deprecated
   private TDistribution distribution;

   public TTestImpl() {
      this(new TDistributionImpl(1.0));
   }

   @Deprecated
   public TTestImpl(TDistribution t) {
      this.setDistribution(t);
   }

   @Override
   public double pairedT(double[] sample1, double[] sample2) throws IllegalArgumentException, MathException {
      this.checkSampleData(sample1);
      this.checkSampleData(sample2);
      double meanDifference = StatUtils.meanDifference(sample1, sample2);
      return this.t(meanDifference, 0.0, StatUtils.varianceDifference(sample1, sample2, meanDifference), (double)sample1.length);
   }

   @Override
   public double pairedTTest(double[] sample1, double[] sample2) throws IllegalArgumentException, MathException {
      double meanDifference = StatUtils.meanDifference(sample1, sample2);
      return this.tTest(meanDifference, 0.0, StatUtils.varianceDifference(sample1, sample2, meanDifference), (double)sample1.length);
   }

   @Override
   public boolean pairedTTest(double[] sample1, double[] sample2, double alpha) throws IllegalArgumentException, MathException {
      this.checkSignificanceLevel(alpha);
      return this.pairedTTest(sample1, sample2) < alpha;
   }

   @Override
   public double t(double mu, double[] observed) throws IllegalArgumentException {
      this.checkSampleData(observed);
      return this.t(StatUtils.mean(observed), mu, StatUtils.variance(observed), (double)observed.length);
   }

   @Override
   public double t(double mu, StatisticalSummary sampleStats) throws IllegalArgumentException {
      this.checkSampleData(sampleStats);
      return this.t(sampleStats.getMean(), mu, sampleStats.getVariance(), (double)sampleStats.getN());
   }

   @Override
   public double homoscedasticT(double[] sample1, double[] sample2) throws IllegalArgumentException {
      this.checkSampleData(sample1);
      this.checkSampleData(sample2);
      return this.homoscedasticT(
         StatUtils.mean(sample1),
         StatUtils.mean(sample2),
         StatUtils.variance(sample1),
         StatUtils.variance(sample2),
         (double)sample1.length,
         (double)sample2.length
      );
   }

   @Override
   public double t(double[] sample1, double[] sample2) throws IllegalArgumentException {
      this.checkSampleData(sample1);
      this.checkSampleData(sample2);
      return this.t(
         StatUtils.mean(sample1),
         StatUtils.mean(sample2),
         StatUtils.variance(sample1),
         StatUtils.variance(sample2),
         (double)sample1.length,
         (double)sample2.length
      );
   }

   @Override
   public double t(StatisticalSummary sampleStats1, StatisticalSummary sampleStats2) throws IllegalArgumentException {
      this.checkSampleData(sampleStats1);
      this.checkSampleData(sampleStats2);
      return this.t(
         sampleStats1.getMean(),
         sampleStats2.getMean(),
         sampleStats1.getVariance(),
         sampleStats2.getVariance(),
         (double)sampleStats1.getN(),
         (double)sampleStats2.getN()
      );
   }

   @Override
   public double homoscedasticT(StatisticalSummary sampleStats1, StatisticalSummary sampleStats2) throws IllegalArgumentException {
      this.checkSampleData(sampleStats1);
      this.checkSampleData(sampleStats2);
      return this.homoscedasticT(
         sampleStats1.getMean(),
         sampleStats2.getMean(),
         sampleStats1.getVariance(),
         sampleStats2.getVariance(),
         (double)sampleStats1.getN(),
         (double)sampleStats2.getN()
      );
   }

   @Override
   public double tTest(double mu, double[] sample) throws IllegalArgumentException, MathException {
      this.checkSampleData(sample);
      return this.tTest(StatUtils.mean(sample), mu, StatUtils.variance(sample), (double)sample.length);
   }

   @Override
   public boolean tTest(double mu, double[] sample, double alpha) throws IllegalArgumentException, MathException {
      this.checkSignificanceLevel(alpha);
      return this.tTest(mu, sample) < alpha;
   }

   @Override
   public double tTest(double mu, StatisticalSummary sampleStats) throws IllegalArgumentException, MathException {
      this.checkSampleData(sampleStats);
      return this.tTest(sampleStats.getMean(), mu, sampleStats.getVariance(), (double)sampleStats.getN());
   }

   @Override
   public boolean tTest(double mu, StatisticalSummary sampleStats, double alpha) throws IllegalArgumentException, MathException {
      this.checkSignificanceLevel(alpha);
      return this.tTest(mu, sampleStats) < alpha;
   }

   @Override
   public double tTest(double[] sample1, double[] sample2) throws IllegalArgumentException, MathException {
      this.checkSampleData(sample1);
      this.checkSampleData(sample2);
      return this.tTest(
         StatUtils.mean(sample1),
         StatUtils.mean(sample2),
         StatUtils.variance(sample1),
         StatUtils.variance(sample2),
         (double)sample1.length,
         (double)sample2.length
      );
   }

   @Override
   public double homoscedasticTTest(double[] sample1, double[] sample2) throws IllegalArgumentException, MathException {
      this.checkSampleData(sample1);
      this.checkSampleData(sample2);
      return this.homoscedasticTTest(
         StatUtils.mean(sample1),
         StatUtils.mean(sample2),
         StatUtils.variance(sample1),
         StatUtils.variance(sample2),
         (double)sample1.length,
         (double)sample2.length
      );
   }

   @Override
   public boolean tTest(double[] sample1, double[] sample2, double alpha) throws IllegalArgumentException, MathException {
      this.checkSignificanceLevel(alpha);
      return this.tTest(sample1, sample2) < alpha;
   }

   @Override
   public boolean homoscedasticTTest(double[] sample1, double[] sample2, double alpha) throws IllegalArgumentException, MathException {
      this.checkSignificanceLevel(alpha);
      return this.homoscedasticTTest(sample1, sample2) < alpha;
   }

   @Override
   public double tTest(StatisticalSummary sampleStats1, StatisticalSummary sampleStats2) throws IllegalArgumentException, MathException {
      this.checkSampleData(sampleStats1);
      this.checkSampleData(sampleStats2);
      return this.tTest(
         sampleStats1.getMean(),
         sampleStats2.getMean(),
         sampleStats1.getVariance(),
         sampleStats2.getVariance(),
         (double)sampleStats1.getN(),
         (double)sampleStats2.getN()
      );
   }

   @Override
   public double homoscedasticTTest(StatisticalSummary sampleStats1, StatisticalSummary sampleStats2) throws IllegalArgumentException, MathException {
      this.checkSampleData(sampleStats1);
      this.checkSampleData(sampleStats2);
      return this.homoscedasticTTest(
         sampleStats1.getMean(),
         sampleStats2.getMean(),
         sampleStats1.getVariance(),
         sampleStats2.getVariance(),
         (double)sampleStats1.getN(),
         (double)sampleStats2.getN()
      );
   }

   @Override
   public boolean tTest(StatisticalSummary sampleStats1, StatisticalSummary sampleStats2, double alpha) throws IllegalArgumentException, MathException {
      this.checkSignificanceLevel(alpha);
      return this.tTest(sampleStats1, sampleStats2) < alpha;
   }

   protected double df(double v1, double v2, double n1, double n2) {
      return (v1 / n1 + v2 / n2) * (v1 / n1 + v2 / n2) / (v1 * v1 / (n1 * n1 * (n1 - 1.0)) + v2 * v2 / (n2 * n2 * (n2 - 1.0)));
   }

   protected double t(double m, double mu, double v, double n) {
      return (m - mu) / FastMath.sqrt(v / n);
   }

   protected double t(double m1, double m2, double v1, double v2, double n1, double n2) {
      return (m1 - m2) / FastMath.sqrt(v1 / n1 + v2 / n2);
   }

   protected double homoscedasticT(double m1, double m2, double v1, double v2, double n1, double n2) {
      double pooledVariance = ((n1 - 1.0) * v1 + (n2 - 1.0) * v2) / (n1 + n2 - 2.0);
      return (m1 - m2) / FastMath.sqrt(pooledVariance * (1.0 / n1 + 1.0 / n2));
   }

   protected double tTest(double m, double mu, double v, double n) throws MathException {
      double t = FastMath.abs(this.t(m, mu, v, n));
      this.distribution.setDegreesOfFreedom(n - 1.0);
      return 2.0 * this.distribution.cumulativeProbability(-t);
   }

   protected double tTest(double m1, double m2, double v1, double v2, double n1, double n2) throws MathException {
      double t = FastMath.abs(this.t(m1, m2, v1, v2, n1, n2));
      double degreesOfFreedom = 0.0;
      degreesOfFreedom = this.df(v1, v2, n1, n2);
      this.distribution.setDegreesOfFreedom(degreesOfFreedom);
      return 2.0 * this.distribution.cumulativeProbability(-t);
   }

   protected double homoscedasticTTest(double m1, double m2, double v1, double v2, double n1, double n2) throws MathException {
      double t = FastMath.abs(this.homoscedasticT(m1, m2, v1, v2, n1, n2));
      double degreesOfFreedom = n1 + n2 - 2.0;
      this.distribution.setDegreesOfFreedom(degreesOfFreedom);
      return 2.0 * this.distribution.cumulativeProbability(-t);
   }

   @Deprecated
   public void setDistribution(TDistribution value) {
      this.distribution = value;
   }

   private void checkSignificanceLevel(double alpha) throws IllegalArgumentException {
      if (alpha <= 0.0 || alpha > 0.5) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0.0, 0.5);
      }
   }

   private void checkSampleData(double[] data) throws IllegalArgumentException {
      if (data == null || data.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DATA_FOR_T_STATISTIC, data == null ? 0 : data.length);
      }
   }

   private void checkSampleData(StatisticalSummary stat) throws IllegalArgumentException {
      if (stat == null || stat.getN() < 2L) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DATA_FOR_T_STATISTIC, stat == null ? 0L : stat.getN());
      }
   }
}
