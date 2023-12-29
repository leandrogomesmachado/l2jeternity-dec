package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;

public class GaussNewtonOptimizer extends AbstractLeastSquaresOptimizer {
   private final boolean useLU;

   public GaussNewtonOptimizer(boolean useLU) {
      this.useLU = useLU;
   }

   @Override
   public VectorialPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      VectorialPointValuePair current = null;
      boolean converged = false;

      while(!converged) {
         this.incrementIterationsCounter();
         VectorialPointValuePair previous = current;
         this.updateResidualsAndCost();
         this.updateJacobian();
         current = new VectorialPointValuePair(this.point, this.objective);
         double[] b = new double[this.cols];
         double[][] a = new double[this.cols][this.cols];

         for(int i = 0; i < this.rows; ++i) {
            double[] grad = this.jacobian[i];
            double weight = this.residualsWeights[i];
            double residual = this.objective[i] - this.targetValues[i];
            double wr = weight * residual;

            for(int j = 0; j < this.cols; ++j) {
               b[j] += wr * grad[j];
            }

            for(int k = 0; k < this.cols; ++k) {
               double[] ak = a[k];
               double wgk = weight * grad[k];

               for(int l = 0; l < this.cols; ++l) {
                  ak[l] += wgk * grad[l];
               }
            }
         }

         try {
            RealMatrix mA = new BlockRealMatrix(a);
            DecompositionSolver solver = this.useLU ? new LUDecompositionImpl(mA).getSolver() : new QRDecompositionImpl(mA).getSolver();
            double[] dX = solver.solve(b);

            for(int i = 0; i < this.cols; ++i) {
               this.point[i] += dX[i];
            }
         } catch (InvalidMatrixException var19) {
            throw new OptimizationException(LocalizedFormats.UNABLE_TO_SOLVE_SINGULAR_PROBLEM);
         }

         if (previous != null) {
            converged = this.checker.converged(this.getIterations(), previous, current);
         }
      }

      return current;
   }
}
