package gnu.trove.map.hash;

import gnu.trove.TFloatCollection;
import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TFloatIntHash;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.iterator.TFloatIntIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TFloatIntMap;
import gnu.trove.procedure.TFloatIntProcedure;
import gnu.trove.procedure.TFloatProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TFloatSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

public class TFloatIntHashMap extends TFloatIntHash implements TFloatIntMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient int[] _values;

   public TFloatIntHashMap() {
   }

   public TFloatIntHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TFloatIntHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TFloatIntHashMap(int initialCapacity, float loadFactor, float noEntryKey, int noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TFloatIntHashMap(float[] keys, int[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TFloatIntHashMap(TFloatIntMap map) {
      super(map.size());
      if (map instanceof TFloatIntHashMap) {
         TFloatIntHashMap hashmap = (TFloatIntHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_key = hashmap.no_entry_key;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_key != 0.0F) {
            Arrays.fill(this._set, this.no_entry_key);
         }

         if (this.no_entry_value != 0) {
            Arrays.fill(this._values, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.putAll(map);
   }

   @Override
   protected int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = new int[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      float[] oldKeys = this._set;
      int[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new float[newCapacity];
      this._values = new int[newCapacity];
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            float o = oldKeys[i];
            int index = this.insertKey(o);
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public int put(float key, int value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public int putIfAbsent(float key, int value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private int doPut(float key, int value, int index) {
      int previous = this.no_entry_value;
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
   public void putAll(Map<? extends Float, ? extends Integer> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Float, ? extends Integer> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TFloatIntMap map) {
      this.ensureCapacity(map.size());
      TFloatIntIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public int get(float key) {
      int index = this.index(key);
      return index < 0 ? this.no_entry_value : this._values[index];
   }

   @Override
   public void clear() {
      super.clear();
      Arrays.fill(this._set, 0, this._set.length, this.no_entry_key);
      Arrays.fill(this._values, 0, this._values.length, this.no_entry_value);
      Arrays.fill(this._states, 0, this._states.length, (byte)0);
   }

   @Override
   public boolean isEmpty() {
      return 0 == this._size;
   }

   @Override
   public int remove(float key) {
      int prev = this.no_entry_value;
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
   public TFloatSet keySet() {
      return new TFloatIntHashMap.TKeyView();
   }

   @Override
   public float[] keys() {
      float[] keys = new float[this.size()];
      float[] k = this._set;
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
   public float[] keys(float[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new float[size];
      }

      float[] keys = this._set;
      byte[] states = this._states;
      int i = keys.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            array[j++] = keys[i];
         }
      }

      return array;
   }

   @Override
   public TIntCollection valueCollection() {
      return new TFloatIntHashMap.TValueView();
   }

   @Override
   public int[] values() {
      int[] vals = new int[this.size()];
      int[] v = this._values;
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
   public int[] values(int[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new int[size];
      }

      int[] v = this._values;
      byte[] states = this._states;
      int i = v.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            array[j++] = v[i];
         }
      }

      return array;
   }

   @Override
   public boolean containsValue(int val) {
      byte[] states = this._states;
      int[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (states[i] == 1 && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(float key) {
      return this.contains(key);
   }

   @Override
   public TFloatIntIterator iterator() {
      return new TFloatIntHashMap.TFloatIntHashIterator(this);
   }

   @Override
   public boolean forEachKey(TFloatProcedure procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TIntProcedure procedure) {
      byte[] states = this._states;
      int[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TFloatIntProcedure procedure) {
      byte[] states = this._states;
      float[] keys = this._set;
      int[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void transformValues(TIntFunction function) {
      byte[] states = this._states;
      int[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean retainEntries(TFloatIntProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      float[] keys = this._set;
      int[] values = this._values;
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
   public boolean increment(float key) {
      return this.adjustValue(key, 1);
   }

   @Override
   public boolean adjustValue(float key, int amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public int adjustOrPutValue(float key, int adjust_amount, int put_amount) {
      int index = this.insertKey(key);
      boolean isNewMapping;
      int newValue;
      if (index < 0) {
         index = -index - 1;
         newValue = this._values[index] += adjust_amount;
         isNewMapping = false;
      } else {
         newValue = this._values[index] = put_amount;
         isNewMapping = true;
      }

      byte previousState = this._states[index];
      if (isNewMapping) {
         this.postInsertHook(this.consumeFreeSlot);
      }

      return newValue;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TFloatIntMap)) {
         return false;
      } else {
         TFloatIntMap that = (TFloatIntMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            int[] values = this._values;
            byte[] states = this._states;
            int this_no_entry_value = this.getNoEntryValue();
            int that_no_entry_value = that.getNoEntryValue();
            int i = values.length;

            while(i-- > 0) {
               if (states[i] == 1) {
                  float key = this._set[i];
                  int that_value = that.get(key);
                  int this_value = values[i];
                  if (this_value != that_value && this_value != this_no_entry_value && that_value != that_no_entry_value) {
                     return false;
                  }
               }
            }

            return true;
         }
      }
   }

   @Override
   public int hashCode() {
      int hashcode = 0;
      byte[] states = this._states;
      int i = this._values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            hashcode += HashFunctions.hash(this._set[i]) ^ HashFunctions.hash(this._values[i]);
         }
      }

      return hashcode;
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEachEntry(new TFloatIntProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(float key, int value) {
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
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      super.writeExternal(out);
      out.writeInt(this._size);
      int i = this._states.length;

      while(i-- > 0) {
         if (this._states[i] == 1) {
            out.writeFloat(this._set[i]);
            out.writeInt(this._values[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      super.readExternal(in);
      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         float key = in.readFloat();
         int val = in.readInt();
         this.put(key, val);
      }
   }

   class TFloatIntHashIterator extends THashPrimitiveIterator implements TFloatIntIterator {
      TFloatIntHashIterator(TFloatIntHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public float key() {
         return TFloatIntHashMap.this._set[this._index];
      }

      @Override
      public int value() {
         return TFloatIntHashMap.this._values[this._index];
      }

      @Override
      public int setValue(int val) {
         int old = this.value();
         TFloatIntHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TFloatIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TFloatIntKeyHashIterator extends THashPrimitiveIterator implements TFloatIterator {
      TFloatIntKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public float next() {
         this.moveToNextIndex();
         return TFloatIntHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TFloatIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TFloatIntValueHashIterator extends THashPrimitiveIterator implements TIntIterator {
      TFloatIntValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public int next() {
         this.moveToNextIndex();
         return TFloatIntHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TFloatIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   protected class TKeyView implements TFloatSet {
      @Override
      public TFloatIterator iterator() {
         return TFloatIntHashMap.this.new TFloatIntKeyHashIterator(TFloatIntHashMap.this);
      }

      @Override
      public float getNoEntryValue() {
         return TFloatIntHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TFloatIntHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TFloatIntHashMap.this._size;
      }

      @Override
      public boolean contains(float entry) {
         return TFloatIntHashMap.this.contains(entry);
      }

      @Override
      public float[] toArray() {
         return TFloatIntHashMap.this.keys();
      }

      @Override
      public float[] toArray(float[] dest) {
         return TFloatIntHashMap.this.keys(dest);
      }

      @Override
      public boolean add(float entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(float entry) {
         return TFloatIntHashMap.this.no_entry_value != TFloatIntHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Float)) {
               return false;
            }

            float ele = (Float)element;
            if (!TFloatIntHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TFloatCollection collection) {
         TFloatIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TFloatIntHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(float[] array) {
         for(float element : array) {
            if (!TFloatIntHashMap.this.contains(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Float> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TFloatCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(float[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TFloatIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TFloatCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TFloatIterator iter = this.iterator();

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
      public boolean retainAll(float[] array) {
         boolean changed = false;
         Arrays.sort(array);
         float[] set = TFloatIntHashMap.this._set;
         byte[] states = TFloatIntHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TFloatIntHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Float) {
               float c = (Float)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TFloatCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TFloatIterator iter = collection.iterator();

            while(iter.hasNext()) {
               float element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(float[] array) {
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
         TFloatIntHashMap.this.clear();
      }

      @Override
      public boolean forEach(TFloatProcedure procedure) {
         return TFloatIntHashMap.this.forEachKey(procedure);
      }

      @Override
      public boolean equals(Object other) {
         if (!(other instanceof TFloatSet)) {
            return false;
         } else {
            TFloatSet that = (TFloatSet)other;
            if (that.size() != this.size()) {
               return false;
            } else {
               int i = TFloatIntHashMap.this._states.length;

               while(i-- > 0) {
                  if (TFloatIntHashMap.this._states[i] == 1 && !that.contains(TFloatIntHashMap.this._set[i])) {
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
         int i = TFloatIntHashMap.this._states.length;

         while(i-- > 0) {
            if (TFloatIntHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TFloatIntHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TFloatIntHashMap.this.forEachKey(new TFloatProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(float key) {
               if (this.first) {
                  this.first = false;
               } else {
                  buf.append(", ");
               }

               buf.append(key);
               return true;
            }
         });
         buf.append("}");
         return buf.toString();
      }
   }

   protected class TValueView implements TIntCollection {
      @Override
      public TIntIterator iterator() {
         return TFloatIntHashMap.this.new TFloatIntValueHashIterator(TFloatIntHashMap.this);
      }

      @Override
      public int getNoEntryValue() {
         return TFloatIntHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TFloatIntHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TFloatIntHashMap.this._size;
      }

      @Override
      public boolean contains(int entry) {
         return TFloatIntHashMap.this.containsValue(entry);
      }

      @Override
      public int[] toArray() {
         return TFloatIntHashMap.this.values();
      }

      @Override
      public int[] toArray(int[] dest) {
         return TFloatIntHashMap.this.values(dest);
      }

      @Override
      public boolean add(int entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(int entry) {
         int[] values = TFloatIntHashMap.this._values;
         float[] set = TFloatIntHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0.0F && set[i] != 2.0F && entry == values[i]) {
               TFloatIntHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Integer)) {
               return false;
            }

            int ele = (Integer)element;
            if (!TFloatIntHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TIntCollection collection) {
         TIntIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TFloatIntHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(int[] array) {
         for(int element : array) {
            if (!TFloatIntHashMap.this.containsValue(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Integer> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TIntCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(int[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TIntIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TIntCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TIntIterator iter = this.iterator();

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
      public boolean retainAll(int[] array) {
         boolean changed = false;
         Arrays.sort(array);
         int[] values = TFloatIntHashMap.this._values;
         byte[] states = TFloatIntHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TFloatIntHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Integer) {
               int c = (Integer)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TIntCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TIntIterator iter = collection.iterator();

            while(iter.hasNext()) {
               int element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(int[] array) {
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
         TFloatIntHashMap.this.clear();
      }

      @Override
      public boolean forEach(TIntProcedure procedure) {
         return TFloatIntHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TFloatIntHashMap.this.forEachValue(new TIntProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(int value) {
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
   }
}
