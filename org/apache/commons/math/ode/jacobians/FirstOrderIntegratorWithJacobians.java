package org.apache.commons.math.ode.jacobians;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.ExtendedFirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

@Deprecated
public class FirstOrderIntegratorWithJacobians {
   private final FirstOrderIntegrator integrator;
   private final ODEWithJacobians ode;
   private int maxEvaluations;
   private int evaluations;

   public FirstOrderIntegratorWithJacobians(FirstOrderIntegrator integrator, ParameterizedODE ode, double[] p, double[] hY, double[] hP) {
      this.checkDimension(ode.getDimension(), hY);
      this.checkDimension(ode.getParametersDimension(), p);
      this.checkDimension(ode.getParametersDimension(), hP);
      this.integrator = integrator;
      this.ode = new FirstOrderIntegratorWithJacobians.FiniteDifferencesWrapper(ode, p, hY, hP);
      this.setMaxEvaluations(-1);
   }

   public FirstOrderIntegratorWithJacobians(FirstOrderIntegrator integrator, ODEWithJacobians ode) {
      this.integrator = integrator;
      this.ode = ode;
      this.setMaxEvaluations(-1);
   }

   public void addStepHandler(StepHandlerWithJacobians handler) {
      int n = this.ode.getDimension();
      int k = this.ode.getParametersDimension();
      this.integrator.addStepHandler(new FirstOrderIntegratorWithJacobians.StepHandlerWrapper(handler, n, k));
   }

   public Collection<StepHandlerWithJacobians> getStepHandlers() {
      Collection<StepHandlerWithJacobians> handlers = new ArrayList<>();

      for(StepHandler handler : this.integrator.getStepHandlers()) {
         if (handler instanceof FirstOrderIntegratorWithJacobians.StepHandlerWrapper) {
            handlers.add(((FirstOrderIntegratorWithJacobians.StepHandlerWrapper)handler).getHandler());
         }
      }

      return handlers;
   }

   public void clearStepHandlers() {
      this.integrator.clearStepHandlers();
   }

   public void addEventHandler(EventHandlerWithJacobians handler, double maxCheckInterval, double convergence, int maxIterationCount) {
      int n = this.ode.getDimension();
      int k = this.ode.getParametersDimension();
      this.integrator
         .addEventHandler(new FirstOrderIntegratorWithJacobians.EventHandlerWrapper(handler, n, k), maxCheckInterval, convergence, maxIterationCount);
   }

   public Collection<EventHandlerWithJacobians> getEventHandlers() {
      Collection<EventHandlerWithJacobians> handlers = new ArrayList<>();

      for(EventHandler handler : this.integrator.getEventHandlers()) {
         if (handler instanceof FirstOrderIntegratorWithJacobians.EventHandlerWrapper) {
            handlers.add(((FirstOrderIntegratorWithJacobians.EventHandlerWrapper)handler).getHandler());
         }
      }

      return handlers;
   }

   public void clearEventHandlers() {
      this.integrator.clearEventHandlers();
   }

   public double integrate(double t0, double[] y0, double[][] dY0dP, double t, double[] y, double[][] dYdY0, double[][] dYdP) throws DerivativeException, IntegratorException {
      int n = this.ode.getDimension();
      int k = this.ode.getParametersDimension();
      this.checkDimension(n, y0);
      this.checkDimension(n, y);
      this.checkDimension(n, dYdY0);
      this.checkDimension(n, dYdY0[0]);
      if (k != 0) {
         this.checkDimension(n, dY0dP);
         this.checkDimension(k, dY0dP[0]);
         this.checkDimension(n, dYdP);
         this.checkDimension(k, dYdP[0]);
      }

      double[] z = new double[n * (1 + n + k)];
      System.arraycopy(y0, 0, z, 0, n);

      for(int i = 0; i < n; ++i) {
         z[i * (1 + n) + n] = 1.0;
         System.arraycopy(dY0dP[i], 0, z, n * (n + 1) + i * k, k);
      }

      this.evaluations = 0;
      double stopTime = this.integrator.integrate(new FirstOrderIntegratorWithJacobians.MappingWrapper(), t0, z, t, z);
      dispatchCompoundState(z, y, dYdY0, dYdP);
      return stopTime;
   }

   private static void dispatchCompoundState(double[] z, double[] y, double[][] dydy0, double[][] dydp) {
      int n = y.length;
      int k = dydp[0].length;
      System.arraycopy(z, 0, y, 0, n);

      for(int i = 0; i < n; ++i) {
         System.arraycopy(z, n * (i + 1), dydy0[i], 0, n);
      }

      for(int i = 0; i < n; ++i) {
         System.arraycopy(z, n * (n + 1) + i * k, dydp[i], 0, k);
      }
   }

