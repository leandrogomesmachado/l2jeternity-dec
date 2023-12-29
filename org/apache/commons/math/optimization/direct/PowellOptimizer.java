package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.general.AbstractScalarDifferentiableOptimizer;
import org.apache.commons.math.optimization.univariate.AbstractUnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BracketFinder;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;

public class PowellOptimizer extends AbstractScalarDifferentiableOptimizer {
   public static final double DEFAULT_LS_RELATIVE_TOLERANCE = 1.0E-7;
   public static final double DEFAULT_LS_ABSOLUTE_TOLERANCE = 1.0E-11;
   private final PowellOptimizer.LineSearch line;

   public PowellOptimizer() {
      this(1.0E-7, 1.0E-11);
   }

   public PowellOptimizer(double lsRelativeTolerance) {
      this(lsRelativeTolerance, 1.0E-11);
   }

   public PowellOptimizer(double lsRelativeTolerance, double lsAbsoluteTolerance) {
      this.line = new PowellOptimizer.LineSearch(lsRelativeTolerance, lsAbsoluteTolerance);
   }

   @Override
   protected RealPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException {
      double[] guess = (double[])this.point.clone();
      int n = guess.length;
      double[][] direc = new double[n][n];

      for(int i = 0; i < n; ++i) {
         direc[i][i] = 1.0;
      }

      double[] x = guess;
      double fVal = this.computeObjectiveValue(guess);
      double[] x1 = (double[])guess.clone();

      while(true) {
         this.incrementIterationsCounter();
         double fX = fVal;
         double fX2 = 0.0;
         double delta = 0.0;
         int bigInd = 0;
         double alphaMin = 0.0;

         for(int i = 0; i < n; ++i) {
            double[] d = this.copyOf(direc[i], n);
            fX2 = fVal;
            this.line.search(x, d);
            fVal = this.line.getValueAtOptimum();
            alphaMin = this.line.getOptimum();
            double[][] result = this.newPointAndDirection(x, d, alphaMin);
            x = result[0];
            if (fX2 - fVal > delta) {
               delta = fX2 - fVal;
               bigInd = i;
            }
         }

         RealPointValuePair previous = new RealPointValuePair(x1, fX);
         RealPointValuePair current = new RealPointValuePair(x, fVal);
         if (this.getConvergenceChecker().converged(this.getIterations(), previous, current)) {
            if (this.goal == GoalType.MINIMIZE) {
               return fVal < fX ? current : previous;
            }

            return fVal > fX ? current : previous;
         }

         double[] d = new double[n];
         double[] x2 = new double[n];

         for(int i = 0; i < n; ++i) {
            d[i] = x[i] - x1[i];
            x2[i] = 2.0 * x[i] - x1[i];
         }

         x1 = (double[])x.clone();
         fX2 = this.computeObjectiveValue(x2);
         if (fX > fX2) {
            double t = 2.0 * (fX + fX2 - 2.0 * fVal);
            double temp = fX - fVal - delta;
            t *= temp * temp;
            temp = fX - fX2;
            t -= delta * temp * temp;
            if (t < 0.0) {
               this.line.search(x, d);
               fVal = this.line.getValueAtOptimum();
               alphaMin = this.line.getOptimum();
               double[][] result = this.newPointAndDirection(x, d, alphaMin);
               x = result[0];
               int lastInd = n - 1;
               direc[bigInd] = direc[lastInd];
               direc[lastInd] = result[1];
            }
         }
      }
   }

   private double[][] newPointAndDirection(double[] p, double[] d, double optimum) {
      int n = p.length;
      double[][] result = new double[2][n];
      double[] nP = result[0];
      double[] nD = result[1];

      for(int i = 0; i < n; ++i) {
         nD[i] = d[i] * optimum;
         nP[i] = p[i] + nD[i];
      }

      return result;
   }

   private double[] copyOf(double[] source, int newLen) {
      double[] output = new double[newLen];
      System.arraycopy(source, 0, output, 0, Math.min(source.length, newLen));
      return output;
   }

   private class LineSearch {
      private final AbstractUnivariateRealOptimizer optim = new BrentOptimizer();
      private final BracketFinder bracket = new BracketFinder();
      private double optimum = Double.NaN;
      private double valueAtOptimum = Double.NaN;

      public LineSearch(double relativeTolerance, double absoluteTolerance) {
         this.optim.setRelativeAccuracy(relativeTolerance);
         this.optim.setAbsoluteAccuracy(absoluteTolerance);
      }

      public void search(final double[] p, final double[] d) throws OptimizationException, FunctionEvaluationException {
         this.optimum = Double.NaN;
         this.valueAtOptimum = Double.NaN;

         try {
            final int n = p.length;
            UnivariateRealFunction f = new UnivariateRealFunction() {
               @Override
               public double value(double alpha) throws FunctionEvaluationException {
                  double[] x = new double[n];

                  for(int i = 0; i < n; ++i) {
                     x[i] = p[i] + alpha * d[i];
                  }

                  return PowellOptimizer.this.computeObjectiveValue(x);
               }
            };
            this.bracket.search(f, PowellOptimizer.this.goal, 0.0, 1.0);
            this.optimum = this.optim.optimize(f, PowellOptimizer.this.goal, this.bracket.getLo(), this.bracket.getHi(), this.bracket.getMid());
            this.valueAtOptimum = this.optim.getFunctionValue();
         } catch (MaxIterationsExceededException var5) {
            throw new OptimizationException(var5);
         }
      }

      public double getOptimum() {
         return this.optimum;
      }

      public double getValueAtOptimum() {
         return this.valueAtOptimum;
      }
   }
}
