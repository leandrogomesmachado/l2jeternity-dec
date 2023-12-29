package org.apache.commons.math.optimization;

import org.apache.commons.math.util.FastMath;

public class SimpleRealPointChecker implements RealConvergenceChecker {
   private static final double DEFAULT_RELATIVE_THRESHOLD = 1.110223E-14F;
   private static final double DEFAULT_ABSOLUTE_THRESHOLD = 2.2250738585072014E-306;
   private final double relativeThreshold;
   private final double absoluteThreshold;

   public SimpleRealPointChecker() {
      this.relativeThreshold = 1.110223E-14F;
      this.absoluteThreshold = 2.2250738585072014E-306;
   }

   public SimpleRealPointChecker(double relativeThreshold, double absoluteThreshold) {
      this.relativeThreshold = relativeThreshold;
      this.absoluteThreshold = absoluteThreshold;
   }

   @Override
   public boolean converged(int iteration, RealPointValuePair previous, RealPointValuePair current) {
      double[] p = previous.getPoint();
      double[] c = current.getPoint();

      for(int i = 0; i < p.length; ++i) {
         double difference = FastMath.abs(p[i] - c[i]);
         double size = FastMath.max(FastMath.abs(p[i]), FastMath.abs(c[i]));
         if (difference > size * this.relativeThreshold && difference > this.absoluteThreshold) {
            return false;
         }
      }

      return true;
   }
}
