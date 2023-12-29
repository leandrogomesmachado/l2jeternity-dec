package gnu.trove.impl.sync;

import gnu.trove.function.TLongFunction;
import gnu.trove.list.TLongList;
import gnu.trove.procedure.TLongProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TSynchronizedLongList extends TSynchronizedLongCollection implements TLongList {
   static final long serialVersionUID = -7754090372962971524L;
   final TLongList list;

   public TSynchronizedLongList(TLongList list) {
      super(list);
      this.list = list;
   }

   public TSynchronizedLongList(TLongList list, Object mutex) {
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
   public long get(int index) {
      synchronized(this.mutex) {
         return this.list.get(index);
      }
   }

   @Override
   public long set(int index, long element) {
      synchronized(this.mutex) {
         return this.list.set(index, element);
      }
   }

   @Override
   public void set(int offset, long[] values) {
      synchronized(this.mutex) {
         this.list.set(offset, values);
      }
   }

   @Override
   public void set(int offset, long[] values, int valOffset, int length) {
      synchronized(this.mutex) {
         this.list.set(offset, values, valOffset, length);
      }
   }

   @Override
   public long replace(int offset, long val) {
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
   public long removeAt(int offset) {
      synchronized(this.mutex) {
         return this.list.removeAt(offset);
      }
   }

   @Override
   public void add(long[] vals) {
      synchronized(this.mutex) {
         this.list.add(vals);
      }
   }

   @Override
   public void add(long[] vals, int offset, int length) {
      synchronized(this.mutex) {
         this.list.add(vals, offset, length);
      }
   }

   @Override
   public void insert(int offset, long value) {
      synchronized(this.mutex) {
         this.list.insert(offset, value);
      }
   }

   @Override
   public void insert(int offset, long[] values) {
      synchronized(this.mutex) {
         this.list.insert(offset, values);
      }
   }

   @Override
   public void insert(int offset, long[] values, int valOffset, int len) {
      synchronized(this.mutex) {
         this.list.insert(offset, values, valOffset, len);
      }
   }

   @Override
   public int indexOf(long o) {
      synchronized(this.mutex) {
         return this.list.indexOf(o);
      }
   }

   @Override
   public int lastIndexOf(long o) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(o);
      }
   }

   @Override
   public TLongList subList(int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return new TSynchronizedLongList(this.list.subList(fromIndex, toIndex), this.mutex);
      }
   }

   @Override
   public long[] toArray(int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(offset, len);
      }
   }

   @Override
   public long[] toArray(long[] dest, int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, offset, len);
      }
   }

   @Override
   public long[] toArray(long[] dest, int source_pos, int dest_pos, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, source_pos, dest_pos, len);
      }
   }

   @Override
   public int indexOf(int offset, long value) {
      synchronized(this.mutex) {
         return this.list.indexOf(offset, value);
      }
   }

   @Override
   public int lastIndexOf(int offset, long value) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(offset, value);
      }
   }

   @Override
   public void fill(long val) {
      synchronized(this.mutex) {
         this.list.fill(val);
      }
   }

   @Override
   public void fill(int fromIndex, int toIndex, long val) {
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
   public int binarySearch(long value) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value);
      }
   }

   @Override
   public int binarySearch(long value, int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value, fromIndex, toIndex);
      }
   }

   @Override
   public TLongList grep(TLongProcedure condition) {
      synchronized(this.mutex) {
         return this.list.grep(condition);
      }
   }

   @Override
   public TLongList inverseGrep(TLongProcedure condition) {
      synchronized(this.mutex) {
         return this.list.inverseGrep(condition);
      }
   }

   @Override
   public long max() {
      synchronized(this.mutex) {
         return this.list.max();
      }
   }

   @Override
   public long min() {
      synchronized(this.mutex) {
         return this.list.min();
      }
   }

   @Override
   public long sum() {
      synchronized(this.mutex) {
         return this.list.sum();
      }
   }

   @Override
   public boolean forEachDescending(TLongProcedure procedure) {
      synchronized(this.mutex) {
         return this.list.forEachDescending(procedure);
      }
   }

   @Override
   public void transformValues(TLongFunction function) {
      synchronized(this.mutex) {
         this.list.transformValues(function);
      }
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TSynchronizedRandomAccessLongList(this.list) : this;
   }
}
