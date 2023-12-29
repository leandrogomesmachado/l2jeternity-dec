package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.util.FastMath;

public abstract class AbstractContinuousDistribution extends AbstractDistribution implements ContinuousDistribution, Serializable {
   private static final long serialVersionUID = -38038050983108802L;
   protected final RandomDataImpl randomData = new RandomDataImpl();
   private double solverAbsoluteAccuracy = 1.0E-6;

   protected AbstractContinuousDistribution() {
   }

   public double density(double x) throws MathRuntimeException {
      throw new MathRuntimeException(new UnsupportedOperationException(), LocalizedFormats.NO_DENSITY_FOR_THIS_DISTRIBUTION);
   }

   @Override
   public double inverseCumulativeProbability(final double p) throws MathException {
      if (!(p < 0.0) && !(p > 1.0)) {
         UnivariateRealFunction rootFindingFunction = new UnivariateRealFunction() {
            @Override
            public double value(double x) throws FunctionEvaluationException {
               double ret = Double.NaN;

               try {
                  ret = AbstractContinuousDistribution.this.cumulativeProbability(x) - p;
               } catch (MathException var6) {
                  throw new FunctionEvaluationException(x, var6.getSpecificPattern(), var6.getGeneralPattern(), var6.getArguments());
               }

               if (Double.isNaN(ret)) {
                  throw new FunctionEvaluationException(x, LocalizedFormats.CUMULATIVE_PROBABILITY_RETURNED_NAN, x, p);
               } else {
                  return ret;
               }
            }
         };
         double lowerBound = this.getDomainLowerBound(p);
         double upperBound = this.getDomainUpperBound(p);
         double[] bracket = null;

         try {
            bracket = UnivariateRealSolverUtils.bracket(rootFindingFunction, this.getInitialDomain(p), lowerBound, upperBound);
         } catch (ConvergenceException var11) {
            if (FastMath.abs(rootFindingFunction.value(lowerBound)) < this.getSolverAbsoluteAccuracy()) {
               return lowerBound;
            }

            if (FastMath.abs(rootFindingFunction.value(upperBound)) < this.getSolverAbsoluteAccuracy()) {
               return upperBound;
            }

            throw new MathException(var11);
         }

         return UnivariateRealSolverUtils.solve(rootFindingFunction, bracket[0], bracket[1], this.getSolverAbsoluteAccuracy());
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, p, 0.0, 1.0);
      }
   }

   public void reseedRandomGenerator(long seed) {
      this.randomData.reSeed(seed);
   }

   public double sample() throws MathException {
      return this.randomData.nextInversionDeviate(this);
   }

   public double[] sample(int sampleSize) throws MathException {
      if (sampleSize <= 0) {
         MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_SAMPLE_SIZE, sampleSize);
      }

      double[] out = new double[sampleSize];

      for(int i = 0; i < sampleSize; ++i) {
         out[i] = this.sample();
      }

      return out;
   }

   protected abstract double getInitialDomain(double var1);

   protected abstract double getDomainLowerBound(double var1);

   protected abstract double getDomainUpperBound(double var1);

   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }
}
