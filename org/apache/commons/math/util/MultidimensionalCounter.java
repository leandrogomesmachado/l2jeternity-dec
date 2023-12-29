package org.apache.commons.math.util;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.OutOfRangeException;

public class MultidimensionalCounter implements Iterable<Integer> {
   private final int dimension;
   private final int[] uniCounterOffset;
   private final int[] size;
   private final int totalSize;
   private final int last;

   public MultidimensionalCounter(int... size) {
      this.dimension = size.length;
      this.size = this.copyOf(size, this.dimension);
      this.uniCounterOffset = new int[this.dimension];
      this.last = this.dimension - 1;
      int tS = size[this.last];

      for(int i = 0; i < this.last; ++i) {
         int count = 1;

         for(int j = i + 1; j < this.dimension; ++j) {
            count *= size[j];
         }

         this.uniCounterOffset[i] = count;
         tS *= size[i];
      }

      this.uniCounterOffset[this.last] = 0;
      if (tS <= 0) {
         throw new NotStrictlyPositiveException(tS);
      } else {
         this.totalSize = tS;
      }
   }

   public MultidimensionalCounter.Iterator iterator() {
      return new MultidimensionalCounter.Iterator();
   }

   public int getDimension() {
      return this.dimension;
   }

   public int[] getCounts(int index) {
      if (index >= 0 && index < this.totalSize) {
         int[] indices = new int[this.dimension];
         int count = 0;

         for(int i = 0; i < this.last; ++i) {
            int idx = 0;

            int offset;
            for(offset = this.uniCounterOffset[i]; count <= index; ++idx) {
               count += offset;
            }

            --idx;
            count -= offset;
            indices[i] = idx;
         }

         int idx;
         for(idx = 1; count < index; ++idx) {
            count += idx;
         }

         indices[this.last] = --idx;
         return indices;
      } else {
         throw new OutOfRangeException(index, 0, this.totalSize);
      }
   }

   public int getCount(int... c) throws OutOfRangeException {
      if (c.length != this.dimension) {
         throw new DimensionMismatchException(c.length, this.dimension);
      } else {
         int count = 0;

         for(int i = 0; i < this.dimension; ++i) {
            int index = c[i];
            if (index < 0 || index >= this.size[i]) {
               throw new OutOfRangeException(index, 0, this.size[i] - 1);
            }

            count += this.uniCounterOffset[i] * c[i];
         }

         return count + c[this.last];
      }
   }

   public int getSize() {
      return this.totalSize;
   }

   public int[] getSizes() {
      return this.copyOf(this.size, this.dimension);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < this.dimension; ++i) {
         sb.append("[").append(this.getCount(i)).append("]");
      }

      return sb.toString();
   }

   private int[] copyOf(int[] source, int newLen) {
      int[] output = new int[newLen];
      System.arraycopy(source, 0, output, 0, Math.min(source.length, newLen));
      return output;
   }

   public class Iterator implements java.util.Iterator<Integer> {
      private final int[] counter = new int[MultidimensionalCounter.this.dimension];
      private int count = -1;

      Iterator() {
         this.counter[MultidimensionalCounter.this.last] = -1;
      }

      @Override
      public boolean hasNext() {
         for(int i = 0; i < MultidimensionalCounter.this.dimension; ++i) {
            if (this.counter[i] != MultidimensionalCounter.this.size[i] - 1) {
               return true;
            }
         }

         return false;
      }

      public Integer next() {
         for(int i = MultidimensionalCounter.this.last; i >= 0; --i) {
            if (this.counter[i] != MultidimensionalCounter.this.size[i] - 1) {
               this.counter[i]++;
               break;
            }

            this.counter[i] = 0;
         }

         return ++this.count;
      }

      public int getCount() {
         return this.count;
      }

      public int[] getCounts() {
         return MultidimensionalCounter.this.copyOf(this.counter, MultidimensionalCounter.this.dimension);
      }

      public int getCount(int dim) {
         return this.counter[dim];
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
