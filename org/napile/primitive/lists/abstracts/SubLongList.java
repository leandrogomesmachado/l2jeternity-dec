package org.napile.primitive.lists.abstracts;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.iterators.LongListIterator;
import org.napile.primitive.lists.LongList;

class SubLongList extends AbstractLongList {
   private AbstractLongList l;
   private int offset;
   private int size;
   private int expectedModCount;

   SubLongList(AbstractLongList list, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
      } else if (toIndex > list.size()) {
         throw new IndexOutOfBoundsException("toIndex = " + toIndex);
      } else if (fromIndex > toIndex) {
         throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
      } else {
         this.l = list;
         this.offset = fromIndex;
         this.size = toIndex - fromIndex;
         this.expectedModCount = this.l.modCount;
      }
   }

   @Override
   public long set(int index, long element) {
      this.rangeCheck(index);
      this.checkForComodification();
      return this.l.set(index + this.offset, element);
   }

   @Override
   public long get(int index) {
      this.rangeCheck(index);
      this.checkForComodification();
      return this.l.get(index + this.offset);
   }

   @Override
   public int size() {
      this.checkForComodification();
      return this.size;
   }

   @Override
   public void add(int index, long element) {
      if (index >= 0 && index <= this.size) {
         this.checkForComodification();
         this.l.add(index + this.offset, element);
         this.expectedModCount = this.l.modCount;
         ++this.size;
         ++this.modCount;
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   @Override
   public long removeByIndex(int index) {
      this.rangeCheck(index);
      this.checkForComodification();
      long result = this.l.removeByIndex(index + this.offset);
      this.expectedModCount = this.l.modCount;
      --this.size;
      ++this.modCount;
      return result;
   }

   @Override
   protected void removeRange(int fromIndex, int toIndex) {
      this.checkForComodification();
      this.l.removeRange(fromIndex + this.offset, toIndex + this.offset);
      this.expectedModCount = this.l.modCount;
      this.size -= toIndex - fromIndex;
      ++this.modCount;
   }

   @Override
   public boolean addAll(LongCollection c) {
      return this.addAll(this.size, c);
   }

   @Override
   public boolean addAll(int index, LongCollection c) {
      if (index >= 0 && index <= this.size) {
         int cSize = c.size();
         if (cSize == 0) {
            return false;
         } else {
            this.checkForComodification();
            this.l.addAll(this.offset + index, c);
            this.expectedModCount = this.l.modCount;
            this.size += cSize;
            ++this.modCount;
            return true;
         }
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   @Override
   public LongIterator iterator() {
      return this.listIterator();
   }

   @Override
   public LongListIterator listIterator(final int index) {
      this.checkForComodification();
      if (index >= 0 && index <= this.size) {
         return new LongListIterator() {
            private LongListIterator i = SubLongList.this.l.listIterator(index + SubLongList.this.offset);

            @Override
            public boolean hasNext() {
               return this.nextIndex() < SubLongList.this.size;
            }

            @Override
            public long next() {
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
            public long previous() {
               if (this.hasPrevious()) {
                  return this.i.previous();
               } else {
                  throw new NoSuchElementException();
               }
            }

            @Override
            public int nextIndex() {
               return this.i.nextIndex() - SubLongList.this.offset;
            }

            @Override
            public int previousIndex() {
               return this.i.previousIndex() - SubLongList.this.offset;
            }

            @Override
            public void remove() {
               this.i.remove();
               SubLongList.this.expectedModCount = SubLongList.this.l.modCount;
               SubLongList.this.size--;
               ++SubLongList.this.modCount;
            }

            @Override
            public void set(long e) {
               this.i.set(e);
            }

            @Override
            public void add(long e) {
               this.i.add(e);
               SubLongList.this.expectedModCount = SubLongList.this.l.modCount;
               SubLongList.this.size++;
               ++SubLongList.this.modCount;
            }
         };
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   @Override
   public LongList subList(int fromIndex, int toIndex) {
      return new SubLongList(this, fromIndex, toIndex);
   }

   private void rangeCheck(int index) {
      if (index < 0 || index >= this.size) {
         throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + this.size);
      }
   }

   private void checkForComodification() {
      if (this.l.modCount != this.expectedModCount) {
         throw new ConcurrentModificationException();
      }
   }
}
