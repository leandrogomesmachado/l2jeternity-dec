package gnu.trove.impl.sync;

import gnu.trove.TShortCollection;
import gnu.trove.function.TShortFunction;
import gnu.trove.iterator.TDoubleShortIterator;
import gnu.trove.map.TDoubleShortMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TDoubleShortProcedure;
import gnu.trove.procedure.TShortProcedure;
import gnu.trove.set.TDoubleSet;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public class TSynchronizedDoubleShortMap implements TDoubleShortMap, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TDoubleShortMap m;
   final Object mutex;
   private transient TDoubleSet keySet = null;
   private transient TShortCollection values = null;

   public TSynchronizedDoubleShortMap(TDoubleShortMap m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedDoubleShortMap(TDoubleShortMap m, Object mutex) {
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
   public boolean containsKey(double key) {
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
   public short get(double key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public short put(double key, short value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public short remove(double key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends Double, ? extends Short> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TDoubleShortMap map) {
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
   public TDoubleSet keySet() {
      synchronized(this.mutex) {
         if (this.keySet == null) {
            this.keySet = new TSynchronizedDoubleSet(this.m.keySet(), this.mutex);
         }

         return this.keySet;
      }
   }

   @Override
   public double[] keys() {
      synchronized(this.mutex) {
         return this.m.keys();
      }
   }

   @Override
   public double[] keys(double[] array) {
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
   public TDoubleShortIterator iterator() {
      return this.m.iterator();
   }

   @Override
   public double getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public short getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public short putIfAbsent(double key, short value) {
      synchronized(this.mutex) {
         return this.m.putIfAbsent(key, value);
      }
   }

   @Override
   public boolean forEachKey(TDoubleProcedure procedure) {
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
   public boolean forEachEntry(TDoubleShortProcedure procedure) {
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
   public boolean retainEntries(TDoubleShortProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.retainEntries(procedure);
      }
   }

   @Override
   public boolean increment(double key) {
      synchronized(this.mutex) {
         return this.m.increment(key);
      }
   }

   @Override
   public boolean adjustValue(double key, short amount) {
      synchronized(this.mutex) {
         return this.m.adjustValue(key, amount);
      }
   }

   @Override
   public short adjustOrPutValue(double key, short adjust_amount, short put_amount) {
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
