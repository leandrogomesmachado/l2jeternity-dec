package org.apache.commons.math.analysis.integration;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class LegendreGaussIntegrator extends UnivariateRealIntegratorImpl {
   private static final double[] ABSCISSAS_2 = new double[]{-1.0 / FastMath.sqrt(3.0), 1.0 / FastMath.sqrt(3.0)};
   private static final double[] WEIGHTS_2 = new double[]{1.0, 1.0};
   private static final double[] ABSCISSAS_3 = new double[]{-FastMath.sqrt(0.6), 0.0, FastMath.sqrt(0.6)};
   private static final double[] WEIGHTS_3 = new double[]{0.5555555555555556, 0.8888888888888888, 0.5555555555555556};
   private static final double[] ABSCISSAS_4 = new double[]{
      -FastMath.sqrt((15.0 + 2.0 * FastMath.sqrt(30.0)) / 35.0),
      -FastMath.sqrt((15.0 - 2.0 * FastMath.sqrt(30.0)) / 35.0),
      FastMath.sqrt((15.0 - 2.0 * FastMath.sqrt(30.0)) / 35.0),
      FastMath.sqrt((15.0 + 2.0 * FastMath.sqrt(30.0)) / 35.0)
   };
   private static final double[] WEIGHTS_4 = new double[]{
      (90.0 - 5.0 * FastMath.sqrt(30.0)) / 180.0,
      (90.0 + 5.0 * FastMath.sqrt(30.0)) / 180.0,
      (90.0 + 5.0 * FastMath.sqrt(30.0)) / 180.0,
      (90.0 - 5.0 * FastMath.sqrt(30.0)) / 180.0
   };
   private static final double[] ABSCISSAS_5 = new double[]{
      -FastMath.sqrt((35.0 + 2.0 * FastMath.sqrt(70.0)) / 63.0),
      -FastMath.sqrt((35.0 - 2.0 * FastMath.sqrt(70.0)) / 63.0),
      0.0,
      FastMath.sqrt((35.0 - 2.0 * FastMath.sqrt(70.0)) / 63.0),
      FastMath.sqrt((35.0 + 2.0 * FastMath.sqrt(70.0)) / 63.0)
   };
   private static final double[] WEIGHTS_5 = new double[]{
      (322.0 - 13.0 * FastMath.sqrt(70.0)) / 900.0,
      (322.0 + 13.0 * FastMath.sqrt(70.0)) / 900.0,
      0.5688888888888889,
      (322.0 + 13.0 * FastMath.sqrt(70.0)) / 900.0,
      (322.0 - 13.0 * FastMath.sqrt(70.0)) / 900.0
   };
   private final double[] abscissas;
   private final double[] weights;

   public LegendreGaussIntegrator(int n, int defaultMaximalIterationCount) throws IllegalArgumentException {
      super(defaultMaximalIterationCount);
      switch(n) {
         case 2:
            this.abscissas = ABSCISSAS_2;
            this.weights = WEIGHTS_2;
            break;
         case 3:
            this.abscissas = ABSCISSAS_3;
            this.weights = WEIGHTS_3;
            break;
         case 4:
            this.abscissas = ABSCISSAS_4;
            this.weights = WEIGHTS_4;
            break;
         case 5:
            this.abscissas = ABSCISSAS_5;
            this.weights = WEIGHTS_5;
            break;
         default:
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.N_POINTS_GAUSS_LEGENDRE_INTEGRATOR_NOT_SUPPORTED, n, 2, 5);
      }
   }

   @Deprecated
   @Override
   public double integrate(double min, double max) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException {
      return this.integrate(this.f, min, max);
   }

   @Override
   public double integrate(UnivariateRealFunction f, double min, double max) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException {
      this.clearResult();
      this.verifyInterval(min, max);
      this.verifyIterationCount();
      double oldt = this.stage(f, min, max, 1);
      int n = 2;

      for(int i = 0; i < this.maximalIterationCount; ++i) {
         double t = this.stage(f, min, max, n);
         double delta = FastMath.abs(t - oldt);
         double limit = FastMath.max(this.absoluteAccuracy, this.relativeAccuracy * (FastMath.abs(oldt) + FastMath.abs(t)) * 0.5);
         if (i + 1 >= this.minimalIterationCount && delta <= limit) {
            this.setResult(t, i);
            return this.result;
         }

         double ratio = FastMath.min(4.0, FastMath.pow(delta / limit, 0.5 / (double)this.abscissas.length));
         n = FastMath.max((int)(ratio * (double)n), n + 1);
         oldt = t;
      }

      throw new MaxIterationsExceededException(this.maximalIterationCount);
   }

   private double stage(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException {
      double step = (max - min) / (double)n;
      double halfStep = step / 2.0;
      double midPoint = min + halfStep;
      double sum = 0.0;

      for(int i = 0; i < n; ++i) {
         for(int j = 0; j < this.abscissas.length; ++j) {
            sum += this.weights[j] * f.value(midPoint + halfStep * this.abscissas[j]);
         }

         midPoint += step;
      }

      return halfStep * sum;
   }
}
