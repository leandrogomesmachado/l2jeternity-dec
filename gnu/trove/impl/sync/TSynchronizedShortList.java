package gnu.trove.impl.sync;

import gnu.trove.function.TShortFunction;
import gnu.trove.list.TShortList;
import gnu.trove.procedure.TShortProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TSynchronizedShortList extends TSynchronizedShortCollection implements TShortList {
   static final long serialVersionUID = -7754090372962971524L;
   final TShortList list;

   public TSynchronizedShortList(TShortList list) {
      super(list);
      this.list = list;
   }

   public TSynchronizedShortList(TShortList list, Object mutex) {
      super(list, mutex);
      this.list = list;
   }

   @Override
   public boolean equals(Object o) {
      synchronized(this.mutex) {
         return this.list.equals(o);
      }
   }

   @Override
   public int hashCode() {
      synchronized(this.mutex) {
         return this.list.hashCode();
      }
   }

   @Override
   public short get(int index) {
      synchronized(this.mutex) {
         return this.list.get(index);
      }
   }

   @Override
   public short set(int index, short element) {
      synchronized(this.mutex) {
         return this.list.set(index, element);
      }
   }

   @Override
   public void set(int offset, short[] values) {
      synchronized(this.mutex) {
         this.list.set(offset, values);
      }
   }

   @Override
   public void set(int offset, short[] values, int valOffset, int length) {
      synchronized(this.mutex) {
         this.list.set(offset, values, valOffset, length);
      }
   }

   @Override
   public short replace(int offset, short val) {
      synchronized(this.mutex) {
         return this.list.replace(offset, val);
      }
   }

   @Override
   public void remove(int offset, int length) {
      synchronized(this.mutex) {
         this.list.remove(offset, length);
      }
   }

   @Override
   public short removeAt(int offset) {
      synchronized(this.mutex) {
         return this.list.removeAt(offset);
      }
   }

   @Override
   public void add(short[] vals) {
      synchronized(this.mutex) {
         this.list.add(vals);
      }
   }

   @Override
   public void add(short[] vals, int offset, int length) {
      synchronized(this.mutex) {
         this.list.add(vals, offset, length);
      }
   }

   @Override
   public void insert(int offset, short value) {
      synchronized(this.mutex) {
         this.list.insert(offset, value);
      }
   }

   @Override
   public void insert(int offset, short[] values) {
      synchronized(this.mutex) {
         this.list.insert(offset, values);
      }
   }

   @Override
   public void insert(int offset, short[] values, int valOffset, int len) {
      synchronized(this.mutex) {
         this.list.insert(offset, values, valOffset, len);
      }
   }

   @Override
   public int indexOf(short o) {
      synchronized(this.mutex) {
         return this.list.indexOf(o);
      }
   }

   @Override
   public int lastIndexOf(short o) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(o);
      }
   }

   @Override
   public TShortList subList(int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return new TSynchronizedShortList(this.list.subList(fromIndex, toIndex), this.mutex);
      }
   }

   @Override
   public short[] toArray(int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(offset, len);
      }
   }

   @Override
   public short[] toArray(short[] dest, int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, offset, len);
      }
   }

   @Override
   public short[] toArray(short[] dest, int source_pos, int dest_pos, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, source_pos, dest_pos, len);
      }
   }

   @Override
   public int indexOf(int offset, short value) {
      synchronized(this.mutex) {
         return this.list.indexOf(offset, value);
      }
   }

   @Override
   public int lastIndexOf(int offset, short value) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(offset, value);
      }
   }

   @Override
   public void fill(short val) {
      synchronized(this.mutex) {
         this.list.fill(val);
      }
   }

   @Override
   public void fill(int fromIndex, int toIndex, short val) {
      synchronized(this.mutex) {
         this.list.fill(fromIndex, toIndex, val);
      }
   }

   @Override
   public void reverse() {
      synchronized(this.mutex) {
         this.list.reverse();
      }
   }

   @Override
   public void reverse(int from, int to) {
      synchronized(this.mutex) {
         this.list.reverse(from, to);
      }
   }

   @Override
   public void shuffle(Random rand) {
      synchronized(this.mutex) {
         this.list.shuffle(rand);
      }
   }

   @Override
   public void sort() {
      synchronized(this.mutex) {
         this.list.sort();
      }
   }

   @Override
   public void sort(int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         this.list.sort(fromIndex, toIndex);
      }
   }

   @Override
   public int binarySearch(short value) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value);
      }
   }

   @Override
   public int binarySearch(short value, int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value, fromIndex, toIndex);
      }
   }

   @Override
   public TShortList grep(TShortProcedure condition) {
      synchronized(this.mutex) {
         return this.list.grep(condition);
      }
   }

   @Override
   public TShortList inverseGrep(TShortProcedure condition) {
      synchronized(this.mutex) {
         return this.list.inverseGrep(condition);
      }
   }

   @Override
   public short max() {
      synchronized(this.mutex) {
         return this.list.max();
      }
   }

   @Override
   public short min() {
      synchronized(this.mutex) {
         return this.list.min();
      }
   }

   @Override
   public short sum() {
      synchronized(this.mutex) {
         return this.list.sum();
      }
   }

   @Override
   public boolean forEachDescending(TShortProcedure procedure) {
      synchronized(this.mutex) {
         return this.list.forEachDescending(procedure);
      }
   }

   @Override
   public void transformValues(TShortFunction function) {
      synchronized(this.mutex) {
         this.list.transformValues(function);
      }
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TSynchronizedRandomAccessShortList(this.list) : this;
   }
}
