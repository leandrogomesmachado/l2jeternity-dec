package org.apache.commons.math.optimization.linear;

import java.util.Collection;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;

public interface LinearOptimizer {
   void setMaxIterations(int var1);

   int getMaxIterations();

   int getIterations();

   RealPointValuePair optimize(LinearObjectiveFunction var1, Collection<LinearConstraint> var2, GoalType var3, boolean var4) throws OptimizationException;
}
