package org.apache.commons.math.stat.descriptive.rank;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.AbstractUnivariateStatistic;
import org.apache.commons.math.util.FastMath;

public class Percentile extends AbstractUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = -8091216485095130416L;
   private static final int MIN_SELECT_SIZE = 15;
   private static final int MAX_CACHED_LEVELS = 10;
   private double quantile = 0.0;
   private int[] cachedPivots;

   public Percentile() {
      this(50.0);
   }

   public Percentile(double p) {
      this.setQuantile(p);
      this.cachedPivots = null;
   }

   public Percentile(Percentile original) {
      copy(original, this);
   }

   @Override
   public void setData(double[] values) {
      if (values == null) {
         this.cachedPivots = null;
      } else {
         this.cachedPivots = new int[1023];
         Arrays.fill(this.cachedPivots, -1);
      }

      super.setData(values);
   }

   @Override
   public void setData(double[] values, int begin, int length) {
      if (values == null) {
         this.cachedPivots = null;
      } else {
         this.cachedPivots = new int[1023];
         Arrays.fill(this.cachedPivots, -1);
      }

      super.setData(values, begin, length);
   }

   public double evaluate(double p) {
      return this.evaluate(this.getDataRef(), p);
   }

   public double evaluate(double[] values, double p) {
      this.test(values, 0, 0);
      return this.evaluate(values, 0, values.length, p);
   }

   @Override
   public double evaluate(double[] values, int start, int length) {
      return this.evaluate(values, start, length, this.quantile);
   }

   public double evaluate(double[] values, int begin, int length, double p) {
      this.test(values, begin, length);
      if (p > 100.0 || p <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUNDS_QUANTILE_VALUE, p);
      } else if (length == 0) {
         return Double.NaN;
      } else if (length == 1) {
         return values[begin];
      } else {
         double n = (double)length;
         double pos = p * (n + 1.0) / 100.0;
         double fpos = FastMath.floor(pos);
         int intPos = (int)fpos;
         double dif = pos - fpos;
         double[] work;
         int[] pivotsHeap;
         if (values == this.getDataRef()) {
            work = this.getDataRef();
            pivotsHeap = this.cachedPivots;
         } else {
            work = new double[length];
            System.arraycopy(values, begin, work, 0, length);
            pivotsHeap = new int[1023];
            Arrays.fill(pivotsHeap, -1);
         }

         if (pos < 1.0) {
            return this.select(work, pivotsHeap, 0);
         } else if (pos >= n) {
            return this.select(work, pivotsHeap, length - 1);
         } else {
            double lower = this.select(work, pivotsHeap, intPos - 1);
            double upper = this.select(work, pivotsHeap, intPos);
            return lower + dif * (upper - lower);
         }
      }
   }

   private double select(double[] work, int[] pivotsHeap, int k) {
      int begin = 0;
      int end = work.length;
      int node = 0;

      while(end - begin > 15) {
         int pivot;
         if (node < pivotsHeap.length && pivotsHeap[node] >= 0) {
            pivot = pivotsHeap[node];
         } else {
            pivot = this.partition(work, begin, end, this.medianOf3(work, begin, end));
            if (node < pivotsHeap.length) {
               pivotsHeap[node] = pivot;
            }
         }

         if (k == pivot) {
            return work[k];
         }

         if (k < pivot) {
            end = pivot;
            node = Math.min(2 * node + 1, pivotsHeap.length);
         } else {
            begin = pivot + 1;
            node = Math.min(2 * node + 2, pivotsHeap.length);
         }
      }

      this.insertionSort(work, begin, end);
      return work[k];
   }

   int medianOf3(double[] work, int begin, int end) {
      int inclusiveEnd = end - 1;
      int middle = begin + (inclusiveEnd - begin) / 2;
      double wBegin = work[begin];
      double wMiddle = work[middle];
      double wEnd = work[inclusiveEnd];
      if (wBegin < wMiddle) {
         if (wMiddle < wEnd) {
            return middle;
         } else {
            return wBegin < wEnd ? inclusiveEnd : begin;
         }
      } else if (wBegin < wEnd) {
         return begin;
      } else {
         return wMiddle < wEnd ? inclusiveEnd : middle;
      }
   }

   private int partition(double[] work, int begin, int end, int pivot) {
      double value = work[pivot];
      work[pivot] = work[begin];
      int i = begin + 1;
      int j = end - 1;

      while(i < j) {
         while(i < j && work[j] >= value) {
            --j;
         }

         while(i < j && work[i] <= value) {
            ++i;
         }

         if (i < j) {
            double tmp = work[i];
            work[i++] = work[j];
            work[j--] = tmp;
         }
      }

      if (i >= end || work[i] > value) {
         --i;
      }

      work[begin] = work[i];
      work[i] = value;
      return i;
   }

   private void insertionSort(double[] work, int begin, int end) {
      for(int j = begin + 1; j < end; ++j) {
         double saved = work[j];

         int i;
         for(i = j - 1; i >= begin && saved < work[i]; --i) {
            work[i + 1] = work[i];
         }

         work[i + 1] = saved;
      }
   }

   public double getQuantile() {
      return this.quantile;
   }

   public void setQuantile(double p) {
      if (!(p <= 0.0) && !(p > 100.0)) {
         this.quantile = p;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUNDS_QUANTILE_VALUE, p);
      }
   }

   public Percentile copy() {
      Percentile result = new Percentile();
      copy(this, result);
      return result;
   }

   public static void copy(Percentile source, Percentile dest) {
      dest.setData(source.getDataRef());
      if (source.cachedPivots != null) {
         System.arraycopy(source.cachedPivots, 0, dest.cachedPivots, 0, source.cachedPivots.length);
      }

      dest.quantile = source.quantile;
   }
}
