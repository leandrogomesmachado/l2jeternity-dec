package org.napile.primitive.lists.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.concurrent.locks.ReentrantLock;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.IntListIterator;
import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.abstracts.AbstractIntList;

public class CArrayIntList implements IntList, RandomAccess, Cloneable, Serializable {
   private final transient ReentrantLock lock = new ReentrantLock();
   private transient volatile int[] array;

   final int[] getArray() {
      return this.array;
   }

   final void setArray(int[] a) {
      this.array = a;
   }

   public CArrayIntList() {
      this.setArray(new int[0]);
   }

   public CArrayIntList(IntCollection c) {
      int[] elements = c.toArray();
      this.setArray(elements);
   }

   public CArrayIntList(int[] toCopyIn) {
      this.setArray(Arrays.copyOf(toCopyIn, toCopyIn.length));
   }

   @Override
   public int size() {
      return this.getArray().length;
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   private static boolean eq(int o1, int o2) {
      return o1 == o2;
   }

   private static int indexOf(int o, int[] elements, int index, int fence) {
      for(int i = index; i < fence; ++i) {
         if (o == elements[i]) {
            return i;
         }
      }

      return -1;
   }

   private static int lastIndexOf(int o, int[] elements, int index) {
      for(int i = index; i >= 0; --i) {
         if (o == elements[i]) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public boolean contains(int o) {
      int[] elements = this.getArray();
      return indexOf(o, elements, 0, elements.length) >= 0;
   }

   @Override
   public int indexOf(int o) {
      int[] elements = this.getArray();
      return indexOf(o, elements, 0, elements.length);
   }

   public int indexOf(int e, int index) {
      int[] elements = this.getArray();
      return indexOf(e, elements, index, elements.length);
   }

   @Override
   public int lastIndexOf(int o) {
      int[] elements = this.getArray();
      return lastIndexOf(o, elements, elements.length - 1);
   }

   public int lastIndexOf(int e, int index) {
      int[] elements = this.getArray();
      return lastIndexOf(e, elements, index);
   }

   @Override
   public Object clone() {
      try {
         return (CArrayIntList)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new InternalError();
      }
   }

   @Override
   public int[] toArray() {
      int[] elements = this.getArray();
      return Arrays.copyOf(elements, elements.length);
   }

   @Override
   public int[] toArray(int[] a) {
      int[] elements = this.getArray();
      int len = elements.length;
      if (a.length < len) {
         return Arrays.copyOf(elements, len);
      } else {
         System.arraycopy(elements, 0, a, 0, len);
         if (a.length > len) {
            a[len] = 0;
         }

         return a;
      }
   }

   @Override
   public int get(int index) {
      return this.getArray()[index];
   }

   @Override
   public int set(int index, int element) {
      ReentrantLock lock = this.lock;
      lock.lock();

      int len;
      try {
         int[] elements = this.getArray();
         int oldValue = elements[index];
         if (oldValue != element) {
            len = elements.length;
            int[] newElements = Arrays.copyOf(elements, len);
            newElements[index] = element;
            this.setArray(newElements);
         } else {
            this.setArray(elements);
         }

         len = oldValue;
      } finally {
         lock.unlock();
      }

      return len;
   }

   @Override
   public boolean add(int e) {
      ReentrantLock lock = this.lock;
      lock.lock();

      boolean var6;
      try {
         int[] elements = this.getArray();
         int len = elements.length;
         int[] newElements = Arrays.copyOf(elements, len + 1);
         newElements[len] = e;
         this.setArray(newElements);
         var6 = true;
      } finally {
         lock.unlock();
      }

      return var6;
   }

   @Override
   public void add(int index, int element) {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (index > len || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
         }

         int numMoved = len - index;
         int[] newElements;
         if (numMoved == 0) {
            newElements = Arrays.copyOf(elements, len + 1);
         } else {
            newElements = new int[len + 1];
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index, newElements, index + 1, numMoved);
         }

         newElements[index] = element;
         this.setArray(newElements);
      } finally {
         lock.unlock();
      }
   }

   @Override
   public int removeByIndex(int index) {
      ReentrantLock lock = this.lock;
      lock.lock();

      int var11;
      try {
         int[] elements = this.getArray();
         int len = elements.length;
         int oldValue = elements[index];
         int numMoved = len - index - 1;
         if (numMoved == 0) {
            this.setArray(Arrays.copyOf(elements, len - 1));
         } else {
            int[] newElements = new int[len - 1];
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index + 1, newElements, index, numMoved);
            this.setArray(newElements);
         }

         var11 = oldValue;
      } finally {
         lock.unlock();
      }

      return var11;
   }

