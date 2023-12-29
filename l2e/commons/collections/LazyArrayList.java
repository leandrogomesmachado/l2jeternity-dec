package l2e.commons.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class LazyArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
   private static final long serialVersionUID = 8683452581122892189L;
   private static final int POOL_SIZE = Integer.parseInt(System.getProperty("lazyarraylist.poolsize", "-1"));
   private static final ObjectPool POOL = new GenericObjectPool(new LazyArrayList.PoolableLazyArrayListFactory(), POOL_SIZE, (byte)2, 0L, -1);
   private static final int L = 8;
   private static final int H = 1024;
   protected transient Object[] elementData;
   protected transient int size = 0;
   protected transient int capacity = 8;

   public static <E> LazyArrayList<E> newInstance() {
      try {
         return (LazyArrayList<E>)POOL.borrowObject();
      } catch (Exception var1) {
         var1.printStackTrace();
         return new LazyArrayList<>();
      }
   }

   public static <E> void recycle(LazyArrayList<E> obj) {
      try {
         POOL.returnObject(obj);
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public LazyArrayList(int initialCapacity) {
      if (initialCapacity < 1024) {
         while(this.capacity < initialCapacity) {
            this.capacity <<= 1;
         }
      } else {
         this.capacity = initialCapacity;
      }
   }

   public LazyArrayList() {
      this(8);
   }

   @Override
   public boolean add(E element) {
      this.ensureCapacity(this.size + 1);
      this.elementData[this.size++] = element;
      return true;
   }

   @Override
   public E set(int index, E element) {
      this.rangeCheck(index);
      E e = null;
      e = (E)this.elementData[index];
      this.elementData[index] = element;
      return e;
   }

   @Override
   public void add(int index, E element) {
      this.rangeCheck(index);
      this.ensureCapacity(this.size + 1);
      System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
      this.elementData[index] = element;
      ++this.size;
   }

   @Override
   public boolean addAll(int index, Collection<? extends E> c) {
      this.rangeCheck(index);
      if (c != null && !c.isEmpty()) {
         Object[] a = c.toArray();
         int numNew = a.length;
         this.ensureCapacity(this.size + numNew);
         int numMoved = this.size - index;
         if (numMoved > 0) {
            System.arraycopy(this.elementData, index, this.elementData, index + numNew, numMoved);
         }

         System.arraycopy(a, 0, this.elementData, index, numNew);
         this.size += numNew;
         return true;
      } else {
         return false;
      }
   }

   protected void ensureCapacity(int newSize) {
      if (newSize > this.capacity) {
         if (newSize < 1024) {
            while(this.capacity < newSize) {
               this.capacity <<= 1;
            }
         } else {
            while(this.capacity < newSize) {
               this.capacity = this.capacity * 3 / 2;
            }
         }

         Object[] elementDataResized = new Object[this.capacity];
         if (this.elementData != null) {
            System.arraycopy(this.elementData, 0, elementDataResized, 0, this.size);
         }

         this.elementData = elementDataResized;
      } else if (this.elementData == null) {
         this.elementData = new Object[this.capacity];
      }
   }

   protected void rangeCheck(int index) {
      if (index < 0 || index >= this.size) {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   @Override
   public E remove(int index) {
      this.rangeCheck(index);
      E e = null;
      --this.size;
      e = (E)this.elementData[index];
      this.elementData[index] = this.elementData[this.size];
      this.elementData[this.size] = null;
      this.trim();
      return e;
   }

   @Override
   public boolean remove(Object o) {
      if (this.size == 0) {
         return false;
      } else {
         int index = -1;

         for(int i = 0; i < this.size; ++i) {
            if (this.elementData[i] == o) {
               index = i;
               break;
            }
         }

         if (index == -1) {
            return false;
         } else {
            --this.size;
            this.elementData[index] = this.elementData[this.size];
            this.elementData[this.size] = null;
            this.trim();
            return true;
         }
      }
   }

   @Override
   public boolean contains(Object o) {
      if (this.size == 0) {
         return false;
      } else {
         for(int i = 0; i < this.size; ++i) {
            if (this.elementData[i] == o) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public int indexOf(Object o) {
      if (this.size == 0) {
         return -1;
      } else {
         int index = -1;

         for(int i = 0; i < this.size; ++i) {
            if (this.elementData[i] == o) {
               index = i;
               break;
            }
         }

         return index;
      }
   }

   @Override
   public int lastIndexOf(Object o) {
      if (this.size == 0) {
         return -1;
      } else {
         int index = -1;

         for(int i = 0; i < this.size; ++i) {
            if (this.elementData[i] == o) {
               index = i;
            }
         }

         return index;
      }
   }

   protected void trim() {
   }

   @Override
   public E get(int index) {
      this.rangeCheck(index);
      return (E)this.elementData[index];
   }

   @Override
   public Object clone() {
      LazyArrayList<E> clone = new LazyArrayList<>();
      if (this.size > 0) {
         clone.capacity = this.capacity;
         clone.elementData = new Object[this.elementData.length];
         System.arraycopy(this.elementData, 0, clone.elementData, 0, this.size);
      }

      return clone;
   }

   @Override
   public void clear() {
      if (this.size != 0) {
         for(int i = 0; i < this.size; ++i) {
            this.elementData[i] = null;
         }

         this.size = 0;
         this.trim();
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

   public int capacity() {
      return this.capacity;
   }

   @Override
   public boolean addAll(Collection<? extends E> c) {
      if (c != null && !c.isEmpty()) {
         Object[] a = c.toArray();
         int numNew = a.length;
         this.ensureCapacity(this.size + numNew);
         System.arraycopy(a, 0, this.elementData, this.size, numNew);
         this.size += numNew;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      if (c == null) {
         return false;
      } else if (c.isEmpty()) {
         return true;
      } else {
         Iterator<?> e = c.iterator();

         while(e.hasNext()) {
            if (!this.contains(e.next())) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      if (c == null) {
         return false;
      } else {
         boolean modified = false;
         Iterator<E> e = this.iterator();

         while(e.hasNext()) {
            if (!c.contains(e.next())) {
               e.remove();
               modified = true;
            }
         }

         return modified;
      }
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      if (c != null && !c.isEmpty()) {
         boolean modified = false;
         Iterator<?> e = this.iterator();

         while(e.hasNext()) {
            if (c.contains(e.next())) {
               e.remove();
               modified = true;
            }
         }

         return modified;
      } else {
         return false;
      }
   }

   @Override
   public Object[] toArray() {
      Object[] r = new Object[this.size];
      if (this.size > 0) {
         System.arraycopy(this.elementData, 0, r, 0, this.size);
      }

      return r;
   }

   @Override
   public <T> T[] toArray(T[] a) {
      T[] r = (T[])(a.length >= this.size ? a : (Object[])Array.newInstance(a.getClass().getComponentType(), this.size));
      if (this.size > 0) {
         System.arraycopy(this.elementData, 0, r, 0, this.size);
      }

      if (r.length > this.size) {
         r[this.size] = null;
      }

      return r;
   }

   @Override
   public Iterator<E> iterator() {
      return new LazyArrayList.LazyItr();
   }

   @Override
   public ListIterator<E> listIterator() {
      return new LazyArrayList.LazyListItr(0);
   }

   @Override
   public ListIterator<E> listIterator(int index) {
      return new LazyArrayList.LazyListItr(index);
   }

   @Override
   public String toString() {
      if (this.size == 0) {
         return "[]";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('[');

         for(int i = 0; i < this.size; ++i) {
            Object e = this.elementData[i];
            sb.append(e == this ? "this" : e);
            if (i == this.size - 1) {
               sb.append(']');
            } else {
               sb.append(", ");
            }
         }

         return sb.toString();
      }
   }

   @Override
   public List<E> subList(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException();
   }

   private class LazyItr implements Iterator<E> {
      int cursor = 0;
      int lastRet = -1;

      private LazyItr() {
      }

      @Override
      public boolean hasNext() {
         return this.cursor < LazyArrayList.this.size();
      }

      @Override
      public E next() {
         E next = LazyArrayList.this.get(this.cursor);
         this.lastRet = this.cursor++;
         return next;
      }

      @Override
      public void remove() {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            LazyArrayList.this.remove(this.lastRet);
            if (this.lastRet < this.cursor) {
               --this.cursor;
            }

            this.lastRet = -1;
         }
      }
   }

   private class LazyListItr extends LazyArrayList<E>.LazyItr implements ListIterator<E> {
      LazyListItr(int index) {
         this.cursor = index;
      }

      @Override
      public boolean hasPrevious() {
         return this.cursor > 0;
      }

      @Override
      public E previous() {
         int i = this.cursor - 1;
         E previous = LazyArrayList.this.get(i);
         this.lastRet = this.cursor = i;
         return previous;
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
      public void set(E e) {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            LazyArrayList.this.set(this.lastRet, e);
         }
      }

      @Override
      public void add(E e) {
         LazyArrayList.this.add(this.cursor++, e);
         this.lastRet = -1;
      }
   }

   private static class PoolableLazyArrayListFactory implements PoolableObjectFactory {
      private PoolableLazyArrayListFactory() {
      }

      @Override
      public Object makeObject() throws Exception {
         return new LazyArrayList();
      }

      @Override
      public void destroyObject(Object obj) throws Exception {
         ((LazyArrayList)obj).clear();
      }

      @Override
      public boolean validateObject(Object obj) {
         return true;
      }

      @Override
      public void activateObject(Object obj) throws Exception {
      }

      @Override
      public void passivateObject(Object obj) throws Exception {
         ((LazyArrayList)obj).clear();
      }
   }
}
