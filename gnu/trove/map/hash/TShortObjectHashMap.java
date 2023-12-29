package gnu.trove.map.hash;

import gnu.trove.TShortCollection;
import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TShortHash;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.procedure.TShortObjectProcedure;
import gnu.trove.procedure.TShortProcedure;
import gnu.trove.set.TShortSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TShortObjectHashMap<V> extends TShortHash implements TShortObjectMap<V>, Externalizable {
   static final long serialVersionUID = 1L;
   private final TShortObjectProcedure<V> PUT_ALL_PROC = new TShortObjectProcedure<V>() {
      @Override
      public boolean execute(short key, V value) {
         TShortObjectHashMap.this.put(key, value);
         return true;
      }
   };
   protected transient V[] _values;
   protected short no_entry_key;

   public TShortObjectHashMap() {
   }

   public TShortObjectHashMap(int initialCapacity) {
      super(initialCapacity);
      this.no_entry_key = Constants.DEFAULT_SHORT_NO_ENTRY_VALUE;
   }

   public TShortObjectHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.no_entry_key = Constants.DEFAULT_SHORT_NO_ENTRY_VALUE;
   }

   public TShortObjectHashMap(int initialCapacity, float loadFactor, short noEntryKey) {
      super(initialCapacity, loadFactor);
      this.no_entry_key = noEntryKey;
   }

   public TShortObjectHashMap(TShortObjectMap<? extends V> map) {
      this(map.size(), 0.5F, map.getNoEntryKey());
      this.putAll(map);
   }

   @Override
   protected int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = (V[])(new Object[capacity]);
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      short[] oldKeys = this._set;
      V[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new short[newCapacity];
      this._values = (V[])(new Object[newCapacity]);
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            short o = oldKeys[i];
            int index = this.insertKey(o);
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public short getNoEntryKey() {
      return this.no_entry_key;
   }

   @Override
   public boolean containsKey(short key) {
      return this.contains(key);
   }

   @Override
   public boolean containsValue(Object val) {
      byte[] states = this._states;
      V[] vals = this._values;
      if (null == val) {
         int i = vals.length;

         while(i-- > 0) {
            if (states[i] == 1 && null == vals[i]) {
               return true;
            }
         }
      } else {
         int i = vals.length;

         while(i-- > 0) {
            if (states[i] == 1 && (val == vals[i] || val.equals(vals[i]))) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public V get(short key) {
      int index = this.index(key);
      return index < 0 ? null : this._values[index];
   }

   @Override
   public V put(short key, V value) {
      int index = this.insertKey(key);
      return this.doPut(value, index);
   }

   @Override
   public V putIfAbsent(short key, V value) {
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
   public V remove(short key) {
      V prev = null;
      int index = this.index(key);
      if (index >= 0) {
         prev = this._values[index];
         this.removeAt(index);
      }

      return prev;
   }

   @Override
   protected void removeAt(int index) {
      this._values[index] = null;
      super.removeAt(index);
   }

   @Override
   public void putAll(Map<? extends Short, ? extends V> map) {
      for(Entry<? extends Short, ? extends V> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TShortObjectMap<? extends V> map) {
      map.forEachEntry(this.PUT_ALL_PROC);
   }

   @Override
   public void clear() {
      super.clear();
      Arrays.fill(this._set, 0, this._set.length, this.no_entry_key);
      Arrays.fill(this._states, 0, this._states.length, (byte)0);
      Arrays.fill(this._values, 0, this._values.length, null);
   }

   @Override
   public TShortSet keySet() {
      return new TShortObjectHashMap.KeyView();
   }

   @Override
   public short[] keys() {
      short[] keys = new short[this.size()];
      short[] k = this._set;
      byte[] states = this._states;
      int i = k.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            keys[j++] = k[i];
         }
      }

      return keys;
   }

   @Override
   public short[] keys(short[] dest) {
      if (dest.length < this._size) {
         dest = new short[this._size];
      }

      short[] k = this._set;
      byte[] states = this._states;
      int i = k.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            dest[j++] = k[i];
         }
      }

      return dest;
   }

   @Override
   public Collection<V> valueCollection() {
      return new TShortObjectHashMap.ValueView();
   }

   @Override
   public Object[] values() {
      Object[] vals = new Object[this.size()];
      V[] v = this._values;
      byte[] states = this._states;
      int i = v.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            vals[j++] = v[i];
         }
      }

      return vals;
   }

   @Override
   public V[] values(V[] dest) {
      if (dest.length < this._size) {
         dest = (V[])((Object[])Array.newInstance(dest.getClass().getComponentType(), this._size));
      }

      V[] v = this._values;
      byte[] states = this._states;
      int i = v.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            dest[j++] = v[i];
         }
      }

      return dest;
   }

   @Override
   public TShortObjectIterator<V> iterator() {
      return new TShortObjectHashMap.TShortObjectHashIterator<>(this);
   }

   @Override
   public boolean forEachKey(TShortProcedure procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TObjectProcedure<? super V> procedure) {
      byte[] states = this._states;
      V[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TShortObjectProcedure<? super V> procedure) {
      byte[] states = this._states;
      short[] keys = this._set;
      V[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean retainEntries(TShortObjectProcedure<? super V> procedure) {
      boolean modified = false;
      byte[] states = this._states;
      short[] keys = this._set;
      V[] values = this._values;
      this.tempDisableAutoCompaction();

      try {
         int i = keys.length;

         while(i-- > 0) {
            if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
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
      byte[] states = this._states;
      V[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TShortObjectMap)) {
         return false;
      } else {
         TShortObjectMap that = (TShortObjectMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            try {
               TShortObjectIterator iter = this.iterator();

               while(iter.hasNext()) {
                  iter.advance();
                  short key = iter.key();
                  Object value = iter.value();
                  if (value == null) {
                     if (that.get(key) != null || !that.containsKey(key)) {
                        return false;
                     }
                  } else if (!value.equals(that.get(key))) {
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
      V[] values = this._values;
      byte[] states = this._states;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            hashcode += HashFunctions.hash(this._set[i]) ^ (values[i] == null ? 0 : values[i].hashCode());
         }
      }

      return hashcode;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      super.writeExternal(out);
      out.writeShort(this.no_entry_key);
      out.writeInt(this._size);
      int i = this._states.length;

      while(i-- > 0) {
         if (this._states[i] == 1) {
            out.writeShort(this._set[i]);
            out.writeObject(this._values[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      super.readExternal(in);
      this.no_entry_key = in.readShort();
      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         short key = in.readShort();
         V val = (V)in.readObject();
         this.put(key, val);
      }
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEachEntry(new TShortObjectProcedure<V>() {
         private boolean first = true;

         @Override
         public boolean execute(short key, Object value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(",");
            }

            buf.append((int)key);
            buf.append("=");
            buf.append(value);
            return true;
         }
      });
      buf.append("}");
      return buf.toString();
   }

   class KeyView implements TShortSet {
      @Override
      public short getNoEntryValue() {
         return TShortObjectHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TShortObjectHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return TShortObjectHashMap.this._size == 0;
      }

      @Override
      public boolean contains(short entry) {
         return TShortObjectHashMap.this.containsKey(entry);
      }

      @Override
      public TShortIterator iterator() {
         return new TShortObjectHashMap.KeyView.TShortHashIterator(TShortObjectHashMap.this);
      }

      @Override
      public short[] toArray() {
         return TShortObjectHashMap.this.keys();
      }

      @Override
      public short[] toArray(short[] dest) {
         return TShortObjectHashMap.this.keys(dest);
      }

      @Override
      public boolean add(short entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(short entry) {
         return null != TShortObjectHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!TShortObjectHashMap.this.containsKey((Short)element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TShortCollection collection) {
         if (collection == this) {
            return true;
         } else {
            TShortIterator iter = collection.iterator();

            while(iter.hasNext()) {
               if (!TShortObjectHashMap.this.containsKey(iter.next())) {
                  return false;
               }
            }

            return true;
         }
      }

      @Override
      public boolean containsAll(short[] array) {
         for(short element : array) {
            if (!TShortObjectHashMap.this.containsKey(element)) {
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
         short[] set = TShortObjectHashMap.this._set;
         byte[] states = TShortObjectHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TShortObjectHashMap.this.removeAt(i);
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
         if (collection == this) {
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
         TShortObjectHashMap.this.clear();
      }

      @Override
      public boolean forEach(TShortProcedure procedure) {
         return TShortObjectHashMap.this.forEachKey(procedure);
      }

      @Override
      public boolean equals(Object other) {
         if (!(other instanceof TShortSet)) {
            return false;
         } else {
            TShortSet that = (TShortSet)other;
            if (that.size() != this.size()) {
               return false;
            } else {
               int i = TShortObjectHashMap.this._states.length;

               while(i-- > 0) {
                  if (TShortObjectHashMap.this._states[i] == 1 && !that.contains(TShortObjectHashMap.this._set[i])) {
                     return false;
                  }
               }

               return true;
            }
         }
      }

      @Override
      public int hashCode() {
         int hashcode = 0;
         int i = TShortObjectHashMap.this._states.length;

         while(i-- > 0) {
            if (TShortObjectHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TShortObjectHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         StringBuilder buf = new StringBuilder("{");
         boolean first = true;
         int i = TShortObjectHashMap.this._states.length;

         while(i-- > 0) {
            if (TShortObjectHashMap.this._states[i] == 1) {
               if (first) {
                  first = false;
               } else {
                  buf.append(",");
               }

               buf.append(TShortObjectHashMap.this._set[i]);
            }
         }

         return buf.toString();
      }

      class TShortHashIterator extends THashPrimitiveIterator implements TShortIterator {
         private final TShortHash _hash;

         public TShortHashIterator(TShortHash hash) {
            super(hash);
            this._hash = hash;
         }

         @Override
         public short next() {
            this.moveToNextIndex();
            return this._hash._set[this._index];
         }
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
         TShortObjectHashMap.this.clear();
      }

      @Override
      public boolean add(E obj) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         return TShortObjectHashMap.this.size();
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
         return TShortObjectHashMap.this.isEmpty();
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

   class TShortObjectHashIterator<V> extends THashPrimitiveIterator implements TShortObjectIterator<V> {
      private final TShortObjectHashMap<V> _map;

      public TShortObjectHashIterator(TShortObjectHashMap<V> map) {
         super(map);
         this._map = map;
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public short key() {
         return this._map._set[this._index];
      }

      @Override
      public V value() {
         return this._map._values[this._index];
      }

      @Override
      public V setValue(V val) {
         V old = this.value();
         this._map._values[this._index] = val;
         return old;
      }
   }

   protected class ValueView extends TShortObjectHashMap<V>.MapBackedView<V> {
      @Override
      public Iterator<V> iterator() {
         return new TShortObjectHashMap.ValueView.TShortObjectValueHashIterator(TShortObjectHashMap.this) {
            @Override
            protected V objectAtIndex(int index) {
               return TShortObjectHashMap.this._values[index];
            }
         };
      }

      @Override
      public boolean containsElement(V value) {
         return TShortObjectHashMap.this.containsValue(value);
      }

      @Override
      public boolean removeElement(V value) {
         V[] values = TShortObjectHashMap.this._values;
         byte[] states = TShortObjectHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && (value == values[i] || null != values[i] && values[i].equals(value))) {
               TShortObjectHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      class TShortObjectValueHashIterator extends THashPrimitiveIterator implements Iterator<V> {
         protected final TShortObjectHashMap _map;

         public TShortObjectValueHashIterator(TShortObjectHashMap map) {
            super(map);
            this._map = map;
         }

         protected V objectAtIndex(int index) {
            byte[] states = TShortObjectHashMap.this._states;
            Object value = this._map._values[index];
            return (V)(states[index] != 1 ? null : value);
         }

         @Override
         public V next() {
            this.moveToNextIndex();
            return this._map._values[this._index];
         }
      }
   }
}
