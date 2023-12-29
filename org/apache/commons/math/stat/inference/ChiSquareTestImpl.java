package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class ChiSquareTestImpl implements UnknownDistributionChiSquareTest {
   private ChiSquaredDistribution distribution;

   public ChiSquareTestImpl() {
      this(new ChiSquaredDistributionImpl(1.0));
   }

   public ChiSquareTestImpl(ChiSquaredDistribution x) {
      this.setDistribution(x);
   }

   @Override
   public double chiSquare(double[] expected, long[] observed) throws IllegalArgumentException {
      if (expected.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, expected.length, 2);
      } else if (expected.length != observed.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, expected.length, observed.length);
      } else {
         this.checkPositive(expected);
         this.checkNonNegative(observed);
         double sumExpected = 0.0;
         double sumObserved = 0.0;

         for(int i = 0; i < observed.length; ++i) {
            sumExpected += expected[i];
            sumObserved += (double)observed[i];
         }

         double ratio = 1.0;
         boolean rescale = false;
         if (FastMath.abs(sumExpected - sumObserved) > 1.0E-5) {
            ratio = sumObserved / sumExpected;
            rescale = true;
         }

         double sumSq = 0.0;

         for(int i = 0; i < observed.length; ++i) {
            if (rescale) {
               double dev = (double)observed[i] - ratio * expected[i];
               sumSq += dev * dev / (ratio * expected[i]);
            } else {
               double dev = (double)observed[i] - expected[i];
               sumSq += dev * dev / expected[i];
            }
         }

         return sumSq;
      }
   }

   @Override
   public double chiSquareTest(double[] expected, long[] observed) throws IllegalArgumentException, MathException {
      this.distribution.setDegreesOfFreedom((double)expected.length - 1.0);
      return 1.0 - this.distribution.cumulativeProbability(this.chiSquare(expected, observed));
   }

   @Override
   public boolean chiSquareTest(double[] expected, long[] observed, double alpha) throws IllegalArgumentException, MathException {
      if (!(alpha <= 0.0) && !(alpha > 0.5)) {
         return this.chiSquareTest(expected, observed) < alpha;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5);
      }
   }

   @Override
   public double chiSquare(long[][] counts) throws IllegalArgumentException {
      this.checkArray(counts);
      int nRows = counts.length;
      int nCols = counts[0].length;
      double[] rowSum = new double[nRows];
      double[] colSum = new double[nCols];
      double total = 0.0;

      for(int row = 0; row < nRows; ++row) {
         for(int col = 0; col < nCols; ++col) {
            rowSum[row] += (double)counts[row][col];
            colSum[col] += (double)counts[row][col];
            total += (double)counts[row][col];
         }
      }

      double sumSq = 0.0;
      double expected = 0.0;

      for(int row = 0; row < nRows; ++row) {
         for(int col = 0; col < nCols; ++col) {
            expected = rowSum[row] * colSum[col] / total;
            sumSq += ((double)counts[row][col] - expected) * ((double)counts[row][col] - expected) / expected;
         }
      }

      return sumSq;
   }

   @Override
   public double chiSquareTest(long[][] counts) throws IllegalArgumentException, MathException {
      this.checkArray(counts);
      double df = ((double)counts.length - 1.0) * ((double)counts[0].length - 1.0);
      this.distribution.setDegreesOfFreedom(df);
      return 1.0 - this.distribution.cumulativeProbability(this.chiSquare(counts));
   }

   @Override
   public boolean chiSquareTest(long[][] counts, double alpha) throws IllegalArgumentException, MathException {
      if (!(alpha <= 0.0) && !(alpha > 0.5)) {
         return this.chiSquareTest(counts) < alpha;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0.0, 0.5);
      }
   }

   @Override
   public double chiSquareDataSetsComparison(long[] observed1, long[] observed2) throws IllegalArgumentException {
      if (observed1.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, observed1.length, 2);
      } else if (observed1.length != observed2.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, observed1.length, observed2.length);
      } else {
         this.checkNonNegative(observed1);
         this.checkNonNegative(observed2);
         long countSum1 = 0L;
         long countSum2 = 0L;
         boolean unequalCounts = false;
         double weight = 0.0;

         for(int i = 0; i < observed1.length; ++i) {
            countSum1 += observed1[i];
            countSum2 += observed2[i];
         }

         if (countSum1 == 0L) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OBSERVED_COUNTS_ALL_ZERO, 1);
         } else if (countSum2 == 0L) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OBSERVED_COUNTS_ALL_ZERO, 2);
         } else {
            unequalCounts = countSum1 != countSum2;
            if (unequalCounts) {
               weight = FastMath.sqrt((double)countSum1 / (double)countSum2);
            }

            double sumSq = 0.0;
            double dev = 0.0;
            double obs1 = 0.0;
            double obs2 = 0.0;

            for(int i = 0; i < observed1.length; ++i) {
               if (observed1[i] == 0L && observed2[i] == 0L) {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OBSERVED_COUNTS_BOTTH_ZERO_FOR_ENTRY, i);
               }

               obs1 = (double)observed1[i];
               obs2 = (double)observed2[i];
               if (unequalCounts) {
                  dev = obs1 / weight - obs2 * weight;
               } else {
                  dev = obs1 - obs2;
               }

               sumSq += dev * dev / (obs1 + obs2);
            }

            return sumSq;
         }
      }
   }

   @Override
   public double chiSquareTestDataSetsComparison(long[] observed1, long[] observed2) throws IllegalArgumentException, MathException {
      this.distribution.setDegreesOfFreedom((double)observed1.length - 1.0);
      return 1.0 - this.distribution.cumulativeProbability(this.chiSquareDataSetsComparison(observed1, observed2));
   }

   @Override
   public boolean chiSquareTestDataSetsComparison(long[] observed1, long[] observed2, double alpha) throws IllegalArgumentException, MathException {
      if (!(alpha <= 0.0) && !(alpha > 0.5)) {
         return this.chiSquareTestDataSetsComparison(observed1, observed2) < alpha;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0.0, 0.5);
      }
   }

   private void checkArray(long[][] in) throws IllegalArgumentException {
      if (in.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, in.length, 2);
      } else if (in[0].length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, in[0].length, 2);
      } else {
         this.checkRectangular(in);
         this.checkNonNegative(in);
      }
   }

   private void checkRectangular(long[][] in) {
      for(int i = 1; i < in.length; ++i) {
         if (in[i].length != in[0].length) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, in[i].length, in[0].length);
         }
      }
   }

   private void checkPositive(double[] in) throws IllegalArgumentException {
      for(int i = 0; i < in.length; ++i) {
         if (in[i] <= 0.0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_ELEMENT_AT_INDEX, i, in[i]);
         }
      }
   }

   private void checkNonNegative(long[] in) throws IllegalArgumentException {
      for(int i = 0; i < in.length; ++i) {
         if (in[i] < 0L) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NEGATIVE_ELEMENT_AT_INDEX, i, in[i]);
         }
      }
   }

   private void checkNonNegative(long[][] in) throws IllegalArgumentException {
      for(int i = 0; i < in.length; ++i) {
         for(int j = 0; j < in[i].length; ++j) {
            if (in[i][j] < 0L) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NEGATIVE_ELEMENT_AT_2D_INDEX, i, j, in[i][j]);
            }
         }
      }
   }

   public void setDistribution(ChiSquaredDistribution value) {
      this.distribution = value;
   }
}
