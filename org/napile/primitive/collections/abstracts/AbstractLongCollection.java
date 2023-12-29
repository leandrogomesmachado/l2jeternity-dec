package org.napile.primitive.collections.abstracts;

import java.util.Arrays;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.LongIterator;

public abstract class AbstractLongCollection implements LongCollection {
   protected AbstractLongCollection() {
   }

   @Override
   public abstract LongIterator iterator();

   @Override
   public abstract int size();

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean contains(long o) {
      LongIterator e = this.iterator();

      while(e.hasNext()) {
         if (o == e.next()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public long[] toArray() {
      long[] r = new long[this.size()];
      LongIterator it = this.iterator();

      for(int i = 0; i < r.length; ++i) {
         if (!it.hasNext()) {
            return Arrays.copyOf(r, i);
         }

         r[i] = it.next();
      }

      return it.hasNext() ? finishToArray(r, it) : r;
   }

   @Override
   public long[] toArray(long[] a) {
      int size = this.size();
      long[] r = a.length >= size ? a : new long[size];
      LongIterator it = this.iterator();

      for(int i = 0; i < r.length; ++i) {
         if (!it.hasNext()) {
            if (a != r) {
               return Arrays.copyOf(r, i);
            }

            r[i] = 0L;
            return r;
         }

         r[i] = it.next();
      }

      return it.hasNext() ? finishToArray(r, it) : r;
   }

   private static long[] finishToArray(long[] r, LongIterator it) {
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
   public boolean add(long e) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(long o) {
      LongIterator e = this.iterator();

      while(e.hasNext()) {
         if (o == e.next()) {
            e.remove();
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsAll(LongCollection c) {
      LongIterator e = c.iterator();

      while(e.hasNext()) {
         if (!this.contains(e.next())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(LongCollection c) {
      boolean modified = false;
      LongIterator e = c.iterator();

      while(e.hasNext()) {
         if (this.add(e.next())) {
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(LongCollection c) {
      boolean modified = false;
      LongIterator e = this.iterator();

      while(e.hasNext()) {
         if (c.contains(e.next())) {
            e.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(LongCollection c) {
      boolean modified = false;
      LongIterator e = this.iterator();

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
      LongIterator e = this.iterator();

      while(e.hasNext()) {
         e.next();
         e.remove();
      }
   }

   @Override
   public String toString() {
      LongIterator i = this.iterator();
      if (!i.hasNext()) {
         return "[]";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('[');

         while(true) {
            long e = i.next();
            sb.append(e);
            if (!i.hasNext()) {
               return sb.append(']').toString();
            }

            sb.append(", ");
         }
      }
   }
}
