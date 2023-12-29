package gnu.trove.set.hash;

import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.procedure.array.ToObjectArrayProceedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class THashSet<E> extends TObjectHash<E> implements Set<E>, Iterable<E>, Externalizable {
   static final long serialVersionUID = 1L;

   public THashSet() {
   }

   public THashSet(int initialCapacity) {
      super(initialCapacity);
   }

   public THashSet(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public THashSet(Collection<? extends E> collection) {
      this(collection.size());
      this.addAll(collection);
   }

   @Override
   public boolean add(E obj) {
      int index = this.insertKey(obj);
      if (index < 0) {
         return false;
      } else {
         this.postInsertHook(this.consumeFreeSlot);
         return true;
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof Set)) {
         return false;
      } else {
         Set that = (Set)other;
         return that.size() != this.size() ? false : this.containsAll(that);
      }
   }

   @Override
   public int hashCode() {
      THashSet<E>.HashProcedure p = new THashSet.HashProcedure();
      this.forEach(p);
      return p.getHashCode();
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      int oldSize = this.size();
      Object[] oldSet = this._set;
      this._set = new Object[newCapacity];
      Arrays.fill(this._set, FREE);
      int count = 0;
      int i = oldCapacity;

      while(i-- > 0) {
         E o = (E)oldSet[i];
         if (o != FREE && o != REMOVED) {
            int index = this.insertKey(o);
            if (index < 0) {
               this.throwObjectContractViolation(this._set[-index - 1], o, this.size(), oldSize, oldSet);
            }

            ++count;
         }
      }

      reportPotentialConcurrentMod(this.size(), oldSize);
   }

   @Override
   public Object[] toArray() {
      Object[] result = new Object[this.size()];
      this.forEach(new ToObjectArrayProceedure<>((E[])result));
      return result;
   }

   @Override
   public <T> T[] toArray(T[] a) {
      int size = this.size();
      if (a.length < size) {
         a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
      }

      this.forEach(new ToObjectArrayProceedure<>((E[])a));
      if (a.length > size) {
         a[size] = null;
      }

      return a;
   }

   @Override
   public void clear() {
      super.clear();
      Arrays.fill(this._set, 0, this._set.length, FREE);
   }

   @Override
   public boolean remove(Object obj) {
      int index = this.index(obj);
      if (index >= 0) {
         this.removeAt(index);
         return true;
      } else {
         return false;
      }
   }

   public TObjectHashIterator<E> iterator() {
      return new TObjectHashIterator<>(this);
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      Iterator i = collection.iterator();

      while(i.hasNext()) {
         if (!this.contains(i.next())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection<? extends E> collection) {
      boolean changed = false;
      int size = collection.size();
      this.ensureCapacity(size);
      Iterator<? extends E> it = collection.iterator();

      while(size-- > 0) {
         if (this.add(it.next())) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      boolean changed = false;
      int size = collection.size();
      Iterator it = collection.iterator();

      while(size-- > 0) {
         if (this.remove(it.next())) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean changed = false;
      int size = this.size();
      Iterator<E> it = this.iterator();

      while(size-- > 0) {
         if (!collection.contains(it.next())) {
            it.remove();
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public String toString() {
      final StringBuilder buf = new StringBuilder("{");
      this.forEach(new TObjectProcedure<E>() {
         private boolean first = true;

         @Override
         public boolean execute(Object value) {
            if (this.first) {
               this.first = false;
            } else {
               buf.append(", ");
            }

            buf.append(value);
            return true;
         }
      });
      buf.append("}");
      return buf.toString();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(1);
      super.writeExternal(out);
      out.writeInt(this._size);
      this.writeEntries(out);
   }

   protected void writeEntries(ObjectOutput out) throws IOException {
      int i = this._set.length;

      while(i-- > 0) {
         if (this._set[i] != REMOVED && this._set[i] != FREE) {
            out.writeObject(this._set[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      byte version = in.readByte();
      if (version != 0) {
         super.readExternal(in);
      }

      int size = in.readInt();
      this.setUp(size);

      while(size-- > 0) {
         E val = (E)in.readObject();
         this.add(val);
      }
   }

   private final class HashProcedure implements TObjectProcedure<E> {
      private int h = 0;

      private HashProcedure() {
      }

      public int getHashCode() {
         return this.h;
      }

      @Override
      public final boolean execute(E key) {
         this.h += HashFunctions.hash(key);
         return true;
      }
   }
}
