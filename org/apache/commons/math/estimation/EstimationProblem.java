package org.apache.commons.math.estimation;

@Deprecated
public interface EstimationProblem {
   WeightedMeasurement[] getMeasurements();

   EstimatedParameter[] getUnboundParameters();

   EstimatedParameter[] getAllParameters();
}
