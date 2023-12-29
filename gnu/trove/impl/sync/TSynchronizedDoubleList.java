package gnu.trove.impl.sync;

import gnu.trove.function.TDoubleFunction;
import gnu.trove.list.TDoubleList;
import gnu.trove.procedure.TDoubleProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TSynchronizedDoubleList extends TSynchronizedDoubleCollection implements TDoubleList {
   static final long serialVersionUID = -7754090372962971524L;
   final TDoubleList list;

   public TSynchronizedDoubleList(TDoubleList list) {
      super(list);
      this.list = list;
   }

   public TSynchronizedDoubleList(TDoubleList list, Object mutex) {
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
   public double get(int index) {
      synchronized(this.mutex) {
         return this.list.get(index);
      }
   }

   @Override
   public double set(int index, double element) {
      synchronized(this.mutex) {
         return this.list.set(index, element);
      }
   }

   @Override
   public void set(int offset, double[] values) {
      synchronized(this.mutex) {
         this.list.set(offset, values);
      }
   }

   @Override
   public void set(int offset, double[] values, int valOffset, int length) {
      synchronized(this.mutex) {
         this.list.set(offset, values, valOffset, length);
      }
   }

   @Override
   public double replace(int offset, double val) {
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
   public double removeAt(int offset) {
      synchronized(this.mutex) {
         return this.list.removeAt(offset);
      }
   }

   @Override
   public void add(double[] vals) {
      synchronized(this.mutex) {
         this.list.add(vals);
      }
   }

   @Override
   public void add(double[] vals, int offset, int length) {
      synchronized(this.mutex) {
         this.list.add(vals, offset, length);
      }
   }

   @Override
   public void insert(int offset, double value) {
      synchronized(this.mutex) {
         this.list.insert(offset, value);
      }
   }

   @Override
   public void insert(int offset, double[] values) {
      synchronized(this.mutex) {
         this.list.insert(offset, values);
      }
   }

   @Override
   public void insert(int offset, double[] values, int valOffset, int len) {
      synchronized(this.mutex) {
         this.list.insert(offset, values, valOffset, len);
      }
   }

   @Override
   public int indexOf(double o) {
      synchronized(this.mutex) {
         return this.list.indexOf(o);
      }
   }

   @Override
   public int lastIndexOf(double o) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(o);
      }
   }

   @Override
   public TDoubleList subList(int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return new TSynchronizedDoubleList(this.list.subList(fromIndex, toIndex), this.mutex);
      }
   }

   @Override
   public double[] toArray(int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(offset, len);
      }
   }

   @Override
   public double[] toArray(double[] dest, int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, offset, len);
      }
   }

   @Override
   public double[] toArray(double[] dest, int source_pos, int dest_pos, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, source_pos, dest_pos, len);
      }
   }

   @Override
   public int indexOf(int offset, double value) {
      synchronized(this.mutex) {
         return this.list.indexOf(offset, value);
      }
   }

   @Override
   public int lastIndexOf(int offset, double value) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(offset, value);
      }
   }

   @Override
   public void fill(double val) {
      synchronized(this.mutex) {
         this.list.fill(val);
      }
   }

   @Override
   public void fill(int fromIndex, int toIndex, double val) {
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
   public int binarySearch(double value) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value);
      }
   }

   @Override
   public int binarySearch(double value, int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value, fromIndex, toIndex);
      }
   }

   @Override
   public TDoubleList grep(TDoubleProcedure condition) {
      synchronized(this.mutex) {
         return this.list.grep(condition);
      }
   }

   @Override
   public TDoubleList inverseGrep(TDoubleProcedure condition) {
      synchronized(this.mutex) {
         return this.list.inverseGrep(condition);
      }
   }

   @Override
   public double max() {
      synchronized(this.mutex) {
         return this.list.max();
      }
   }

   @Override
   public double min() {
      synchronized(this.mutex) {
         return this.list.min();
      }
   }

   @Override
   public double sum() {
      synchronized(this.mutex) {
         return this.list.sum();
      }
   }

   @Override
   public boolean forEachDescending(TDoubleProcedure procedure) {
      synchronized(this.mutex) {
         return this.list.forEachDescending(procedure);
      }
   }

   @Override
   public void transformValues(TDoubleFunction function) {
      synchronized(this.mutex) {
         this.list.transformValues(function);
      }
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TSynchronizedRandomAccessDoubleList(this.list) : this;
   }
}
