package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.util.FastMath;

public abstract class AbstractLeastSquaresOptimizer implements DifferentiableMultivariateVectorialOptimizer {
   public static final int DEFAULT_MAX_ITERATIONS = 100;
   protected VectorialConvergenceChecker checker;
   protected double[][] jacobian;
   protected int cols;
   protected int rows;
   protected double[] targetValues;
   protected double[] residualsWeights;
   protected double[] point;
   protected double[] objective;
   protected double[] residuals;
   protected double[][] wjacobian;
   protected double[] wresiduals;
   protected double cost;
   private int maxIterations;
   private int iterations;
   private int maxEvaluations;
   private int objectiveEvaluations;
   private int jacobianEvaluations;
   private DifferentiableMultivariateVectorialFunction function;
   private MultivariateMatrixFunction jF;

   protected AbstractLeastSquaresOptimizer() {
      this.setConvergenceChecker(new SimpleVectorialValueChecker());
      this.setMaxIterations(100);
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   @Override
   public void setMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
   }

   @Override
   public int getMaxIterations() {
      return this.maxIterations;
   }

   @Override
   public int getIterations() {
      return this.iterations;
   }

   @Override
   public void setMaxEvaluations(int maxEvaluations) {
      this.maxEvaluations = maxEvaluations;
   }

   @Override
   public int getMaxEvaluations() {
      return this.maxEvaluations;
   }

   @Override
   public int getEvaluations() {
      return this.objectiveEvaluations;
   }

   @Override
   public int getJacobianEvaluations() {
      return this.jacobianEvaluations;
   }

   @Override
   public void setConvergenceChecker(VectorialConvergenceChecker convergenceChecker) {
      this.checker = convergenceChecker;
   }

   @Override
   public VectorialConvergenceChecker getConvergenceChecker() {
      return this.checker;
   }

   protected void incrementIterationsCounter() throws OptimizationException {
      if (++this.iterations > this.maxIterations) {
         throw new OptimizationException(new MaxIterationsExceededException(this.maxIterations));
      }
   }

   protected void updateJacobian() throws FunctionEvaluationException {
      ++this.jacobianEvaluations;
      this.jacobian = this.jF.value(this.point);
      if (this.jacobian.length != this.rows) {
         throw new FunctionEvaluationException(this.point, LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, this.jacobian.length, this.rows);
      } else {
         for(int i = 0; i < this.rows; ++i) {
            double[] ji = this.jacobian[i];
            double wi = FastMath.sqrt(this.residualsWeights[i]);

            for(int j = 0; j < this.cols; ++j) {
               ji[j] *= -1.0;
               this.wjacobian[i][j] = ji[j] * wi;
            }
         }
      }
   }

   protected void updateResidualsAndCost() throws FunctionEvaluationException {
      if (++this.objectiveEvaluations > this.maxEvaluations) {
         throw new FunctionEvaluationException(new MaxEvaluationsExceededException(this.maxEvaluations), this.point);
      } else {
         this.objective = this.function.value(this.point);
         if (this.objective.length != this.rows) {
            throw new FunctionEvaluationException(this.point, LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, this.objective.length, this.rows);
         } else {
            this.cost = 0.0;
            int index = 0;

            for(int i = 0; i < this.rows; ++i) {
               double residual = this.targetValues[i] - this.objective[i];
               this.residuals[i] = residual;
               this.wresiduals[i] = residual * FastMath.sqrt(this.residualsWeights[i]);
               this.cost += this.residualsWeights[i] * residual * residual;
               index += this.cols;
            }

            this.cost = FastMath.sqrt(this.cost);
         }
      }
   }

   public double getRMS() {
      return FastMath.sqrt(this.getChiSquare() / (double)this.rows);
   }

   public double getChiSquare() {
      return this.cost * this.cost;
   }

   public double[][] getCovariances() throws FunctionEvaluationException, OptimizationException {
      this.updateJacobian();
      double[][] jTj = new double[this.cols][this.cols];

      for(int i = 0; i < this.cols; ++i) {
         for(int j = i; j < this.cols; ++j) {
            double sum = 0.0;

            for(int k = 0; k < this.rows; ++k) {
               sum += this.wjacobian[k][i] * this.wjacobian[k][j];
            }

            jTj[i][j] = sum;
            jTj[j][i] = sum;
         }
      }

      try {
         RealMatrix inverse = new LUDecompositionImpl(MatrixUtils.createRealMatrix(jTj)).getSolver().getInverse();
         return inverse.getData();
      } catch (InvalidMatrixException var7) {
         throw new OptimizationException(LocalizedFormats.UNABLE_TO_COMPUTE_COVARIANCE_SINGULAR_PROBLEM);
      }
   }

   public double[] guessParametersErrors() throws FunctionEvaluationException, OptimizationException {
      if (this.rows <= this.cols) {
         throw new OptimizationException(LocalizedFormats.NO_DEGREES_OF_FREEDOM, this.rows, this.cols);
      } else {
         double[] errors = new double[this.cols];
         double c = FastMath.sqrt(this.getChiSquare() / (double)(this.rows - this.cols));
         double[][] covar = this.getCovariances();

         for(int i = 0; i < errors.length; ++i) {
            errors[i] = FastMath.sqrt(covar[i][i]) * c;
         }

         return errors;
      }
   }

   @Override
   public VectorialPointValuePair optimize(DifferentiableMultivariateVectorialFunction f, double[] target, double[] weights, double[] startPoint) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      if (target.length != weights.length) {
         throw new OptimizationException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, target.length, weights.length);
      } else {
         this.iterations = 0;
         this.objectiveEvaluations = 0;
         this.jacobianEvaluations = 0;
         this.function = f;
         this.jF = f.jacobian();
         this.targetValues = (double[])target.clone();
         this.residualsWeights = (double[])weights.clone();
         this.point = (double[])startPoint.clone();
         this.residuals = new double[target.length];
         this.rows = target.length;
         this.cols = this.point.length;
         this.jacobian = new double[this.rows][this.cols];
         this.wjacobian = new double[this.rows][this.cols];
         this.wresiduals = new double[this.rows];
         this.cost = Double.POSITIVE_INFINITY;
         return this.doOptimize();
      }
   }

   protected abstract VectorialPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException, IllegalArgumentException;
}
