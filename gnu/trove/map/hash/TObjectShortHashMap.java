package gnu.trove.map.hash;

import gnu.trove.TShortCollection;
import gnu.trove.function.TShortFunction;
import gnu.trove.impl.Constants;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THash;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.TObjectShortIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.procedure.TObjectShortProcedure;
import gnu.trove.procedure.TShortProcedure;
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

public class TObjectShortHashMap<K> extends TObjectHash<K> implements TObjectShortMap<K>, Externalizable {
   static final long serialVersionUID = 1L;
   private final TObjectShortProcedure<K> PUT_ALL_PROC = new TObjectShortProcedure<K>() {
      @Override
      public boolean execute(K key, short value) {
         TObjectShortHashMap.this.put(key, value);
         return true;
      }
   };
   protected transient short[] _values;
   protected short no_entry_value;

   public TObjectShortHashMap() {
      this.no_entry_value = Constants.DEFAULT_SHORT_NO_ENTRY_VALUE;
   }

   public TObjectShortHashMap(int initialCapacity) {
      super(initialCapacity);
      this.no_entry_value = Constants.DEFAULT_SHORT_NO_ENTRY_VALUE;
   }

   public TObjectShortHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.no_entry_value = Constants.DEFAULT_SHORT_NO_ENTRY_VALUE;
   }

   public TObjectShortHashMap(int initialCapacity, float loadFactor, short noEntryValue) {
      super(initialCapacity, loadFactor);
      this.no_entry_value = noEntryValue;
      if (this.no_entry_value != 0) {
         Arrays.fill(this._values, this.no_entry_value);
      }
   }

   public TObjectShortHashMap(TObjectShortMap<? extends K> map) {
      this(map.size(), 0.5F, map.getNoEntryValue());
      if (map instanceof TObjectShortHashMap) {
         TObjectShortHashMap hashmap = (TObjectShortHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_value != 0) {
            Arrays.fill(this._values, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.putAll(map);
   }

   @Override
   public int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = new short[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      K[] oldKeys = (K[])this._set;
      short[] oldVals = this._values;
      this._set = new Object[newCapacity];
      Arrays.fill(this._set, FREE);
      this._values = new short[newCapacity];
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
   public short getNoEntryValue() {
      return this.no_entry_value;
   }

   @Override
   public boolean containsKey(Object key) {
      return this.contains(key);
   }

   @Override
   public boolean containsValue(short val) {
      Object[] keys = this._set;
      short[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public short get(Object key) {
      int index = this.index(key);
      return index < 0 ? this.no_entry_value : this._values[index];
   }

   @Override
   public short put(K key, short value) {
      int index = this.insertKey(key);
      return this.doPut(value, index);
   }

   @Override
   public short putIfAbsent(K key, short value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(value, index);
   }

   private short doPut(short value, int index) {
      short previous = this.no_entry_value;
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
   public short remove(Object key) {
      short prev = this.no_entry_value;
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
   public void putAll(Map<? extends K, ? extends Short> map) {
      for(Entry<? extends K, ? extends Short> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TObjectShortMap<? extends K> map) {
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
      return new TObjectShortHashMap.KeyView();
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
   public TShortCollection valueCollection() {
      return new TObjectShortHashMap.TShortValueCollection();
   }

   @Override
   public short[] values() {
      short[] vals = new short[this.size()];
      short[] v = this._values;
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
   public short[] values(short[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new short[size];
      }

      short[] v = this._values;
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
   public TObjectShortIterator<K> iterator() {
      return new TObjectShortHashMap.TObjectShortHashIterator<>(this);
   }

   @Override
   public boolean increment(K key) {
      return this.adjustValue(key, (short)1);
   }

   @Override
   public boolean adjustValue(K key, short amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public short adjustOrPutValue(K key, short adjust_amount, short put_amount) {
      int index = this.insertKey(key);
      boolean isNewMapping;
      short newValue;
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
   public boolean forEachValue(TShortProcedure procedure) {
      Object[] keys = this._set;
      short[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TObjectShortProcedure<? super K> procedure) {
      Object[] keys = this._set;
      short[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute((K)keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean retainEntries(TObjectShortProcedure<? super K> procedure) {
      boolean modified = false;
      K[] keys = (K[])this._set;
      short[] values = this._values;
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
   public void transformValues(TShortFunction function) {
      Object[] keys = this._set;
      short[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != null && keys[i] != REMOVED) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TObjectShortMap)) {
         return false;
      } else {
         TObjectShortMap that = (TObjectShortMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            try {
               TObjectShortIterator iter = this.iterator();

               while(iter.hasNext()) {
                  iter.advance();
                  Object key = iter.key();
                  short value = iter.value();
                  if (value == this.no_entry_value) {
                     if (that.get(key) != that.getNoEntryValue() || !that.containsKey(key)) {
                        return false;
                     }
                  } else if (value != that.get(key)) {
                     return false;
                  }
               }
            } catch (ClassCastException var6) {
            }

            return true;
         }
      }
   }

   @Override
   public int hashCode() {
      int hashcode = 0;
      Object[] keys = this._set;
      short[] values = this._values;
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
      out.writeShort(this.no_entry_value);
      out.writeInt(this._size);
      int i = this._set.length;

      while(i-- > 0) {
         if (this._set[i] != REMOVED && this._set[i] != FREE) {
            out.writeObject(this._set[i]);
            out.writeShort(this._values[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      super.readExternal(in);
      this.no_entry_value = in.readShort();
      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         K key = (K)in.readObject();
         short val = in.readShort();
         this.put(key, val);
      }
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEachEntry(new TObjectShortProcedure<K>() {
         private boolean first = true;

         @Override
         public boolean execute(K key, short value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(",");
            }

            buf.append(key).append("=").append((int)value);
            return true;
         }
      });
      buf.append("}");
      return buf.toString();
   }

   protected class KeyView extends TObjectShortHashMap<K>.MapBackedView<K> {
      @Override
      public Iterator<K> iterator() {
         return new TObjectHashIterator<>(TObjectShortHashMap.this);
      }

      @Override
      public boolean removeElement(K key) {
         return TObjectShortHashMap.this.no_entry_value != TObjectShortHashMap.this.remove(key);
      }

      @Override
      public boolean containsElement(K key) {
         return TObjectShortHashMap.this.contains(key);
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
         TObjectShortHashMap.this.clear();
      }

      @Override
      public boolean add(E obj) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         return TObjectShortHashMap.this.size();
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
         return TObjectShortHashMap.this.isEmpty();
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

   class TObjectShortHashIterator<K> extends TObjectHashIterator<K> implements TObjectShortIterator<K> {
      private final TObjectShortHashMap<K> _map;

      public TObjectShortHashIterator(TObjectShortHashMap<K> map) {
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
      public short value() {
         return this._map._values[this._index];
      }

      @Override
      public short setValue(short val) {
         short old = this.value();
         this._map._values[this._index] = val;
         return old;
      }
   }

   class TShortValueCollection implements TShortCollection {
      @Override
      public TShortIterator iterator() {
         return new TObjectShortHashMap.TShortValueCollection.TObjectShortValueHashIterator();
      }

      @Override
      public short getNoEntryValue() {
         return TObjectShortHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TObjectShortHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TObjectShortHashMap.this._size;
      }

      @Override
      public boolean contains(short entry) {
         return TObjectShortHashMap.this.containsValue(entry);
      }

      @Override
      public short[] toArray() {
         return TObjectShortHashMap.this.values();
      }

      @Override
      public short[] toArray(short[] dest) {
         return TObjectShortHashMap.this.values(dest);
      }

      @Override
      public boolean add(short entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(short entry) {
         short[] values = TObjectShortHashMap.this._values;
         Object[] set = TObjectShortHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && entry == values[i]) {
               TObjectShortHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Short)) {
               return false;
            }

            short ele = (Short)element;
            if (!TObjectShortHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TShortCollection collection) {
         TShortIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TObjectShortHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(short[] array) {
         for(short element : array) {
            if (!TObjectShortHashMap.this.containsValue(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Short> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TShortCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(short[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TShortIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TShortCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TShortIterator iter = this.iterator();

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
      public boolean retainAll(short[] array) {
         boolean changed = false;
         Arrays.sort(array);
         short[] values = TObjectShortHashMap.this._values;
         Object[] set = TObjectShortHashMap.this._set;
         int i = set.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && Arrays.binarySearch(array, values[i]) < 0) {
               TObjectShortHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Short) {
               short c = (Short)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TShortCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TShortIterator iter = collection.iterator();

            while(iter.hasNext()) {
               short element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(short[] array) {
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
         TObjectShortHashMap.this.clear();
      }

      @Override
      public boolean forEach(TShortProcedure procedure) {
         return TObjectShortHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TObjectShortHashMap.this.forEachValue(new TShortProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(short value) {
               if (this.first) {
                  this.first = false;
               } else {
                  buf.append(", ");
               }

               buf.append((int)value);
               return true;
            }
         });
         buf.append("}");
         return buf.toString();
      }

      class TObjectShortValueHashIterator implements TShortIterator {
         protected THash _hash = TObjectShortHashMap.this;
         protected int _expectedSize = this._hash.size();
         protected int _index = this._hash.capacity();

         @Override
         public boolean hasNext() {
            return this.nextIndex() >= 0;
         }

         @Override
         public short next() {
            this.moveToNextIndex();
            return TObjectShortHashMap.this._values[this._index];
         }

         @Override
         public void remove() {
            if (this._expectedSize != this._hash.size()) {
               throw new ConcurrentModificationException();
            } else {
               try {
                  this._hash.tempDisableAutoCompaction();
                  TObjectShortHashMap.this.removeAt(this._index);
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
               Object[] set = TObjectShortHashMap.this._set;
               int i = this._index;

               while(i-- > 0 && (set[i] == TObjectHash.FREE || set[i] == TObjectHash.REMOVED)) {
               }

               return i;
            }
         }
      }
   }
}
