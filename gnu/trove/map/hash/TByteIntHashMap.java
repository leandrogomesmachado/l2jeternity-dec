package gnu.trove.map.hash;

import gnu.trove.TByteCollection;
import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TByteIntHash;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.iterator.TByteIntIterator;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TByteIntMap;
import gnu.trove.procedure.TByteIntProcedure;
import gnu.trove.procedure.TByteProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TByteSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

public class TByteIntHashMap extends TByteIntHash implements TByteIntMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient int[] _values;

   public TByteIntHashMap() {
   }

   public TByteIntHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TByteIntHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TByteIntHashMap(int initialCapacity, float loadFactor, byte noEntryKey, int noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TByteIntHashMap(byte[] keys, int[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TByteIntHashMap(TByteIntMap map) {
      super(map.size());
      if (map instanceof TByteIntHashMap) {
         TByteIntHashMap hashmap = (TByteIntHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_key = hashmap.no_entry_key;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_key != 0) {
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
      byte[] oldKeys = this._set;
      int[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new byte[newCapacity];
      this._values = new int[newCapacity];
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            byte o = oldKeys[i];
            int index = this.insertKey(o);
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public int put(byte key, int value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public int putIfAbsent(byte key, int value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private int doPut(byte key, int value, int index) {
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
   public void putAll(Map<? extends Byte, ? extends Integer> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Byte, ? extends Integer> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TByteIntMap map) {
      this.ensureCapacity(map.size());
      TByteIntIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public int get(byte key) {
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
   public int remove(byte key) {
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
   public TByteSet keySet() {
      return new TByteIntHashMap.TKeyView();
   }

   @Override
   public byte[] keys() {
      byte[] keys = new byte[this.size()];
      byte[] k = this._set;
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
   public byte[] keys(byte[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new byte[size];
      }

      byte[] keys = this._set;
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
      return new TByteIntHashMap.TValueView();
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
   public boolean containsKey(byte key) {
      return this.contains(key);
   }

   @Override
   public TByteIntIterator iterator() {
      return new TByteIntHashMap.TByteIntHashIterator(this);
   }

   @Override
   public boolean forEachKey(TByteProcedure procedure) {
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
   public boolean forEachEntry(TByteIntProcedure procedure) {
      byte[] states = this._states;
      byte[] keys = this._set;
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
   public boolean retainEntries(TByteIntProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      byte[] keys = this._set;
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
   public boolean increment(byte key) {
      return this.adjustValue(key, 1);
   }

   @Override
   public boolean adjustValue(byte key, int amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public int adjustOrPutValue(byte key, int adjust_amount, int put_amount) {
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
      if (!(other instanceof TByteIntMap)) {
         return false;
      } else {
         TByteIntMap that = (TByteIntMap)other;
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
                  byte key = this._set[i];
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
      this.forEachEntry(new TByteIntProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(byte key, int value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(", ");
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

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      super.writeExternal(out);
      out.writeInt(this._size);
      int i = this._states.length;

      while(i-- > 0) {
         if (this._states[i] == 1) {
            out.writeByte(this._set[i]);
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
         byte key = in.readByte();
         int val = in.readInt();
         this.put(key, val);
      }
   }

   class TByteIntHashIterator extends THashPrimitiveIterator implements TByteIntIterator {
      TByteIntHashIterator(TByteIntHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public byte key() {
         return TByteIntHashMap.this._set[this._index];
      }

      @Override
      public int value() {
         return TByteIntHashMap.this._values[this._index];
      }

      @Override
      public int setValue(int val) {
         int old = this.value();
         TByteIntHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TByteIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TByteIntKeyHashIterator extends THashPrimitiveIterator implements TByteIterator {
      TByteIntKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public byte next() {
         this.moveToNextIndex();
         return TByteIntHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TByteIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TByteIntValueHashIterator extends THashPrimitiveIterator implements TIntIterator {
      TByteIntValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public int next() {
         this.moveToNextIndex();
         return TByteIntHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TByteIntHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   protected class TKeyView implements TByteSet {
      @Override
      public TByteIterator iterator() {
         return TByteIntHashMap.this.new TByteIntKeyHashIterator(TByteIntHashMap.this);
      }

      @Override
      public byte getNoEntryValue() {
         return TByteIntHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TByteIntHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TByteIntHashMap.this._size;
      }

      @Override
      public boolean contains(byte entry) {
         return TByteIntHashMap.this.contains(entry);
      }

      @Override
      public byte[] toArray() {
         return TByteIntHashMap.this.keys();
      }

      @Override
      public byte[] toArray(byte[] dest) {
         return TByteIntHashMap.this.keys(dest);
      }

      @Override
      public boolean add(byte entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(byte entry) {
         return TByteIntHashMap.this.no_entry_value != TByteIntHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Byte)) {
               return false;
            }

            byte ele = (Byte)element;
            if (!TByteIntHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TByteCollection collection) {
         TByteIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TByteIntHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(byte[] array) {
         for(byte element : array) {
            if (!TByteIntHashMap.this.contains(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Byte> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TByteCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(byte[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TByteIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TByteCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TByteIterator iter = this.iterator();

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
      public boolean retainAll(byte[] array) {
         boolean changed = false;
         Arrays.sort(array);
         byte[] set = TByteIntHashMap.this._set;
         byte[] states = TByteIntHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TByteIntHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Byte) {
               byte c = (Byte)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TByteCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TByteIterator iter = collection.iterator();

            while(iter.hasNext()) {
               byte element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(byte[] array) {
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
         TByteIntHashMap.this.clear();
      }

      @Override
      public boolean forEach(TByteProcedure procedure) {
         return TByteIntHashMap.this.forEachKey(procedure);
      }

      @Override
      public boolean equals(Object other) {
         if (!(other instanceof TByteSet)) {
            return false;
         } else {
            TByteSet that = (TByteSet)other;
            if (that.size() != this.size()) {
               return false;
            } else {
               int i = TByteIntHashMap.this._states.length;

               while(i-- > 0) {
                  if (TByteIntHashMap.this._states[i] == 1 && !that.contains(TByteIntHashMap.this._set[i])) {
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
         int i = TByteIntHashMap.this._states.length;

         while(i-- > 0) {
            if (TByteIntHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TByteIntHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TByteIntHashMap.this.forEachKey(new TByteProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(byte key) {
               if (this.first) {
                  this.first = false;
               } else {
                  buf.append(", ");
               }

               buf.append((int)key);
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
         return TByteIntHashMap.this.new TByteIntValueHashIterator(TByteIntHashMap.this);
      }

      @Override
      public int getNoEntryValue() {
         return TByteIntHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TByteIntHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TByteIntHashMap.this._size;
      }

      @Override
      public boolean contains(int entry) {
         return TByteIntHashMap.this.containsValue(entry);
      }

      @Override
      public int[] toArray() {
         return TByteIntHashMap.this.values();
      }

      @Override
      public int[] toArray(int[] dest) {
         return TByteIntHashMap.this.values(dest);
      }

      @Override
      public boolean add(int entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(int entry) {
         int[] values = TByteIntHashMap.this._values;
         byte[] set = TByteIntHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0 && set[i] != 2 && entry == values[i]) {
               TByteIntHashMap.this.removeAt(i);
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
            if (!TByteIntHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TIntCollection collection) {
         TIntIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TByteIntHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(int[] array) {
         for(int element : array) {
            if (!TByteIntHashMap.this.containsValue(element)) {
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
         int[] values = TByteIntHashMap.this._values;
         byte[] states = TByteIntHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TByteIntHashMap.this.removeAt(i);
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
         TByteIntHashMap.this.clear();
      }

      @Override
      public boolean forEach(TIntProcedure procedure) {
         return TByteIntHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TByteIntHashMap.this.forEachValue(new TIntProcedure() {
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
