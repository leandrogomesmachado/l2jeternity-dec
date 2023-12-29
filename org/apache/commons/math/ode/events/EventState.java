package org.apache.commons.math.ode.events;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.BrentSolver;
import org.apache.commons.math.exception.MathInternalError;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.util.FastMath;

public class EventState {
   private final EventHandler handler;
   private final double maxCheckInterval;
   private final double convergence;
   private final int maxIterationCount;
   private double t0;
   private double g0;
   private boolean g0Positive;
   private boolean pendingEvent;
   private double pendingEventTime;
   private double previousEventTime;
   private boolean forward;
   private boolean increasing;
   private int nextAction;

   public EventState(EventHandler handler, double maxCheckInterval, double convergence, int maxIterationCount) {
      this.handler = handler;
      this.maxCheckInterval = maxCheckInterval;
      this.convergence = FastMath.abs(convergence);
      this.maxIterationCount = maxIterationCount;
      this.t0 = Double.NaN;
      this.g0 = Double.NaN;
      this.g0Positive = true;
      this.pendingEvent = false;
      this.pendingEventTime = Double.NaN;
      this.previousEventTime = Double.NaN;
      this.increasing = true;
      this.nextAction = 3;
   }

   public EventHandler getEventHandler() {
      return this.handler;
   }

   public double getMaxCheckInterval() {
      return this.maxCheckInterval;
   }

   public double getConvergence() {
      return this.convergence;
   }

   public int getMaxIterationCount() {
      return this.maxIterationCount;
   }

   public void reinitializeBegin(StepInterpolator interpolator) throws EventException {
      try {
         double ignoreZone = interpolator.isForward() ? this.getConvergence() : -this.getConvergence();
         this.t0 = interpolator.getPreviousTime() + ignoreZone;
         interpolator.setInterpolatedTime(this.t0);
         this.g0 = this.handler.g(this.t0, interpolator.getInterpolatedState());
         if (this.g0 == 0.0) {
            double tStart = interpolator.getPreviousTime();
            interpolator.setInterpolatedTime(tStart);
            this.g0Positive = this.handler.g(tStart, interpolator.getInterpolatedState()) <= 0.0;
         } else {
            this.g0Positive = this.g0 >= 0.0;
         }
      } catch (DerivativeException var6) {
         throw new EventException(var6);
      }
   }

   public boolean evaluateStep(final StepInterpolator interpolator) throws DerivativeException, EventException, ConvergenceException {
      try {
         this.forward = interpolator.isForward();
         double t1 = interpolator.getCurrentTime();
         if (FastMath.abs(t1 - this.t0) < this.convergence) {
            return false;
         } else {
            double start = this.forward ? this.t0 + this.convergence : this.t0 - this.convergence;
            double dt = t1 - start;
            int n = FastMath.max(1, (int)FastMath.ceil(FastMath.abs(dt) / this.maxCheckInterval));
            double h = dt / (double)n;
            double ta = this.t0;
            double ga = this.g0;

            for(int i = 0; i < n; ++i) {
               double tb = start + (double)(i + 1) * h;
               interpolator.setInterpolatedTime(tb);
               double gb = this.handler.g(tb, interpolator.getInterpolatedState());
               if (this.g0Positive ^ gb >= 0.0) {
                  this.increasing = gb >= ga;
                  UnivariateRealFunction f = new UnivariateRealFunction() {
                     @Override
                     public double value(double t) {
                        try {
                           interpolator.setInterpolatedTime(t);
                           return EventState.this.handler.g(t, interpolator.getInterpolatedState());
                        } catch (DerivativeException var4) {
                           throw new EventState.EmbeddedDerivativeException(var4);
                        } catch (EventException var5) {
                           throw new EventState.EmbeddedEventException(var5);
                        }
                     }
                  };
                  BrentSolver solver = new BrentSolver(this.convergence);
                  if (ga * gb >= 0.0) {
                     double epsilon = (this.forward ? 0.25 : -0.25) * this.convergence;

                     for(int k = 0; k < 4 && ga * gb > 0.0; ++k) {
                        ta += epsilon;

                        try {
                           ga = f.value(ta);
                        } catch (FunctionEvaluationException var27) {
                           throw new DerivativeException(var27);
                        }
                     }

                     if (ga * gb > 0.0) {
                        throw new MathInternalError();
                     }
                  }

                  double root;
                  try {
                     root = ta <= tb ? solver.solve(this.maxIterationCount, f, ta, tb) : solver.solve(this.maxIterationCount, f, tb, ta);
                  } catch (FunctionEvaluationException var26) {
                     throw new DerivativeException(var26);
                  }

                  if (!Double.isNaN(this.previousEventTime)
                     && FastMath.abs(root - ta) <= this.convergence
                     && FastMath.abs(root - this.previousEventTime) <= this.convergence) {
                     ta = tb;
                     ga = gb;
                  } else {
                     if (Double.isNaN(this.previousEventTime) || FastMath.abs(this.previousEventTime - root) > this.convergence) {
                        this.pendingEventTime = root;
                        this.pendingEvent = true;
                        return true;
                     }

                     ta = tb;
                     ga = gb;
                  }
               } else {
                  ta = tb;
                  ga = gb;
               }
            }

            this.pendingEvent = false;
            this.pendingEventTime = Double.NaN;
            return false;
         }
      } catch (EventState.EmbeddedDerivativeException var28) {
         throw var28.getDerivativeException();
      } catch (EventState.EmbeddedEventException var29) {
         throw var29.getEventException();
      }
   }

   public double getEventTime() {
      return this.pendingEvent ? this.pendingEventTime : Double.POSITIVE_INFINITY;
   }

   public void stepAccepted(double t, double[] y) throws EventException {
      this.t0 = t;
      this.g0 = this.handler.g(t, y);
      if (this.pendingEvent && FastMath.abs(this.pendingEventTime - t) <= this.convergence) {
         this.previousEventTime = t;
         this.g0Positive = this.increasing;
         this.nextAction = this.handler.eventOccurred(t, y, !(this.increasing ^ this.forward));
      } else {
         this.g0Positive = this.g0 >= 0.0;
         this.nextAction = 3;
      }
   }

   public boolean stop() {
      return this.nextAction == 0;
   }

   public boolean reset(double t, double[] y) throws EventException {
      if (this.pendingEvent && FastMath.abs(this.pendingEventTime - t) <= this.convergence) {
         if (this.nextAction == 1) {
            this.handler.resetState(t, y);
         }

         this.pendingEvent = false;
         this.pendingEventTime = Double.NaN;
         return this.nextAction == 1 || this.nextAction == 2;
      } else {
         return false;
      }
   }

   private static class EmbeddedDerivativeException extends RuntimeException {
      private static final long serialVersionUID = 3574188382434584610L;
      private final DerivativeException derivativeException;

      public EmbeddedDerivativeException(DerivativeException derivativeException) {
         this.derivativeException = derivativeException;
      }

      public DerivativeException getDerivativeException() {
         return this.derivativeException;
      }
   }

   private static class EmbeddedEventException extends RuntimeException {
      private static final long serialVersionUID = -1337749250090455474L;
      private final EventException eventException;

      public EmbeddedEventException(EventException eventException) {
         this.eventException = eventException;
      }

      public EventException getEventException() {
         return this.eventException;
      }
   }
}
