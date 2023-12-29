package org.apache.commons.io.comparator;

import java.io.Serializable;
import java.util.Comparator;

class ReverseComparator implements Comparator, Serializable {
   private final Comparator delegate;

   public ReverseComparator(Comparator delegate) {
      if (delegate == null) {
         throw new IllegalArgumentException("Delegate comparator is missing");
      } else {
         this.delegate = delegate;
      }
   }

   public int compare(Object obj1, Object obj2) {
      return this.delegate.compare(obj2, obj1);
   }
}
