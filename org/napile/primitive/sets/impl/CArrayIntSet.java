package org.napile.primitive.sets.impl;

import java.io.Serializable;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.lists.impl.CArrayIntList;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.abstracts.AbstractIntSet;

public class CArrayIntSet extends AbstractIntSet implements Serializable {
   private final CArrayIntList al = new CArrayIntList();

   public CArrayIntSet() {
   }

   public CArrayIntSet(IntCollection c) {
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

   @Override
   public boolean contains(int o) {
      return this.al.contains(o);
   }

   @Override
   public int[] toArray() {
      return this.al.toArray();
   }

   @Override
   public int[] toArray(int[] a) {
      return this.al.toArray(a);
   }

   @Override
   public void clear() {
      this.al.clear();
   }

   @Override
   public boolean remove(int o) {
      return this.al.remove(o);
   }

   @Override
   public boolean add(int e) {
      return this.al.addIfAbsent(e);
   }

   @Override
   public boolean containsAll(IntCollection c) {
      return this.al.containsAll(c);
   }

   @Override
   public boolean addAll(IntCollection c) {
      return this.al.addAllAbsent(c) > 0;
   }

   @Override
   public boolean removeAll(IntCollection c) {
      return this.al.removeAll(c);
   }

   @Override
   public boolean retainAll(IntCollection c) {
      return this.al.retainAll(c);
   }

   @Override
   public IntIterator iterator() {
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
         int[] elements = this.al.toArray();
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
               if (!matched[i] && x == elements[i]) {
                  continue label42;
               }
            }

            return false;
         }

         return k == len;
      }
   }
}
