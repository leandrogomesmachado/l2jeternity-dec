package org.apache.commons.math.ode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.ode.events.CombinedEventsManager;
import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.events.EventState;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public abstract class AbstractIntegrator implements FirstOrderIntegrator {
   protected Collection<StepHandler> stepHandlers;
   protected double stepStart;
   protected double stepSize;
   protected boolean isLastStep;
   protected boolean resetOccurred;
   private Collection<EventState> eventsStates;
   private boolean statesInitialized;
   private final String name;
   private int maxEvaluations;
   private int evaluations;
   private transient FirstOrderDifferentialEquations equations;

   public AbstractIntegrator(String name) {
      this.name = name;
      this.stepHandlers = new ArrayList<>();
      this.stepStart = Double.NaN;
      this.stepSize = Double.NaN;
      this.eventsStates = new ArrayList<>();
      this.statesInitialized = false;
      this.setMaxEvaluations(-1);
      this.resetEvaluations();
   }

   protected AbstractIntegrator() {
      this(null);
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public void addStepHandler(StepHandler handler) {
      this.stepHandlers.add(handler);
   }

   @Override
   public Collection<StepHandler> getStepHandlers() {
      return Collections.unmodifiableCollection(this.stepHandlers);
   }

   @Override
   public void clearStepHandlers() {
      this.stepHandlers.clear();
   }

   @Override
   public void addEventHandler(EventHandler handler, double maxCheckInterval, double convergence, int maxIterationCount) {
      this.eventsStates.add(new EventState(handler, maxCheckInterval, convergence, maxIterationCount));
   }

   @Override
   public Collection<EventHandler> getEventHandlers() {
      List<EventHandler> list = new ArrayList<>();

      for(EventState state : this.eventsStates) {
         list.add(state.getEventHandler());
      }

      return Collections.unmodifiableCollection(list);
   }

   @Override
   public void clearEventHandlers() {
      this.eventsStates.clear();
   }

   protected boolean requiresDenseOutput() {
      if (!this.eventsStates.isEmpty()) {
         return true;
      } else {
         for(StepHandler handler : this.stepHandlers) {
            if (handler.requiresDenseOutput()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public double getCurrentStepStart() {
      return this.stepStart;
   }

   @Override
   public double getCurrentSignedStepsize() {
      return this.stepSize;
   }

   @Override
   public void setMaxEvaluations(int maxEvaluations) {
      this.maxEvaluations = maxEvaluations < 0 ? Integer.MAX_VALUE : maxEvaluations;
   }

   @Override
   public int getMaxEvaluations() {
      return this.maxEvaluations;
   }

   @Override
   public int getEvaluations() {
      return this.evaluations;
   }

   protected void resetEvaluations() {
      this.evaluations = 0;
   }

   protected void setEquations(FirstOrderDifferentialEquations equations) {
      this.equations = equations;
   }

   public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
      if (++this.evaluations > this.maxEvaluations) {
         throw new DerivativeException(new MaxEvaluationsExceededException(this.maxEvaluations));
      } else {
         this.equations.computeDerivatives(t, y, yDot);
      }
   }

   protected void setStateInitialized(boolean stateInitialized) {
      this.statesInitialized = stateInitialized;
   }

   protected double acceptStep(AbstractStepInterpolator interpolator, double[] y, double[] yDot, double tEnd) throws DerivativeException, IntegratorException {
      try {
         double previousT = interpolator.getGlobalPreviousTime();
         double currentT = interpolator.getGlobalCurrentTime();
         this.resetOccurred = false;
         if (!this.statesInitialized) {
            for(EventState state : this.eventsStates) {
               state.reinitializeBegin(interpolator);
            }

            this.statesInitialized = true;
         }

         final int orderingSign = interpolator.isForward() ? 1 : -1;
         SortedSet<EventState> occuringEvents = new TreeSet<>(new Comparator<EventState>() {
            public int compare(EventState es0, EventState es1) {
               return orderingSign * Double.compare(es0.getEventTime(), es1.getEventTime());
            }
         });

         for(EventState state : this.eventsStates) {
            if (state.evaluateStep(interpolator)) {
               occuringEvents.add(state);
            }
         }

         while(!occuringEvents.isEmpty()) {
            Iterator<EventState> iterator = occuringEvents.iterator();
            EventState currentEvent = iterator.next();
            iterator.remove();
            double eventT = currentEvent.getEventTime();
            interpolator.setSoftPreviousTime(previousT);
            interpolator.setSoftCurrentTime(eventT);
            interpolator.setInterpolatedTime(eventT);
            double[] eventY = interpolator.getInterpolatedState();
            currentEvent.stepAccepted(eventT, eventY);
            this.isLastStep = currentEvent.stop();

            for(StepHandler handler : this.stepHandlers) {
               handler.handleStep(interpolator, this.isLastStep);
            }

            if (this.isLastStep) {
               System.arraycopy(eventY, 0, y, 0, y.length);
               return eventT;
            }

            if (currentEvent.reset(eventT, eventY)) {
               System.arraycopy(eventY, 0, y, 0, y.length);
               this.computeDerivatives(eventT, y, yDot);
               this.resetOccurred = true;
               return eventT;
            }

            previousT = eventT;
            interpolator.setSoftPreviousTime(eventT);
            interpolator.setSoftCurrentTime(currentT);
            if (currentEvent.evaluateStep(interpolator)) {
               occuringEvents.add(currentEvent);
            }
         }

         interpolator.setInterpolatedTime(currentT);
         double[] currentY = interpolator.getInterpolatedState();

         for(EventState state : this.eventsStates) {
            state.stepAccepted(currentT, currentY);
            this.isLastStep = this.isLastStep || state.stop();
         }

         this.isLastStep = this.isLastStep || MathUtils.equals(currentT, tEnd, 1);

         for(StepHandler handler : this.stepHandlers) {
            handler.handleStep(interpolator, this.isLastStep);
         }

         return currentT;
      } catch (EventException var19) {
         Throwable cause = var19.getCause();
         if (cause != null && cause instanceof DerivativeException) {
            throw (DerivativeException)cause;
         } else {
            throw new IntegratorException(var19);
         }
      } catch (ConvergenceException var20) {
         throw new IntegratorException(var20);
      }
   }

   protected void sanityChecks(FirstOrderDifferentialEquations ode, double t0, double[] y0, double t, double[] y) throws IntegratorException {
      if (ode.getDimension() != y0.length) {
         throw new IntegratorException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, ode.getDimension(), y0.length);
      } else if (ode.getDimension() != y.length) {
         throw new IntegratorException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, ode.getDimension(), y.length);
      } else if (FastMath.abs(t - t0) <= 1.0E-12 * FastMath.max(FastMath.abs(t0), FastMath.abs(t))) {
         throw new IntegratorException(LocalizedFormats.TOO_SMALL_INTEGRATION_INTERVAL, FastMath.abs(t - t0));
      }
   }

   @Deprecated
   protected CombinedEventsManager addEndTimeChecker(double startTime, double endTime, CombinedEventsManager manager) {
      CombinedEventsManager newManager = new CombinedEventsManager();

      for(EventState state : manager.getEventsStates()) {
         newManager.addEventHandler(state.getEventHandler(), state.getMaxCheckInterval(), state.getConvergence(), state.getMaxIterationCount());
      }

      newManager.addEventHandler(
         new AbstractIntegrator.EndTimeChecker(endTime),
         Double.POSITIVE_INFINITY,
         FastMath.ulp(FastMath.max(FastMath.abs(startTime), FastMath.abs(endTime))),
         100
      );
      return newManager;
   }

   @Deprecated
   private static class EndTimeChecker implements EventHandler {
      private final double endTime;

      public EndTimeChecker(double endTime) {
         this.endTime = endTime;
      }

      @Override
      public int eventOccurred(double t, double[] y, boolean increasing) {
         return 0;
      }

      @Override
      public double g(double t, double[] y) {
         return t - this.endTime;
      }

      @Override
      public void resetState(double t, double[] y) {
      }
   }
}
