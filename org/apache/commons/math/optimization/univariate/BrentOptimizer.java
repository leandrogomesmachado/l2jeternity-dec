package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.util.FastMath;

public class BrentOptimizer extends AbstractUnivariateRealOptimizer {
   private static final double GOLDEN_SECTION = 0.5 * (3.0 - FastMath.sqrt(5.0));

   public BrentOptimizer() {
      this.setMaxEvaluations(1000);
      this.setMaximalIterationCount(100);
      this.setAbsoluteAccuracy(1.0E-11);
      this.setRelativeAccuracy(1.0E-9);
   }

   @Override
   protected double doOptimize() throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.localMin(
         this.getGoalType() == GoalType.MINIMIZE, this.getMin(), this.getStartValue(), this.getMax(), this.getRelativeAccuracy(), this.getAbsoluteAccuracy()
      );
   }

   private double localMin(boolean isMinim, double lo, double mid, double hi, double eps, double t) throws MaxIterationsExceededException, FunctionEvaluationException {
      if (eps <= 0.0) {
         throw new NotStrictlyPositiveException(eps);
      } else if (t <= 0.0) {
         throw new NotStrictlyPositiveException(t);
      } else {
         double a;
         double b;
         if (lo < hi) {
            a = lo;
            b = hi;
         } else {
            a = hi;
            b = lo;
         }

         double x = mid;
         double v = mid;
         double w = mid;
         double d = 0.0;
         double e = 0.0;
         double fx = this.computeObjectiveValue(mid);
         if (!isMinim) {
            fx = -fx;
         }

         double fv = fx;
         double fw = fx;

         while(true) {
            double m = 0.5 * (a + b);
            double tol1 = eps * FastMath.abs(x) + t;
            double tol2 = 2.0 * tol1;
            if (!(FastMath.abs(x - m) > tol2 - 0.5 * (b - a))) {
               this.setFunctionValue(isMinim ? fx : -fx);
               return x;
            }

            double p = 0.0;
            double q = 0.0;
            double r = 0.0;
            double u = 0.0;
            if (FastMath.abs(e) > tol1) {
               r = (x - w) * (fx - fv);
               q = (x - v) * (fx - fw);
               p = (x - v) * q - (x - w) * r;
               q = 2.0 * (q - r);
               if (q > 0.0) {
                  p = -p;
               } else {
                  q = -q;
               }

               r = e;
               e = d;
               if (p > q * (a - x) && p < q * (b - x) && FastMath.abs(p) < FastMath.abs(0.5 * q * r)) {
                  d = p / q;
                  u = x + d;
                  if (u - a < tol2 || b - u < tol2) {
                     if (x <= m) {
                        d = tol1;
                     } else {
                        d = -tol1;
                     }
                  }
               } else {
                  if (x < m) {
                     e = b - x;
                  } else {
                     e = a - x;
                  }

                  d = GOLDEN_SECTION * e;
               }
            } else {
               if (x < m) {
                  e = b - x;
               } else {
                  e = a - x;
               }

               d = GOLDEN_SECTION * e;
            }

            if (FastMath.abs(d) < tol1) {
               if (d >= 0.0) {
                  u = x + tol1;
               } else {
                  u = x - tol1;
               }
            } else {
               u = x + d;
            }

            double fu = this.computeObjectiveValue(u);
            if (!isMinim) {
               fu = -fu;
            }

            if (fu <= fx) {
               if (u < x) {
                  b = x;
               } else {
                  a = x;
               }

               v = w;
               fv = fw;
               w = x;
               fw = fx;
               x = u;
               fx = fu;
            } else {
               if (u < x) {
                  a = u;
               } else {
                  b = u;
               }

               if (fu <= fw || w == x) {
                  v = w;
                  fv = fw;
                  w = u;
                  fw = fu;
               } else if (fu <= fv || v == x || v == w) {
                  v = u;
                  fv = fu;
               }
            }

            this.incrementIterationsCounter();
         }
      }
   }
}
