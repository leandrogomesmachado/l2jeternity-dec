package org.napile.primitive.collections.abstracts;

import java.util.Arrays;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;

public abstract class AbstractIntCollection implements IntCollection {
   protected AbstractIntCollection() {
   }

   @Override
   public abstract IntIterator iterator();

   @Override
   public abstract int size();

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean contains(int o) {
      IntIterator e = this.iterator();

      while(e.hasNext()) {
         if (o == e.next()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int[] toArray() {
      int[] r = new int[this.size()];
      IntIterator it = this.iterator();

      for(int i = 0; i < r.length; ++i) {
         if (!it.hasNext()) {
            return Arrays.copyOf(r, i);
         }

         r[i] = it.next();
      }

      return it.hasNext() ? finishToArray(r, it) : r;
   }

   @Override
   public int[] toArray(int[] a) {
      int size = this.size();
      int[] r = a.length >= size ? a : new int[size];
      IntIterator it = this.iterator();

      for(int i = 0; i < r.length; ++i) {
         if (!it.hasNext()) {
            if (a != r) {
               return Arrays.copyOf(r, i);
            }

            r[i] = 0;
            return r;
         }

         r[i] = it.next();
      }

      return it.hasNext() ? finishToArray(r, it) : r;
   }

   private static int[] finishToArray(int[] r, IntIterator it) {
      int i;
      for(i = r.length; it.hasNext(); r[i++] = it.next()) {
         int cap = r.length;
         if (i == cap) {
            int newCap = (cap / 2 + 1) * 3;
            if (newCap <= cap) {
               if (cap == Integer.MAX_VALUE) {
                  throw new OutOfMemoryError("Required array size too large");
               }

               newCap = Integer.MAX_VALUE;
            }

            r = Arrays.copyOf(r, newCap);
         }
      }

      return i == r.length ? r : Arrays.copyOf(r, i);
   }

   @Override
   public boolean add(int e) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(int o) {
      IntIterator e = this.iterator();

      while(e.hasNext()) {
         if (o == e.next()) {
            e.remove();
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsAll(IntCollection c) {
      IntIterator e = c.iterator();

      while(e.hasNext()) {
         if (!this.contains(e.next())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(IntCollection c) {
      boolean modified = false;
      IntIterator e = c.iterator();

      while(e.hasNext()) {
         if (this.add(e.next())) {
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(IntCollection c) {
      boolean modified = false;
      IntIterator e = this.iterator();

      while(e.hasNext()) {
         if (c.contains(e.next())) {
            e.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(IntCollection c) {
      boolean modified = false;
      IntIterator e = this.iterator();

      while(e.hasNext()) {
         if (!c.contains(e.next())) {
            e.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public void clear() {
      IntIterator e = this.iterator();

      while(e.hasNext()) {
         e.next();
         e.remove();
      }
   }

   @Override
   public String toString() {
      IntIterator i = this.iterator();
      if (!i.hasNext()) {
         return "[]";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('[');

         while(true) {
            int e = i.next();
            sb.append(e);
            if (!i.hasNext()) {
               return sb.append(']').toString();
            }

            sb.append(", ");
         }
      }
   }
}
