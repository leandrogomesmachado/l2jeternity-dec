package gnu.trove.map.hash;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TCustomObjectHash;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TMap;
import gnu.trove.procedure.TObjectObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.strategy.HashingStrategy;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TCustomHashMap<K, V> extends TCustomObjectHash<K> implements TMap<K, V>, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient V[] _values;

   public TCustomHashMap() {
   }

   public TCustomHashMap(HashingStrategy<? super K> strategy) {
      super(strategy);
   }

   public TCustomHashMap(HashingStrategy<? super K> strategy, int initialCapacity) {
      super(strategy, initialCapacity);
   }

   public TCustomHashMap(HashingStrategy<? super K> strategy, int initialCapacity, float loadFactor) {
      super(strategy, initialCapacity, loadFactor);
   }

   public TCustomHashMap(HashingStrategy<? super K> strategy, Map<? extends K, ? extends V> map) {
      this(strategy, map.size());
      this.putAll(map);
   }

   public TCustomHashMap(HashingStrategy<? super K> strategy, TCustomHashMap<? extends K, ? extends V> map) {
      this(strategy, map.size());
      this.putAll(map);
   }

   @Override
   public int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = (V[])(new Object[capacity]);
      return capacity;
   }

   @Override
   public V put(K key, V value) {
      int index = this.insertKey(key);
      return this.doPut(value, index);
   }

   @Override
   public V putIfAbsent(K key, V value) {
      int index = this.insertKey(key);
      return (V)(index < 0 ? this._values[-index - 1] : this.doPut(value, index));
   }

   private V doPut(V value, int index) {
      V previous = null;
      boolean isNewMapping = true;
      if (index < 0) {
         index = -index - 1;
         previous = this._values[index];
         isNewMapping = false;
      }

      this._values[index] = value;
      if (isNewMapping) {
         this.postInsertHook(this.consumeFreeSlot);
      }

      return previous;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof Map)) {
         return false;
      } else {
         Map<K, V> that = (Map)other;
         return that.size() != this.size() ? false : this.forEachEntry(new TCustomHashMap.EqProcedure<>(that));
      }
   }

   @Override
   public int hashCode() {
      TCustomHashMap<K, V>.HashProcedure p = new TCustomHashMap.HashProcedure();
      this.forEachEntry(p);
      return p.getHashCode();
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEachEntry(new TObjectObjectProcedure<K, V>() {
         private boolean first = true;

         @Override
         public boolean execute(K key, V value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(", ");
            }

            buf.append(key);
            buf.append("=");
            buf.append(value);
            return true;
         }
      });
      buf.append("}");
      return buf.toString();
   }

   @Override
   public boolean forEachKey(TObjectProcedure<? super K> procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TObjectProcedure<? super V> procedure) {
      V[] values = this._values;
      Object[] set = this._set;
      int i = values.length;

      while(i-- > 0) {
         if (set[i] != FREE && set[i] != REMOVED && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TObjectObjectProcedure<? super K, ? super V> procedure) {
      Object[] keys = this._set;
      V[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute((K)keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean retainEntries(TObjectObjectProcedure<? super K, ? super V> procedure) {
      boolean modified = false;
      Object[] keys = this._set;
      V[] values = this._values;
      this.tempDisableAutoCompaction();

      try {
         int i = keys.length;

         while(i-- > 0) {
            if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute((K)keys[i], values[i])) {
               this.removeAt(i);
               modified = true;
            }
         }
      } finally {
         this.reenableAutoCompaction(true);
      }

      return modified;
   }

   @Override
   public void transformValues(TObjectFunction<V, V> function) {
      V[] values = this._values;
      Object[] set = this._set;
      int i = values.length;

      while(i-- > 0) {
         if (set[i] != FREE && set[i] != REMOVED) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      int oldSize = this.size();
      Object[] oldKeys = this._set;
      V[] oldVals = this._values;
      this._set = new Object[newCapacity];
      Arrays.fill(this._set, FREE);
      this._values = (V[])(new Object[newCapacity]);
      int i = oldCapacity;

      while(i-- > 0) {
         Object o = oldKeys[i];
         if (o != FREE && o != REMOVED) {
            int index = this.insertKey(o);
            if (index < 0) {
               this.throwObjectContractViolation(this._set[-index - 1], o, this.size(), oldSize, oldKeys);
            }

            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public V get(Object key) {
      int index = this.index(key);
      return index >= 0 && this.strategy.equals((K)this._set[index], (K)key) ? this._values[index] : null;
   }

   @Override
   public void clear() {
      if (this.size() != 0) {
         super.clear();
         Arrays.fill(this._set, 0, this._set.length, FREE);
         Arrays.fill(this._values, 0, this._values.length, null);
      }
   }

   @Override
   public V remove(Object key) {
      V prev = null;
      int index = this.index(key);
      if (index >= 0) {
         prev = this._values[index];
         this.removeAt(index);
      }

      return prev;
   }

   @Override
   public void removeAt(int index) {
      this._values[index] = null;
      super.removeAt(index);
   }

   @Override
   public Collection<V> values() {
      return new TCustomHashMap.ValueView();
   }

   @Override
   public Set<K> keySet() {
      return new TCustomHashMap.KeyView();
   }

   @Override
   public Set<java.util.Map.Entry<K, V>> entrySet() {
      return new TCustomHashMap.EntryView();
   }

   @Override
   public boolean containsValue(Object val) {
      Object[] set = this._set;
      V[] vals = this._values;
      if (null == val) {
         int i = vals.length;

         while(i-- > 0) {
            if (set[i] != FREE && set[i] != REMOVED && val == vals[i]) {
               return true;
            }
         }
      } else {
         int i = vals.length;

         while(i-- > 0) {
            if (set[i] != FREE && set[i] != REMOVED && (val == vals[i] || this.strategy.equals((K)val, (K)vals[i]))) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(Object key) {
      return this.contains(key);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      this.ensureCapacity(map.size());

      for(java.util.Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(1);
      super.writeExternal(out);
      out.writeInt(this._size);
      int i = this._set.length;

      while(i-- > 0) {
         if (this._set[i] != REMOVED && this._set[i] != FREE) {
            out.writeObject(this._set[i]);
            out.writeObject(this._values[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      byte version = in.readByte();
      if (version != 0) {
         super.readExternal(in);
      }

      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         K key = (K)in.readObject();
         V val = (V)in.readObject();
         this.put(key, val);
      }
   }

   final class Entry implements java.util.Map.Entry<K, V> {
      private K key;
      private V val;
      private final int index;

      Entry(K key, V value, int index) {
         this.key = key;
         this.val = value;
         this.index = index;
      }

      @Override
      public K getKey() {
         return this.key;
      }

      @Override
      public V getValue() {
         return this.val;
      }

      @Override
      public V setValue(V o) {
         if (TCustomHashMap.this._values[this.index] != this.val) {
            throw new ConcurrentModificationException();
         } else {
            V retval = this.val;
            TCustomHashMap.this._values[this.index] = o;
            this.val = o;
            return retval;
         }
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof java.util.Map.Entry)) {
            return false;
         } else {
            java.util.Map.Entry e2 = (java.util.Map.Entry)o;
            return (this.getKey() == null ? e2.getKey() == null : TCustomHashMap.this.strategy.equals((K)this.getKey(), (K)e2.getKey()))
               && (this.getValue() == null ? e2.getValue() == null : this.getValue().equals(e2.getValue()));
         }
      }

      @Override
      public int hashCode() {
         return (this.getKey() == null ? 0 : this.getKey().hashCode()) ^ (this.getValue() == null ? 0 : this.getValue().hashCode());
      }

      @Override
      public String toString() {
         return this.key + "=" + this.val;
      }
   }

   protected class EntryView extends TCustomHashMap<K, V>.MapBackedView<java.util.Map.Entry<K, V>> {
      @Override
      public Iterator<java.util.Map.Entry<K, V>> iterator() {
         return new TCustomHashMap.EntryView.EntryIterator(TCustomHashMap.this);
      }

      public boolean removeElement(java.util.Map.Entry<K, V> entry) {
         K key = (K)this.keyForEntry(entry);
         int index = TCustomHashMap.this.index(key);
         if (index >= 0) {
            Object val = this.valueForEntry(entry);
            if (val == TCustomHashMap.this._values[index] || null != val && TCustomHashMap.this.strategy.equals((K)val, (K)TCustomHashMap.this._values[index])
               )
             {
               TCustomHashMap.this.removeAt(index);
               return true;
            }
         }

         return false;
      }

      public boolean containsElement(java.util.Map.Entry<K, V> entry) {
         Object val = TCustomHashMap.this.get(this.keyForEntry(entry));
         Object entryValue = entry.getValue();
         return entryValue == val || null != val && TCustomHashMap.this.strategy.equals((K)val, (K)entryValue);
      }

      protected V valueForEntry(java.util.Map.Entry<K, V> entry) {
         return entry.getValue();
      }

      protected K keyForEntry(java.util.Map.Entry<K, V> entry) {
         return entry.getKey();
      }

      private final class EntryIterator extends TObjectHashIterator {
         EntryIterator(TCustomHashMap<K, V> map) {
            super(map);
         }

         public TCustomHashMap<K, V>.Entry objectAtIndex(int index) {
            return TCustomHashMap.this.new Entry(TCustomHashMap.this._set[index], TCustomHashMap.this._values[index], index);
         }
      }
   }

   private static final class EqProcedure<K, V> implements TObjectObjectProcedure<K, V> {
      private final Map<K, V> _otherMap;

      EqProcedure(Map<K, V> otherMap) {
         this._otherMap = otherMap;
      }

      @Override
      public final boolean execute(K key, V value) {
         if (value == null && !this._otherMap.containsKey(key)) {
            return false;
         } else {
            V oValue = this._otherMap.get(key);
            return oValue == value || oValue != null && oValue.equals(value);
         }
      }
   }

   private final class HashProcedure implements TObjectObjectProcedure<K, V> {
      private int h = 0;

      private HashProcedure() {
      }

      public int getHashCode() {
         return this.h;
      }

      @Override
      public final boolean execute(K key, V value) {
         this.h += HashFunctions.hash(key) ^ (value == null ? 0 : value.hashCode());
         return true;
      }
   }

   protected class KeyView extends TCustomHashMap<K, V>.MapBackedView<K> {
      @Override
      public Iterator<K> iterator() {
         return new TObjectHashIterator<>(TCustomHashMap.this);
      }

      @Override
      public boolean removeElement(K key) {
         return null != TCustomHashMap.this.remove(key);
      }

      @Override
      public boolean containsElement(K key) {
         return TCustomHashMap.this.contains(key);
      }
   }

   private abstract class MapBackedView<E> extends AbstractSet<E> implements Set<E>, Iterable<E> {
      private MapBackedView() {
      }

      @Override
      public abstract Iterator<E> iterator();

      public abstract boolean removeElement(E var1);

      public abstract boolean containsElement(E var1);

      @Override
      public boolean contains(Object key) {
         return this.containsElement((E)key);
      }

      @Override
      public boolean remove(Object o) {
         return this.removeElement((E)o);
      }

      @Override
      public void clear() {
         TCustomHashMap.this.clear();
      }

      @Override
      public boolean add(E obj) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         return TCustomHashMap.this.size();
      }

      @Override
      public Object[] toArray() {
         Object[] result = new Object[this.size()];
         Iterator<E> e = this.iterator();

         for(int i = 0; e.hasNext(); ++i) {
            result[i] = e.next();
         }

         return result;
      }

      @Override
      public <T> T[] toArray(T[] a) {
         int size = this.size();
         if (a.length < size) {
            a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
         }

         Iterator<E> it = this.iterator();
         Object[] result = a;

         for(int i = 0; i < size; ++i) {
            result[i] = it.next();
         }

         if (a.length > size) {
            a[size] = null;
         }

         return a;
      }

      @Override
      public boolean isEmpty() {
         return TCustomHashMap.this.isEmpty();
      }

      @Override
      public boolean addAll(Collection<? extends E> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean changed = false;
         Iterator<E> i = this.iterator();

         while(i.hasNext()) {
            if (!collection.contains(i.next())) {
               i.remove();
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public String toString() {
         Iterator<E> i = this.iterator();
         if (!i.hasNext()) {
            return "{}";
         } else {
            StringBuilder sb = new StringBuilder();
            sb.append('{');

            while(true) {
               E e = i.next();
               sb.append(e == this ? "(this Collection)" : e);
               if (!i.hasNext()) {
                  return sb.append('}').toString();
               }

               sb.append(", ");
            }
         }
      }
   }

   protected class ValueView extends TCustomHashMap<K, V>.MapBackedView<V> {
      @Override
      public Iterator<V> iterator() {
         return new TObjectHashIterator(TCustomHashMap.this) {
            @Override
            protected V objectAtIndex(int index) {
               return TCustomHashMap.this._values[index];
            }
         };
      }

      @Override
      public boolean containsElement(V value) {
         return TCustomHashMap.this.containsValue(value);
      }

      @Override
      public boolean removeElement(V value) {
         Object[] values = TCustomHashMap.this._values;
         Object[] set = TCustomHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && value == values[i]
               || null != values[i] && TCustomHashMap.this.strategy.equals((K)values[i], (K)value)) {
               TCustomHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }
   }
}
