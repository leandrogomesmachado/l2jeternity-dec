package gnu.trove.impl.sync;

import gnu.trove.TShortCollection;
import gnu.trove.function.TShortFunction;
import gnu.trove.iterator.TShortShortIterator;
import gnu.trove.map.TShortShortMap;
import gnu.trove.procedure.TShortProcedure;
import gnu.trove.procedure.TShortShortProcedure;
import gnu.trove.set.TShortSet;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public class TSynchronizedShortShortMap implements TShortShortMap, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TShortShortMap m;
   final Object mutex;
   private transient TShortSet keySet = null;
   private transient TShortCollection values = null;

   public TSynchronizedShortShortMap(TShortShortMap m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedShortShortMap(TShortShortMap m, Object mutex) {
      this.m = m;
      this.mutex = mutex;
   }

   @Override
   public int size() {
      synchronized(this.mutex) {
         return this.m.size();
      }
   }

   @Override
   public boolean isEmpty() {
      synchronized(this.mutex) {
         return this.m.isEmpty();
      }
   }

   @Override
   public boolean containsKey(short key) {
      synchronized(this.mutex) {
         return this.m.containsKey(key);
      }
   }

   @Override
   public boolean containsValue(short value) {
      synchronized(this.mutex) {
         return this.m.containsValue(value);
      }
   }

   @Override
   public short get(short key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public short put(short key, short value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public short remove(short key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends Short, ? extends Short> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TShortShortMap map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void clear() {
      synchronized(this.mutex) {
         this.m.clear();
      }
   }

   @Override
   public TShortSet keySet() {
      synchronized(this.mutex) {
         if (this.keySet == null) {
            this.keySet = new TSynchronizedShortSet(this.m.keySet(), this.mutex);
         }

         return this.keySet;
      }
   }

   @Override
   public short[] keys() {
      synchronized(this.mutex) {
         return this.m.keys();
      }
   }

   @Override
   public short[] keys(short[] array) {
      synchronized(this.mutex) {
         return this.m.keys(array);
      }
   }

   @Override
   public TShortCollection valueCollection() {
      synchronized(this.mutex) {
         if (this.values == null) {
            this.values = new TSynchronizedShortCollection(this.m.valueCollection(), this.mutex);
         }

         return this.values;
      }
   }

   @Override
   public short[] values() {
      synchronized(this.mutex) {
         return this.m.values();
      }
   }

   @Override
   public short[] values(short[] array) {
      synchronized(this.mutex) {
         return this.m.values(array);
      }
   }

   @Override
   public TShortShortIterator iterator() {
      return this.m.iterator();
   }

   @Override
   public short getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public short getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public short putIfAbsent(short key, short value) {
      synchronized(this.mutex) {
         return this.m.putIfAbsent(key, value);
      }
   }

   @Override
   public boolean forEachKey(TShortProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachKey(procedure);
      }
   }

   @Override
   public boolean forEachValue(TShortProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachValue(procedure);
      }
   }

   @Override
   public boolean forEachEntry(TShortShortProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachEntry(procedure);
      }
   }

   @Override
   public void transformValues(TShortFunction function) {
      synchronized(this.mutex) {
         this.m.transformValues(function);
      }
   }

   @Override
   public boolean retainEntries(TShortShortProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.retainEntries(procedure);
      }
   }

   @Override
   public boolean increment(short key) {
      synchronized(this.mutex) {
         return this.m.increment(key);
      }
   }

   @Override
   public boolean adjustValue(short key, short amount) {
      synchronized(this.mutex) {
         return this.m.adjustValue(key, amount);
      }
   }

   @Override
   public short adjustOrPutValue(short key, short adjust_amount, short put_amount) {
      synchronized(this.mutex) {
         return this.m.adjustOrPutValue(key, adjust_amount, put_amount);
      }
   }

   @Override
   public boolean equals(Object o) {
      synchronized(this.mutex) {
         return this.m.equals(o);
      }
   }

   @Override
   public int hashCode() {
      synchronized(this.mutex) {
         return this.m.hashCode();
      }
   }

   @Override
   public String toString() {
      synchronized(this.mutex) {
         return this.m.toString();
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      synchronized(this.mutex) {
         s.defaultWriteObject();
      }
   }
}
