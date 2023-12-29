package gnu.trove.map.custom_hash;

import gnu.trove.TDoubleCollection;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.impl.Constants;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TCustomObjectHash;
import gnu.trove.impl.hash.THash;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TObjectDoubleProcedure;
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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

public class TObjectDoubleCustomHashMap<K> extends TCustomObjectHash<K> implements TObjectDoubleMap<K>, Externalizable {
   static final long serialVersionUID = 1L;
   private final TObjectDoubleProcedure<K> PUT_ALL_PROC = new TObjectDoubleProcedure<K>() {
      @Override
      public boolean execute(K key, double value) {
         TObjectDoubleCustomHashMap.this.put(key, value);
         return true;
      }
   };
   protected transient double[] _values;
   protected double no_entry_value;

   public TObjectDoubleCustomHashMap() {
   }

   public TObjectDoubleCustomHashMap(HashingStrategy<? super K> strategy) {
      super(strategy);
      this.no_entry_value = Constants.DEFAULT_DOUBLE_NO_ENTRY_VALUE;
   }

   public TObjectDoubleCustomHashMap(HashingStrategy<? super K> strategy, int initialCapacity) {
      super(strategy, initialCapacity);
      this.no_entry_value = Constants.DEFAULT_DOUBLE_NO_ENTRY_VALUE;
   }

   public TObjectDoubleCustomHashMap(HashingStrategy<? super K> strategy, int initialCapacity, float loadFactor) {
      super(strategy, initialCapacity, loadFactor);
      this.no_entry_value = Constants.DEFAULT_DOUBLE_NO_ENTRY_VALUE;
   }

   public TObjectDoubleCustomHashMap(HashingStrategy<? super K> strategy, int initialCapacity, float loadFactor, double noEntryValue) {
      super(strategy, initialCapacity, loadFactor);
      this.no_entry_value = noEntryValue;
      if (this.no_entry_value != 0.0) {
         Arrays.fill(this._values, this.no_entry_value);
      }
   }

