package org.apache.commons.math.ode;

import java.util.Collection;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;

public interface ODEIntegrator {
   String getName();

   void addStepHandler(StepHandler var1);

   Collection<StepHandler> getStepHandlers();

   void clearStepHandlers();

   void addEventHandler(EventHandler var1, double var2, double var4, int var6);

   Collection<EventHandler> getEventHandlers();

   void clearEventHandlers();

   double getCurrentStepStart();

   double getCurrentSignedStepsize();

   void setMaxEvaluations(int var1);

   int getMaxEvaluations();

   int getEvaluations();
}
