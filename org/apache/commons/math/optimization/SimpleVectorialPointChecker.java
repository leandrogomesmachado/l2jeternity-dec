package org.apache.commons.math.optimization;

import org.apache.commons.math.util.FastMath;

public class SimpleVectorialPointChecker implements VectorialConvergenceChecker {
   private static final double DEFAULT_RELATIVE_THRESHOLD = 1.110223E-14F;
   private static final double DEFAULT_ABSOLUTE_THRESHOLD = 2.2250738585072014E-306;
   private final double relativeThreshold;
   private final double absoluteThreshold;

   public SimpleVectorialPointChecker() {
      this.relativeThreshold = 1.110223E-14F;
      this.absoluteThreshold = 2.2250738585072014E-306;
   }

   public SimpleVectorialPointChecker(double relativeThreshold, double absoluteThreshold) {
      this.relativeThreshold = relativeThreshold;
      this.absoluteThreshold = absoluteThreshold;
   }

   @Override
   public boolean converged(int iteration, VectorialPointValuePair previous, VectorialPointValuePair current) {
      double[] p = previous.getPointRef();
      double[] c = current.getPointRef();

      for(int i = 0; i < p.length; ++i) {
         double pi = p[i];
         double ci = c[i];
         double difference = FastMath.abs(pi - ci);
         double size = FastMath.max(FastMath.abs(pi), FastMath.abs(ci));
         if (difference > size * this.relativeThreshold && difference > this.absoluteThreshold) {
            return false;
         }
      }

      return true;
   }
}
