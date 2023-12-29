package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.util.FastMath;

public class HarmonicFunction implements DifferentiableUnivariateRealFunction {
   private final double a;
   private final double omega;
   private final double phi;

   public HarmonicFunction(double a, double omega, double phi) {
      this.a = a;
      this.omega = omega;
      this.phi = phi;
   }

   @Override
   public double value(double x) {
      return this.a * FastMath.cos(this.omega * x + this.phi);
   }

   public HarmonicFunction derivative() {
      return new HarmonicFunction(this.a * this.omega, this.omega, this.phi + (Math.PI / 2));
   }

   public double getAmplitude() {
      return this.a;
   }

   public double getPulsation() {
      return this.omega;
   }

   public double getPhase() {
      return this.phi;
   }
}
