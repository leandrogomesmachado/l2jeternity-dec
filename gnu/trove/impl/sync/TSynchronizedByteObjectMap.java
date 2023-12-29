package gnu.trove.impl.sync;

import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.procedure.TByteObjectProcedure;
import gnu.trove.procedure.TByteProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TByteSet;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class TSynchronizedByteObjectMap<V> implements TByteObjectMap<V>, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TByteObjectMap<V> m;
   final Object mutex;
   private transient TByteSet keySet = null;
   private transient Collection<V> values = null;

   public TSynchronizedByteObjectMap(TByteObjectMap<V> m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedByteObjectMap(TByteObjectMap<V> m, Object mutex) {
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
   public boolean containsKey(byte key) {
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
   public V get(byte key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public V put(byte key, V value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public V remove(byte key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends Byte, ? extends V> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TByteObjectMap<? extends V> map) {
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
   public TByteSet keySet() {
      synchronized(this.mutex) {
         if (this.keySet == null) {
            this.keySet = new TSynchronizedByteSet(this.m.keySet(), this.mutex);
         }

         return this.keySet;
      }
   }

   @Override
   public byte[] keys() {
      synchronized(this.mutex) {
         return this.m.keys();
      }
   }

   @Override
   public byte[] keys(byte[] array) {
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
   public TByteObjectIterator<V> iterator() {
      return this.m.iterator();
   }

   @Override
   public byte getNoEntryKey() {
      return this.m.getNoEntryKey();
   }

   @Override
   public V putIfAbsent(byte key, V value) {
      synchronized(this.mutex) {
         return this.m.putIfAbsent(key, value);
      }
   }

   @Override
   public boolean forEachKey(TByteProcedure procedure) {
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
   public boolean forEachEntry(TByteObjectProcedure<? super V> procedure) {
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
   public boolean retainEntries(TByteObjectProcedure<? super V> procedure) {
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
