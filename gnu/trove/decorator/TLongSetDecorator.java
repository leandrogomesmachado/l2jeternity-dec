package gnu.trove.decorator;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class TLongSetDecorator extends AbstractSet<Long> implements Set<Long>, Externalizable {
   static final long serialVersionUID = 1L;
   protected TLongSet _set;

   public TLongSetDecorator() {
   }

   public TLongSetDecorator(TLongSet set) {
      this._set = set;
   }

   public TLongSet getSet() {
      return this._set;
   }

   public boolean add(Long value) {
      return value != null && this._set.add(value);
   }

   @Override
   public boolean equals(Object other) {
      if (this._set.equals(other)) {
         return true;
      } else if (other instanceof Set) {
         Set that = (Set)other;
         if (that.size() != this._set.size()) {
            return false;
         } else {
            Iterator it = that.iterator();
            int i = that.size();

            while(i-- > 0) {
               Object val = it.next();
               if (!(val instanceof Long)) {
                  return false;
               }

               long v = (Long)val;
               if (!this._set.contains(v)) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void clear() {
      this._set.clear();
   }

   @Override
   public boolean remove(Object value) {
      return value instanceof Long && this._set.remove((Long)value);
   }

   @Override
   public Iterator<Long> iterator() {
      return new Iterator<Long>() {
         private final TLongIterator it = TLongSetDecorator.this._set.iterator();

         public Long next() {
            return this.it.next();
         }

         @Override
         public boolean hasNext() {
            return this.it.hasNext();
         }

         @Override
         public void remove() {
            this.it.remove();
         }
      };
   }

   @Override
   public int size() {
      return this._set.size();
   }

   @Override
   public boolean isEmpty() {
      return this._set.size() == 0;
   }

   @Override
   public boolean contains(Object o) {
      return !(o instanceof Long) ? false : this._set.contains((Long)o);
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._set = (TLongSet)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._set);
   }
}
