package gnu.trove.map.hash;

import gnu.trove.TLongCollection;
import gnu.trove.function.TLongFunction;
import gnu.trove.impl.Constants;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THash;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TObjectLongProcedure;
import gnu.trove.procedure.TObjectProcedure;
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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

public class TObjectLongHashMap<K> extends TObjectHash<K> implements TObjectLongMap<K>, Externalizable {
   static final long serialVersionUID = 1L;
   private final TObjectLongProcedure<K> PUT_ALL_PROC = new TObjectLongProcedure<K>() {
      @Override
      public boolean execute(K key, long value) {
         TObjectLongHashMap.this.put(key, value);
         return true;
      }
   };
   protected transient long[] _values;
   protected long no_entry_value;

   public TObjectLongHashMap() {
      this.no_entry_value = Constants.DEFAULT_LONG_NO_ENTRY_VALUE;
   }

   public TObjectLongHashMap(int initialCapacity) {
      super(initialCapacity);
      this.no_entry_value = Constants.DEFAULT_LONG_NO_ENTRY_VALUE;
   }

   public TObjectLongHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.no_entry_value = Constants.DEFAULT_LONG_NO_ENTRY_VALUE;
   }

   public TObjectLongHashMap(int initialCapacity, float loadFactor, long noEntryValue) {
      super(initialCapacity, loadFactor);
      this.no_entry_value = noEntryValue;
      if (this.no_entry_value != 0L) {
         Arrays.fill(this._values, this.no_entry_value);
      }
   }

   public TObjectLongHashMap(TObjectLongMap<? extends K> map) {
      this(map.size(), 0.5F, map.getNoEntryValue());
      if (map instanceof TObjectLongHashMap) {
         TObjectLongHashMap hashmap = (TObjectLongHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_value != 0L) {
            Arrays.fill(this._values, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.putAll(map);
   }

   @Override
   public int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = new long[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      K[] oldKeys = (K[])this._set;
      long[] oldVals = this._values;
      this._set = new Object[newCapacity];
      Arrays.fill(this._set, FREE);
      this._values = new long[newCapacity];
      Arrays.fill(this._values, this.no_entry_value);
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldKeys[i] != FREE && oldKeys[i] != REMOVED) {
            K o = oldKeys[i];
            int index = this.insertKey(o);
            if (index < 0) {
               this.throwObjectContractViolation(this._set[-index - 1], o);
            }

            this._set[index] = o;
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public long getNoEntryValue() {
      return this.no_entry_value;
   }

   @Override
   public boolean containsKey(Object key) {
      return this.contains(key);
   }

   @Override
   public boolean containsValue(long val) {
      Object[] keys = this._set;
      long[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public long get(Object key) {
      int index = this.index(key);
      return index < 0 ? this.no_entry_value : this._values[index];
   }

   @Override
   public long put(K key, long value) {
      int index = this.insertKey(key);
      return this.doPut(value, index);
   }

   @Override
   public long putIfAbsent(K key, long value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(value, index);
   }

   private long doPut(long value, int index) {
      long previous = this.no_entry_value;
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
   public long remove(Object key) {
      long prev = this.no_entry_value;
      int index = this.index(key);
      if (index >= 0) {
         prev = this._values[index];
         this.removeAt(index);
      }

      return prev;
   }

   @Override
   protected void removeAt(int index) {
      this._values[index] = this.no_entry_value;
      super.removeAt(index);
   }

   @Override
   public void putAll(Map<? extends K, ? extends Long> map) {
      for(Entry<? extends K, ? extends Long> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TObjectLongMap<? extends K> map) {
      map.forEachEntry(this.PUT_ALL_PROC);
   }

   @Override
   public void clear() {
      super.clear();
      Arrays.fill(this._set, 0, this._set.length, FREE);
      Arrays.fill(this._values, 0, this._values.length, this.no_entry_value);
   }

   @Override
   public Set<K> keySet() {
      return new TObjectLongHashMap.KeyView();
   }

   @Override
   public Object[] keys() {
      K[] keys = (K[])(new Object[this.size()]);
      Object[] k = this._set;
      int i = k.length;
      int j = 0;

      while(i-- > 0) {
         if (k[i] != FREE && k[i] != REMOVED) {
            keys[j++] = (K)k[i];
         }
      }

      return keys;
   }

   @Override
   public K[] keys(K[] a) {
      int size = this.size();
      if (a.length < size) {
         a = (K[])((Object[])Array.newInstance(a.getClass().getComponentType(), size));
      }

      Object[] k = this._set;
      int i = k.length;
      int j = 0;

      while(i-- > 0) {
         if (k[i] != FREE && k[i] != REMOVED) {
            a[j++] = (K)k[i];
         }
      }

      return a;
   }

   @Override
   public TLongCollection valueCollection() {
      return new TObjectLongHashMap.TLongValueCollection();
   }

   @Override
   public long[] values() {
      long[] vals = new long[this.size()];
      long[] v = this._values;
      Object[] keys = this._set;
      int i = v.length;
      int j = 0;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED) {
            vals[j++] = v[i];
         }
      }

      return vals;
   }

   @Override
   public long[] values(long[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new long[size];
      }

      long[] v = this._values;
      Object[] keys = this._set;
      int i = v.length;
      int j = 0;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED) {
            array[j++] = v[i];
         }
      }

      if (array.length > size) {
         array[size] = this.no_entry_value;
      }

      return array;
   }

   @Override
   public TObjectLongIterator<K> iterator() {
      return new TObjectLongHashMap.TObjectLongHashIterator<>(this);
   }

   @Override
   public boolean increment(K key) {
      return this.adjustValue(key, 1L);
   }

   @Override
   public boolean adjustValue(K key, long amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public long adjustOrPutValue(K key, long adjust_amount, long put_amount) {
      int index = this.insertKey(key);
      boolean isNewMapping;
      long newValue;
      if (index < 0) {
         index = -index - 1;
         newValue = this._values[index] += adjust_amount;
         isNewMapping = false;
      } else {
         newValue = this._values[index] = put_amount;
         isNewMapping = true;
      }

      if (isNewMapping) {
         this.postInsertHook(this.consumeFreeSlot);
      }

      return newValue;
   }

   @Override
   public boolean forEachKey(TObjectProcedure<? super K> procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TLongProcedure procedure) {
      Object[] keys = this._set;
      long[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TObjectLongProcedure<? super K> procedure) {
      Object[] keys = this._set;
      long[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute((K)keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean retainEntries(TObjectLongProcedure<? super K> procedure) {
      boolean modified = false;
      K[] keys = (K[])this._set;
      long[] values = this._values;
      this.tempDisableAutoCompaction();

      try {
         int i = keys.length;

         while(i-- > 0) {
            if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute(keys[i], values[i])) {
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
   public void transformValues(TLongFunction function) {
      Object[] keys = this._set;
      long[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != null && keys[i] != REMOVED) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TObjectLongMap)) {
         return false;
      } else {
         TObjectLongMap that = (TObjectLongMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            try {
               TObjectLongIterator iter = this.iterator();

               while(iter.hasNext()) {
                  iter.advance();
                  Object key = iter.key();
                  long value = iter.value();
                  if (value == this.no_entry_value) {
                     if (that.get(key) != that.getNoEntryValue() || !that.containsKey(key)) {
                        return false;
                     }
                  } else if (value != that.get(key)) {
                     return false;
                  }
               }
            } catch (ClassCastException var7) {
            }

            return true;
         }
      }
   }

   @Override
   public int hashCode() {
      int hashcode = 0;
      Object[] keys = this._set;
      long[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED) {
            hashcode += HashFunctions.hash(values[i]) ^ (keys[i] == null ? 0 : keys[i].hashCode());
         }
      }

      return hashcode;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      super.writeExternal(out);
      out.writeLong(this.no_entry_value);
      out.writeInt(this._size);
      int i = this._set.length;

      while(i-- > 0) {
         if (this._set[i] != REMOVED && this._set[i] != FREE) {
            out.writeObject(this._set[i]);
            out.writeLong(this._values[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      super.readExternal(in);
      this.no_entry_value = in.readLong();
      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         K key = (K)in.readObject();
         long val = in.readLong();
         this.put(key, val);
      }
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEachEntry(new TObjectLongProcedure<K>() {
         private boolean first = true;

         @Override
         public boolean execute(K key, long value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(",");
            }

            buf.append(key).append("=").append(value);
            return true;
         }
      });
      buf.append("}");
      return buf.toString();
   }

   protected class KeyView extends TObjectLongHashMap<K>.MapBackedView<K> {
      @Override
      public Iterator<K> iterator() {
         return new TObjectHashIterator<>(TObjectLongHashMap.this);
      }

      @Override
      public boolean removeElement(K key) {
         return TObjectLongHashMap.this.no_entry_value != TObjectLongHashMap.this.remove(key);
      }

      @Override
      public boolean containsElement(K key) {
         return TObjectLongHashMap.this.contains(key);
      }
   }

   private abstract class MapBackedView<E> extends AbstractSet<E> implements Set<E>, Iterable<E> {
      private MapBackedView() {
      }

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
         TObjectLongHashMap.this.clear();
      }

      @Override
      public boolean add(E obj) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         return TObjectLongHashMap.this.size();
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
         return TObjectLongHashMap.this.isEmpty();
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
   }

   class TLongValueCollection implements TLongCollection {
      @Override
      public TLongIterator iterator() {
         return new TObjectLongHashMap.TLongValueCollection.TObjectLongValueHashIterator();
      }

      @Override
      public long getNoEntryValue() {
         return TObjectLongHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TObjectLongHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TObjectLongHashMap.this._size;
      }

      @Override
      public boolean contains(long entry) {
         return TObjectLongHashMap.this.containsValue(entry);
      }

      @Override
      public long[] toArray() {
         return TObjectLongHashMap.this.values();
      }

      @Override
      public long[] toArray(long[] dest) {
         return TObjectLongHashMap.this.values(dest);
      }

      @Override
      public boolean add(long entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(long entry) {
         long[] values = TObjectLongHashMap.this._values;
         Object[] set = TObjectLongHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && entry == values[i]) {
               TObjectLongHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Long)) {
               return false;
            }

            long ele = (Long)element;
            if (!TObjectLongHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TLongCollection collection) {
         TLongIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TObjectLongHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(long[] array) {
         for(long element : array) {
            if (!TObjectLongHashMap.this.containsValue(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Long> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TLongCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(long[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TLongIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TLongCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TLongIterator iter = this.iterator();

            while(iter.hasNext()) {
               if (!collection.contains(iter.next())) {
                  iter.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }

      @Override
      public boolean retainAll(long[] array) {
         boolean changed = false;
         Arrays.sort(array);
         long[] values = TObjectLongHashMap.this._values;
         Object[] set = TObjectLongHashMap.this._set;
         int i = set.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && Arrays.binarySearch(array, values[i]) < 0) {
               TObjectLongHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Long) {
               long c = (Long)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TLongCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TLongIterator iter = collection.iterator();

            while(iter.hasNext()) {
               long element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(long[] array) {
         boolean changed = false;
         int i = array.length;

         while(i-- > 0) {
            if (this.remove(array[i])) {
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public void clear() {
         TObjectLongHashMap.this.clear();
      }

      @Override
      public boolean forEach(TLongProcedure procedure) {
         return TObjectLongHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TObjectLongHashMap.this.forEachValue(new TLongProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(long value) {
               if (this.first) {
                  this.first = false;
               } else {
                  buf.append(", ");
               }

               buf.append(value);
               return true;
            }
         });
         buf.append("}");
         return buf.toString();
      }

      class TObjectLongValueHashIterator implements TLongIterator {
         protected THash _hash = TObjectLongHashMap.this;
         protected int _expectedSize = this._hash.size();
         protected int _index = this._hash.capacity();

         @Override
         public boolean hasNext() {
            return this.nextIndex() >= 0;
         }

         @Override
         public long next() {
            this.moveToNextIndex();
            return TObjectLongHashMap.this._values[this._index];
         }

         @Override
         public void remove() {
            if (this._expectedSize != this._hash.size()) {
               throw new ConcurrentModificationException();
            } else {
               try {
                  this._hash.tempDisableAutoCompaction();
                  TObjectLongHashMap.this.removeAt(this._index);
               } finally {
                  this._hash.reenableAutoCompaction(false);
               }

               --this._expectedSize;
            }
         }

         protected final void moveToNextIndex() {
            if ((this._index = this.nextIndex()) < 0) {
               throw new NoSuchElementException();
            }
         }

         protected final int nextIndex() {
            if (this._expectedSize != this._hash.size()) {
               throw new ConcurrentModificationException();
            } else {
               Object[] set = TObjectLongHashMap.this._set;
               int i = this._index;

               while(i-- > 0 && (set[i] == TObjectHash.FREE || set[i] == TObjectHash.REMOVED)) {
               }

               return i;
            }
         }
      }
   }

   class TObjectLongHashIterator<K> extends TObjectHashIterator<K> implements TObjectLongIterator<K> {
      private final TObjectLongHashMap<K> _map;

      public TObjectLongHashIterator(TObjectLongHashMap<K> map) {
         super(map);
         this._map = map;
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public K key() {
         return (K)this._map._set[this._index];
      }

      @Override
      public long value() {
         return this._map._values[this._index];
      }

      @Override
      public long setValue(long val) {
         long old = this.value();
         this._map._values[this._index] = val;
         return old;
      }
   }
}