   public TObjectDoubleCustomHashMap(HashingStrategy<? super K> strategy, TObjectDoubleMap<? extends K> map) {
      this(strategy, map.size(), 0.5F, map.getNoEntryValue());
      if (map instanceof TObjectDoubleCustomHashMap) {
         TObjectDoubleCustomHashMap hashmap = (TObjectDoubleCustomHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_value = hashmap.no_entry_value;
         this.strategy = hashmap.strategy;
         if (this.no_entry_value != 0.0) {
            Arrays.fill(this._values, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.putAll(map);
   }

   @Override
   public int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = new double[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      K[] oldKeys = (K[])this._set;
      double[] oldVals = this._values;
      this._set = new Object[newCapacity];
      Arrays.fill(this._set, FREE);
      this._values = new double[newCapacity];
      Arrays.fill(this._values, this.no_entry_value);
      int i = oldCapacity;

      while(i-- > 0) {
         K o = oldKeys[i];
         if (o != FREE && o != REMOVED) {
            int index = this.insertKey(o);
            if (index < 0) {
               this.throwObjectContractViolation(this._set[-index - 1], o);
            }

            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public double getNoEntryValue() {
      return this.no_entry_value;
   }

   @Override
   public boolean containsKey(Object key) {
      return this.contains(key);
   }

   @Override
   public boolean containsValue(double val) {
      Object[] keys = this._set;
      double[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public double get(Object key) {
      int index = this.index(key);
      return index < 0 ? this.no_entry_value : this._values[index];
   }

   @Override
   public double put(K key, double value) {
      int index = this.insertKey(key);
      return this.doPut(value, index);
   }

   @Override
   public double putIfAbsent(K key, double value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(value, index);
   }

   private double doPut(double value, int index) {
      double previous = this.no_entry_value;
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
   public double remove(Object key) {
      double prev = this.no_entry_value;
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
   public void putAll(Map<? extends K, ? extends Double> map) {
      for(Entry<? extends K, ? extends Double> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TObjectDoubleMap<? extends K> map) {
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
      return new TObjectDoubleCustomHashMap.KeyView();
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
   public TDoubleCollection valueCollection() {
      return new TObjectDoubleCustomHashMap.TDoubleValueCollection();
   }

   @Override
   public double[] values() {
      double[] vals = new double[this.size()];
      double[] v = this._values;
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
   public double[] values(double[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new double[size];
      }

      double[] v = this._values;
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
   public TObjectDoubleIterator<K> iterator() {
      return new TObjectDoubleCustomHashMap.TObjectDoubleHashIterator<>(this);
   }

   @Override
   public boolean increment(K key) {
      return this.adjustValue(key, 1.0);
   }

   @Override
   public boolean adjustValue(K key, double amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public double adjustOrPutValue(K key, double adjust_amount, double put_amount) {
      int index = this.insertKey(key);
      boolean isNewMapping;
      double newValue;
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
   public boolean forEachValue(TDoubleProcedure procedure) {
      Object[] keys = this._set;
      double[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TObjectDoubleProcedure<? super K> procedure) {
      Object[] keys = this._set;
      double[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (keys[i] != FREE && keys[i] != REMOVED && !procedure.execute((K)keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean retainEntries(TObjectDoubleProcedure<? super K> procedure) {
      boolean modified = false;
      K[] keys = (K[])this._set;
      double[] values = this._values;
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
   public void transformValues(TDoubleFunction function) {
      Object[] keys = this._set;
      double[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (keys[i] != null && keys[i] != REMOVED) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TObjectDoubleMap)) {
         return false;
      } else {
         TObjectDoubleMap that = (TObjectDoubleMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            try {
               TObjectDoubleIterator iter = this.iterator();

               while(iter.hasNext()) {
                  iter.advance();
                  Object key = iter.key();
                  double value = iter.value();
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
      double[] values = this._values;
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
      out.writeObject(this.strategy);
      out.writeDouble(this.no_entry_value);
      out.writeInt(this._size);
      int i = this._set.length;

      while(i-- > 0) {
         if (this._set[i] != REMOVED && this._set[i] != FREE) {
            out.writeObject(this._set[i]);
            out.writeDouble(this._values[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      super.readExternal(in);
      this.strategy = (HashingStrategy)in.readObject();
      this.no_entry_value = in.readDouble();
      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         K key = (K)in.readObject();
         double val = in.readDouble();
         this.put(key, val);
      }
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEachEntry(new TObjectDoubleProcedure<K>() {
         private boolean first = true;

         @Override
         public boolean execute(K key, double value) {
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

   protected class KeyView extends TObjectDoubleCustomHashMap<K>.MapBackedView<K> {
      @Override
      public Iterator<K> iterator() {
         return new TObjectHashIterator<>(TObjectDoubleCustomHashMap.this);
      }

      @Override
      public boolean removeElement(K key) {
         return TObjectDoubleCustomHashMap.this.no_entry_value != TObjectDoubleCustomHashMap.this.remove(key);
      }

      @Override
      public boolean containsElement(K key) {
         return TObjectDoubleCustomHashMap.this.contains(key);
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
         TObjectDoubleCustomHashMap.this.clear();
      }

      @Override
      public boolean add(E obj) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         return TObjectDoubleCustomHashMap.this.size();
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
         return TObjectDoubleCustomHashMap.this.isEmpty();
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

   class TDoubleValueCollection implements TDoubleCollection {
      @Override
      public TDoubleIterator iterator() {
         return new TObjectDoubleCustomHashMap.TDoubleValueCollection.TObjectDoubleValueHashIterator();
      }

      @Override
      public double getNoEntryValue() {
         return TObjectDoubleCustomHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TObjectDoubleCustomHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TObjectDoubleCustomHashMap.this._size;
      }

      @Override
      public boolean contains(double entry) {
         return TObjectDoubleCustomHashMap.this.containsValue(entry);
      }

      @Override
      public double[] toArray() {
         return TObjectDoubleCustomHashMap.this.values();
      }

      @Override
      public double[] toArray(double[] dest) {
         return TObjectDoubleCustomHashMap.this.values(dest);
      }

      @Override
      public boolean add(double entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(double entry) {
         double[] values = TObjectDoubleCustomHashMap.this._values;
         Object[] set = TObjectDoubleCustomHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && entry == values[i]) {
               TObjectDoubleCustomHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Double)) {
               return false;
            }

            double ele = (Double)element;
            if (!TObjectDoubleCustomHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TDoubleCollection collection) {
         TDoubleIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TObjectDoubleCustomHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(double[] array) {
         for(double element : array) {
            if (!TObjectDoubleCustomHashMap.this.containsValue(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Double> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TDoubleCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(double[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TDoubleIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TDoubleCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TDoubleIterator iter = this.iterator();

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
      public boolean retainAll(double[] array) {
         boolean changed = false;
         Arrays.sort(array);
         double[] values = TObjectDoubleCustomHashMap.this._values;
         Object[] set = TObjectDoubleCustomHashMap.this._set;
         int i = set.length;

         while(i-- > 0) {
            if (set[i] != TObjectHash.FREE && set[i] != TObjectHash.REMOVED && Arrays.binarySearch(array, values[i]) < 0) {
               TObjectDoubleCustomHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Double) {
               double c = (Double)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TDoubleCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TDoubleIterator iter = collection.iterator();

            while(iter.hasNext()) {
               double element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(double[] array) {
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
         TObjectDoubleCustomHashMap.this.clear();
      }

      @Override
      public boolean forEach(TDoubleProcedure procedure) {
         return TObjectDoubleCustomHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TObjectDoubleCustomHashMap.this.forEachValue(new TDoubleProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(double value) {
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

      class TObjectDoubleValueHashIterator implements TDoubleIterator {
         protected THash _hash = TObjectDoubleCustomHashMap.this;
         protected int _expectedSize = this._hash.size();
         protected int _index = this._hash.capacity();

         @Override
         public boolean hasNext() {
            return this.nextIndex() >= 0;
         }

         @Override
         public double next() {
            this.moveToNextIndex();
            return TObjectDoubleCustomHashMap.this._values[this._index];
         }

         @Override
         public void remove() {
            if (this._expectedSize != this._hash.size()) {
               throw new ConcurrentModificationException();
            } else {
               try {
                  this._hash.tempDisableAutoCompaction();
                  TObjectDoubleCustomHashMap.this.removeAt(this._index);
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
               Object[] set = TObjectDoubleCustomHashMap.this._set;
               int i = this._index;

               while(i-- > 0 && (set[i] == TCustomObjectHash.FREE || set[i] == TCustomObjectHash.REMOVED)) {
               }

               return i;
            }
         }
      }
   }

   class TObjectDoubleHashIterator<K> extends TObjectHashIterator<K> implements TObjectDoubleIterator<K> {
      private final TObjectDoubleCustomHashMap<K> _map;

      public TObjectDoubleHashIterator(TObjectDoubleCustomHashMap<K> map) {
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
      public double value() {
         return this._map._values[this._index];
      }

      @Override
      public double setValue(double val) {
         double old = this.value();
         this._map._values[this._index] = val;
         return old;
      }
   }
}
