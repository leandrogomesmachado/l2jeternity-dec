package org.apache.commons.lang3.concurrent;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AtomicInitializer<T> implements ConcurrentInitializer<T> {
   private final AtomicReference<T> reference = new AtomicReference<>();

   @Override
   public T get() throws ConcurrentException {
      T result = this.reference.get();
      if (result == null) {
         result = this.initialize();
         if (!this.reference.compareAndSet((T)null, result)) {
            result = this.reference.get();
         }
      }

      return result;
   }

   protected abstract T initialize() throws ConcurrentException;
}
