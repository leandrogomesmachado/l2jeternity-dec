package org.apache.commons.math.optimization.fitting;

import java.io.Serializable;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.ZeroException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class GaussianDerivativeFunction implements UnivariateRealFunction, Serializable {
   private static final long serialVersionUID = -6500229089670174766L;
   private final double b;
   private final double c;
   private final double d2;

   public GaussianDerivativeFunction(double b, double c, double d) {
      if (d == 0.0) {
         throw new ZeroException();
      } else {
         this.b = b;
         this.c = c;
         this.d2 = d * d;
      }
   }

   public GaussianDerivativeFunction(double[] parameters) {
      if (parameters == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else if (parameters.length != 3) {
         throw new DimensionMismatchException(3, parameters.length);
      } else if (parameters[2] == 0.0) {
         throw new ZeroException();
      } else {
         this.b = parameters[0];
         this.c = parameters[1];
         this.d2 = parameters[2] * parameters[2];
      }
   }

   @Override
   public double value(double x) {
      double xMc = x - this.c;
      return -this.b / this.d2 * xMc * Math.exp(-(xMc * xMc) / (2.0 * this.d2));
   }
}
