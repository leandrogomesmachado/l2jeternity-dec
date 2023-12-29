package gnu.trove.set.hash;

import gnu.trove.TDoubleCollection;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TDoubleHash;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.set.TDoubleSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;

public class TDoubleHashSet extends TDoubleHash implements TDoubleSet, Externalizable {
   static final long serialVersionUID = 1L;

   public TDoubleHashSet() {
   }

   public TDoubleHashSet(int initialCapacity) {
      super(initialCapacity);
   }

   public TDoubleHashSet(int initialCapacity, float load_factor) {
      super(initialCapacity, load_factor);
   }

   public TDoubleHashSet(int initial_capacity, float load_factor, double no_entry_value) {
      super(initial_capacity, load_factor, no_entry_value);
      if (no_entry_value != 0.0) {
         Arrays.fill(this._set, no_entry_value);
      }
   }

   public TDoubleHashSet(Collection<? extends Double> collection) {
      this(Math.max(collection.size(), 10));
      this.addAll(collection);
   }

   public TDoubleHashSet(TDoubleCollection collection) {
      this(Math.max(collection.size(), 10));
      if (collection instanceof TDoubleHashSet) {
         TDoubleHashSet hashset = (TDoubleHashSet)collection;
         this._loadFactor = hashset._loadFactor;
         this.no_entry_value = hashset.no_entry_value;
         if (this.no_entry_value != 0.0) {
            Arrays.fill(this._set, this.no_entry_value);
         }

         this.setUp((int)Math.ceil((double)(10.0F / this._loadFactor)));
      }

      this.addAll(collection);
   }

   public TDoubleHashSet(double[] array) {
      this(Math.max(array.length, 10));
      this.addAll(array);
   }

   @Override
   public TDoubleIterator iterator() {
      return new TDoubleHashSet.TDoubleHashIterator(this);
   }

   @Override
   public double[] toArray() {
      double[] result = new double[this.size()];
      double[] set = this._set;
      byte[] states = this._states;
      int i = states.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            result[j++] = set[i];
         }
      }

      return result;
   }

   @Override
   public double[] toArray(double[] dest) {
      double[] set = this._set;
      byte[] states = this._states;
      int i = states.length;
      int j = 0;

      while(i-- > 0) {
         if (states[i] == 1) {
            dest[j++] = set[i];
         }
      }

      if (dest.length > this._size) {
         dest[this._size] = this.no_entry_value;
      }

      return dest;
   }

   @Override
   public boolean add(double val) {
      int index = this.insertKey(val);
      if (index < 0) {
         return false;
      } else {
         this.postInsertHook(this.consumeFreeSlot);
         return true;
      }
   }

   @Override
   public boolean remove(double val) {
      int index = this.index(val);
      if (index >= 0) {
         this.removeAt(index);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      for(Object element : collection) {
         if (!(element instanceof Double)) {
            return false;
         }

         double c = (Double)element;
         if (!this.contains(c)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean containsAll(TDoubleCollection collection) {
      TDoubleIterator iter = collection.iterator();

      while(iter.hasNext()) {
         double element = iter.next();
         if (!this.contains(element)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean containsAll(double[] array) {
      int i = array.length;

      while(i-- > 0) {
         if (!this.contains(array[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection<? extends Double> collection) {
      boolean changed = false;

      for(Double element : collection) {
         double e = element;
         if (this.add(e)) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean addAll(TDoubleCollection collection) {
      boolean changed = false;
      TDoubleIterator iter = collection.iterator();

      while(iter.hasNext()) {
         double element = iter.next();
         if (this.add(element)) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean addAll(double[] array) {
      boolean changed = false;
      int i = array.length;

      while(i-- > 0) {
         if (this.add(array[i])) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean modified = false;
      TDoubleIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(TDoubleCollection collection) {
      if (this == collection) {
         return false;
      } else {
         boolean modified = false;
         TDoubleIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }
   }

   @Override
   public boolean retainAll(double[] array) {
      boolean changed = false;
      Arrays.sort(array);
      double[] set = this._set;
      byte[] states = this._states;
      this._autoCompactTemporaryDisable = true;
      int i = set.length;

      while(i-- > 0) {
         if (states[i] == 1 && Arrays.binarySearch(array, set[i]) < 0) {
            this.removeAt(i);
            changed = true;
         }
      }

      this._autoCompactTemporaryDisable = false;
      return changed;
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      boolean changed = false;

      for(Object element : collection) {
         if (element instanceof Double) {
            double c = (Double)element;
            if (this.remove(c)) {
               changed = true;
            }
         }
      }

      return changed;
   }

   @Override
   public boolean removeAll(TDoubleCollection collection) {
      boolean changed = false;
      TDoubleIterator iter = collection.iterator();

      while(iter.hasNext()) {
         double element = iter.next();
         if (this.remove(element)) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean removeAll(double[] array) {
      boolean changed = false;
      int i = array.length;

      while(i-- > 0) {
         if (this.remove(array[i])) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public void clear() {
      super.clear();
      double[] set = this._set;
      byte[] states = this._states;

      for(int i = set.length; i-- > 0; states[i] = 0) {
         set[i] = this.no_entry_value;
      }
   }

   @Override
   protected void rehash(int newCapacity) {
      int oldCapacity = this._set.length;
      double[] oldSet = this._set;
      byte[] oldStates = this._states;
      this._set = new double[newCapacity];
      this._states = new byte[newCapacity];
      int i = oldCapacity;

      while(i-- > 0) {
         if (oldStates[i] == 1) {
            double o = oldSet[i];
            int index = this.insertKey(o);
         }
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof TDoubleSet)) {
         return false;
      } else {
         TDoubleSet that = (TDoubleSet)other;
         if (that.size() != this.size()) {
            return false;
         } else {
            int i = this._states.length;

            while(i-- > 0) {
               if (this._states[i] == 1 && !that.contains(this._set[i])) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Override
   public int hashCode() {
      int hashcode = 0;
      int i = this._states.length;

      while(i-- > 0) {
         if (this._states[i] == 1) {
            hashcode += HashFunctions.hash(this._set[i]);
         }
      }

      return hashcode;
   }

   @Override
   public String toString() {
      StringBuilder buffy = new StringBuilder(this._size * 2 + 2);
      buffy.append("{");
      int i = this._states.length;
      int j = 1;

      while(i-- > 0) {
         if (this._states[i] == 1) {
            buffy.append(this._set[i]);
            if (j++ < this._size) {
               buffy.append(",");
            }
         }
      }

      buffy.append("}");
      return buffy.toString();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(1);
      super.writeExternal(out);
      out.writeInt(this._size);
      out.writeFloat(this._loadFactor);
      out.writeDouble(this.no_entry_value);
      int i = this._states.length;

      while(i-- > 0) {
         if (this._states[i] == 1) {
            out.writeDouble(this._set[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      int version = in.readByte();
      super.readExternal(in);
      int size = in.readInt();
      if (version >= 1) {
         this._loadFactor = in.readFloat();
         this.no_entry_value = in.readDouble();
         if (this.no_entry_value != 0.0) {
            Arrays.fill(this._set, this.no_entry_value);
         }
      }

      this.setUp(size);

      while(size-- > 0) {
         double val = in.readDouble();
         this.add(val);
      }
   }

   class TDoubleHashIterator extends THashPrimitiveIterator implements TDoubleIterator {
      private final TDoubleHash _hash;

      public TDoubleHashIterator(TDoubleHash hash) {
         super(hash);
         this._hash = hash;
      }

      @Override
      public double next() {
         this.moveToNextIndex();
         return this._hash._set[this._index];
      }
   }
}
