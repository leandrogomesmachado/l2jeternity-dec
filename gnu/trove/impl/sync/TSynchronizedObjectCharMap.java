package gnu.trove.impl.sync;

import gnu.trove.TCharCollection;
import gnu.trove.function.TCharFunction;
import gnu.trove.iterator.TObjectCharIterator;
import gnu.trove.map.TObjectCharMap;
import gnu.trove.procedure.TCharProcedure;
import gnu.trove.procedure.TObjectCharProcedure;
import gnu.trove.procedure.TObjectProcedure;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class TSynchronizedObjectCharMap<K> implements TObjectCharMap<K>, Serializable {
   private static final long serialVersionUID = 1978198479659022715L;
   private final TObjectCharMap<K> m;
   final Object mutex;
   private transient Set<K> keySet = null;
   private transient TCharCollection values = null;

   public TSynchronizedObjectCharMap(TObjectCharMap<K> m) {
      if (m == null) {
         throw new NullPointerException();
      } else {
         this.m = m;
         this.mutex = this;
      }
   }

   public TSynchronizedObjectCharMap(TObjectCharMap<K> m, Object mutex) {
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
   public boolean containsKey(Object key) {
      synchronized(this.mutex) {
         return this.m.containsKey(key);
      }
   }

   @Override
   public boolean containsValue(char value) {
      synchronized(this.mutex) {
         return this.m.containsValue(value);
      }
   }

   @Override
   public char get(Object key) {
      synchronized(this.mutex) {
         return this.m.get(key);
      }
   }

   @Override
   public char put(K key, char value) {
      synchronized(this.mutex) {
         return this.m.put(key, value);
      }
   }

   @Override
   public char remove(Object key) {
      synchronized(this.mutex) {
         return this.m.remove(key);
      }
   }

   @Override
   public void putAll(Map<? extends K, ? extends Character> map) {
      synchronized(this.mutex) {
         this.m.putAll(map);
      }
   }

   @Override
   public void putAll(TObjectCharMap<? extends K> map) {
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
   public Set<K> keySet() {
      synchronized(this.mutex) {
         if (this.keySet == null) {
            this.keySet = new SynchronizedSet<>(this.m.keySet(), this.mutex);
         }

         return this.keySet;
      }
   }

   @Override
   public Object[] keys() {
      synchronized(this.mutex) {
         return this.m.keys();
      }
   }

   @Override
   public K[] keys(K[] array) {
      synchronized(this.mutex) {
         return this.m.keys(array);
      }
   }

   @Override
   public TCharCollection valueCollection() {
      synchronized(this.mutex) {
         if (this.values == null) {
            this.values = new TSynchronizedCharCollection(this.m.valueCollection(), this.mutex);
         }

         return this.values;
      }
   }

   @Override
   public char[] values() {
      synchronized(this.mutex) {
         return this.m.values();
      }
   }

   @Override
   public char[] values(char[] array) {
      synchronized(this.mutex) {
         return this.m.values(array);
      }
   }

   @Override
   public TObjectCharIterator<K> iterator() {
      return this.m.iterator();
   }

   @Override
   public char getNoEntryValue() {
      return this.m.getNoEntryValue();
   }

   @Override
   public char putIfAbsent(K key, char value) {
      synchronized(this.mutex) {
         return this.m.putIfAbsent(key, value);
      }
   }

   @Override
   public boolean forEachKey(TObjectProcedure<? super K> procedure) {
      synchronized(this.mutex) {
         return this.m.forEachKey(procedure);
      }
   }

   @Override
   public boolean forEachValue(TCharProcedure procedure) {
      synchronized(this.mutex) {
         return this.m.forEachValue(procedure);
      }
   }

   @Override
   public boolean forEachEntry(TObjectCharProcedure<? super K> procedure) {
      synchronized(this.mutex) {
         return this.m.forEachEntry(procedure);
      }
   }

   @Override
   public void transformValues(TCharFunction function) {
      synchronized(this.mutex) {
         this.m.transformValues(function);
      }
   }

   @Override
   public boolean retainEntries(TObjectCharProcedure<? super K> procedure) {
      synchronized(this.mutex) {
         return this.m.retainEntries(procedure);
      }
   }

   @Override
   public boolean increment(K key) {
      synchronized(this.mutex) {
         return this.m.increment(key);
      }
   }

   @Override
   public boolean adjustValue(K key, char amount) {
      synchronized(this.mutex) {
         return this.m.adjustValue(key, amount);
      }
   }

   @Override
   public char adjustOrPutValue(K key, char adjust_amount, char put_amount) {
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
