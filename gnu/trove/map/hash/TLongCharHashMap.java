package gnu.trove.map.hash;

import gnu.trove.TCharCollection;
import gnu.trove.TLongCollection;
import gnu.trove.function.TCharFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.impl.hash.TLongCharHash;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.iterator.TLongCharIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongCharMap;
import gnu.trove.procedure.TCharProcedure;
import gnu.trove.procedure.TLongCharProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.TLongSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

public class TLongCharHashMap extends TLongCharHash implements TLongCharMap, Externalizable {
   static final long serialVersionUID = 1L;
   protected transient char[] _values;

   public TLongCharHashMap() {
   }

   public TLongCharHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   public TLongCharHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TLongCharHashMap(int initialCapacity, float loadFactor, long noEntryKey, char noEntryValue) {
      super(initialCapacity, loadFactor, noEntryKey, noEntryValue);
   }

   public TLongCharHashMap(long[] keys, char[] values) {
      super(Math.max(keys.length, values.length));
      int size = Math.min(keys.length, values.length);

      for(int i = 0; i < size; ++i) {
         this.put(keys[i], values[i]);
      }
   }

   public TLongCharHashMap(TLongCharMap map) {
      super(map.size());
      if (map instanceof TLongCharHashMap) {
         TLongCharHashMap hashmap = (TLongCharHashMap)map;
         this._loadFactor = hashmap._loadFactor;
         this.no_entry_key = hashmap.no_entry_key;
         this.no_entry_value = hashmap.no_entry_value;
         if (this.no_entry_key != 0L) {
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
      this._values = new char[capacity];
      return capacity;
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      long[] oldKeys = this._set;
      char[] oldVals = this._values;
      byte[] oldStates = this._states;
      this._set = new long[newCapacity];
      this._values = new char[newCapacity];
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            long o = oldKeys[i];
            int index = this.insertKey(o);
            this._values[index] = oldVals[i];
         }
      }
   }

   @Override
   public char put(long key, char value) {
      int index = this.insertKey(key);
      return this.doPut(key, value, index);
   }

   @Override
   public char putIfAbsent(long key, char value) {
      int index = this.insertKey(key);
      return index < 0 ? this._values[-index - 1] : this.doPut(key, value, index);
   }

