package gnu.trove.impl.sync;

import gnu.trove.function.TByteFunction;
import gnu.trove.list.TByteList;
import gnu.trove.procedure.TByteProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TSynchronizedByteList extends TSynchronizedByteCollection implements TByteList {
   static final long serialVersionUID = -7754090372962971524L;
   final TByteList list;

   public TSynchronizedByteList(TByteList list) {
      super(list);
      this.list = list;
   }

   public TSynchronizedByteList(TByteList list, Object mutex) {
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
   public byte get(int index) {
      synchronized(this.mutex) {
         return this.list.get(index);
      }
   }

   @Override
   public byte set(int index, byte element) {
      synchronized(this.mutex) {
         return this.list.set(index, element);
      }
   }

   @Override
   public void set(int offset, byte[] values) {
      synchronized(this.mutex) {
         this.list.set(offset, values);
      }
   }

   @Override
   public void set(int offset, byte[] values, int valOffset, int length) {
      synchronized(this.mutex) {
         this.list.set(offset, values, valOffset, length);
      }
   }

   @Override
   public byte replace(int offset, byte val) {
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
   public byte removeAt(int offset) {
      synchronized(this.mutex) {
         return this.list.removeAt(offset);
      }
   }

   @Override
   public void add(byte[] vals) {
      synchronized(this.mutex) {
         this.list.add(vals);
      }
   }

   @Override
   public void add(byte[] vals, int offset, int length) {
      synchronized(this.mutex) {
         this.list.add(vals, offset, length);
      }
   }

   @Override
   public void insert(int offset, byte value) {
      synchronized(this.mutex) {
         this.list.insert(offset, value);
      }
   }

   @Override
   public void insert(int offset, byte[] values) {
      synchronized(this.mutex) {
         this.list.insert(offset, values);
      }
   }

   @Override
   public void insert(int offset, byte[] values, int valOffset, int len) {
      synchronized(this.mutex) {
         this.list.insert(offset, values, valOffset, len);
      }
   }

   @Override
   public int indexOf(byte o) {
      synchronized(this.mutex) {
         return this.list.indexOf(o);
      }
   }

   @Override
   public int lastIndexOf(byte o) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(o);
      }
   }

   @Override
   public TByteList subList(int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return new TSynchronizedByteList(this.list.subList(fromIndex, toIndex), this.mutex);
      }
   }

   @Override
   public byte[] toArray(int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(offset, len);
      }
   }

   @Override
   public byte[] toArray(byte[] dest, int offset, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, offset, len);
      }
   }

   @Override
   public byte[] toArray(byte[] dest, int source_pos, int dest_pos, int len) {
      synchronized(this.mutex) {
         return this.list.toArray(dest, source_pos, dest_pos, len);
      }
   }

   @Override
   public int indexOf(int offset, byte value) {
      synchronized(this.mutex) {
         return this.list.indexOf(offset, value);
      }
   }

   @Override
   public int lastIndexOf(int offset, byte value) {
      synchronized(this.mutex) {
         return this.list.lastIndexOf(offset, value);
      }
   }

   @Override
   public void fill(byte val) {
      synchronized(this.mutex) {
         this.list.fill(val);
      }
   }

   @Override
   public void fill(int fromIndex, int toIndex, byte val) {
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
   public int binarySearch(byte value) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value);
      }
   }

   @Override
   public int binarySearch(byte value, int fromIndex, int toIndex) {
      synchronized(this.mutex) {
         return this.list.binarySearch(value, fromIndex, toIndex);
      }
   }

   @Override
   public TByteList grep(TByteProcedure condition) {
      synchronized(this.mutex) {
         return this.list.grep(condition);
      }
   }

   @Override
   public TByteList inverseGrep(TByteProcedure condition) {
      synchronized(this.mutex) {
         return this.list.inverseGrep(condition);
      }
   }

   @Override
   public byte max() {
      synchronized(this.mutex) {
         return this.list.max();
      }
   }

   @Override
   public byte min() {
      synchronized(this.mutex) {
         return this.list.min();
      }
   }

   @Override
   public byte sum() {
      synchronized(this.mutex) {
         return this.list.sum();
      }
   }

   @Override
   public boolean forEachDescending(TByteProcedure procedure) {
      synchronized(this.mutex) {
         return this.list.forEachDescending(procedure);
      }
   }

   @Override
   public void transformValues(TByteFunction function) {
      synchronized(this.mutex) {
         this.list.transformValues(function);
      }
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TSynchronizedRandomAccessByteList(this.list) : this;
   }
}
