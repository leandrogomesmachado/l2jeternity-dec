package org.apache.commons.math.estimation;

import java.io.Serializable;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.util.FastMath;

@Deprecated
public class GaussNewtonEstimator extends AbstractEstimator implements Serializable {
   private static final long serialVersionUID = 5485001826076289109L;
   private static final double DEFAULT_STEADY_STATE_THRESHOLD = 1.0E-6;
   private static final double DEFAULT_CONVERGENCE = 1.0E-6;
   private double steadyStateThreshold;
   private double convergence;

   public GaussNewtonEstimator() {
      this.steadyStateThreshold = 1.0E-6;
      this.convergence = 1.0E-6;
   }

   public GaussNewtonEstimator(int maxCostEval, double convergence, double steadyStateThreshold) {
      this.setMaxCostEval(maxCostEval);
      this.steadyStateThreshold = steadyStateThreshold;
      this.convergence = convergence;
   }

   public void setConvergence(double convergence) {
      this.convergence = convergence;
   }

   public void setSteadyStateThreshold(double steadyStateThreshold) {
      this.steadyStateThreshold = steadyStateThreshold;
   }

   @Override
   public void estimate(EstimationProblem problem) throws EstimationException {
      this.initializeEstimate(problem);
      double[] grad = new double[this.parameters.length];
      ArrayRealVector bDecrement = new ArrayRealVector(this.parameters.length);
      double[] bDecrementData = bDecrement.getDataRef();
      RealMatrix wGradGradT = MatrixUtils.createRealMatrix(this.parameters.length, this.parameters.length);
      double previous = Double.POSITIVE_INFINITY;

      do {
         this.incrementJacobianEvaluationsCounter();
         RealVector b = new ArrayRealVector(this.parameters.length);
         RealMatrix a = MatrixUtils.createRealMatrix(this.parameters.length, this.parameters.length);

         for(int i = 0; i < this.measurements.length; ++i) {
            if (!this.measurements[i].isIgnored()) {
               double weight = this.measurements[i].getWeight();
               double residual = this.measurements[i].getResidual();

               for(int j = 0; j < this.parameters.length; ++j) {
                  grad[j] = this.measurements[i].getPartial(this.parameters[j]);
                  bDecrementData[j] = weight * residual * grad[j];
               }

               for(int k = 0; k < this.parameters.length; ++k) {
                  double gk = grad[k];

                  for(int l = 0; l < this.parameters.length; ++l) {
                     wGradGradT.setEntry(k, l, weight * gk * grad[l]);
                  }
               }

               a = a.add(wGradGradT);
               b = b.add(bDecrement);
            }
         }

         try {
            RealVector dX = new LUDecompositionImpl(a).getSolver().solve(b);

            for(int i = 0; i < this.parameters.length; ++i) {
               this.parameters[i].setEstimate(this.parameters[i].getEstimate() + dX.getEntry(i));
            }
         } catch (InvalidMatrixException var19) {
            throw new EstimationException(LocalizedFormats.UNABLE_TO_SOLVE_SINGULAR_PROBLEM);
         }

         previous = this.cost;
         this.updateResidualsAndCost();
      } while(
         this.getCostEvaluations() < 2
            || FastMath.abs(previous - this.cost) > this.cost * this.steadyStateThreshold && FastMath.abs(this.cost) > this.convergence
      );
   }
}
