package org.napile.primitive.lists.abstracts;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.IntListIterator;
import org.napile.primitive.lists.IntList;

class SubIntList extends AbstractIntList {
   private AbstractIntList l;
   private int offset;
   private int size;
   private int expectedModCount;

   SubIntList(AbstractIntList list, int fromIndex, int toIndex) {
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
   public int set(int index, int element) {
      this.rangeCheck(index);
      this.checkForComodification();
      return this.l.set(index + this.offset, element);
   }

   @Override
   public int get(int index) {
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
   public void add(int index, int element) {
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
   public int removeByIndex(int index) {
      this.rangeCheck(index);
      this.checkForComodification();
      int result = this.l.removeByIndex(index + this.offset);
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
   public boolean addAll(IntCollection c) {
      return this.addAll(this.size, c);
   }

   @Override
   public boolean addAll(int index, IntCollection c) {
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
   public IntIterator iterator() {
      return this.listIterator();
   }

   @Override
   public IntListIterator listIterator(final int index) {
      this.checkForComodification();
      if (index >= 0 && index <= this.size) {
         return new IntListIterator() {
            private IntListIterator i = SubIntList.this.l.listIterator(index + SubIntList.this.offset);

            @Override
            public boolean hasNext() {
               return this.nextIndex() < SubIntList.this.size;
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
               return this.i.nextIndex() - SubIntList.this.offset;
            }

            @Override
            public int previousIndex() {
               return this.i.previousIndex() - SubIntList.this.offset;
            }

            @Override
            public void remove() {
               this.i.remove();
               SubIntList.this.expectedModCount = SubIntList.this.l.modCount;
               SubIntList.this.size--;
               ++SubIntList.this.modCount;
            }

            @Override
            public void set(int e) {
               this.i.set(e);
            }

            @Override
            public void add(int e) {
               this.i.add(e);
               SubIntList.this.expectedModCount = SubIntList.this.l.modCount;
               SubIntList.this.size++;
               ++SubIntList.this.modCount;
            }
         };
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }

   @Override
   public IntList subList(int fromIndex, int toIndex) {
      return new SubIntList(this, fromIndex, toIndex);
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
