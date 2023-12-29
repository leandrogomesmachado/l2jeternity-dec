package org.apache.commons.math.util;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public abstract class ContinuedFraction {
   private static final double DEFAULT_EPSILON = 1.0E-8;

   protected ContinuedFraction() {
   }

   protected abstract double getA(int var1, double var2);

   protected abstract double getB(int var1, double var2);

   public double evaluate(double x) throws MathException {
      return this.evaluate(x, 1.0E-8, Integer.MAX_VALUE);
   }

   public double evaluate(double x, double epsilon) throws MathException {
      return this.evaluate(x, epsilon, Integer.MAX_VALUE);
   }

   public double evaluate(double x, int maxIterations) throws MathException {
      return this.evaluate(x, 1.0E-8, maxIterations);
   }

   public double evaluate(double x, double epsilon, int maxIterations) throws MathException {
      double p0 = 1.0;
      double p1 = this.getA(0, x);
      double q0 = 0.0;
      double q1 = 1.0;
      double c = p1 / q1;
      int n = 0;

      double q2;
      for(double relativeError = Double.MAX_VALUE; n < maxIterations && relativeError > epsilon; q1 = q2) {
         double a = this.getA(++n, x);
         double b = this.getB(n, x);
         double p2 = a * p1 + b * p0;
         q2 = a * q1 + b * q0;
         boolean infinite = false;
         if (Double.isInfinite(p2) || Double.isInfinite(q2)) {
            double scaleFactor = 1.0;
            double lastScaleFactor = 1.0;
            int maxPower = 5;
            double scale = FastMath.max(a, b);
            if (scale <= 0.0) {
               throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_INFINITY_DIVERGENCE, x);
            }

            infinite = true;

            for(int i = 0; i < 5; ++i) {
               lastScaleFactor = scaleFactor;
               scaleFactor *= scale;
               if (a != 0.0 && a > b) {
                  p2 = p1 / lastScaleFactor + b / scaleFactor * p0;
                  q2 = q1 / lastScaleFactor + b / scaleFactor * q0;
               } else if (b != 0.0) {
                  p2 = a / scaleFactor * p1 + p0 / lastScaleFactor;
                  q2 = a / scaleFactor * q1 + q0 / lastScaleFactor;
               }

               infinite = Double.isInfinite(p2) || Double.isInfinite(q2);
               if (!infinite) {
                  break;
               }
            }
         }

         if (infinite) {
            throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_INFINITY_DIVERGENCE, x);
         }

         double r = p2 / q2;
         if (Double.isNaN(r)) {
            throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_NAN_DIVERGENCE, x);
         }

         relativeError = FastMath.abs(r / c - 1.0);
         c = p2 / q2;
         p0 = p1;
         p1 = p2;
         q0 = q1;
      }

      if (n >= maxIterations) {
         throw new MaxIterationsExceededException(maxIterations, LocalizedFormats.NON_CONVERGENT_CONTINUED_FRACTION, x);
      } else {
         return c;
      }
   }
}
