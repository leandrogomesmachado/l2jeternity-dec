package org.apache.commons.math.ode.sampling;

public class DummyStepHandler implements StepHandler {
   private DummyStepHandler() {
   }

   public static DummyStepHandler getInstance() {
      return DummyStepHandler.LazyHolder.INSTANCE;
   }

   @Override
   public boolean requiresDenseOutput() {
      return false;
   }

   @Override
   public void reset() {
   }

   @Override
   public void handleStep(StepInterpolator interpolator, boolean isLast) {
   }

   private Object readResolve() {
      return DummyStepHandler.LazyHolder.INSTANCE;
   }

   private static class LazyHolder {
      private static final DummyStepHandler INSTANCE = new DummyStepHandler();
   }
}
