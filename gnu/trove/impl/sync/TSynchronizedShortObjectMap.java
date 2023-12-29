package gnu.trove.impl.sync;

import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.procedure.TShortObjectProcedure;
import gnu.trove.procedure.TShortProcedure;
import gnu.trove.set.TShortSet;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class TSynchronizedShortObjectMap<V> implements TShortObjectMap<V>, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TShortObjectMap<V> m;
   final Object mutex;
   private transient TShortSet keySet = null;
   private transient Collection<V> values = null;

   public TSynchronizedShortObjectMap(TShortObjectMap<V> m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedShortObjectMap(TShortObjectMap<V> m, Object mutex) {
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
   public boolean containsValue(Object value) {
      synchronized(this.mutex) {
         return this.m.containsValue(value);
      }
   }

   @Override
   public V get(short key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public V put(short key, V value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public V remove(short key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends Short, ? extends V> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TShortObjectMap<? extends V> map) {
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
   public TShortObjectIterator<V> iterator() {
      return this.m.iterator();
   }

   @Override
   public short getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public V putIfAbsent(short key, V value) {
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
   public boolean forEachValue(TObjectProcedure<? super V> procedure) {
      synchronized(this.mutex) {
         return this.m.forEachValue(procedure);
      }
   }

   @Override
   public boolean forEachEntry(TShortObjectProcedure<? super V> procedure) {
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
   public boolean retainEntries(TShortObjectProcedure<? super V> procedure) {
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
