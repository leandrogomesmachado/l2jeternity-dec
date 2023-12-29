package gnu.trove.impl.unmodifiable;

import gnu.trove.TCharCollection;
import gnu.trove.TCollections;
import gnu.trove.function.TCharFunction;
import gnu.trove.iterator.TLongCharIterator;
import gnu.trove.map.TLongCharMap;
import gnu.trove.procedure.TCharProcedure;
import gnu.trove.procedure.TLongCharProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.TLongSet;
import java.io.Serializable;
import java.util.Map;

public class TUnmodifiableLongCharMap implements TLongCharMap, Serializable {
   private static final long serialVersionUID = -1034234728574286014L;
   private final TLongCharMap m;
   private transient TLongSet keySet = null;
   private transient TCharCollection values = null;

   public TUnmodifiableLongCharMap(TLongCharMap m) {
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
   public boolean containsKey(long key) {
      return this.m.containsKey(key);
   }

   @Override
   public boolean containsValue(char val) {
      return this.m.containsValue(val);
   }

   @Override
   public char get(long key) {
      return this.m.get(key);
   }

   @Override
   public char put(long key, char value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public char remove(long key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(TLongCharMap m) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(Map<? extends Long, ? extends Character> map) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public TLongSet keySet() {
      if (this.keySet == null) {
         this.keySet = TCollections.unmodifiableSet(this.m.keySet());
      }

      return this.keySet;
   }

   @Override
   public long[] keys() {
      return this.m.keys();
   }

   @Override
   public long[] keys(long[] array) {
      return this.m.keys(array);
   }

   @Override
   public TCharCollection valueCollection() {
      if (this.values == null) {
         this.values = TCollections.unmodifiableCollection(this.m.valueCollection());
      }

      return this.values;
   }

   @Override
   public char[] values() {
      return this.m.values();
   }

   @Override
   public char[] values(char[] array) {
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
   public long getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public char getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public boolean forEachKey(TLongProcedure procedure) {
      return this.m.forEachKey(procedure);
   }

   @Override
   public boolean forEachValue(TCharProcedure procedure) {
      return this.m.forEachValue(procedure);
   }

   @Override
   public boolean forEachEntry(TLongCharProcedure procedure) {
      return this.m.forEachEntry(procedure);
   }

   @Override
   public TLongCharIterator iterator() {
      return new TLongCharIterator() {
         TLongCharIterator iter = TUnmodifiableLongCharMap.this.m.iterator();

         @Override
         public long key() {
            return this.iter.key();
         }

         @Override
         public char value() {
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
         public char setValue(char val) {
            throw new UnsupportedOperationException();
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   @Override
   public char putIfAbsent(long key, char value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TCharFunction function) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainEntries(TLongCharProcedure procedure) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean increment(long key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean adjustValue(long key, char amount) {
      throw new UnsupportedOperationException();
   }

   @Override
   public char adjustOrPutValue(long key, char adjust_amount, char put_amount) {
      throw new UnsupportedOperationException();
   }
}
