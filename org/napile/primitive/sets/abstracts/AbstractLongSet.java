package org.napile.primitive.sets.abstracts;

import org.napile.HashUtils;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.collections.abstracts.AbstractLongCollection;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.LongSet;

public abstract class AbstractLongSet extends AbstractLongCollection implements LongSet {
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IntSet)) {
         return false;
      } else {
         LongCollection c = (LongCollection)o;
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

      long obj;
      for(LongIterator i = this.iterator(); i.hasNext(); h = (int)((long)h + obj)) {
         obj = i.next();
      }

      return HashUtils.hashCode(h);
   }

   @Override
   public boolean removeAll(LongCollection c) {
      boolean modified = false;
      if (this.size() > c.size()) {
         LongIterator i = c.iterator();

         while(i.hasNext()) {
            modified |= this.remove(i.next());
         }
      } else {
         LongIterator i = this.iterator();

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
