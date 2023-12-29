package org.apache.commons.math.ode;

public class FirstOrderConverter implements FirstOrderDifferentialEquations {
   private final SecondOrderDifferentialEquations equations;
   private final int dimension;
   private final double[] z;
   private final double[] zDot;
   private final double[] zDDot;

   public FirstOrderConverter(SecondOrderDifferentialEquations equations) {
      this.equations = equations;
      this.dimension = equations.getDimension();
      this.z = new double[this.dimension];
      this.zDot = new double[this.dimension];
      this.zDDot = new double[this.dimension];
   }

   @Override
   public int getDimension() {
      return 2 * this.dimension;
   }

   @Override
   public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
      System.arraycopy(y, 0, this.z, 0, this.dimension);
      System.arraycopy(y, this.dimension, this.zDot, 0, this.dimension);
      this.equations.computeSecondDerivatives(t, this.z, this.zDot, this.zDDot);
      System.arraycopy(this.zDot, 0, yDot, 0, this.dimension);
      System.arraycopy(this.zDDot, 0, yDot, this.dimension, this.dimension);
   }
}
