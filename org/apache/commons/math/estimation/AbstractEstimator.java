package org.apache.commons.math.estimation;

import java.util.Arrays;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.util.FastMath;

@Deprecated
public abstract class AbstractEstimator implements Estimator {
   public static final int DEFAULT_MAX_COST_EVALUATIONS = 100;
   protected WeightedMeasurement[] measurements;
   protected EstimatedParameter[] parameters;
   protected double[] jacobian;
   protected int cols;
   protected int rows;
   protected double[] residuals;
   protected double cost;
   private int maxCostEval;
   private int costEvaluations;
   private int jacobianEvaluations;

   protected AbstractEstimator() {
      this.setMaxCostEval(100);
   }

   public final void setMaxCostEval(int maxCostEval) {
      this.maxCostEval = maxCostEval;
   }

   public final int getCostEvaluations() {
      return this.costEvaluations;
   }

   public final int getJacobianEvaluations() {
      return this.jacobianEvaluations;
   }

   protected void updateJacobian() {
      this.incrementJacobianEvaluationsCounter();
      Arrays.fill(this.jacobian, 0.0);
      int index = 0;

      for(int i = 0; i < this.rows; ++i) {
         WeightedMeasurement wm = this.measurements[i];
         double factor = -FastMath.sqrt(wm.getWeight());

         for(int j = 0; j < this.cols; ++j) {
            this.jacobian[index++] = factor * wm.getPartial(this.parameters[j]);
         }
      }
   }

   protected final void incrementJacobianEvaluationsCounter() {
      ++this.jacobianEvaluations;
   }

   protected void updateResidualsAndCost() throws EstimationException {
      if (++this.costEvaluations > this.maxCostEval) {
         throw new EstimationException(LocalizedFormats.MAX_EVALUATIONS_EXCEEDED, this.maxCostEval);
      } else {
         this.cost = 0.0;
         int index = 0;

         for(int i = 0; i < this.rows; index += this.cols) {
            WeightedMeasurement wm = this.measurements[i];
            double residual = wm.getResidual();
            this.residuals[i] = FastMath.sqrt(wm.getWeight()) * residual;
            this.cost += wm.getWeight() * residual * residual;
            ++i;
         }

         this.cost = FastMath.sqrt(this.cost);
      }
   }

   @Override
   public double getRMS(EstimationProblem problem) {
      WeightedMeasurement[] wm = problem.getMeasurements();
      double criterion = 0.0;

      for(int i = 0; i < wm.length; ++i) {
         double residual = wm[i].getResidual();
         criterion += wm[i].getWeight() * residual * residual;
      }

      return FastMath.sqrt(criterion / (double)wm.length);
   }

   public double getChiSquare(EstimationProblem problem) {
      WeightedMeasurement[] wm = problem.getMeasurements();
      double chiSquare = 0.0;

      for(int i = 0; i < wm.length; ++i) {
         double residual = wm[i].getResidual();
         chiSquare += residual * residual / wm[i].getWeight();
      }

      return chiSquare;
   }

   @Override
   public double[][] getCovariances(EstimationProblem problem) throws EstimationException {
      this.updateJacobian();
      int n = problem.getMeasurements().length;
      int m = problem.getUnboundParameters().length;
      int max = m * n;
      double[][] jTj = new double[m][m];

      for(int i = 0; i < m; ++i) {
         for(int j = i; j < m; ++j) {
            double sum = 0.0;

            for(int k = 0; k < max; k += m) {
               sum += this.jacobian[k + i] * this.jacobian[k + j];
            }

            jTj[i][j] = sum;
            jTj[j][i] = sum;
         }
      }

      try {
         RealMatrix inverse = new LUDecompositionImpl(MatrixUtils.createRealMatrix(jTj)).getSolver().getInverse();
         return inverse.getData();
      } catch (InvalidMatrixException var11) {
         throw new EstimationException(LocalizedFormats.UNABLE_TO_COMPUTE_COVARIANCE_SINGULAR_PROBLEM);
      }
   }

   @Override
   public double[] guessParametersErrors(EstimationProblem problem) throws EstimationException {
      int m = problem.getMeasurements().length;
      int p = problem.getUnboundParameters().length;
      if (m <= p) {
         throw new EstimationException(LocalizedFormats.NO_DEGREES_OF_FREEDOM, m, p);
      } else {
         double[] errors = new double[problem.getUnboundParameters().length];
         double c = FastMath.sqrt(this.getChiSquare(problem) / (double)(m - p));
         double[][] covar = this.getCovariances(problem);

         for(int i = 0; i < errors.length; ++i) {
            errors[i] = FastMath.sqrt(covar[i][i]) * c;
         }

         return errors;
      }
   }

   protected void initializeEstimate(EstimationProblem problem) {
      this.costEvaluations = 0;
      this.jacobianEvaluations = 0;
      this.measurements = problem.getMeasurements();
      this.parameters = problem.getUnboundParameters();
      this.rows = this.measurements.length;
      this.cols = this.parameters.length;
      this.jacobian = new double[this.rows * this.cols];
      this.residuals = new double[this.rows];
      this.cost = Double.POSITIVE_INFINITY;
   }

   @Override
   public abstract void estimate(EstimationProblem var1) throws EstimationException;
}
