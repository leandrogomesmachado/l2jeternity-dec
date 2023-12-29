package org.apache.commons.math.optimization;

import org.apache.commons.math.util.FastMath;

public class SimpleScalarValueChecker implements RealConvergenceChecker {
   private static final double DEFAULT_RELATIVE_THRESHOLD = 1.110223E-14F;
   private static final double DEFAULT_ABSOLUTE_THRESHOLD = 2.2250738585072014E-306;
   private final double relativeThreshold;
   private final double absoluteThreshold;

   public SimpleScalarValueChecker() {
      this.relativeThreshold = 1.110223E-14F;
      this.absoluteThreshold = 2.2250738585072014E-306;
   }

   public SimpleScalarValueChecker(double relativeThreshold, double absoluteThreshold) {
      this.relativeThreshold = relativeThreshold;
      this.absoluteThreshold = absoluteThreshold;
   }

   @Override
   public boolean converged(int iteration, RealPointValuePair previous, RealPointValuePair current) {
      double p = previous.getValue();
      double c = current.getValue();
      double difference = FastMath.abs(p - c);
      double size = FastMath.max(FastMath.abs(p), FastMath.abs(c));
      return difference <= size * this.relativeThreshold || difference <= this.absoluteThreshold;
   }
}
