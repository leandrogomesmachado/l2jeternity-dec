package gnu.trove.map.hash;

import gnu.trove.TDoubleCollection;
import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TDoubleIntHash;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.iterator.TDoubleIntIterator;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TDoubleIntMap;
import gnu.trove.procedure.TDoubleIntProcedure;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TDoubleSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

public class TDoubleIntHashMap extends TDoubleIntHash implements TDoubleIntMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient int[] _values;

   public TDoubleIntHashMap() {
   }

   public TDoubleIntHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TDoubleIntHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TDoubleIntHashMap(int initialCapacity, float loadFactor, double noEntryKey, int noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TDoubleIntHashMap(double[] keys, int[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TDoubleIntHashMap(TDoubleIntMap map) {
      super(map.size());
      if (map instanceof TDoubleIntHashMap) {
         TDoubleIntHashMap hashmap = (TDoubleIntHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_key = hashmap.no_entry_key;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_key != 0.0) {
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
      double[] oldKeys = this._set;
      int[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new double[newCapacity];
      this._values = new int[newCapacity];
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            double o = oldKeys[i];
            int index = this.insertKey(o);
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public int put(double key, int value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public int putIfAbsent(double key, int value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private int doPut(double key, int value, int index) {
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
   public void putAll(Map<? extends Double, ? extends Integer> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Double, ? extends Integer> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TDoubleIntMap map) {
      this.ensureCapacity(map.size());
      TDoubleIntIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public int get(double key) {
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
   public int remove(double key) {
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
   public TDoubleSet keySet() {
      return new TDoubleIntHashMap.TKeyView();
   }

   @Override
   public double[] keys() {
      double[] keys = new double[this.size()];
      double[] k = this._set;
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
   public double[] keys(double[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new double[size];
      }

      double[] keys = this._set;
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
      return new TDoubleIntHashMap.TValueView();
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
   public boolean containsKey(double key) {
      return this.contains(key);
   }

   @Override
   public TDoubleIntIterator iterator() {
      return new TDoubleIntHashMap.TDoubleIntHashIterator(this);
   }

   @Override
   public boolean forEachKey(TDoubleProcedure procedure) {
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
   public boolean forEachEntry(TDoubleIntProcedure procedure) {
      byte[] states = this._states;
      double[] keys = this._set;
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
   public boolean retainEntries(TDoubleIntProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      double[] keys = this._set;
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
   public boolean increment(double key) {
      return this.adjustValue(key, 1);
   }

   @Override
   public boolean adjustValue(double key, int amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public int adjustOrPutValue(double key, int adjust_amount, int put_amount) {
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
      if (!(other instanceof TDoubleIntMap)) {
         return false;
      } else {
         TDoubleIntMap that = (TDoubleIntMap)other;
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
                  double key = this._set[i];
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
      this.forEachEntry(new TDoubleIntProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(double key, int value) {
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
            out.writeDouble(this._set[i]);
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
         double key = in.readDouble();
         int val = in.readInt();
         this.put(key, val);
      }
   }

   class TDoubleIntHashIterator extends THashPrimitiveIterator implements TDoubleIntIterator {
      TDoubleIntHashIterator(TDoubleIntHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public double key() {
         return TDoubleIntHashMap.this._set[this._index];
      }

      @Override
      public int value() {
         return TDoubleIntHashMap.this._values[this._index];
      }

      @Override
      public int setValue(int val) {
         int old = this.value();
         TDoubleIntHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TDoubleIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TDoubleIntKeyHashIterator extends THashPrimitiveIterator implements TDoubleIterator {
      TDoubleIntKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public double next() {
         this.moveToNextIndex();
         return TDoubleIntHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TDoubleIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TDoubleIntValueHashIterator extends THashPrimitiveIterator implements TIntIterator {
      TDoubleIntValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public int next() {
         this.moveToNextIndex();
         return TDoubleIntHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TDoubleIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   protected class TKeyView implements TDoubleSet {
      @Override
      public TDoubleIterator iterator() {
         return TDoubleIntHashMap.this.new TDoubleIntKeyHashIterator(TDoubleIntHashMap.this);
      }

      @Override
      public double getNoEntryValue() {
         return TDoubleIntHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TDoubleIntHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TDoubleIntHashMap.this._size;
      }

      @Override
      public boolean contains(double entry) {
         return TDoubleIntHashMap.this.contains(entry);
      }

      @Override
      public double[] toArray() {
         return TDoubleIntHashMap.this.keys();
      }

      @Override
      public double[] toArray(double[] dest) {
         return TDoubleIntHashMap.this.keys(dest);
      }

      @Override
      public boolean add(double entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(double entry) {
         return TDoubleIntHashMap.this.no_entry_value != TDoubleIntHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Double)) {
               return false;
            }

            double ele = (Double)element;
            if (!TDoubleIntHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TDoubleCollection collection) {
         TDoubleIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TDoubleIntHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(double[] array) {
         for(double element : array) {
            if (!TDoubleIntHashMap.this.contains(element)) {
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
         double[] set = TDoubleIntHashMap.this._set;
         byte[] states = TDoubleIntHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TDoubleIntHashMap.this.removeAt(i);
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
         TDoubleIntHashMap.this.clear();
      }

      @Override
      public boolean forEach(TDoubleProcedure procedure) {
         return TDoubleIntHashMap.this.forEachKey(procedure);
      }

      @Override
      public boolean equals(Object other) {
         if (!(other instanceof TDoubleSet)) {
            return false;
         } else {
            TDoubleSet that = (TDoubleSet)other;
            if (that.size() != this.size()) {
               return false;
            } else {
               int i = TDoubleIntHashMap.this._states.length;

               while(i-- > 0) {
                  if (TDoubleIntHashMap.this._states[i] == 1 && !that.contains(TDoubleIntHashMap.this._set[i])) {
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
         int i = TDoubleIntHashMap.this._states.length;

         while(i-- > 0) {
            if (TDoubleIntHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TDoubleIntHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TDoubleIntHashMap.this.forEachKey(new TDoubleProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(double key) {
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
         return TDoubleIntHashMap.this.new TDoubleIntValueHashIterator(TDoubleIntHashMap.this);
      }

      @Override
      public int getNoEntryValue() {
         return TDoubleIntHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TDoubleIntHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TDoubleIntHashMap.this._size;
      }

      @Override
      public boolean contains(int entry) {
         return TDoubleIntHashMap.this.containsValue(entry);
      }

      @Override
      public int[] toArray() {
         return TDoubleIntHashMap.this.values();
      }

      @Override
      public int[] toArray(int[] dest) {
         return TDoubleIntHashMap.this.values(dest);
      }

      @Override
      public boolean add(int entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(int entry) {
         int[] values = TDoubleIntHashMap.this._values;
         double[] set = TDoubleIntHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0.0 && set[i] != 2.0 && entry == values[i]) {
               TDoubleIntHashMap.this.removeAt(i);
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
            if (!TDoubleIntHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TIntCollection collection) {
         TIntIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TDoubleIntHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(int[] array) {
         for(int element : array) {
            if (!TDoubleIntHashMap.this.containsValue(element)) {
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
         int[] values = TDoubleIntHashMap.this._values;
         byte[] states = TDoubleIntHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TDoubleIntHashMap.this.removeAt(i);
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
         TDoubleIntHashMap.this.clear();
      }

      @Override
      public boolean forEach(TIntProcedure procedure) {
         return TDoubleIntHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TDoubleIntHashMap.this.forEachValue(new TIntProcedure() {
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
