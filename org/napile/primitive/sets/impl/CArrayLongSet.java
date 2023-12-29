package org.napile.primitive.sets.impl;

import java.io.Serializable;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.lists.impl.CArrayLongList;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractLongSet;

public class CArrayLongSet extends AbstractLongSet implements Serializable {
   private final CArrayLongList al = new CArrayLongList();

   public CArrayLongSet() {
   }

   public CArrayLongSet(LongCollection c) {
      this();
      this.al.addAllAbsent(c);
   }

   @Override
   public int size() {
      return this.al.size();
   }

   @Override
   public boolean isEmpty() {
      return this.al.isEmpty();
   }

   public boolean contains(int o) {
      return this.al.contains((long)o);
   }

   @Override
   public long[] toArray() {
      return this.al.toArray();
   }

   @Override
   public long[] toArray(long[] a) {
      return this.al.toArray(a);
   }

   @Override
   public void clear() {
      this.al.clear();
   }

   @Override
   public boolean remove(long o) {
      return this.al.remove(o);
   }

   @Override
   public boolean add(long e) {
      return this.al.addIfAbsent(e);
   }

   @Override
   public boolean containsAll(LongCollection c) {
      return this.al.containsAll(c);
   }

   @Override
   public boolean addAll(LongCollection c) {
      return this.al.addAllAbsent(c) > 0;
   }

   @Override
   public boolean removeAll(LongCollection c) {
      return this.al.removeAll(c);
   }

   @Override
   public boolean retainAll(LongCollection c) {
      return this.al.retainAll(c);
   }

   @Override
   public LongIterator iterator() {
      return this.al.iterator();
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntSet)) {
         return false;
      } else {
         IntSet set = (IntSet)o;
         IntIterator it = set.iterator();
         long[] elements = this.al.toArray();
         int len = elements.length;
         boolean[] matched = new boolean[len];

         int k;
         int i;
         label42:
         for(k = 0; it.hasNext(); matched[i] = true) {
            if (++k > len) {
               return false;
            }

            int x = it.next();

            for(i = 0; i < len; ++i) {
               if (!matched[i] && (long)x == elements[i]) {
                  continue label42;
               }
            }

            return false;
         }

         return k == len;
      }
   }
}
