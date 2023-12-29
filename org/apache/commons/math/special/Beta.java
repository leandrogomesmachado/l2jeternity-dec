package org.apache.commons.math.special;

import org.apache.commons.math.MathException;
import org.apache.commons.math.util.ContinuedFraction;
import org.apache.commons.math.util.FastMath;

public class Beta {
   private static final double DEFAULT_EPSILON = 1.0E-14;

   private Beta() {
   }

   public static double regularizedBeta(double x, double a, double b) throws MathException {
      return regularizedBeta(x, a, b, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double regularizedBeta(double x, double a, double b, double epsilon) throws MathException {
      return regularizedBeta(x, a, b, epsilon, Integer.MAX_VALUE);
   }

   public static double regularizedBeta(double x, double a, double b, int maxIterations) throws MathException {
      return regularizedBeta(x, a, b, 1.0E-14, maxIterations);
   }

   public static double regularizedBeta(double x, final double a, final double b, double epsilon, int maxIterations) throws MathException {
      double ret;
      if (!Double.isNaN(x) && !Double.isNaN(a) && !Double.isNaN(b) && !(x < 0.0) && !(x > 1.0) && !(a <= 0.0) && !(b <= 0.0)) {
         if (x > (a + 1.0) / (a + b + 2.0)) {
            ret = 1.0 - regularizedBeta(1.0 - x, b, a, epsilon, maxIterations);
         } else {
            ContinuedFraction fraction = new ContinuedFraction() {
               @Override
               protected double getB(int n, double x) {
                  double ret;
                  if (n % 2 == 0) {
                     double m = (double)n / 2.0;
                     ret = m * (b - m) * x / ((a + 2.0 * m - 1.0) * (a + 2.0 * m));
                  } else {
                     double m = ((double)n - 1.0) / 2.0;
                     ret = -((a + m) * (a + b + m) * x) / ((a + 2.0 * m) * (a + 2.0 * m + 1.0));
                  }

                  return ret;
               }

               @Override
               protected double getA(int n, double x) {
                  return 1.0;
               }
            };
            ret = FastMath.exp(a * FastMath.log(x) + b * FastMath.log(1.0 - x) - FastMath.log(a) - logBeta(a, b, epsilon, maxIterations))
               * 1.0
               / fraction.evaluate(x, epsilon, maxIterations);
         }
      } else {
         ret = Double.NaN;
      }

      return ret;
   }

   public static double logBeta(double a, double b) {
      return logBeta(a, b, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double logBeta(double a, double b, double epsilon, int maxIterations) {
      double ret;
      if (!Double.isNaN(a) && !Double.isNaN(b) && !(a <= 0.0) && !(b <= 0.0)) {
         ret = Gamma.logGamma(a) + Gamma.logGamma(b) - Gamma.logGamma(a + b);
      } else {
         ret = Double.NaN;
      }

      return ret;
   }
}
