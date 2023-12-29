package gnu.trove.impl.sync;

import gnu.trove.function.TIntFunction;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TSynchronizedIntList extends TSynchronizedIntCollection implements TIntList {
   static final long serialVersionUID = -7754090372962971524L;
   final TIntList list;

   public TSynchronizedIntList(TIntList list) {
      super(list);
      this.list = list;
   }

   public TSynchronizedIntList(TIntList list, Object mutex) {
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
   public int get(int index) {
      synchronized(this.mutex) {
         return this.list.get(index);
      }
   }

   @Override
   public int set(int index, int element) {
      synchronized(this.mutex) {
         return this.list.set(index, element);
      }
   }

   @Override
   public void set(int offset, int[] values) {
      synchronized(this.mutex) {
         this.list.set(offset, values);
      }
   }

   @Override
   public void set(int offset, int[] values, int valOffset, int length) {
      synchronized(this.mutex) {
         this.list.set(offset, values, valOffset, length);
      }
   }

   @Override
   public int replace(int offset, int val) {
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
   public int removeAt(int offset) {
      synchronized(this.mutex) {
         return this.list.removeAt(offset);
      }
   }

   @Override
   public void add(int[] vals) {
      synchronized(this.mutex) {
         this.list.add(vals);
      }
   }

   @Override
   public void add(int[] vals, int offset, int length) {
      synchronized(this.mutex) {
         this.list.add(vals, offset, length);
      }
   }

   @Override
   public void insert(int offset, int value) {
      synchronized(this.mutex) {
         this.list.insert(offset, value);
      }
   }

   @Override
   public void insert(int offset, int[] values) {
      synchronized(this.mutex) {
         this.list.insert(offset, values);
      }
   }

   @Override
   public void insert(int offset, int[] values, int valOffset, int len) {
      synchronized(this.mutex) {
         this.list.insert(offset, values, valOffset, len);
      }
   }

   @Override
   public int indexOf(int o) {
      synchronized(this.mutex) {
         return this.list.indexOf(o);
      }
   }

   @Override
   public int lastIndexOf(int o) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(o);
      }
   }

   @Override
   public TIntList subList(int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return new TSynchronizedIntList(this.list.subList(fromIndex, toIndex), this.mutex);
      }
   }

   @Override
   public int[] toArray(int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(offset, len);
      }
   }

   @Override
   public int[] toArray(int[] dest, int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, offset, len);
      }
   }

   @Override
   public int[] toArray(int[] dest, int source_pos, int dest_pos, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, source_pos, dest_pos, len);
      }
   }

   @Override
   public int indexOf(int offset, int value) {
      synchronized(this.mutex) {
         return this.list.indexOf(offset, value);
      }
   }

   @Override
   public int lastIndexOf(int offset, int value) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(offset, value);
      }
   }

   @Override
   public void fill(int val) {
      synchronized(this.mutex) {
         this.list.fill(val);
      }
   }

   @Override
   public void fill(int fromIndex, int toIndex, int val) {
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
   public int binarySearch(int value) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value);
      }
   }

   @Override
   public int binarySearch(int value, int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value, fromIndex, toIndex);
      }
   }

   @Override
   public TIntList grep(TIntProcedure condition) {
      synchronized(this.mutex) {
         return this.list.grep(condition);
      }
   }

   @Override
   public TIntList inverseGrep(TIntProcedure condition) {
      synchronized(this.mutex) {
         return this.list.inverseGrep(condition);
      }
   }

   @Override
   public int max() {
      synchronized(this.mutex) {
         return this.list.max();
      }
   }

   @Override
   public int min() {
      synchronized(this.mutex) {
         return this.list.min();
      }
   }

   @Override
   public int sum() {
      synchronized(this.mutex) {
         return this.list.sum();
      }
   }

   @Override
   public boolean forEachDescending(TIntProcedure procedure) {
      synchronized(this.mutex) {
         return this.list.forEachDescending(procedure);
      }
   }

   @Override
   public void transformValues(TIntFunction function) {
      synchronized(this.mutex) {
         this.list.transformValues(function);
      }
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TSynchronizedRandomAccessIntList(this.list) : this;
   }
}