   @Override
   public boolean remove(int o) {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (len != 0) {
            int newlen = len - 1;
            int[] newElements = new int[newlen];

            for(int i = 0; i < newlen; ++i) {
               if (eq(o, elements[i])) {
                  for(int k = i + 1; k < len; ++k) {
                     newElements[k - 1] = elements[k];
                  }

                  this.setArray(newElements);
                  return true;
               }

               newElements[i] = elements[i];
            }

            if (eq(o, elements[newlen])) {
               this.setArray(newElements);
               return true;
            }
         }

         return false;
      } finally {
         lock.unlock();
      }
   }

   private void removeRange(int fromIndex, int toIndex) {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (fromIndex < 0 || fromIndex >= len || toIndex > len || toIndex < fromIndex) {
            throw new IndexOutOfBoundsException();
         }

         int newlen = len - (toIndex - fromIndex);
         int numMoved = len - toIndex;
         if (numMoved == 0) {
            this.setArray(Arrays.copyOf(elements, newlen));
         } else {
            int[] newElements = new int[newlen];
            System.arraycopy(elements, 0, newElements, 0, fromIndex);
            System.arraycopy(elements, toIndex, newElements, fromIndex, numMoved);
            this.setArray(newElements);
         }
      } finally {
         lock.unlock();
      }
   }

   public boolean addIfAbsent(int e) {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         int[] elements = this.getArray();
         int len = elements.length;
         int[] newElements = new int[len + 1];

         for(int i = 0; i < len; ++i) {
            if (eq(e, elements[i])) {
               return false;
            }

            newElements[i] = elements[i];
         }

         newElements[len] = e;
         this.setArray(newElements);
         return true;
      } finally {
         lock.unlock();
      }
   }

   @Override
   public boolean containsAll(IntCollection c) {
      int[] elements = this.getArray();
      int len = elements.length;

      for(int e : c.toArray()) {
         if (indexOf(e, elements, 0, len) < 0) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean removeAll(IntCollection c) {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (len != 0) {
            int newlen = 0;
            int[] temp = new int[len];

            for(int i = 0; i < len; ++i) {
               int element = elements[i];
               if (!c.contains(element)) {
                  temp[newlen++] = element;
               }
            }

            if (newlen != len) {
               this.setArray(Arrays.copyOf(temp, newlen));
               return true;
            }
         }

         return false;
      } finally {
         lock.unlock();
      }
   }

   @Override
   public boolean retainAll(IntCollection c) {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (len != 0) {
            int newlen = 0;
            int[] temp = new int[len];

            for(int i = 0; i < len; ++i) {
               int element = elements[i];
               if (c.contains(element)) {
                  temp[newlen++] = element;
               }
            }

            if (newlen != len) {
               this.setArray(Arrays.copyOf(temp, newlen));
               return true;
            }
         }

         return false;
      } finally {
         lock.unlock();
      }
   }

   public int addAllAbsent(IntCollection c) {
      int[] cs = c.toArray();
      if (cs.length == 0) {
         return 0;
      } else {
         int[] uniq = new int[cs.length];
         ReentrantLock lock = this.lock;
         lock.lock();

         int var14;
         try {
            int[] elements = this.getArray();
            int len = elements.length;
            int added = 0;

            for(int i = 0; i < cs.length; ++i) {
               int e = cs[i];
               if (indexOf(e, elements, 0, len) < 0 && indexOf(e, uniq, 0, added) < 0) {
                  uniq[added++] = e;
               }
            }

            if (added > 0) {
               int[] newElements = Arrays.copyOf(elements, len + added);
               System.arraycopy(uniq, 0, newElements, len, added);
               this.setArray(newElements);
            }

            var14 = added;
         } finally {
            lock.unlock();
         }

         return var14;
      }
   }

   @Override
   public void clear() {
      ReentrantLock lock = this.lock;
      lock.lock();

      try {
         this.setArray(new int[0]);
      } finally {
         lock.unlock();
      }
   }

   @Override
   public boolean addAll(IntCollection c) {
      int[] cs = c.toArray();
      if (cs.length == 0) {
         return false;
      } else {
         ReentrantLock lock = this.lock;
         lock.lock();

         boolean var7;
         try {
            int[] elements = this.getArray();
            int len = elements.length;
            int[] newElements = Arrays.copyOf(elements, len + cs.length);
            System.arraycopy(cs, 0, newElements, len, cs.length);
            this.setArray(newElements);
            var7 = true;
         } finally {
            lock.unlock();
         }

         return var7;
      }
   }

   @Override
   public boolean addAll(int index, IntCollection c) {
      int[] cs = c.toArray();
      ReentrantLock lock = this.lock;
      lock.lock();

      int numMoved;
      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (index > len || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
         }

         if (cs.length != 0) {
            numMoved = len - index;
            int[] newElements;
            if (numMoved == 0) {
               newElements = Arrays.copyOf(elements, len + cs.length);
            } else {
               newElements = new int[len + cs.length];
               System.arraycopy(elements, 0, newElements, 0, index);
               System.arraycopy(elements, index, newElements, index + cs.length, numMoved);
            }

            System.arraycopy(cs, 0, newElements, index, cs.length);
            this.setArray(newElements);
            return true;
         }

         numMoved = 0;
      } finally {
         lock.unlock();
      }

      return (boolean)numMoved;
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      int[] elements = this.getArray();
      int len = elements.length;
      s.writeInt(len);

      for(int i = 0; i < len; ++i) {
         s.writeInt(elements[i]);
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int len = s.readInt();
      int[] elements = new int[len];

      for(int i = 0; i < len; ++i) {
         elements[i] = s.readInt();
      }

      this.setArray(elements);
   }

   @Override
   public String toString() {
      return Arrays.toString(this.getArray());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntList)) {
         return false;
      } else {
         IntList list = (IntList)o;
         IntIterator it = list.iterator();
         int[] elements = this.getArray();
         int len = elements.length;

         for(int i = 0; i < len; ++i) {
            if (!it.hasNext() || !eq(elements[i], it.next())) {
               return false;
            }
         }

         return !it.hasNext();
      }
   }

   @Override
   public int hashCode() {
      int hashCode = 1;
      int[] elements = this.getArray();
      int len = elements.length;

      for(int i = 0; i < len; ++i) {
         Object obj = elements[i];
         hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
      }

      return hashCode;
   }

   @Override
   public IntIterator iterator() {
      return new CArrayIntList.COWIterator(this.getArray(), 0);
   }

   @Override
   public IntListIterator listIterator() {
      return new CArrayIntList.COWIterator(this.getArray(), 0);
   }

   @Override
   public IntListIterator listIterator(int index) {
      int[] elements = this.getArray();
      int len = elements.length;
      if (index >= 0 && index <= len) {
         return new CArrayIntList.COWIterator(elements, index);
      } else {
         throw new IndexOutOfBoundsException("Index: " + index);
      }
   }

   @Override
   public IntList subList(int fromIndex, int toIndex) {
      ReentrantLock lock = this.lock;
      lock.lock();

      CArrayIntList.COWSubList var6;
      try {
         int[] elements = this.getArray();
         int len = elements.length;
         if (fromIndex < 0 || toIndex > len || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
         }

         var6 = new CArrayIntList.COWSubList(this, fromIndex, toIndex);
      } finally {
         lock.unlock();
      }

      return var6;
   }

   private static class COWIterator implements IntListIterator {
      private final int[] snapshot;
      private int cursor;

      private COWIterator(int[] elements, int initialCursor) {
         this.cursor = initialCursor;
         this.snapshot = elements;
      }

      @Override
      public boolean hasNext() {
         return this.cursor < this.snapshot.length;
      }

      @Override
      public boolean hasPrevious() {
         return this.cursor > 0;
      }

      @Override
      public int next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            return this.snapshot[this.cursor++];
         }
      }

      @Override
      public int previous() {
         if (!this.hasPrevious()) {
            throw new NoSuchElementException();
         } else {
            return this.snapshot[--this.cursor];
         }
      }

      @Override
      public int nextIndex() {
         return this.cursor;
      }

      @Override
      public int previousIndex() {
         return this.cursor - 1;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void set(int e) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void add(int e) {
         throw new UnsupportedOperationException();
      }
   }

   private static class COWSubList extends AbstractIntList {
      private final CArrayIntList l;
      private final int offset;
      private int size;
      private int[] expectedArray;

      private COWSubList(CArrayIntList list, int fromIndex, int toIndex) {
         this.l = list;
         this.expectedArray = this.l.getArray();
         this.offset = fromIndex;
         this.size = toIndex - fromIndex;
      }

      private void checkForComodification() {
         if (this.l.getArray() != this.expectedArray) {
            throw new ConcurrentModificationException();
         }
      }

      private void rangeCheck(int index) {
         if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + this.size);
         }
      }

      @Override
      public int set(int index, int element) {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         int var5;
         try {
            this.rangeCheck(index);
            this.checkForComodification();
            int x = this.l.set(index + this.offset, element);
            this.expectedArray = this.l.getArray();
            var5 = x;
         } finally {
            lock.unlock();
         }

         return var5;
      }

      @Override
      public int get(int index) {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         int var3;
         try {
            this.rangeCheck(index);
            this.checkForComodification();
            var3 = this.l.get(index + this.offset);
         } finally {
            lock.unlock();
         }

         return var3;
      }

      @Override
      public int size() {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         int var2;
         try {
            this.checkForComodification();
            var2 = this.size;
         } finally {
            lock.unlock();
         }

         return var2;
      }

      @Override
      public void add(int index, int element) {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         try {
            this.checkForComodification();
            if (index < 0 || index > this.size) {
               throw new IndexOutOfBoundsException();
            }

            this.l.add(index + this.offset, element);
            this.expectedArray = this.l.getArray();
            ++this.size;
         } finally {
            lock.unlock();
         }
      }

      @Override
      public void clear() {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         try {
            this.checkForComodification();
            this.l.removeRange(this.offset, this.offset + this.size);
            this.expectedArray = this.l.getArray();
            this.size = 0;
         } finally {
            lock.unlock();
         }
      }

      @Override
      public int removeByIndex(int index) {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         int var4;
         try {
            this.rangeCheck(index);
            this.checkForComodification();
            int result = this.l.removeByIndex(index + this.offset);
            this.expectedArray = this.l.getArray();
            --this.size;
            var4 = result;
         } finally {
            lock.unlock();
         }

         return var4;
      }

      @Override
      public IntIterator iterator() {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         CArrayIntList.COWSubListIterator var2;
         try {
            this.checkForComodification();
            var2 = new CArrayIntList.COWSubListIterator(this.l, 0, this.offset, this.size);
         } finally {
            lock.unlock();
         }

         return var2;
      }

      @Override
      public IntListIterator listIterator(int index) {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         CArrayIntList.COWSubListIterator var3;
         try {
            this.checkForComodification();
            if (index < 0 || index > this.size) {
               throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
            }

            var3 = new CArrayIntList.COWSubListIterator(this.l, index, this.offset, this.size);
         } finally {
            lock.unlock();
         }

         return var3;
      }

      @Override
      public IntList subList(int fromIndex, int toIndex) {
         ReentrantLock lock = this.l.lock;
         lock.lock();

         CArrayIntList.COWSubList var4;
         try {
            this.checkForComodification();
            if (fromIndex < 0 || toIndex > this.size) {
               throw new IndexOutOfBoundsException();
            }

            var4 = new CArrayIntList.COWSubList(this.l, fromIndex + this.offset, toIndex + this.offset);
         } finally {
            lock.unlock();
         }

         return var4;
      }
   }

   private static class COWSubListIterator implements IntListIterator {
      private final IntListIterator i;
      private final int index;
      private final int offset;
      private final int size;

      private COWSubListIterator(IntList l, int index, int offset, int size) {
         this.index = index;
         this.offset = offset;
         this.size = size;
         this.i = l.listIterator(index + offset);
      }

      @Override
      public boolean hasNext() {
         return this.nextIndex() < this.size;
      }

      @Override
      public int next() {
         if (this.hasNext()) {
            return this.i.next();
         } else {
            throw new NoSuchElementException();
         }
      }

      @Override
      public boolean hasPrevious() {
         return this.previousIndex() >= 0;
      }

      @Override
      public int previous() {
         if (this.hasPrevious()) {
            return this.i.previous();
         } else {
            throw new NoSuchElementException();
         }
      }

      @Override
      public int nextIndex() {
         return this.i.nextIndex() - this.offset;
      }

      @Override
      public int previousIndex() {
         return this.i.previousIndex() - this.offset;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void set(int e) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void add(int e) {
         throw new UnsupportedOperationException();
      }
   }
}
