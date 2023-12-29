package org.apache.commons.math.ode.nonstiff;

public class ThreeEighthesIntegrator extends RungeKuttaIntegrator {
   private static final double[] STATIC_C = new double[]{0.3333333333333333, 0.6666666666666666, 1.0};
   private static final double[][] STATIC_A = new double[][]{{0.3333333333333333}, {-0.3333333333333333, 1.0}, {1.0, -1.0, 1.0}};
   private static final double[] STATIC_B = new double[]{0.125, 0.375, 0.375, 0.125};

   public ThreeEighthesIntegrator(double step) {
      super("3/8", STATIC_C, STATIC_A, STATIC_B, new ThreeEighthesStepInterpolator(), step);
   }
}
