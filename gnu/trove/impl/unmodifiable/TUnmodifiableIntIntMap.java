package gnu.trove.impl.unmodifiable;

import gnu.trove.TCollections;
import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import java.io.Serializable;
import java.util.Map;

public class TUnmodifiableIntIntMap implements TIntIntMap, Serializable {
   private static final long serialVersionUID = -1034234728574286014L;
   private final TIntIntMap m;
   private transient TIntSet keySet = null;
   private transient TIntCollection values = null;

   public TUnmodifiableIntIntMap(TIntIntMap m) {
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
   public boolean containsKey(int key) {
      return this.m.containsKey(key);
   }

   @Override
   public boolean containsValue(int val) {
      return this.m.containsValue(val);
   }

   @Override
   public int get(int key) {
      return this.m.get(key);
   }

   @Override
   public int put(int key, int value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int remove(int key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(TIntIntMap m) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(Map<? extends Integer, ? extends Integer> map) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public TIntSet keySet() {
      if (this.keySet == null) {
         this.keySet = TCollections.unmodifiableSet(this.m.keySet());
      }

      return this.keySet;
   }

   @Override
   public int[] keys() {
      return this.m.keys();
   }

   @Override
   public int[] keys(int[] array) {
      return this.m.keys(array);
   }

   @Override
   public TIntCollection valueCollection() {
      if (this.values == null) {
         this.values = TCollections.unmodifiableCollection(this.m.valueCollection());
      }

      return this.values;
   }

   @Override
   public int[] values() {
      return this.m.values();
   }

   @Override
   public int[] values(int[] array) {
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
   public int getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public int getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public boolean forEachKey(TIntProcedure procedure) {
      return this.m.forEachKey(procedure);
   }

   @Override
   public boolean forEachValue(TIntProcedure procedure) {
      return this.m.forEachValue(procedure);
   }

   @Override
   public boolean forEachEntry(TIntIntProcedure procedure) {
      return this.m.forEachEntry(procedure);
   }

   @Override
   public TIntIntIterator iterator() {
      return new TIntIntIterator() {
         TIntIntIterator iter = TUnmodifiableIntIntMap.this.m.iterator();

         @Override
         public int key() {
            return this.iter.key();
         }

         @Override
         public int value() {
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
         public int setValue(int val) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   @Override
   public int putIfAbsent(int key, int value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TIntFunction function) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainEntries(TIntIntProcedure procedure) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean increment(int key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean adjustValue(int key, int amount) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int adjustOrPutValue(int key, int adjust_amount, int put_amount) {
      throw new UnsupportedOperationException();
   }
}
