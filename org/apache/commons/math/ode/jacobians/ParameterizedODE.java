package org.apache.commons.math.ode.jacobians;

import org.apache.commons.math.ode.FirstOrderDifferentialEquations;

@Deprecated
public interface ParameterizedODE extends FirstOrderDifferentialEquations {
   int getParametersDimension();

   void setParameter(int var1, double var2);
}