   public double getCurrentStepStart() {
      return this.integrator.getCurrentStepStart();
   }

   public double getCurrentSignedStepsize() {
      return this.integrator.getCurrentSignedStepsize();
   }

   public void setMaxEvaluations(int maxEvaluations) {
      this.maxEvaluations = maxEvaluations < 0 ? Integer.MAX_VALUE : maxEvaluations;
   }

   public int getMaxEvaluations() {
      return this.maxEvaluations;
   }

   public int getEvaluations() {
      return this.evaluations;
   }

   private void checkDimension(int expected, Object array) throws IllegalArgumentException {
      int arrayDimension = array == null ? 0 : Array.getLength(array);
      if (arrayDimension != expected) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, arrayDimension, expected);
      }
   }

   private static class EventHandlerWrapper implements EventHandler {
      private final EventHandlerWithJacobians handler;
      private double[] y;
      private double[][] dydy0;
      private double[][] dydp;

      public EventHandlerWrapper(EventHandlerWithJacobians handler, int n, int k) {
         this.handler = handler;
         this.y = new double[n];
         this.dydy0 = new double[n][n];
         this.dydp = new double[n][k];
      }

      public EventHandlerWithJacobians getHandler() {
         return this.handler;
      }

      @Override
      public int eventOccurred(double t, double[] z, boolean increasing) throws EventException {
         FirstOrderIntegratorWithJacobians.dispatchCompoundState(z, this.y, this.dydy0, this.dydp);
         return this.handler.eventOccurred(t, this.y, this.dydy0, this.dydp, increasing);
      }

      @Override
      public double g(double t, double[] z) throws EventException {
         FirstOrderIntegratorWithJacobians.dispatchCompoundState(z, this.y, this.dydy0, this.dydp);
         return this.handler.g(t, this.y, this.dydy0, this.dydp);
      }

      @Override
      public void resetState(double t, double[] z) throws EventException {
         FirstOrderIntegratorWithJacobians.dispatchCompoundState(z, this.y, this.dydy0, this.dydp);
         this.handler.resetState(t, this.y, this.dydy0, this.dydp);
      }
   }

   private class FiniteDifferencesWrapper implements ODEWithJacobians {
      private final ParameterizedODE ode;
      private final double[] p;
      private final double[] hY;
      private final double[] hP;
      private final double[] tmpDot;

      public FiniteDifferencesWrapper(ParameterizedODE ode, double[] p, double[] hY, double[] hP) {
         this.ode = ode;
         this.p = (double[])p.clone();
         this.hY = (double[])hY.clone();
         this.hP = (double[])hP.clone();
         this.tmpDot = new double[ode.getDimension()];
      }

      @Override
      public int getDimension() {
         return this.ode.getDimension();
      }

      @Override
      public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
         this.ode.computeDerivatives(t, y, yDot);
      }

      @Override
      public int getParametersDimension() {
         return this.ode.getParametersDimension();
      }

      @Override
      public void computeJacobians(double t, double[] y, double[] yDot, double[][] dFdY, double[][] dFdP) throws DerivativeException {
         int n = this.hY.length;
         int k = this.hP.length;
         FirstOrderIntegratorWithJacobians.this.evaluations += n + k;
         if (FirstOrderIntegratorWithJacobians.this.evaluations > FirstOrderIntegratorWithJacobians.this.maxEvaluations) {
            throw new DerivativeException(new MaxEvaluationsExceededException(FirstOrderIntegratorWithJacobians.this.maxEvaluations));
         } else {
            for(int j = 0; j < n; ++j) {
               double savedYj = y[j];
               y[j] += this.hY[j];
               this.ode.computeDerivatives(t, y, this.tmpDot);

               for(int i = 0; i < n; ++i) {
                  dFdY[i][j] = (this.tmpDot[i] - yDot[i]) / this.hY[j];
               }

               y[j] = savedYj;
            }

            for(int j = 0; j < k; ++j) {
               this.ode.setParameter(j, this.p[j] + this.hP[j]);
               this.ode.computeDerivatives(t, y, this.tmpDot);

               for(int i = 0; i < n; ++i) {
                  dFdP[i][j] = (this.tmpDot[i] - yDot[i]) / this.hP[j];
               }

               this.ode.setParameter(j, this.p[j]);
            }
         }
      }
   }

   private class MappingWrapper implements ExtendedFirstOrderDifferentialEquations {
      private final double[] y;
      private final double[] yDot;
      private final double[][] dFdY;
      private final double[][] dFdP;

      public MappingWrapper() {
         int n = FirstOrderIntegratorWithJacobians.this.ode.getDimension();
         int k = FirstOrderIntegratorWithJacobians.this.ode.getParametersDimension();
         this.y = new double[n];
         this.yDot = new double[n];
         this.dFdY = new double[n][n];
         this.dFdP = new double[n][k];
      }

      @Override
      public int getDimension() {
         int n = this.y.length;
         int k = this.dFdP[0].length;
         return n * (1 + n + k);
      }

      @Override
      public int getMainSetDimension() {
         return FirstOrderIntegratorWithJacobians.this.ode.getDimension();
      }

      @Override
      public void computeDerivatives(double t, double[] z, double[] zDot) throws DerivativeException {
         int n = this.y.length;
         int k = this.dFdP[0].length;
         System.arraycopy(z, 0, this.y, 0, n);
         if (++FirstOrderIntegratorWithJacobians.this.evaluations > FirstOrderIntegratorWithJacobians.this.maxEvaluations) {
            throw new DerivativeException(new MaxEvaluationsExceededException(FirstOrderIntegratorWithJacobians.this.maxEvaluations));
         } else {
            FirstOrderIntegratorWithJacobians.this.ode.computeDerivatives(t, this.y, this.yDot);
            FirstOrderIntegratorWithJacobians.this.ode.computeJacobians(t, this.y, this.yDot, this.dFdY, this.dFdP);
            System.arraycopy(this.yDot, 0, zDot, 0, n);

            for(int i = 0; i < n; ++i) {
               double[] dFdYi = this.dFdY[i];

               for(int j = 0; j < n; ++j) {
                  double s = 0.0;
                  int startIndex = n + j;
                  int zIndex = startIndex;

                  for(int l = 0; l < n; ++l) {
                     s += dFdYi[l] * z[zIndex];
                     zIndex += n;
                  }

                  zDot[startIndex + i * n] = s;
               }
            }

            for(int i = 0; i < n; ++i) {
               double[] dFdYi = this.dFdY[i];
               double[] dFdPi = this.dFdP[i];

               for(int j = 0; j < k; ++j) {
                  double s = dFdPi[j];
                  int startIndex = n * (n + 1) + j;
                  int zIndex = startIndex;

                  for(int l = 0; l < n; ++l) {
                     s += dFdYi[l] * z[zIndex];
                     zIndex += k;
                  }

                  zDot[startIndex + i * k] = s;
               }
            }
         }
      }
   }

   private static class StepHandlerWrapper implements StepHandler {
      private final StepHandlerWithJacobians handler;
      private final int n;
      private final int k;

      public StepHandlerWrapper(StepHandlerWithJacobians handler, int n, int k) {
         this.handler = handler;
         this.n = n;
         this.k = k;
      }

      public StepHandlerWithJacobians getHandler() {
         return this.handler;
      }

      @Override
      public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
         this.handler.handleStep(new FirstOrderIntegratorWithJacobians.StepInterpolatorWrapper(interpolator, this.n, this.k), isLast);
      }

      @Override
      public boolean requiresDenseOutput() {
         return this.handler.requiresDenseOutput();
      }

      @Override
      public void reset() {
         this.handler.reset();
      }
   }

   private static class StepInterpolatorWrapper implements StepInterpolatorWithJacobians {
      private StepInterpolator interpolator;
      private double[] y;
      private double[][] dydy0;
      private double[][] dydp;
      private double[] yDot;
      private double[][] dydy0Dot;
      private double[][] dydpDot;

      public StepInterpolatorWrapper() {
      }

      public StepInterpolatorWrapper(StepInterpolator interpolator, int n, int k) {
         this.interpolator = interpolator;
         this.y = new double[n];
         this.dydy0 = new double[n][n];
         this.dydp = new double[n][k];
         this.yDot = new double[n];
         this.dydy0Dot = new double[n][n];
         this.dydpDot = new double[n][k];
      }

      @Override
      public void setInterpolatedTime(double time) {
         this.interpolator.setInterpolatedTime(time);
      }

      @Override
      public boolean isForward() {
         return this.interpolator.isForward();
      }

      @Override
      public double getPreviousTime() {
         return this.interpolator.getPreviousTime();
      }

      @Override
      public double getInterpolatedTime() {
         return this.interpolator.getInterpolatedTime();
      }

      @Override
      public double[] getInterpolatedY() throws DerivativeException {
         double[] extendedState = this.interpolator.getInterpolatedState();
         System.arraycopy(extendedState, 0, this.y, 0, this.y.length);
         return this.y;
      }

      @Override
      public double[][] getInterpolatedDyDy0() throws DerivativeException {
         double[] extendedState = this.interpolator.getInterpolatedState();
         int n = this.y.length;
         int start = n;

         for(int i = 0; i < n; ++i) {
            System.arraycopy(extendedState, start, this.dydy0[i], 0, n);
            start += n;
         }

         return this.dydy0;
      }

      @Override
      public double[][] getInterpolatedDyDp() throws DerivativeException {
         double[] extendedState = this.interpolator.getInterpolatedState();
         int n = this.y.length;
         int k = this.dydp[0].length;
         int start = n * (n + 1);

         for(int i = 0; i < n; ++i) {
            System.arraycopy(extendedState, start, this.dydp[i], 0, k);
            start += k;
         }

         return this.dydp;
      }

      @Override
      public double[] getInterpolatedYDot() throws DerivativeException {
         double[] extendedDerivatives = this.interpolator.getInterpolatedDerivatives();
         System.arraycopy(extendedDerivatives, 0, this.yDot, 0, this.yDot.length);
         return this.yDot;
      }

      @Override
      public double[][] getInterpolatedDyDy0Dot() throws DerivativeException {
         double[] extendedDerivatives = this.interpolator.getInterpolatedDerivatives();
         int n = this.y.length;
         int start = n;

         for(int i = 0; i < n; ++i) {
            System.arraycopy(extendedDerivatives, start, this.dydy0Dot[i], 0, n);
            start += n;
         }

         return this.dydy0Dot;
      }

      @Override
      public double[][] getInterpolatedDyDpDot() throws DerivativeException {
         double[] extendedDerivatives = this.interpolator.getInterpolatedDerivatives();
         int n = this.y.length;
         int k = this.dydpDot[0].length;
         int start = n * (n + 1);

         for(int i = 0; i < n; ++i) {
            System.arraycopy(extendedDerivatives, start, this.dydpDot[i], 0, k);
            start += k;
         }

         return this.dydpDot;
      }

      @Override
      public double getCurrentTime() {
         return this.interpolator.getCurrentTime();
      }

      @Override
      public StepInterpolatorWithJacobians copy() throws DerivativeException {
         int n = this.y.length;
         int k = this.dydp[0].length;
         FirstOrderIntegratorWithJacobians.StepInterpolatorWrapper copied = new FirstOrderIntegratorWithJacobians.StepInterpolatorWrapper(
            this.interpolator.copy(), n, k
         );
         copyArray(this.y, copied.y);
         copyArray(this.dydy0, copied.dydy0);
         copyArray(this.dydp, copied.dydp);
         copyArray(this.yDot, copied.yDot);
         copyArray(this.dydy0Dot, copied.dydy0Dot);
         copyArray(this.dydpDot, copied.dydpDot);
         return copied;
      }

      @Override
      public void writeExternal(ObjectOutput out) throws IOException {
         out.writeObject(this.interpolator);
         out.writeInt(this.y.length);
         out.writeInt(this.dydp[0].length);
         writeArray(out, this.y);
         writeArray(out, this.dydy0);
         writeArray(out, this.dydp);
         writeArray(out, this.yDot);
         writeArray(out, this.dydy0Dot);
         writeArray(out, this.dydpDot);
      }

      @Override
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
         this.interpolator = (StepInterpolator)in.readObject();
         int n = in.readInt();
         int k = in.readInt();
         this.y = new double[n];
         this.dydy0 = new double[n][n];
         this.dydp = new double[n][k];
         this.yDot = new double[n];
         this.dydy0Dot = new double[n][n];
         this.dydpDot = new double[n][k];
         readArray(in, this.y);
         readArray(in, this.dydy0);
         readArray(in, this.dydp);
         readArray(in, this.yDot);
         readArray(in, this.dydy0Dot);
         readArray(in, this.dydpDot);
      }

      private static void copyArray(double[] src, double[] dest) {
         System.arraycopy(src, 0, dest, 0, src.length);
      }

      private static void copyArray(double[][] src, double[][] dest) {
         for(int i = 0; i < src.length; ++i) {
            copyArray(src[i], dest[i]);
         }
      }

      private static void writeArray(ObjectOutput out, double[] array) throws IOException {
         for(int i = 0; i < array.length; ++i) {
            out.writeDouble(array[i]);
         }
      }

      private static void writeArray(ObjectOutput out, double[][] array) throws IOException {
         for(int i = 0; i < array.length; ++i) {
            writeArray(out, array[i]);
         }
      }

      private static void readArray(ObjectInput in, double[] array) throws IOException {
         for(int i = 0; i < array.length; ++i) {
            array[i] = in.readDouble();
         }
      }

      private static void readArray(ObjectInput in, double[][] array) throws IOException {
         for(int i = 0; i < array.length; ++i) {
            readArray(in, array[i]);
         }
      }
   }
}
