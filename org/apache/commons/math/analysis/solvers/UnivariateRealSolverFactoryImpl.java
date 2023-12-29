package org.apache.commons.math.analysis.solvers;

public class UnivariateRealSolverFactoryImpl extends UnivariateRealSolverFactory {
   @Override
   public UnivariateRealSolver newDefaultSolver() {
      return this.newBrentSolver();
   }

   @Override
   public UnivariateRealSolver newBisectionSolver() {
      return new BisectionSolver();
   }

   @Override
   public UnivariateRealSolver newBrentSolver() {
      return new BrentSolver();
   }

   @Override
   public UnivariateRealSolver newNewtonSolver() {
      return new NewtonSolver();
   }

   @Override
   public UnivariateRealSolver newSecantSolver() {
      return new SecantSolver();
   }
}
