package org.apache.commons.math.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.math.MathException;

public class TransformerMap implements NumberTransformer, Serializable {
   private static final long serialVersionUID = 4605318041528645258L;
   private NumberTransformer defaultTransformer = null;
   private Map<Class<?>, NumberTransformer> map = null;

   public TransformerMap() {
      this.map = new HashMap<>();
      this.defaultTransformer = new DefaultTransformer();
   }

   public boolean containsClass(Class<?> key) {
      return this.map.containsKey(key);
   }

   public boolean containsTransformer(NumberTransformer value) {
      return this.map.containsValue(value);
   }

   public NumberTransformer getTransformer(Class<?> key) {
      return this.map.get(key);
   }

   public NumberTransformer putTransformer(Class<?> key, NumberTransformer transformer) {
      return this.map.put(key, transformer);
   }

   public NumberTransformer removeTransformer(Class<?> key) {
      return this.map.remove(key);
   }

   public void clear() {
      this.map.clear();
   }

   public Set<Class<?>> classes() {
      return this.map.keySet();
   }

   public Collection<NumberTransformer> transformers() {
      return this.map.values();
   }

   @Override
   public double transform(Object o) throws MathException {
      double value = Double.NaN;
      if (!(o instanceof Number) && !(o instanceof String)) {
         NumberTransformer trans = this.getTransformer(o.getClass());
         if (trans != null) {
            value = trans.transform(o);
         }
      } else {
         value = this.defaultTransformer.transform(o);
      }

      return value;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other instanceof TransformerMap) {
         TransformerMap rhs = (TransformerMap)other;
         if (!this.defaultTransformer.equals(rhs.defaultTransformer)) {
            return false;
         } else if (this.map.size() != rhs.map.size()) {
            return false;
         } else {
            for(Entry<Class<?>, NumberTransformer> entry : this.map.entrySet()) {
               if (!entry.getValue().equals(rhs.map.get(entry.getKey()))) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int hash = this.defaultTransformer.hashCode();

      for(NumberTransformer t : this.map.values()) {
         hash = hash * 31 + t.hashCode();
      }

      return hash;
   }
}
