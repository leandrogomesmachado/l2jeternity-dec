package gnu.trove.decorator;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class TIntSetDecorator extends AbstractSet<Integer> implements Set<Integer>, Externalizable {
   static final long serialVersionUID = 1L;
   protected TIntSet _set;

   public TIntSetDecorator() {
   }

   public TIntSetDecorator(TIntSet set) {
      this._set = set;
   }

   public TIntSet getSet() {
      return this._set;
   }

   public boolean add(Integer value) {
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
               if (!(val instanceof Integer)) {
                  return false;
               }

               int v = (Integer)val;
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
      return value instanceof Integer && this._set.remove((Integer)value);
   }

   @Override
   public Iterator<Integer> iterator() {
      return new Iterator<Integer>() {
         private final TIntIterator it = TIntSetDecorator.this._set.iterator();

         public Integer next() {
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
      return !(o instanceof Integer) ? false : this._set.contains((Integer)o);
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._set = (TIntSet)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._set);
   }
}
