package org.apache.commons.lang3.concurrent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractCircuitBreaker<T> implements CircuitBreaker<T> {
   public static final String PROPERTY_NAME = "open";
   protected final AtomicReference<AbstractCircuitBreaker.State> state = new AtomicReference<>(AbstractCircuitBreaker.State.CLOSED);
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

   @Override
   public boolean isOpen() {
      return isOpen(this.state.get());
   }

   @Override
   public boolean isClosed() {
      return !this.isOpen();
   }

   @Override
   public abstract boolean checkState();

   @Override
   public abstract boolean incrementAndCheckState(T var1);

   @Override
   public void close() {
      this.changeState(AbstractCircuitBreaker.State.CLOSED);
   }

   @Override
   public void open() {
      this.changeState(AbstractCircuitBreaker.State.OPEN);
   }

   protected static boolean isOpen(AbstractCircuitBreaker.State state) {
      return state == AbstractCircuitBreaker.State.OPEN;
   }

   protected void changeState(AbstractCircuitBreaker.State newState) {
      if (this.state.compareAndSet(newState.oppositeState(), newState)) {
         this.changeSupport.firePropertyChange("open", !isOpen(newState), isOpen(newState));
      }
   }

   public void addChangeListener(PropertyChangeListener listener) {
      this.changeSupport.addPropertyChangeListener(listener);
   }

   public void removeChangeListener(PropertyChangeListener listener) {
      this.changeSupport.removePropertyChangeListener(listener);
   }

   protected static enum State {
      CLOSED {
         @Override
         public AbstractCircuitBreaker.State oppositeState() {
            return OPEN;
         }
      },
      OPEN {
         @Override
         public AbstractCircuitBreaker.State oppositeState() {
            return CLOSED;
         }
      };

      private State() {
      }

      public abstract AbstractCircuitBreaker.State oppositeState();
   }
}
