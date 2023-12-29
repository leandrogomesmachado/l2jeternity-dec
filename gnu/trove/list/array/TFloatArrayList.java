package gnu.trove.list.array;

import gnu.trove.TFloatCollection;
import gnu.trove.function.TFloatFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.procedure.TFloatProcedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Random;

public class TFloatArrayList implements TFloatList, Externalizable {
   static final long serialVersionUID = 1L;
   protected float[] _data;
   protected int _pos;
   protected static final int DEFAULT_CAPACITY = 10;
   protected float no_entry_value;

   public TFloatArrayList() {
      this(10, 0.0F);
   }

   public TFloatArrayList(int capacity) {
      this(capacity, 0.0F);
   }

   public TFloatArrayList(int capacity, float no_entry_value) {
      this._data = new float[capacity];
      this._pos = 0;
      this.no_entry_value = no_entry_value;
   }

   public TFloatArrayList(TFloatCollection collection) {
      this(collection.size());
      this.addAll(collection);
   }

   public TFloatArrayList(float[] values) {
      this(values.length);
      this.add(values);
   }

   protected TFloatArrayList(float[] values, float no_entry_value, boolean wrap) {
      if (!wrap) {
         throw new IllegalStateException("Wrong call");
      } else if (values == null) {
         throw new IllegalArgumentException("values can not be null");
      } else {
         this._data = values;
         this._pos = values.length;
         this.no_entry_value = no_entry_value;
      }
   }

   public static TFloatArrayList wrap(float[] values) {
      return wrap(values, 0.0F);
   }

   public static TFloatArrayList wrap(float[] values, float no_entry_value) {
      return new TFloatArrayList(values, no_entry_value, true) {
         @Override
         public void ensureCapacity(int capacity) {
            if (capacity > this._data.length) {
               throw new IllegalStateException("Can not grow ArrayList wrapped external array");
            }
         }
      };
   }

   @Override
   public float getNoEntryValue() {
      return this.no_entry_value;
   }

   public void ensureCapacity(int capacity) {
      if (capacity > this._data.length) {
         int newCap = Math.max(this._data.length << 1, capacity);
         float[] tmp = new float[newCap];
         System.arraycopy(this._data, 0, tmp, 0, this._data.length);
         this._data = tmp;
      }
   }

   @Override
   public int size() {
      return this._pos;
   }

   @Override
   public boolean isEmpty() {
      return this._pos == 0;
   }

   public void trimToSize() {
      if (this._data.length > this.size()) {
         float[] tmp = new float[this.size()];
         this.toArray(tmp, 0, tmp.length);
         this._data = tmp;
      }
   }

   @Override
   public boolean add(float val) {
      this.ensureCapacity(this._pos + 1);
      this._data[this._pos++] = val;
      return true;
   }

   @Override
   public void add(float[] vals) {
      this.add(vals, 0, vals.length);
   }

   @Override
   public void add(float[] vals, int offset, int length) {
      this.ensureCapacity(this._pos + length);
      System.arraycopy(vals, offset, this._data, this._pos, length);
      this._pos += length;
   }

   @Override
   public void insert(int offset, float value) {
      if (offset == this._pos) {
         this.add(value);
      } else {
         this.ensureCapacity(this._pos + 1);
         System.arraycopy(this._data, offset, this._data, offset + 1, this._pos - offset);
         this._data[offset] = value;
         ++this._pos;
      }
   }

   @Override
   public void insert(int offset, float[] values) {
      this.insert(offset, values, 0, values.length);
   }

   @Override
   public void insert(int offset, float[] values, int valOffset, int len) {
      if (offset == this._pos) {
         this.add(values, valOffset, len);
      } else {
         this.ensureCapacity(this._pos + len);
         System.arraycopy(this._data, offset, this._data, offset + len, this._pos - offset);
         System.arraycopy(values, valOffset, this._data, offset, len);
         this._pos += len;
      }
   }

