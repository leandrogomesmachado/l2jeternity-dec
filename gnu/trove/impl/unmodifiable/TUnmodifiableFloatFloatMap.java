package gnu.trove.impl.unmodifiable;

import gnu.trove.TCollections;
import gnu.trove.TFloatCollection;
import gnu.trove.function.TFloatFunction;
import gnu.trove.iterator.TFloatFloatIterator;
import gnu.trove.map.TFloatFloatMap;
import gnu.trove.procedure.TFloatFloatProcedure;
import gnu.trove.procedure.TFloatProcedure;
import gnu.trove.set.TFloatSet;
import java.io.Serializable;
import java.util.Map;

public class TUnmodifiableFloatFloatMap implements TFloatFloatMap, Serializable {
   private static final long serialVersionUID = -1034234728574286014L;
   private final TFloatFloatMap m;
   private transient TFloatSet keySet = null;
   private transient TFloatCollection values = null;

   public TUnmodifiableFloatFloatMap(TFloatFloatMap m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
      }
   }

   @Override
   public int size() {
      return this.m.size();
   }

   @Override
   public boolean isEmpty() {
      return this.m.isEmpty();
   }

   @Override
   public boolean containsKey(float key) {
      return this.m.containsKey(key);
   }

   @Override
   public boolean containsValue(float val) {
      return this.m.containsValue(val);
   }

   @Override
   public float get(float key) {
      return this.m.get(key);
   }

   @Override
   public float put(float key, float value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public float remove(float key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(TFloatFloatMap m) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(Map<? extends Float, ? extends Float> map) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public TFloatSet keySet() {
      if (this.keySet == null) {
         this.keySet = TCollections.unmodifiableSet(this.m.keySet());
      }

      return this.keySet;
   }

   @Override
   public float[] keys() {
      return this.m.keys();
   }

   @Override
   public float[] keys(float[] array) {
      return this.m.keys(array);
   }

   @Override
   public TFloatCollection valueCollection() {
      if (this.values == null) {
         this.values = TCollections.unmodifiableCollection(this.m.valueCollection());
      }

      return this.values;
   }

   @Override
   public float[] values() {
      return this.m.values();
   }

   @Override
   public float[] values(float[] array) {
      return this.m.values(array);
   }

   @Override
   public boolean equals(Object o) {
      return o == this || this.m.equals(o);
   }

   @Override
   public int hashCode() {
      return this.m.hashCode();
   }

   @Override
   public String toString() {
      return this.m.toString();
   }

   @Override
   public float getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public float getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public boolean forEachKey(TFloatProcedure procedure) {
      return this.m.forEachKey(procedure);
   }

   @Override
   public boolean forEachValue(TFloatProcedure procedure) {
      return this.m.forEachValue(procedure);
   }

   @Override
   public boolean forEachEntry(TFloatFloatProcedure procedure) {
      return this.m.forEachEntry(procedure);
   }

   @Override
   public TFloatFloatIterator iterator() {
      return new TFloatFloatIterator() {
         TFloatFloatIterator iter = TUnmodifiableFloatFloatMap.this.m.iterator();

         @Override
         public float key() {
            return this.iter.key();
         }

         @Override
         public float value() {
            return this.iter.value();
         }

         @Override
         public void advance() {
            this.iter.advance();
         }

         @Override
         public boolean hasNext() {
            return this.iter.hasNext();
         }

         @Override
         public float setValue(float val) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   @Override
   public float putIfAbsent(float key, float value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TFloatFunction function) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainEntries(TFloatFloatProcedure procedure) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean increment(float key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean adjustValue(float key, float amount) {
      throw new UnsupportedOperationException();
   }

   @Override
   public float adjustOrPutValue(float key, float adjust_amount, float put_amount) {
      throw new UnsupportedOperationException();
   }
}
