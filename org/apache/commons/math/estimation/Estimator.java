package org.apache.commons.math.estimation;

@Deprecated
public interface Estimator {
   void estimate(EstimationProblem var1) throws EstimationException;

   double getRMS(EstimationProblem var1);

   double[][] getCovariances(EstimationProblem var1) throws EstimationException;

   double[] guessParametersErrors(EstimationProblem var1) throws EstimationException;
}
