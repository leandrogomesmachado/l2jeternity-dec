package gnu.trove.map.hash;

import gnu.trove.TLongCollection;
import gnu.trove.TShortCollection;
import gnu.trove.function.TLongFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.impl.hash.TShortLongHash;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.iterator.TShortLongIterator;
import gnu.trove.map.TShortLongMap;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TShortLongProcedure;
import gnu.trove.procedure.TShortProcedure;
import gnu.trove.set.TShortSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

public class TShortLongHashMap extends TShortLongHash implements TShortLongMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient long[] _values;

   public TShortLongHashMap() {
   }

   public TShortLongHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TShortLongHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TShortLongHashMap(int initialCapacity, float loadFactor, short noEntryKey, long noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TShortLongHashMap(short[] keys, long[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TShortLongHashMap(TShortLongMap map) {
      super(map.size());
      if (map instanceof TShortLongHashMap) {
         TShortLongHashMap hashmap = (TShortLongHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_key = hashmap.no_entry_key;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_key != 0) {
            Arrays.fill(this._set, this.no_entry_key);
         }

         if (this.no_entry_value != 0L) {
            Arrays.fill(this._values, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.putAll(map);
   }

   @Override
   protected int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._values = new long[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      short[] oldKeys = this._set;
      long[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new short[newCapacity];
      this._values = new long[newCapacity];
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
   public long put(short key, long value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public long putIfAbsent(short key, long value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private long doPut(short key, long value, int index) {
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
   public void putAll(Map<? extends Short, ? extends Long> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Short, ? extends Long> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TShortLongMap map) {
      this.ensureCapacity(map.size());
      TShortLongIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public long get(short key) {
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
   public long remove(short key) {
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
   public TShortSet keySet() {
      return new TShortLongHashMap.TKeyView();
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
   public short[] keys(short[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new short[size];
      }

      short[] keys = this._set;
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
   public TLongCollection valueCollection() {
      return new TShortLongHashMap.TValueView();
   }

   @Override
   public long[] values() {
      long[] vals = new long[this.size()];
      long[] v = this._values;
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
   public long[] values(long[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new long[size];
      }

      long[] v = this._values;
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
   public boolean containsValue(long val) {
      byte[] states = this._states;
      long[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (states[i] == 1 && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(short key) {
      return this.contains(key);
   }

   @Override
   public TShortLongIterator iterator() {
      return new TShortLongHashMap.TShortLongHashIterator(this);
   }

   @Override
   public boolean forEachKey(TShortProcedure procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TLongProcedure procedure) {
      byte[] states = this._states;
      long[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TShortLongProcedure procedure) {
      byte[] states = this._states;
      short[] keys = this._set;
      long[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void transformValues(TLongFunction function) {
      byte[] states = this._states;
      long[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean retainEntries(TShortLongProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      short[] keys = this._set;
      long[] values = this._values;
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
   public boolean increment(short key) {
      return this.adjustValue(key, 1L);
   }

   @Override
   public boolean adjustValue(short key, long amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public long adjustOrPutValue(short key, long adjust_amount, long put_amount) {
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

      byte previousState = this._states[index];
      if (isNewMapping) {
         this.postInsertHook(this.consumeFreeSlot);
      }

      return newValue;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TShortLongMap)) {
         return false;
      } else {
         TShortLongMap that = (TShortLongMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            long[] values = this._values;
            byte[] states = this._states;
            long this_no_entry_value = this.getNoEntryValue();
            long that_no_entry_value = that.getNoEntryValue();
            int i = values.length;

            while(i-- > 0) {
               if (states[i] == 1) {
                  short key = this._set[i];
                  long that_value = that.get(key);
                  long this_value = values[i];
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
      this.forEachEntry(new TShortLongProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(short key, long value) {
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
            out.writeShort(this._set[i]);
            out.writeLong(this._values[i]);
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
         short key = in.readShort();
         long val = in.readLong();
         this.put(key, val);
      }
   }

   protected class TKeyView implements TShortSet {
      @Override
      public TShortIterator iterator() {
         return TShortLongHashMap.this.new TShortLongKeyHashIterator(TShortLongHashMap.this);
      }

      @Override
      public short getNoEntryValue() {
         return TShortLongHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TShortLongHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TShortLongHashMap.this._size;
      }

      @Override
      public boolean contains(short entry) {
         return TShortLongHashMap.this.contains(entry);
      }

      @Override
      public short[] toArray() {
         return TShortLongHashMap.this.keys();
      }

      @Override
      public short[] toArray(short[] dest) {
         return TShortLongHashMap.this.keys(dest);
      }

      @Override
      public boolean add(short entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(short entry) {
         return TShortLongHashMap.this.no_entry_value != TShortLongHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Short)) {
               return false;
            }

            short ele = (Short)element;
            if (!TShortLongHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TShortCollection collection) {
         TShortIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TShortLongHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(short[] array) {
         for(short element : array) {
            if (!TShortLongHashMap.this.contains(element)) {
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
         short[] set = TShortLongHashMap.this._set;
         byte[] states = TShortLongHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TShortLongHashMap.this.removeAt(i);
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
         TShortLongHashMap.this.clear();
      }

      @Override
      public boolean forEach(TShortProcedure procedure) {
         return TShortLongHashMap.this.forEachKey(procedure);
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
               int i = TShortLongHashMap.this._states.length;

               while(i-- > 0) {
                  if (TShortLongHashMap.this._states[i] == 1 && !that.contains(TShortLongHashMap.this._set[i])) {
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
         int i = TShortLongHashMap.this._states.length;

         while(i-- > 0) {
            if (TShortLongHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TShortLongHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TShortLongHashMap.this.forEachKey(new TShortProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(short key) {
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

   class TShortLongHashIterator extends THashPrimitiveIterator implements TShortLongIterator {
      TShortLongHashIterator(TShortLongHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public short key() {
         return TShortLongHashMap.this._set[this._index];
      }

      @Override
      public long value() {
         return TShortLongHashMap.this._values[this._index];
      }

      @Override
      public long setValue(long val) {
         long old = this.value();
         TShortLongHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TShortLongHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TShortLongKeyHashIterator extends THashPrimitiveIterator implements TShortIterator {
      TShortLongKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public short next() {
         this.moveToNextIndex();
         return TShortLongHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TShortLongHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TShortLongValueHashIterator extends THashPrimitiveIterator implements TLongIterator {
      TShortLongValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public long next() {
         this.moveToNextIndex();
         return TShortLongHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TShortLongHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   protected class TValueView implements TLongCollection {
      @Override
      public TLongIterator iterator() {
         return TShortLongHashMap.this.new TShortLongValueHashIterator(TShortLongHashMap.this);
      }

      @Override
      public long getNoEntryValue() {
         return TShortLongHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TShortLongHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TShortLongHashMap.this._size;
      }

      @Override
      public boolean contains(long entry) {
         return TShortLongHashMap.this.containsValue(entry);
      }

      @Override
      public long[] toArray() {
         return TShortLongHashMap.this.values();
      }

      @Override
      public long[] toArray(long[] dest) {
         return TShortLongHashMap.this.values(dest);
      }

      @Override
      public boolean add(long entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(long entry) {
         long[] values = TShortLongHashMap.this._values;
         short[] set = TShortLongHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0 && set[i] != 2 && entry == values[i]) {
               TShortLongHashMap.this.removeAt(i);
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
            if (!TShortLongHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TLongCollection collection) {
         TLongIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TShortLongHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(long[] array) {
         for(long element : array) {
            if (!TShortLongHashMap.this.containsValue(element)) {
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
         long[] values = TShortLongHashMap.this._values;
         byte[] states = TShortLongHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TShortLongHashMap.this.removeAt(i);
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
         TShortLongHashMap.this.clear();
      }

      @Override
      public boolean forEach(TLongProcedure procedure) {
         return TShortLongHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TShortLongHashMap.this.forEachValue(new TLongProcedure() {
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
   }
}
