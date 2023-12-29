package org.apache.commons.lang3.mutable;

import java.io.Serializable;

public class MutableObject<T> implements Mutable<T>, Serializable {
   private static final long serialVersionUID = 86241875189L;
   private T value;

   public MutableObject() {
   }

   public MutableObject(T value) {
      this.value = value;
   }

   @Override
   public T getValue() {
      return this.value;
   }

   @Override
   public void setValue(T value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this == obj) {
         return true;
      } else if (this.getClass() == obj.getClass()) {
         MutableObject<?> that = (MutableObject)obj;
         return this.value.equals(that.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.value == null ? 0 : this.value.hashCode();
   }

   @Override
   public String toString() {
      return this.value == null ? "null" : this.value.toString();
   }
}