   private char doPut(long key, char value, int index) {
      char previous = this.no_entry_value;
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
   public void putAll(Map<? extends Long, ? extends Character> map) {
      this.ensureCapacity(map.size());

      for(Entry<? extends Long, ? extends Character> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void putAll(TLongCharMap map) {
      this.ensureCapacity(map.size());
      TLongCharIterator iter = map.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.put(iter.key(), iter.value());
      }
   }

   @Override
   public char get(long key) {
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
   public char remove(long key) {
      char prev = this.no_entry_value;
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
   public TLongSet keySet() {
      return new TLongCharHashMap.TKeyView();
   }

   @Override
   public long[] keys() {
      long[] keys = new long[this.size()];
      long[] k = this._set;
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
   public long[] keys(long[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new long[size];
      }

      long[] keys = this._set;
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
   public TCharCollection valueCollection() {
      return new TLongCharHashMap.TValueView();
   }

   @Override
   public char[] values() {
      char[] vals = new char[this.size()];
      char[] v = this._values;
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
   public char[] values(char[] array) {
      int size = this.size();
      if (array.length < size) {
         array = new char[size];
      }

      char[] v = this._values;
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
   public boolean containsValue(char val) {
      byte[] states = this._states;
      char[] vals = this._values;
      int i = vals.length;

      while(i-- > 0) {
         if (states[i] == 1 && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsKey(long key) {
      return this.contains(key);
   }

   @Override
   public TLongCharIterator iterator() {
      return new TLongCharHashMap.TLongCharHashIterator(this);
   }

   @Override
   public boolean forEachKey(TLongProcedure procedure) {
      return this.forEach(procedure);
   }

   @Override
   public boolean forEachValue(TCharProcedure procedure) {
      byte[] states = this._states;
      char[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachEntry(TLongCharProcedure procedure) {
      byte[] states = this._states;
      long[] keys = this._set;
      char[] values = this._values;
      int i = keys.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void transformValues(TCharFunction function) {
      byte[] states = this._states;
      char[] values = this._values;
      int i = values.length;

      while(i-- > 0) {
         if (states[i] == 1) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   @Override
   public boolean retainEntries(TLongCharProcedure procedure) {
      boolean modified = false;
      byte[] states = this._states;
      long[] keys = this._set;
      char[] values = this._values;
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
   public boolean increment(long key) {
      return this.adjustValue(key, '\u0001');
   }

   @Override
   public boolean adjustValue(long key, char amount) {
      int index = this.index(key);
      if (index < 0) {
         return false;
      } else {
         this._values[index] += amount;
         return true;
      }
   }

   @Override
   public char adjustOrPutValue(long key, char adjust_amount, char put_amount) {
      int index = this.insertKey(key);
      boolean isNewMapping;
      char newValue;
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
      if (!(other instanceof TLongCharMap)) {
         return false;
      } else {
         TLongCharMap that = (TLongCharMap)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            char[] values = this._values;
            byte[] states = this._states;
            char this_no_entry_value = this.getNoEntryValue();
            char that_no_entry_value = that.getNoEntryValue();
            int i = values.length;

            while(i-- > 0) {
               if (states[i] == 1) {
                  long key = this._set[i];
                  char that_value = that.get(key);
                  char this_value = values[i];
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
      this.forEachEntry(new TLongCharProcedure() {
         private boolean first = true;

         @Override
         public boolean execute(long key, char value) {
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
            out.writeLong(this._set[i]);
            out.writeChar(this._values[i]);
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
         long key = in.readLong();
         char val = in.readChar();
         this.put(key, val);
      }
   }

   protected class TKeyView implements TLongSet {
      @Override
      public TLongIterator iterator() {
         return TLongCharHashMap.this.new TLongCharKeyHashIterator(TLongCharHashMap.this);
      }

      @Override
      public long getNoEntryValue() {
         return TLongCharHashMap.this.no_entry_key;
      }

      @Override
      public int size() {
         return TLongCharHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TLongCharHashMap.this._size;
      }

      @Override
      public boolean contains(long entry) {
         return TLongCharHashMap.this.contains(entry);
      }

      @Override
      public long[] toArray() {
         return TLongCharHashMap.this.keys();
      }

      @Override
      public long[] toArray(long[] dest) {
         return TLongCharHashMap.this.keys(dest);
      }

      @Override
      public boolean add(long entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(long entry) {
         return TLongCharHashMap.this.no_entry_value != TLongCharHashMap.this.remove(entry);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Long)) {
               return false;
            }

            long ele = (Long)element;
            if (!TLongCharHashMap.this.containsKey(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TLongCollection collection) {
         TLongIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TLongCharHashMap.this.containsKey(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(long[] array) {
         for(long element : array) {
            if (!TLongCharHashMap.this.contains(element)) {
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
         long[] set = TLongCharHashMap.this._set;
         byte[] states = TLongCharHashMap.this._states;
         int i = set.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
               TLongCharHashMap.this.removeAt(i);
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
         TLongCharHashMap.this.clear();
      }

      @Override
      public boolean forEach(TLongProcedure procedure) {
         return TLongCharHashMap.this.forEachKey(procedure);
      }

      @Override
      public boolean equals(Object other) {
         if (!(other instanceof TLongSet)) {
            return false;
         } else {
            TLongSet that = (TLongSet)other;
            if (that.size() != this.size()) {
               return false;
            } else {
               int i = TLongCharHashMap.this._states.length;

               while(i-- > 0) {
                  if (TLongCharHashMap.this._states[i] == 1 && !that.contains(TLongCharHashMap.this._set[i])) {
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
         int i = TLongCharHashMap.this._states.length;

         while(i-- > 0) {
            if (TLongCharHashMap.this._states[i] == 1) {
               hashcode += HashFunctions.hash(TLongCharHashMap.this._set[i]);
            }
         }

         return hashcode;
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TLongCharHashMap.this.forEachKey(new TLongProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(long key) {
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

   class TLongCharHashIterator extends THashPrimitiveIterator implements TLongCharIterator {
      TLongCharHashIterator(TLongCharHashMap map) {
         super(map);
      }

      @Override
      public void advance() {
         this.moveToNextIndex();
      }

      @Override
      public long key() {
         return TLongCharHashMap.this._set[this._index];
      }

      @Override
      public char value() {
         return TLongCharHashMap.this._values[this._index];
      }

      @Override
      public char setValue(char val) {
         char old = this.value();
         TLongCharHashMap.this._values[this._index] = val;
         return old;
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TLongCharHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TLongCharKeyHashIterator extends THashPrimitiveIterator implements TLongIterator {
      TLongCharKeyHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public long next() {
         this.moveToNextIndex();
         return TLongCharHashMap.this._set[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TLongCharHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   class TLongCharValueHashIterator extends THashPrimitiveIterator implements TCharIterator {
      TLongCharValueHashIterator(TPrimitiveHash hash) {
         super(hash);
      }

      @Override
      public char next() {
         this.moveToNextIndex();
         return TLongCharHashMap.this._values[this._index];
      }

      @Override
      public void remove() {
         if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
         } else {
            try {
               this._hash.tempDisableAutoCompaction();
               TLongCharHashMap.this.removeAt(this._index);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }

            --this._expectedSize;
         }
      }
   }

   protected class TValueView implements TCharCollection {
      @Override
      public TCharIterator iterator() {
         return TLongCharHashMap.this.new TLongCharValueHashIterator(TLongCharHashMap.this);
      }

      @Override
      public char getNoEntryValue() {
         return TLongCharHashMap.this.no_entry_value;
      }

      @Override
      public int size() {
         return TLongCharHashMap.this._size;
      }

      @Override
      public boolean isEmpty() {
         return 0 == TLongCharHashMap.this._size;
      }

      @Override
      public boolean contains(char entry) {
         return TLongCharHashMap.this.containsValue(entry);
      }

      @Override
      public char[] toArray() {
         return TLongCharHashMap.this.values();
      }

      @Override
      public char[] toArray(char[] dest) {
         return TLongCharHashMap.this.values(dest);
      }

      @Override
      public boolean add(char entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(char entry) {
         char[] values = TLongCharHashMap.this._values;
         long[] set = TLongCharHashMap.this._set;
         int i = values.length;

         while(i-- > 0) {
            if (set[i] != 0L && set[i] != 2L && entry == values[i]) {
               TLongCharHashMap.this.removeAt(i);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Character)) {
               return false;
            }

            char ele = (Character)element;
            if (!TLongCharHashMap.this.containsValue(ele)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(TCharCollection collection) {
         TCharIterator iter = collection.iterator();

         while(iter.hasNext()) {
            if (!TLongCharHashMap.this.containsValue(iter.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean containsAll(char[] array) {
         for(char element : array) {
            if (!TLongCharHashMap.this.containsValue(element)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean addAll(Collection<? extends Character> collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(TCharCollection collection) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(char[] array) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         TCharIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(TCharCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            TCharIterator iter = this.iterator();

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
      public boolean retainAll(char[] array) {
         boolean changed = false;
         Arrays.sort(array);
         char[] values = TLongCharHashMap.this._values;
         byte[] states = TLongCharHashMap.this._states;
         int i = values.length;

         while(i-- > 0) {
            if (states[i] == 1 && Arrays.binarySearch(array, values[i]) < 0) {
               TLongCharHashMap.this.removeAt(i);
               changed = true;
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Character) {
               char c = (Character)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      @Override
      public boolean removeAll(TCharCollection collection) {
         if (this == collection) {
            this.clear();
            return true;
         } else {
            boolean changed = false;
            TCharIterator iter = collection.iterator();

            while(iter.hasNext()) {
               char element = iter.next();
               if (this.remove(element)) {
                  changed = true;
               }
            }

            return changed;
         }
      }

      @Override
      public boolean removeAll(char[] array) {
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
         TLongCharHashMap.this.clear();
      }

      @Override
      public boolean forEach(TCharProcedure procedure) {
         return TLongCharHashMap.this.forEachValue(procedure);
      }

      @Override
      public String toString() {
         final StringBuilder buf = new StringBuilder("{");
         TLongCharHashMap.this.forEachValue(new TCharProcedure() {
            private boolean first = true;

            @Override
            public boolean execute(char value) {
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
