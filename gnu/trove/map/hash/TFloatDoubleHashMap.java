package gnu.trove.map.hash;

import gnu.trove.TDoubleCollection;
import gnu.trove.TFloatCollection;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TFloatDoubleHash;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.map.TFloatDoubleMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TFloatDoubleProcedure;
import gnu.trove.procedure.TFloatProcedure;
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

public class TFloatDoubleHashMap extends TFloatDoubleHash implements TFloatDoubleMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient double[] _values;

   public TFloatDoubleHashMap() {
   }

   public TFloatDoubleHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TFloatDoubleHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TFloatDoubleHashMap(int initialCapacity, float loadFactor, float noEntryKey, double noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TFloatDoubleHashMap(float[] keys, double[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TFloatDoubleHashMap(TFloatDoubleMap map) {
      super(map.size());
      if (map instanceof TFloatDoubleHashMap) {
         TFloatDoubleHashMap hashmap = (TFloatDoubleHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_key = hashmap.no_entry_key;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_key != 0.0F) {
            Arrays.fill(this._set, this.no_entry_key);
         }

         if (this.no_entry_value != 0.0) {
            Arrays.fill(this._values, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.putAll(map);
   }

   @Override
   protected int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = new double[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      float[] oldKeys = this._set;
      double[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new float[newCapacity];
      this._values = new double[newCapacity];
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
   public double put(float key, double value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public double putIfAbsent(float key, double value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private double doPut(float key, double value, int index) {
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
   public void putAll(Map<? extends Float, ? extends Double> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Float, ? extends Double> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TFloatDoubleMap map) {
      this.ensureCapacity(map.size());
      TFloatDoubleIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public double get(float key) {
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
   public double remove(float key) {
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
   public TFloatSet keySet() {
      return new TFloatDoubleHashMap.TKeyView();
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
   public TDoubleCollection valueCollection() {
      return new TFloatDoubleHashMap.TValueView();
   }

   @Override
   public double[] values() {
      double[] vals = new double[this.size()];
      double[] v = this._values;
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
   public double[] values(double[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new double[size];
      }

      double[] v = this._values;
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
   public boolean containsValue(double val) {
      byte[] states = this._states;
      double[] vals = this._values;
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
   public TFloatDoubleIterator iterator() {
      return new TFloatDoubleHashMap.TFloatDoubleHashIterator(this);
   }

   @Override
   public boolean forEachKey(TFloatProcedure procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TDoubleProcedure procedure) {
      byte[] states = this._states;
      double[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TFloatDoubleProcedure procedure) {
      byte[] states = this._states;
      float[] keys = this._set;
      double[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void transformValues(TDoubleFunction function) {
      byte[] states = this._states;
      double[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean retainEntries(TFloatDoubleProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      float[] keys = this._set;
      double[] values = this._values;
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
      return this.adjustValue(key, 1.0);
   }

   @Override
   public boolean adjustValue(float key, double amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public double adjustOrPutValue(float key, double adjust_amount, double put_amount) {
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

      byte previousState = this._states[index];
      if (isNewMapping) {
         this.postInsertHook(this.consumeFreeSlot);
      }

      return newValue;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TFloatDoubleMap)) {
         return false;
      } else {
         TFloatDoubleMap that = (TFloatDoubleMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            double[] values = this._values;
            byte[] states = this._states;
            double this_no_entry_value = this.getNoEntryValue();
            double that_no_entry_value = that.getNoEntryValue();
            int i = values.length;

            while(i-- > 0) {
               if (states[i] == 1) {
                  float key = this._set[i];
                  double that_value = that.get(key);
                  double this_value = values[i];
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
      this.forEachEntry(new TFloatDoubleProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(float key, double value) {
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
            out.writeDouble(this._values[i]);
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
         double val = in.readDouble();
         this.put(key, val);
      }
   }

   class TFloatDoubleHashIterator extends THashPrimitiveIterator implements TFloatDoubleIterator {
      TFloatDoubleHashIterator(TFloatDoubleHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public float key() {
         return TFloatDoubleHashMap.this._set[this._index];
      }

      @Override
      public double value() {
         return TFloatDoubleHashMap.this._values[this._index];
      }

      @Override
      public double setValue(double val) {
         double old = this.value();
         TFloatDoubleHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TFloatDoubleHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TFloatDoubleKeyHashIterator extends THashPrimitiveIterator implements TFloatIterator {
      TFloatDoubleKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public float next() {
         this.moveToNextIndex();
         return TFloatDoubleHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TFloatDoubleHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TFloatDoubleValueHashIterator extends THashPrimitiveIterator implements TDoubleIterator {
      TFloatDoubleValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public double next() {
         this.moveToNextIndex();
         return TFloatDoubleHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TFloatDoubleHashMap.this.removeAt(this._index);
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
         return TFloatDoubleHashMap.this.new TFloatDoubleKeyHashIterator(TFloatDoubleHashMap.this);
      }

      @Override
      public float getNoEntryValue() {
         return TFloatDoubleHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TFloatDoubleHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TFloatDoubleHashMap.this._size;
      }

      @Override
      public boolean contains(float entry) {
         return TFloatDoubleHashMap.this.contains(entry);
      }

      @Override
      public float[] toArray() {
         return TFloatDoubleHashMap.this.keys();
      }

      @Override
      public float[] toArray(float[] dest) {
         return TFloatDoubleHashMap.this.keys(dest);
      }

      @Override
      public boolean add(float entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(float entry) {
         return TFloatDoubleHashMap.this.no_entry_value != TFloatDoubleHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Float)) {
               return false;
            }

            float ele = (Float)element;
            if (!TFloatDoubleHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TFloatCollection collection) {
         TFloatIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TFloatDoubleHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(float[] array) {
         for(float element : array) {
            if (!TFloatDoubleHashMap.this.contains(element)) {
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
         float[] set = TFloatDoubleHashMap.this._set;
         byte[] states = TFloatDoubleHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TFloatDoubleHashMap.this.removeAt(i);
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
         TFloatDoubleHashMap.this.clear();
      }

      @Override
      public boolean forEach(TFloatProcedure procedure) {
         return TFloatDoubleHashMap.this.forEachKey(procedure);
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
               int i = TFloatDoubleHashMap.this._states.length;

               while(i-- > 0) {
                  if (TFloatDoubleHashMap.this._states[i] == 1 && !that.contains(TFloatDoubleHashMap.this._set[i])) {
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
         int i = TFloatDoubleHashMap.this._states.length;

         while(i-- > 0) {
            if (TFloatDoubleHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TFloatDoubleHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TFloatDoubleHashMap.this.forEachKey(new TFloatProcedure() {
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

   protected class TValueView implements TDoubleCollection {
      @Override
      public TDoubleIterator iterator() {
         return TFloatDoubleHashMap.this.new TFloatDoubleValueHashIterator(TFloatDoubleHashMap.this);
      }

      @Override
      public double getNoEntryValue() {
         return TFloatDoubleHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TFloatDoubleHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TFloatDoubleHashMap.this._size;
      }

      @Override
      public boolean contains(double entry) {
         return TFloatDoubleHashMap.this.containsValue(entry);
      }

      @Override
      public double[] toArray() {
         return TFloatDoubleHashMap.this.values();
      }

      @Override
      public double[] toArray(double[] dest) {
         return TFloatDoubleHashMap.this.values(dest);
      }

      @Override
      public boolean add(double entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(double entry) {
         double[] values = TFloatDoubleHashMap.this._values;
         float[] set = TFloatDoubleHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0.0F && set[i] != 2.0F && entry == values[i]) {
               TFloatDoubleHashMap.this.removeAt(i);
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
            if (!TFloatDoubleHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TDoubleCollection collection) {
         TDoubleIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TFloatDoubleHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(double[] array) {
         for(double element : array) {
            if (!TFloatDoubleHashMap.this.containsValue(element)) {
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
         double[] values = TFloatDoubleHashMap.this._values;
         byte[] states = TFloatDoubleHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TFloatDoubleHashMap.this.removeAt(i);
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
         TFloatDoubleHashMap.this.clear();
      }

      @Override
      public boolean forEach(TDoubleProcedure procedure) {
         return TFloatDoubleHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TFloatDoubleHashMap.this.forEachValue(new TDoubleProcedure() {
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
   }
}
