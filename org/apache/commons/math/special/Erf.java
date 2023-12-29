package org.apache.commons.math.special;

import org.apache.commons.math.MathException;
import org.apache.commons.math.util.FastMath;

public class Erf {
   private Erf() {
   }

   public static double erf(double x) throws MathException {
      if (FastMath.abs(x) > 40.0) {
         return x > 0.0 ? 1.0 : -1.0;
      } else {
         double ret = Gamma.regularizedGammaP(0.5, x * x, 1.0E-15, 10000);
         if (x < 0.0) {
            ret = -ret;
         }

         return ret;
      }
   }

   public static double erfc(double x) throws MathException {
      if (FastMath.abs(x) > 40.0) {
         return x > 0.0 ? 0.0 : 2.0;
      } else {
         double ret = Gamma.regularizedGammaQ(0.5, x * x, 1.0E-15, 10000);
         return x < 0.0 ? 2.0 - ret : ret;
      }
   }
}
