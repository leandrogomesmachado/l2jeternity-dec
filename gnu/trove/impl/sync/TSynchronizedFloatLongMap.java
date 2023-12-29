package gnu.trove.impl.sync;

import gnu.trove.TLongCollection;
import gnu.trove.function.TLongFunction;
import gnu.trove.iterator.TFloatLongIterator;
import gnu.trove.map.TFloatLongMap;
import gnu.trove.procedure.TFloatLongProcedure;
import gnu.trove.procedure.TFloatProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.TFloatSet;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public class TSynchronizedFloatLongMap implements TFloatLongMap, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TFloatLongMap m;
   final Object mutex;
   private transient TFloatSet keySet = null;
   private transient TLongCollection values = null;

   public TSynchronizedFloatLongMap(TFloatLongMap m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedFloatLongMap(TFloatLongMap m, Object mutex) {
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
   public boolean containsKey(float key) {
      synchronized(this.mutex) {
         return this.m.containsKey(key);
      }
   }

   @Override
   public boolean containsValue(long value) {
      synchronized(this.mutex) {
         return this.m.containsValue(value);
      }
   }

   @Override
   public long get(float key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public long put(float key, long value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public long remove(float key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends Float, ? extends Long> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TFloatLongMap map) {
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
   public TFloatSet keySet() {
      synchronized(this.mutex) {
         if (this.keySet == null) {
            this.keySet = new TSynchronizedFloatSet(this.m.keySet(), this.mutex);
         }

         return this.keySet;
      }
   }

   @Override
   public float[] keys() {
      synchronized(this.mutex) {
         return this.m.keys();
      }
   }

   @Override
   public float[] keys(float[] array) {
      synchronized(this.mutex) {
         return this.m.keys(array);
      }
   }

   @Override
   public TLongCollection valueCollection() {
      synchronized(this.mutex) {
         if (this.values == null) {
            this.values = new TSynchronizedLongCollection(this.m.valueCollection(), this.mutex);
         }

         return this.values;
      }
   }

   @Override
   public long[] values() {
      synchronized(this.mutex) {
         return this.m.values();
      }
   }

   @Override
   public long[] values(long[] array) {
      synchronized(this.mutex) {
         return this.m.values(array);
      }
   }

   @Override
   public TFloatLongIterator iterator() {
      return this.m.iterator();
   }

   @Override
   public float getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public long getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public long putIfAbsent(float key, long value) {
      synchronized(this.mutex) {
         return this.m.putIfAbsent(key, value);
      }
   }

   @Override
   public boolean forEachKey(TFloatProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachKey(procedure);
      }
   }

   @Override
   public boolean forEachValue(TLongProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachValue(procedure);
      }
   }

   @Override
   public boolean forEachEntry(TFloatLongProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachEntry(procedure);
      }
   }

   @Override
   public void transformValues(TLongFunction function) {
      synchronized(this.mutex) {
         this.m.transformValues(function);
      }
   }

   @Override
   public boolean retainEntries(TFloatLongProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.retainEntries(procedure);
      }
   }

   @Override
   public boolean increment(float key) {
      synchronized(this.mutex) {
         return this.m.increment(key);
      }
   }

   @Override
   public boolean adjustValue(float key, long amount) {
      synchronized(this.mutex) {
         return this.m.adjustValue(key, amount);
      }
   }

   @Override
   public long adjustOrPutValue(float key, long adjust_amount, long put_amount) {
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