   @Override
   public float get(int offset) {
      if (offset >= this._pos) {
         throw new ArrayIndexOutOfBoundsException(offset);
      } else {
         return this._data[offset];
      }
   }

   public float getQuick(int offset) {
      return this._data[offset];
   }

   @Override
   public float set(int offset, float val) {
      if (offset >= this._pos) {
         throw new ArrayIndexOutOfBoundsException(offset);
      } else {
         float prev_val = this._data[offset];
         this._data[offset] = val;
         return prev_val;
      }
   }

   @Override
   public float replace(int offset, float val) {
      if (offset >= this._pos) {
         throw new ArrayIndexOutOfBoundsException(offset);
      } else {
         float old = this._data[offset];
         this._data[offset] = val;
         return old;
      }
   }

   @Override
   public void set(int offset, float[] values) {
      this.set(offset, values, 0, values.length);
   }

   @Override
   public void set(int offset, float[] values, int valOffset, int length) {
      if (offset >= 0 && offset + length <= this._pos) {
         System.arraycopy(values, valOffset, this._data, offset, length);
      } else {
         throw new ArrayIndexOutOfBoundsException(offset);
      }
   }

   public void setQuick(int offset, float val) {
      this._data[offset] = val;
   }

   @Override
   public void clear() {
      this.clear(10);
   }

   public void clear(int capacity) {
      this._data = new float[capacity];
      this._pos = 0;
   }

   public void reset() {
      this._pos = 0;
      Arrays.fill(this._data, this.no_entry_value);
   }

   public void resetQuick() {
      this._pos = 0;
   }

   @Override
   public boolean remove(float value) {
      for(int index = 0; index < this._pos; ++index) {
         if (value == this._data[index]) {
            this.remove(index, 1);
            return true;
         }
      }

      return false;
   }

   @Override
   public float removeAt(int offset) {
      float old = this.get(offset);
      this.remove(offset, 1);
      return old;
   }

   @Override
   public void remove(int offset, int length) {
      if (length != 0) {
         if (offset >= 0 && offset < this._pos) {
            if (offset == 0) {
               System.arraycopy(this._data, length, this._data, 0, this._pos - length);
            } else if (this._pos - length != offset) {
               System.arraycopy(this._data, offset + length, this._data, offset, this._pos - (offset + length));
            }

            this._pos -= length;
         } else {
            throw new ArrayIndexOutOfBoundsException(offset);
         }
      }
   }

