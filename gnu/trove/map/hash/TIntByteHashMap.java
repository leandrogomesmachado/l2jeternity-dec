package gnu.trove.map.hash;

import gnu.trove.TByteCollection;
import gnu.trove.TIntCollection;
import gnu.trove.function.TByteFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TIntByteHash;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.iterator.TIntByteIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntByteMap;
import gnu.trove.procedure.TByteProcedure;
import gnu.trove.procedure.TIntByteProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

public class TIntByteHashMap extends TIntByteHash implements TIntByteMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient byte[] _values;

   public TIntByteHashMap() {
   }

   public TIntByteHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TIntByteHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TIntByteHashMap(int initialCapacity, float loadFactor, int noEntryKey, byte noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TIntByteHashMap(int[] keys, byte[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TIntByteHashMap(TIntByteMap map) {
      super(map.size());
      if (map instanceof TIntByteHashMap) {
         TIntByteHashMap hashmap = (TIntByteHashMap)map;
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
      this._values = new byte[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      int[] oldKeys = this._set;
      byte[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new int[newCapacity];
      this._values = new byte[newCapacity];
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            int o = oldKeys[i];
            int index = this.insertKey(o);
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public byte put(int key, byte value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public byte putIfAbsent(int key, byte value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private byte doPut(int key, byte value, int index) {
      byte previous = this.no_entry_value;
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
   public void putAll(Map<? extends Integer, ? extends Byte> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Integer, ? extends Byte> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TIntByteMap map) {
      this.ensureCapacity(map.size());
      TIntByteIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public byte get(int key) {
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
   public byte remove(int key) {
      byte prev = this.no_entry_value;
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
   public TIntSet keySet() {
      return new TIntByteHashMap.TKeyView();
   }

   @Override
   public int[] keys() {
      int[] keys = new int[this.size()];
      int[] k = this._set;
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
   public int[] keys(int[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new int[size];
      }

      int[] keys = this._set;
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
   public TByteCollection valueCollection() {
      return new TIntByteHashMap.TValueView();
   }

   @Override
   public byte[] values() {
      byte[] vals = new byte[this.size()];
      byte[] v = this._values;
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
   public byte[] values(byte[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new byte[size];
      }

      byte[] v = this._values;
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
   public boolean containsValue(byte val) {
      byte[] states = this._states;
      byte[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (states[i] == 1 && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(int key) {
      return this.contains(key);
   }

   @Override
   public TIntByteIterator iterator() {
      return new TIntByteHashMap.TIntByteHashIterator(this);
   }

   @Override
   public boolean forEachKey(TIntProcedure procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TByteProcedure procedure) {
      byte[] states = this._states;
      byte[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TIntByteProcedure procedure) {
      byte[] states = this._states;
      int[] keys = this._set;
      byte[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void transformValues(TByteFunction function) {
      byte[] states = this._states;
      byte[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean retainEntries(TIntByteProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      int[] keys = this._set;
      byte[] values = this._values;
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
   public boolean increment(int key) {
      return this.adjustValue(key, (byte)1);
   }

   @Override
   public boolean adjustValue(int key, byte amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public byte adjustOrPutValue(int key, byte adjust_amount, byte put_amount) {
      int index = this.insertKey(key);
      boolean isNewMapping;
      byte newValue;
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
      if (!(other instanceof TIntByteMap)) {
         return false;
      } else {
         TIntByteMap that = (TIntByteMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            byte[] values = this._values;
            byte[] states = this._states;
            byte this_no_entry_value = this.getNoEntryValue();
            byte that_no_entry_value = that.getNoEntryValue();
            int i = values.length;

            while(i-- > 0) {
               if (states[i] == 1) {
                  int key = this._set[i];
                  byte that_value = that.get(key);
                  byte this_value = values[i];
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
      this.forEachEntry(new TIntByteProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(int key, byte value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(", ");
            }

            buf.append(key);
            buf.append("=");
            buf.append((int)value);
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
            out.writeInt(this._set[i]);
            out.writeByte(this._values[i]);
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
         int key = in.readInt();
         byte val = in.readByte();
         this.put(key, val);
      }
   }

   class TIntByteHashIterator extends THashPrimitiveIterator implements TIntByteIterator {
      TIntByteHashIterator(TIntByteHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public int key() {
         return TIntByteHashMap.this._set[this._index];
      }

      @Override
      public byte value() {
         return TIntByteHashMap.this._values[this._index];
      }

      @Override
      public byte setValue(byte val) {
         byte old = this.value();
         TIntByteHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TIntByteHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TIntByteKeyHashIterator extends THashPrimitiveIterator implements TIntIterator {
      TIntByteKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public int next() {
         this.moveToNextIndex();
         return TIntByteHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TIntByteHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TIntByteValueHashIterator extends THashPrimitiveIterator implements TByteIterator {
      TIntByteValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public byte next() {
         this.moveToNextIndex();
         return TIntByteHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TIntByteHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   protected class TKeyView implements TIntSet {
      @Override
      public TIntIterator iterator() {
         return TIntByteHashMap.this.new TIntByteKeyHashIterator(TIntByteHashMap.this);
      }

      @Override
      public int getNoEntryValue() {
         return TIntByteHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TIntByteHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TIntByteHashMap.this._size;
      }

      @Override
      public boolean contains(int entry) {
         return TIntByteHashMap.this.contains(entry);
      }

      @Override
      public int[] toArray() {
         return TIntByteHashMap.this.keys();
      }

      @Override
      public int[] toArray(int[] dest) {
         return TIntByteHashMap.this.keys(dest);
      }

      @Override
      public boolean add(int entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(int entry) {
         return TIntByteHashMap.this.no_entry_value != TIntByteHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Integer)) {
               return false;
            }

            int ele = (Integer)element;
            if (!TIntByteHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TIntCollection collection) {
         TIntIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TIntByteHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(int[] array) {
         for(int element : array) {
            if (!TIntByteHashMap.this.contains(element)) {
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
         int[] set = TIntByteHashMap.this._set;
         byte[] states = TIntByteHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TIntByteHashMap.this.removeAt(i);
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
         TIntByteHashMap.this.clear();
      }

      @Override
      public boolean forEach(TIntProcedure procedure) {
         return TIntByteHashMap.this.forEachKey(procedure);
      }

      @Override
      public boolean equals(Object other) {
         if (!(other instanceof TIntSet)) {
            return false;
         } else {
            TIntSet that = (TIntSet)other;
            if (that.size() != this.size()) {
               return false;
            } else {
               int i = TIntByteHashMap.this._states.length;

               while(i-- > 0) {
                  if (TIntByteHashMap.this._states[i] == 1 && !that.contains(TIntByteHashMap.this._set[i])) {
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
         int i = TIntByteHashMap.this._states.length;

         while(i-- > 0) {
            if (TIntByteHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TIntByteHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TIntByteHashMap.this.forEachKey(new TIntProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(int key) {
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

   protected class TValueView implements TByteCollection {
      @Override
      public TByteIterator iterator() {
         return TIntByteHashMap.this.new TIntByteValueHashIterator(TIntByteHashMap.this);
      }

      @Override
      public byte getNoEntryValue() {
         return TIntByteHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TIntByteHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TIntByteHashMap.this._size;
      }

      @Override
      public boolean contains(byte entry) {
         return TIntByteHashMap.this.containsValue(entry);
      }

      @Override
      public byte[] toArray() {
         return TIntByteHashMap.this.values();
      }

      @Override
      public byte[] toArray(byte[] dest) {
         return TIntByteHashMap.this.values(dest);
      }

      @Override
      public boolean add(byte entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(byte entry) {
         byte[] values = TIntByteHashMap.this._values;
         int[] set = TIntByteHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0 && set[i] != 2 && entry == values[i]) {
               TIntByteHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Byte)) {
               return false;
            }

            byte ele = (Byte)element;
            if (!TIntByteHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TByteCollection collection) {
         TByteIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TIntByteHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(byte[] array) {
         for(byte element : array) {
            if (!TIntByteHashMap.this.containsValue(element)) {
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
         byte[] values = TIntByteHashMap.this._values;
         byte[] states = TIntByteHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TIntByteHashMap.this.removeAt(i);
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
         TIntByteHashMap.this.clear();
      }

      @Override
      public boolean forEach(TByteProcedure procedure) {
         return TIntByteHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TIntByteHashMap.this.forEachValue(new TByteProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(byte value) {
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
   }
}
