package gnu.trove.impl.sync;

import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class TSynchronizedIntObjectMap<V> implements TIntObjectMap<V>, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TIntObjectMap<V> m;
   final Object mutex;
   private transient TIntSet keySet = null;
   private transient Collection<V> values = null;

   public TSynchronizedIntObjectMap(TIntObjectMap<V> m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedIntObjectMap(TIntObjectMap<V> m, Object mutex) {
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
   public boolean containsKey(int key) {
      synchronized(this.mutex) {
         return this.m.containsKey(key);
      }
   }

   @Override
   public boolean containsValue(Object value) {
      synchronized(this.mutex) {
         return this.m.containsValue(value);
      }
   }

   @Override
   public V get(int key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public V put(int key, V value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public V remove(int key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends Integer, ? extends V> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TIntObjectMap<? extends V> map) {
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
   public TIntSet keySet() {
      synchronized(this.mutex) {
         if (this.keySet == null) {
            this.keySet = new TSynchronizedIntSet(this.m.keySet(), this.mutex);
         }

         return this.keySet;
      }
   }

   @Override
   public int[] keys() {
      synchronized(this.mutex) {
         return this.m.keys();
      }
   }

   @Override
   public int[] keys(int[] array) {
      synchronized(this.mutex) {
         return this.m.keys(array);
      }
   }

   @Override
   public Collection<V> valueCollection() {
      synchronized(this.mutex) {
         if (this.values == null) {
            this.values = new SynchronizedCollection<>(this.m.valueCollection(), this.mutex);
         }

         return this.values;
      }
   }

   @Override
   public Object[] values() {
      synchronized(this.mutex) {
         return this.m.values();
      }
   }

   @Override
   public V[] values(V[] array) {
      synchronized(this.mutex) {
         return this.m.values(array);
      }
   }

   @Override
   public TIntObjectIterator<V> iterator() {
      return this.m.iterator();
   }

   @Override
   public int getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public V putIfAbsent(int key, V value) {
      synchronized(this.mutex) {
         return this.m.putIfAbsent(key, value);
      }
   }

   @Override
   public boolean forEachKey(TIntProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachKey(procedure);
      }
   }

   @Override
   public boolean forEachValue(TObjectProcedure<? super V> procedure) {
      synchronized(this.mutex) {
         return this.m.forEachValue(procedure);
      }
   }

   @Override
   public boolean forEachEntry(TIntObjectProcedure<? super V> procedure) {
      synchronized(this.mutex) {
         return this.m.forEachEntry(procedure);
      }
   }

   @Override
   public void transformValues(TObjectFunction<V, V> function) {
      synchronized(this.mutex) {
         this.m.transformValues(function);
      }
   }

   @Override
   public boolean retainEntries(TIntObjectProcedure<? super V> procedure) {
      synchronized(this.mutex) {
         return this.m.retainEntries(procedure);
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
