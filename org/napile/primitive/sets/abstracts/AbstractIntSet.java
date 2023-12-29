package org.napile.primitive.sets.abstracts;

import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.collections.abstracts.AbstractIntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.sets.IntSet;

public abstract class AbstractIntSet extends AbstractIntCollection implements IntSet {
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntSet)) {
         return false;
      } else {
         IntCollection c = (IntCollection)o;
         if (c.size() != this.size()) {
            return false;
         } else {
            try {
               return this.containsAll(c);
            } catch (ClassCastException var4) {
               return false;
            } catch (NullPointerException var5) {
               return false;
            }
         }
      }
   }

   @Override
   public int hashCode() {
      int h = 0;

      int obj;
      for(IntIterator i = this.iterator(); i.hasNext(); h += obj) {
         obj = i.next();
      }

      return h;
   }

   @Override
   public boolean removeAll(IntCollection c) {
      boolean modified = false;
      if (this.size() > c.size()) {
         IntIterator i = c.iterator();

         while(i.hasNext()) {
            modified |= this.remove(i.next());
         }
      } else {
         IntIterator i = this.iterator();

         while(i.hasNext()) {
            if (c.contains(i.next())) {
               i.remove();
               modified = true;
            }
         }
      }

      return modified;
   }
}
