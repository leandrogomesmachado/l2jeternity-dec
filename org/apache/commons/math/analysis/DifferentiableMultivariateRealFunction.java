package org.apache.commons.math.analysis;

public interface DifferentiableMultivariateRealFunction extends MultivariateRealFunction {
   MultivariateRealFunction partialDerivative(int var1);

   MultivariateVectorialFunction gradient();
}
