package org.apache.commons.math.optimization.general;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.BrentSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.util.FastMath;

public class NonLinearConjugateGradientOptimizer extends AbstractScalarDifferentiableOptimizer {
   private final ConjugateGradientFormula updateFormula;
   private Preconditioner preconditioner;
   private UnivariateRealSolver solver;
   private double initialStep;

   public NonLinearConjugateGradientOptimizer(ConjugateGradientFormula updateFormula) {
      this.updateFormula = updateFormula;
      this.preconditioner = null;
      this.solver = null;
      this.initialStep = 1.0;
   }

   public void setPreconditioner(Preconditioner preconditioner) {
      this.preconditioner = preconditioner;
   }

   public void setLineSearchSolver(UnivariateRealSolver lineSearchSolver) {
      this.solver = lineSearchSolver;
   }

   public void setInitialStep(double initialStep) {
      if (initialStep <= 0.0) {
         this.initialStep = 1.0;
      } else {
         this.initialStep = initialStep;
      }
   }

   @Override
   protected RealPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      try {
         if (this.preconditioner == null) {
            this.preconditioner = new NonLinearConjugateGradientOptimizer.IdentityPreconditioner();
         }

         if (this.solver == null) {
            this.solver = new BrentSolver();
         }

         int n = this.point.length;
         double[] r = this.computeObjectiveGradient(this.point);
         if (this.goal == GoalType.MINIMIZE) {
            for(int i = 0; i < n; ++i) {
               r[i] = -r[i];
            }
         }

         double[] steepestDescent = this.preconditioner.precondition(this.point, r);
         double[] searchDirection = (double[])steepestDescent.clone();
         double delta = 0.0;

         for(int i = 0; i < n; ++i) {
            delta += r[i] * searchDirection[i];
         }

         RealPointValuePair current = null;

         while(true) {
            double objective = this.computeObjectiveValue(this.point);
            RealPointValuePair previous = current;
            current = new RealPointValuePair(this.point, objective);
            if (previous != null && this.checker.converged(this.getIterations(), previous, current)) {
               return current;
            }

            this.incrementIterationsCounter();
            double dTd = 0.0;

            for(double di : searchDirection) {
               dTd += di * di;
            }

            UnivariateRealFunction lsf = new NonLinearConjugateGradientOptimizer.LineSearchFunction(searchDirection);
            double step = this.solver.solve(lsf, 0.0, this.findUpperBound(lsf, 0.0, this.initialStep));

            for(int i = 0; i < this.point.length; ++i) {
               this.point[i] += step * searchDirection[i];
            }

            r = this.computeObjectiveGradient(this.point);
            if (this.goal == GoalType.MINIMIZE) {
               for(int i = 0; i < n; ++i) {
                  r[i] = -r[i];
               }
            }

            double deltaOld = delta;
            double[] newSteepestDescent = this.preconditioner.precondition(this.point, r);
            delta = 0.0;

            for(int i = 0; i < n; ++i) {
               delta += r[i] * newSteepestDescent[i];
            }

            double beta;
            if (this.updateFormula == ConjugateGradientFormula.FLETCHER_REEVES) {
               beta = delta / deltaOld;
            } else {
               double deltaMid = 0.0;

               for(int i = 0; i < r.length; ++i) {
                  deltaMid += r[i] * steepestDescent[i];
               }

               beta = (delta - deltaMid) / deltaOld;
            }

            steepestDescent = newSteepestDescent;
            if (this.getIterations() % n != 0 && !(beta < 0.0)) {
               for(int i = 0; i < n; ++i) {
                  searchDirection[i] = steepestDescent[i] + beta * searchDirection[i];
               }
            } else {
               searchDirection = (double[])newSteepestDescent.clone();
            }
         }
      } catch (ConvergenceException var24) {
         throw new OptimizationException(var24);
      }
   }

   private double findUpperBound(UnivariateRealFunction f, double a, double h) throws FunctionEvaluationException, OptimizationException {
      double yA = f.value(a);

      double yB;
      for(double step = h; step < Double.MAX_VALUE; step *= FastMath.max(2.0, yA / yB)) {
         double b = a + step;
         yB = f.value(b);
         if (yA * yB <= 0.0) {
            return b;
         }
      }

      throw new OptimizationException(LocalizedFormats.UNABLE_TO_BRACKET_OPTIMUM_IN_LINE_SEARCH);
   }

   private static class IdentityPreconditioner implements Preconditioner {
      private IdentityPreconditioner() {
      }

      @Override
      public double[] precondition(double[] variables, double[] r) {
         return (double[])r.clone();
      }
   }

   private class LineSearchFunction implements UnivariateRealFunction {
      private final double[] searchDirection;

      public LineSearchFunction(double[] searchDirection) {
         this.searchDirection = searchDirection;
      }

      @Override
      public double value(double x) throws FunctionEvaluationException {
         double[] shiftedPoint = (double[])NonLinearConjugateGradientOptimizer.this.point.clone();

         for(int i = 0; i < shiftedPoint.length; ++i) {
            shiftedPoint[i] += x * this.searchDirection[i];
         }

         double[] gradient = NonLinearConjugateGradientOptimizer.this.computeObjectiveGradient(shiftedPoint);
         double dotProduct = 0.0;

         for(int i = 0; i < gradient.length; ++i) {
            dotProduct += gradient[i] * this.searchDirection[i];
         }

         return dotProduct;
      }
   }
}
