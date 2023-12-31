package org.apache.commons.lang3.concurrent;

import java.util.Objects;

public class ConstantInitializer<T> implements ConcurrentInitializer<T> {
   private static final String FMT_TO_STRING = "ConstantInitializer@%d [ object = %s ]";
   private final T object;

   public ConstantInitializer(T obj) {
      this.object = obj;
   }

   public final T getObject() {
      return this.object;
   }

   @Override
   public T get() throws ConcurrentException {
      return this.getObject();
   }

   @Override
   public int hashCode() {
      return this.getObject() != null ? this.getObject().hashCode() : 0;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ConstantInitializer)) {
         return false;
      } else {
         ConstantInitializer<?> c = (ConstantInitializer)obj;
         return Objects.equals(this.getObject(), c.getObject());
      }
   }

   @Override
   public String toString() {
      return String.format("ConstantInitializer@%d [ object = %s ]", System.identityHashCode(this), String.valueOf(this.getObject()));
   }
}
