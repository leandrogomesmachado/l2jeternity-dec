package org.napile.primitive.lists.abstracts;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.collections.abstracts.AbstractIntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.IntListIterator;
import org.napile.primitive.lists.IntList;

public abstract class AbstractIntList extends AbstractIntCollection implements IntList {
   protected transient int modCount = 0;

   protected AbstractIntList() {
   }

   @Override
   public boolean add(int e) {
      this.add(this.size(), e);
      return true;
   }

   @Override
   public abstract int get(int var1);

   @Override
   public int set(int index, int element) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(int index, int element) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int removeByIndex(int index) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int indexOf(int o) {
      IntListIterator e = this.listIterator();

      while(e.hasNext()) {
         if (o == e.next()) {
            return e.previousIndex();
         }
      }

      return -1;
   }

   @Override
   public int lastIndexOf(int o) {
      IntListIterator e = this.listIterator(this.size());

      while(e.hasPrevious()) {
         if (o == e.previous()) {
            return e.nextIndex();
         }
      }

      return -1;
   }

   @Override
   public void clear() {
      this.removeRange(0, this.size());
   }

   @Override
   public boolean addAll(int index, IntCollection c) {
      boolean modified = false;

      for(IntIterator e = c.iterator(); e.hasNext(); modified = true) {
         this.add(index++, e.next());
      }

      return modified;
   }

   @Override
   public IntIterator iterator() {
      return new AbstractIntList.Itr();
   }

   @Override
   public IntListIterator listIterator() {
      return this.listIterator(0);
   }

   @Override
   public IntListIterator listIterator(int index) {
      if (index >= 0 && index <= this.size()) {
         return new AbstractIntList.ListItr(index);
      } else {
         throw new IndexOutOfBoundsException("Index: " + index);
      }
   }

   @Override
   public IntList subList(int fromIndex, int toIndex) {
      return (IntList)(this instanceof RandomAccess ? new RandomAccessSubIntList(this, fromIndex, toIndex) : new SubIntList(this, fromIndex, toIndex));
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntList)) {
         return false;
      } else {
         IntListIterator e1 = this.listIterator();
         IntListIterator e2 = ((IntList)o).listIterator();

         while(e1.hasNext() && e2.hasNext()) {
            int o1 = e1.next();
            int o2 = e2.next();
            if (o1 != o2) {
               return false;
            }
         }

         return !e1.hasNext() && !e2.hasNext();
      }
   }

   @Override
   public int hashCode() {
      int hashCode = 1;

      int obj;
      for(IntIterator i = this.iterator(); i.hasNext(); hashCode = 31 * hashCode + obj) {
         obj = i.next();
      }

      return hashCode;
   }

   protected void removeRange(int fromIndex, int toIndex) {
      IntListIterator it = this.listIterator(fromIndex);
      int i = 0;

      for(int n = toIndex - fromIndex; i < n; ++i) {
         it.next();
         it.remove();
      }
   }

   private class Itr implements IntIterator {
      int cursor = 0;
      int lastRet = -1;
      int expectedModCount = AbstractIntList.this.modCount;

      private Itr() {
      }

      @Override
      public boolean hasNext() {
         return this.cursor != AbstractIntList.this.size();
      }

      @Override
      public int next() {
         this.checkForComodification();

         try {
            int next = AbstractIntList.this.get(this.cursor);
            this.lastRet = this.cursor++;
            return next;
         } catch (IndexOutOfBoundsException var2) {
            this.checkForComodification();
            throw new NoSuchElementException();
         }
      }

      @Override
      public void remove() {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            this.checkForComodification();

            try {
               AbstractIntList.this.remove(this.lastRet);
               if (this.lastRet < this.cursor) {
                  --this.cursor;
               }

               this.lastRet = -1;
               this.expectedModCount = AbstractIntList.this.modCount;
            } catch (IndexOutOfBoundsException var2) {
               throw new ConcurrentModificationException();
            }
         }
      }

      final void checkForComodification() {
         if (AbstractIntList.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }
   }

   private class ListItr extends AbstractIntList.Itr implements IntListIterator {
      ListItr(int index) {
         this.cursor = index;
      }

      @Override
      public boolean hasPrevious() {
         return this.cursor != 0;
      }

      @Override
      public int previous() {
         this.checkForComodification();

         try {
            int i = this.cursor - 1;
            int previous = AbstractIntList.this.get(i);
            this.lastRet = this.cursor = i;
            return previous;
         } catch (IndexOutOfBoundsException var3) {
            this.checkForComodification();
            throw new NoSuchElementException();
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
      public void set(int e) {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            this.checkForComodification();

            try {
               AbstractIntList.this.set(this.lastRet, e);
               this.expectedModCount = AbstractIntList.this.modCount;
            } catch (IndexOutOfBoundsException var3) {
               throw new ConcurrentModificationException();
            }
         }
      }

      @Override
      public void add(int e) {
         this.checkForComodification();

         try {
            AbstractIntList.this.add(this.cursor++, e);
            this.lastRet = -1;
            this.expectedModCount = AbstractIntList.this.modCount;
         } catch (IndexOutOfBoundsException var3) {
            throw new ConcurrentModificationException();
         }
      }
   }
}
