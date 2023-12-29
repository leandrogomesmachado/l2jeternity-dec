package org.napile.primitive.lists.abstracts;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.collections.abstracts.AbstractLongCollection;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.iterators.LongListIterator;
import org.napile.primitive.lists.LongList;

public abstract class AbstractLongList extends AbstractLongCollection implements LongList {
   protected transient int modCount = 0;

   protected AbstractLongList() {
   }

   @Override
   public boolean add(long e) {
      this.add(this.size(), e);
      return true;
   }

   @Override
   public abstract long get(int var1);

   @Override
   public long set(int index, long element) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(int index, long element) {
      throw new UnsupportedOperationException();
   }

   @Override
   public long removeByIndex(int index) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int indexOf(long o) {
      LongListIterator e = this.listIterator();

      while(e.hasNext()) {
         if (o == e.next()) {
            return e.previousIndex();
         }
      }

      return -1;
   }

   @Override
   public int lastIndexOf(long o) {
      LongListIterator e = this.listIterator(this.size());

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
   public boolean addAll(int index, LongCollection c) {
      boolean modified = false;

      for(LongIterator e = c.iterator(); e.hasNext(); modified = true) {
         this.add(index++, e.next());
      }

      return modified;
   }

   @Override
   public LongIterator iterator() {
      return new AbstractLongList.Itr();
   }

   @Override
   public LongListIterator listIterator() {
      return this.listIterator(0);
   }

   @Override
   public LongListIterator listIterator(int index) {
      if (index >= 0 && index <= this.size()) {
         return new AbstractLongList.ListItr(index);
      } else {
         throw new IndexOutOfBoundsException("Index: " + index);
      }
   }

   @Override
   public LongList subList(int fromIndex, int toIndex) {
      return (LongList)(this instanceof RandomAccess ? new RandomAccessSubLongList(this, fromIndex, toIndex) : new SubLongList(this, fromIndex, toIndex));
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof LongList)) {
         return false;
      } else {
         LongListIterator e1 = this.listIterator();
         LongListIterator e2 = ((LongList)o).listIterator();

         while(e1.hasNext() && e2.hasNext()) {
            long o1 = e1.next();
            long o2 = e2.next();
            if (o1 != o2) {
               return false;
            }
         }

         return !e1.hasNext() && !e2.hasNext();
      }
   }

   @Override
   public int hashCode() {
      long hashCode = 1L;

      long obj;
      for(LongIterator i = this.iterator(); i.hasNext(); hashCode = 31L * hashCode + obj) {
         obj = i.next();
      }

      return (int)hashCode;
   }

   protected void removeRange(int fromIndex, int toIndex) {
      LongListIterator it = this.listIterator(fromIndex);
      int i = 0;

      for(int n = toIndex - fromIndex; i < n; ++i) {
         it.next();
         it.remove();
      }
   }

   private class Itr implements LongIterator {
      int cursor = 0;
      int lastRet = -1;
      int expectedModCount = AbstractLongList.this.modCount;

      private Itr() {
      }

      @Override
      public boolean hasNext() {
         return this.cursor != AbstractLongList.this.size();
      }

      @Override
      public long next() {
         this.checkForComodification();

         try {
            long next = AbstractLongList.this.get(this.cursor);
            this.lastRet = this.cursor++;
            return next;
         } catch (IndexOutOfBoundsException var3) {
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
               AbstractLongList.this.remove((long)this.lastRet);
               if (this.lastRet < this.cursor) {
                  --this.cursor;
               }

               this.lastRet = -1;
               this.expectedModCount = AbstractLongList.this.modCount;
            } catch (IndexOutOfBoundsException var2) {
               throw new ConcurrentModificationException();
            }
         }
      }

      final void checkForComodification() {
         if (AbstractLongList.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }
   }

   private class ListItr extends AbstractLongList.Itr implements LongListIterator {
      ListItr(int index) {
         this.cursor = index;
      }

      @Override
      public boolean hasPrevious() {
         return this.cursor != 0;
      }

      @Override
      public long previous() {
         this.checkForComodification();

         try {
            int i = this.cursor - 1;
            long previous = AbstractLongList.this.get(i);
            this.lastRet = this.cursor = i;
            return previous;
         } catch (IndexOutOfBoundsException var4) {
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
      public void set(long e) {
         if (this.lastRet == -1) {
            throw new IllegalStateException();
         } else {
            this.checkForComodification();

            try {
               AbstractLongList.this.set(this.lastRet, e);
               this.expectedModCount = AbstractLongList.this.modCount;
            } catch (IndexOutOfBoundsException var4) {
               throw new ConcurrentModificationException();
            }
         }
      }

      @Override
      public void add(long e) {
         this.checkForComodification();

         try {
            AbstractLongList.this.add(this.cursor++, e);
            this.lastRet = -1;
            this.expectedModCount = AbstractLongList.this.modCount;
         } catch (IndexOutOfBoundsException var4) {
            throw new ConcurrentModificationException();
         }
      }
   }
}
