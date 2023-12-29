package org.napile.primitive.lists.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.RandomAccess;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.lists.LongList;
import org.napile.primitive.lists.abstracts.AbstractLongList;

public class ArrayLongList extends AbstractLongList implements LongList, RandomAccess, Cloneable, Serializable {
   private transient long[] elementData;
   private int size;

   public ArrayLongList(int initialCapacity) {
      if (initialCapacity < 0) {
         throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
      } else {
         this.elementData = new long[initialCapacity];
      }
   }

   public ArrayLongList() {
      this(10);
   }

   public ArrayLongList(LongCollection c) {
      this.elementData = c.toArray();
      this.size = this.elementData.length;
   }

   public void trimToSize() {
      ++this.modCount;
      int oldCapacity = this.elementData.length;
      if (this.size < oldCapacity) {
         this.elementData = Arrays.copyOf(this.elementData, this.size);
      }
   }

   public void ensureCapacity(int minCapacity) {
      ++this.modCount;
      int oldCapacity = this.elementData.length;
      if (minCapacity > oldCapacity) {
         long[] oldData = this.elementData;
         int newCapacity = oldCapacity * 3 / 2 + 1;
         if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
         }

         this.elementData = Arrays.copyOf(this.elementData, newCapacity);
      }
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.size == 0;
   }

   @Override
   public boolean contains(long o) {
      return this.indexOf(o) >= 0;
   }

   @Override
   public int indexOf(long o) {
      for(int i = 0; i < this.size; ++i) {
         if (o == this.elementData[i]) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public int lastIndexOf(long o) {
      for(int i = this.size - 1; i >= 0; --i) {
         if (o == this.elementData[i]) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public Object clone() {
      try {
         ArrayLongList v = (ArrayLongList)super.clone();
         v.elementData = Arrays.copyOf(this.elementData, this.size);
         v.modCount = 0;
         return v;
      } catch (CloneNotSupportedException var2) {
         throw new InternalError();
      }
   }

   @Override
   public long[] toArray() {
      return Arrays.copyOf(this.elementData, this.size);
   }

   @Override
   public long[] toArray(long[] a) {
      if (a.length < this.size) {
         return Arrays.copyOf(this.elementData, this.size);
      } else {
         System.arraycopy(this.elementData, 0, a, 0, this.size);
         if (a.length > this.size) {
            a[this.size] = 0L;
         }

         return a;
      }
   }

   @Override
   public long get(int index) {
      this.RangeCheck(index);
      return this.elementData[index];
   }

   @Override
   public long set(int index, long element) {
      this.RangeCheck(index);
      long oldValue = this.elementData[index];
      this.elementData[index] = element;
      return oldValue;
   }

   @Override
   public boolean add(long e) {
      this.ensureCapacity(this.size + 1);
      this.elementData[this.size++] = e;
      return true;
   }

   @Override
   public void add(int index, long element) {
      if (index <= this.size && index >= 0) {
         this.ensureCapacity(this.size + 1);
         System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
         this.elementData[index] = element;
         ++this.size;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   @Override
   public long removeByIndex(int index) {
      this.RangeCheck(index);
      ++this.modCount;
      long oldValue = this.elementData[index];
      int numMoved = this.size - index - 1;
      if (numMoved > 0) {
         System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
      }

      this.elementData[--this.size] = 0L;
      return oldValue;
   }

   @Override
   public boolean remove(long o) {
      for(int index = 0; index < this.size; ++index) {
         if (o == this.elementData[index]) {
            this.fastRemove(index);
            return true;
         }
      }

      return false;
   }

   private void fastRemove(int index) {
      ++this.modCount;
      int numMoved = this.size - index - 1;
      if (numMoved > 0) {
         System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
      }

      this.elementData[--this.size] = 0L;
   }

   @Override
   public void clear() {
      ++this.modCount;

      for(int i = 0; i < this.size; ++i) {
         this.elementData[i] = 0L;
      }

      this.size = 0;
   }

   @Override
   public boolean addAll(LongCollection c) {
      long[] a = c.toArray();
      int numNew = a.length;
      this.ensureCapacity(this.size + numNew);
      System.arraycopy(a, 0, this.elementData, this.size, numNew);
      this.size += numNew;
      return numNew != 0;
   }

   @Override
   public boolean addAll(int index, LongCollection c) {
      if (index <= this.size && index >= 0) {
         long[] a = c.toArray();
         int numNew = a.length;
         this.ensureCapacity(this.size + numNew);
         int numMoved = this.size - index;
         if (numMoved > 0) {
            System.arraycopy(this.elementData, index, this.elementData, index + numNew, numMoved);
         }

         System.arraycopy(a, 0, this.elementData, index, numNew);
         this.size += numNew;
         return numNew != 0;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   @Override
   protected void removeRange(int fromIndex, int toIndex) {
      ++this.modCount;
      int numMoved = this.size - toIndex;
      System.arraycopy(this.elementData, toIndex, this.elementData, fromIndex, numMoved);
      int newSize = this.size - (toIndex - fromIndex);

      while(this.size != newSize) {
         this.elementData[--this.size] = 0L;
      }
   }

   private void RangeCheck(int index) {
      if (index >= this.size) {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      int expectedModCount = this.modCount;
      s.defaultWriteObject();
      s.writeInt(this.elementData.length);

      for(int i = 0; i < this.size; ++i) {
         s.writeLong(this.elementData[i]);
      }

      if (this.modCount != expectedModCount) {
         throw new ConcurrentModificationException();
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int arrayLength = s.readInt();
      long[] a = this.elementData = new long[arrayLength];

      for(int i = 0; i < this.size; ++i) {
         a[i] = s.readLong();
      }
   }
}
