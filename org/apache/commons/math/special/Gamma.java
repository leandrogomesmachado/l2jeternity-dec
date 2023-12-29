package org.apache.commons.math.special;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.util.ContinuedFraction;
import org.apache.commons.math.util.FastMath;

public class Gamma {
   public static final double GAMMA = 0.5772156649015329;
   private static final double DEFAULT_EPSILON = 1.0E-14;
   private static final double[] LANCZOS = new double[]{
      0.9999999999999971,
      57.15623566586292,
      -59.59796035547549,
      14.136097974741746,
      -0.4919138160976202,
      3.399464998481189E-5,
      4.652362892704858E-5,
      -9.837447530487956E-5,
      1.580887032249125E-4,
      -2.1026444172410488E-4,
      2.1743961811521265E-4,
      -1.643181065367639E-4,
      8.441822398385275E-5,
      -2.6190838401581408E-5,
      3.6899182659531625E-6
   };
   private static final double HALF_LOG_2_PI = 0.5 * FastMath.log(Math.PI * 2);
   private static final double C_LIMIT = 49.0;
   private static final double S_LIMIT = 1.0E-5;

   private Gamma() {
   }

   public static double logGamma(double x) {
      double ret;
      if (!Double.isNaN(x) && !(x <= 0.0)) {
         double g = 4.7421875;
         double sum = 0.0;

         for(int i = LANCZOS.length - 1; i > 0; --i) {
            sum += LANCZOS[i] / (x + (double)i);
         }

         sum += LANCZOS[0];
         double tmp = x + g + 0.5;
         ret = (x + 0.5) * FastMath.log(tmp) - tmp + HALF_LOG_2_PI + FastMath.log(sum / x);
      } else {
         ret = Double.NaN;
      }

      return ret;
   }

   public static double regularizedGammaP(double a, double x) throws MathException {
      return regularizedGammaP(a, x, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double regularizedGammaP(double a, double x, double epsilon, int maxIterations) throws MathException {
      double ret;
      if (Double.isNaN(a) || Double.isNaN(x) || a <= 0.0 || x < 0.0) {
         ret = Double.NaN;
      } else if (x == 0.0) {
         ret = 0.0;
      } else if (x >= a + 1.0) {
         ret = 1.0 - regularizedGammaQ(a, x, epsilon, maxIterations);
      } else {
         double n = 0.0;
         double an = 1.0 / a;

         double sum;
         for(sum = an; FastMath.abs(an / sum) > epsilon && n < (double)maxIterations && sum < Double.POSITIVE_INFINITY; sum += an) {
            ++n;
            an *= x / (a + n);
         }

         if (n >= (double)maxIterations) {
            throw new MaxIterationsExceededException(maxIterations);
         }

         if (Double.isInfinite(sum)) {
            ret = 1.0;
         } else {
            ret = FastMath.exp(-x + a * FastMath.log(x) - logGamma(a)) * sum;
         }
      }

      return ret;
   }

   public static double regularizedGammaQ(double a, double x) throws MathException {
      return regularizedGammaQ(a, x, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double regularizedGammaQ(final double a, double x, double epsilon, int maxIterations) throws MathException {
      double ret;
      if (Double.isNaN(a) || Double.isNaN(x) || a <= 0.0 || x < 0.0) {
         ret = Double.NaN;
      } else if (x == 0.0) {
         ret = 1.0;
      } else if (x < a + 1.0) {
         ret = 1.0 - regularizedGammaP(a, x, epsilon, maxIterations);
      } else {
         ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
               return 2.0 * (double)n + 1.0 - a + x;
            }

            @Override
            protected double getB(int n, double x) {
               return (double)n * (a - (double)n);
            }
         };
         ret = 1.0 / cf.evaluate(x, epsilon, maxIterations);
         ret = FastMath.exp(-x + a * FastMath.log(x) - logGamma(a)) * ret;
      }

      return ret;
   }

   public static double digamma(double x) {
      if (x > 0.0 && x <= 1.0E-5) {
         return -0.5772156649015329 - 1.0 / x;
      } else if (x >= 49.0) {
         double inv = 1.0 / (x * x);
         return FastMath.log(x) - 0.5 / x - inv * (0.08333333333333333 + inv * (0.008333333333333333 - inv / 252.0));
      } else {
         return digamma(x + 1.0) - 1.0 / x;
      }
   }

   public static double trigamma(double x) {
      if (x > 0.0 && x <= 1.0E-5) {
         return 1.0 / (x * x);
      } else if (x >= 49.0) {
         double inv = 1.0 / (x * x);
         return 1.0 / x + inv / 2.0 + inv / x * (0.16666666666666666 - inv * (0.03333333333333333 + inv / 42.0));
      } else {
         return trigamma(x + 1.0) + 1.0 / (x * x);
      }
   }
}