   @Override
   public TFloatIterator iterator() {
      return new TFloatArrayList.TFloatArrayIterator(0);
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      for(Object element : collection) {
         if (!(element instanceof Float)) {
            return false;
         }

         float c = (Float)element;
         if (!this.contains(c)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean containsAll(TFloatCollection collection) {
      if (this == collection) {
         return true;
      } else {
         TFloatIterator iter = collection.iterator();

         while(iter.hasNext()) {
            float element = iter.next();
            if (!this.contains(element)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(float[] array) {
      int i = array.length;

      while(i-- > 0) {
         if (!this.contains(array[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection<? extends Float> collection) {
      boolean changed = false;

      for(Float element : collection) {
         float e = element;
         if (this.add(e)) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean addAll(TFloatCollection collection) {
      boolean changed = false;
      TFloatIterator iter = collection.iterator();

      while(iter.hasNext()) {
         float element = iter.next();
         if (this.add(element)) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean addAll(float[] array) {
      boolean changed = false;

      for(float element : array) {
         if (this.add(element)) {
            changed = true;
         }
      }

      return changed;
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
      float[] data = this._data;
      int i = this._pos;

      while(i-- > 0) {
         if (Arrays.binarySearch(array, data[i]) < 0) {
            this.remove(i, 1);
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
      if (collection == this) {
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
   public void transformValues(TFloatFunction function) {
      int i = this._pos;

      while(i-- > 0) {
         this._data[i] = function.execute(this._data[i]);
      }
   }

   @Override
   public void reverse() {
      this.reverse(0, this._pos);
   }

   @Override
   public void reverse(int from, int to) {
      if (from != to) {
         if (from > to) {
            throw new IllegalArgumentException("from cannot be greater than to");
         } else {
            int i = from;

            for(int j = to - 1; i < j; --j) {
               this.swap(i, j);
               ++i;
            }
         }
      }
   }

   @Override
   public void shuffle(Random rand) {
      int i = this._pos;

      while(i-- > 1) {
         this.swap(i, rand.nextInt(i));
      }
   }

   private void swap(int i, int j) {
      float tmp = this._data[i];
      this._data[i] = this._data[j];
      this._data[j] = tmp;
   }

   @Override
   public TFloatList subList(int begin, int end) {
      if (end < begin) {
         throw new IllegalArgumentException("end index " + end + " greater than begin index " + begin);
      } else if (begin < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (end > this._data.length) {
         throw new IndexOutOfBoundsException("end index < " + this._data.length);
      } else {
         TFloatArrayList list = new TFloatArrayList(end - begin);

         for(int i = begin; i < end; ++i) {
            list.add(this._data[i]);
         }

         return list;
      }
   }

   @Override
   public float[] toArray() {
      return this.toArray(0, this._pos);
   }

   @Override
   public float[] toArray(int offset, int len) {
      float[] rv = new float[len];
      this.toArray(rv, offset, len);
      return rv;
   }

   @Override
   public float[] toArray(float[] dest) {
      int len = dest.length;
      if (dest.length > this._pos) {
         len = this._pos;
         dest[len] = this.no_entry_value;
      }

      this.toArray(dest, 0, len);
      return dest;
   }

   @Override
   public float[] toArray(float[] dest, int offset, int len) {
      if (len == 0) {
         return dest;
      } else if (offset >= 0 && offset < this._pos) {
         System.arraycopy(this._data, offset, dest, 0, len);
         return dest;
      } else {
         throw new ArrayIndexOutOfBoundsException(offset);
      }
   }

   @Override
   public float[] toArray(float[] dest, int source_pos, int dest_pos, int len) {
      if (len == 0) {
         return dest;
      } else if (source_pos >= 0 && source_pos < this._pos) {
         System.arraycopy(this._data, source_pos, dest, dest_pos, len);
         return dest;
      } else {
         throw new ArrayIndexOutOfBoundsException(source_pos);
      }
   }

   @Override
   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else if (other instanceof TFloatArrayList) {
         TFloatArrayList that = (TFloatArrayList)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            int i = this._pos;

            while(i-- > 0) {
               if (this._data[i] != that._data[i]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int h = 0;
      int i = this._pos;

      while(i-- > 0) {
         h += HashFunctions.hash(this._data[i]);
      }

      return h;
   }

   @Override
   public boolean forEach(TFloatProcedure procedure) {
      for(int i = 0; i < this._pos; ++i) {
         if (!procedure.execute(this._data[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachDescending(TFloatProcedure procedure) {
      int i = this._pos;

      while(i-- > 0) {
         if (!procedure.execute(this._data[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void sort() {
      Arrays.sort(this._data, 0, this._pos);
   }

   @Override
   public void sort(int fromIndex, int toIndex) {
      Arrays.sort(this._data, fromIndex, toIndex);
   }

   @Override
   public void fill(float val) {
      Arrays.fill(this._data, 0, this._pos, val);
   }

   @Override
   public void fill(int fromIndex, int toIndex, float val) {
      if (toIndex > this._pos) {
         this.ensureCapacity(toIndex);
         this._pos = toIndex;
      }

      Arrays.fill(this._data, fromIndex, toIndex, val);
   }

   @Override
   public int binarySearch(float value) {
      return this.binarySearch(value, 0, this._pos);
   }

   @Override
   public int binarySearch(float value, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new ArrayIndexOutOfBoundsException(fromIndex);
      } else if (toIndex > this._pos) {
         throw new ArrayIndexOutOfBoundsException(toIndex);
      } else {
         int low = fromIndex;
         int high = toIndex - 1;

         while(low <= high) {
            int mid = low + high >>> 1;
            float midVal = this._data[mid];
            if (midVal < value) {
               low = mid + 1;
            } else {
               if (!(midVal > value)) {
                  return mid;
               }

               high = mid - 1;
            }
         }

         return -(low + 1);
      }
   }

   @Override
   public int indexOf(float value) {
      return this.indexOf(0, value);
   }

   @Override
   public int indexOf(int offset, float value) {
      for(int i = offset; i < this._pos; ++i) {
         if (this._data[i] == value) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public int lastIndexOf(float value) {
      return this.lastIndexOf(this._pos, value);
   }

   @Override
   public int lastIndexOf(int offset, float value) {
      int i = offset;

      while(i-- > 0) {
         if (this._data[i] == value) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public boolean contains(float value) {
      return this.lastIndexOf(value) >= 0;
   }

   @Override
   public TFloatList grep(TFloatProcedure condition) {
      TFloatArrayList list = new TFloatArrayList();

      for(int i = 0; i < this._pos; ++i) {
         if (condition.execute(this._data[i])) {
            list.add(this._data[i]);
         }
      }

      return list;
   }

   @Override
   public TFloatList inverseGrep(TFloatProcedure condition) {
      TFloatArrayList list = new TFloatArrayList();

      for(int i = 0; i < this._pos; ++i) {
         if (!condition.execute(this._data[i])) {
            list.add(this._data[i]);
         }
      }

      return list;
   }

   @Override
   public float max() {
      if (this.size() == 0) {
         throw new IllegalStateException("cannot find maximum of an empty list");
      } else {
         float max = Float.NEGATIVE_INFINITY;

         for(int i = 0; i < this._pos; ++i) {
            if (this._data[i] > max) {
               max = this._data[i];
            }
         }

         return max;
      }
   }

   @Override
   public float min() {
      if (this.size() == 0) {
         throw new IllegalStateException("cannot find minimum of an empty list");
      } else {
         float min = Float.POSITIVE_INFINITY;

         for(int i = 0; i < this._pos; ++i) {
            if (this._data[i] < min) {
               min = this._data[i];
            }
         }

         return min;
      }
   }

   @Override
   public float sum() {
      float sum = 0.0F;

      for(int i = 0; i < this._pos; ++i) {
         sum += this._data[i];
      }

      return sum;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      int i = 0;

      for(int end = this._pos - 1; i < end; ++i) {
         buf.append(this._data[i]);
         buf.append(", ");
      }

      if (this.size() > 0) {
         buf.append(this._data[this._pos - 1]);
      }

      buf.append("}");
      return buf.toString();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeInt(this._pos);
      out.writeFloat(this.no_entry_value);
      int len = this._data.length;
      out.writeInt(len);

      for(int i = 0; i < len; ++i) {
         out.writeFloat(this._data[i]);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._pos = in.readInt();
      this.no_entry_value = in.readFloat();
      int len = in.readInt();
      this._data = new float[len];

      for(int i = 0; i < len; ++i) {
         this._data[i] = in.readFloat();
      }
   }

   class TFloatArrayIterator implements TFloatIterator {
      private int cursor = 0;
      int lastRet = -1;

      TFloatArrayIterator(int index) {
         this.cursor = index;
      }

      @Override
      public boolean hasNext() {
         return this.cursor < TFloatArrayList.this.size();
      }

      @Override
      public float next() {
         try {
            float next = TFloatArrayList.this.get(this.cursor);
            this.lastRet = this.cursor++;
            return next;
         } catch (IndexOutOfBoundsException var2) {
            throw new NoSuchElementException();
         }
      }

      @Override
      public void remove() {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            try {
               TFloatArrayList.this.remove(this.lastRet, 1);
               if (this.lastRet < this.cursor) {
                  --this.cursor;
               }

               this.lastRet = -1;
            } catch (IndexOutOfBoundsException var2) {
               throw new ConcurrentModificationException();
            }
         }
      }
   }
}
