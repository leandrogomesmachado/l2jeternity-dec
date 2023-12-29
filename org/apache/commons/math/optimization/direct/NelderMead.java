package org.apache.commons.math.optimization.direct;

import java.util.Comparator;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;

public class NelderMead extends DirectSearchOptimizer {
   private final double rho;
   private final double khi;
   private final double gamma;
   private final double sigma;

   public NelderMead() {
      this.rho = 1.0;
      this.khi = 2.0;
      this.gamma = 0.5;
      this.sigma = 0.5;
   }

   public NelderMead(double rho, double khi, double gamma, double sigma) {
      this.rho = rho;
      this.khi = khi;
      this.gamma = gamma;
      this.sigma = sigma;
   }

   @Override
   protected void iterateSimplex(Comparator<RealPointValuePair> comparator) throws FunctionEvaluationException, OptimizationException {
      this.incrementIterationsCounter();
      int n = this.simplex.length - 1;
      RealPointValuePair best = this.simplex[0];
      RealPointValuePair secondBest = this.simplex[n - 1];
      RealPointValuePair worst = this.simplex[n];
      double[] xWorst = worst.getPointRef();
      double[] centroid = new double[n];

      for(int i = 0; i < n; ++i) {
         double[] x = this.simplex[i].getPointRef();

         for(int j = 0; j < n; ++j) {
            centroid[j] += x[j];
         }
      }

      double scaling = 1.0 / (double)n;

      for(int j = 0; j < n; ++j) {
         centroid[j] *= scaling;
      }

      double[] xR = new double[n];

      for(int j = 0; j < n; ++j) {
         xR[j] = centroid[j] + this.rho * (centroid[j] - xWorst[j]);
      }

      RealPointValuePair reflected = new RealPointValuePair(xR, this.evaluate(xR), false);
      if (comparator.compare(best, reflected) <= 0 && comparator.compare(reflected, secondBest) < 0) {
         this.replaceWorstPoint(reflected, comparator);
      } else if (comparator.compare(reflected, best) < 0) {
         double[] xE = new double[n];

         for(int j = 0; j < n; ++j) {
            xE[j] = centroid[j] + this.khi * (xR[j] - centroid[j]);
         }

         RealPointValuePair expanded = new RealPointValuePair(xE, this.evaluate(xE), false);
         if (comparator.compare(expanded, reflected) < 0) {
            this.replaceWorstPoint(expanded, comparator);
         } else {
            this.replaceWorstPoint(reflected, comparator);
         }
      } else {
         if (comparator.compare(reflected, worst) < 0) {
            double[] xC = new double[n];

            for(int j = 0; j < n; ++j) {
               xC[j] = centroid[j] + this.gamma * (xR[j] - centroid[j]);
            }

            RealPointValuePair outContracted = new RealPointValuePair(xC, this.evaluate(xC), false);
            if (comparator.compare(outContracted, reflected) <= 0) {
               this.replaceWorstPoint(outContracted, comparator);
               return;
            }
         } else {
            double[] xC = new double[n];

            for(int j = 0; j < n; ++j) {
               xC[j] = centroid[j] - this.gamma * (centroid[j] - xWorst[j]);
            }

            RealPointValuePair inContracted = new RealPointValuePair(xC, this.evaluate(xC), false);
            if (comparator.compare(inContracted, worst) < 0) {
               this.replaceWorstPoint(inContracted, comparator);
               return;
            }
         }

         double[] xSmallest = this.simplex[0].getPointRef();

         for(int i = 1; i < this.simplex.length; ++i) {
            double[] x = this.simplex[i].getPoint();

            for(int j = 0; j < n; ++j) {
               x[j] = xSmallest[j] + this.sigma * (x[j] - xSmallest[j]);
            }

            this.simplex[i] = new RealPointValuePair(x, Double.NaN, false);
         }

         this.evaluateSimplex(comparator);
      }
   }
}
