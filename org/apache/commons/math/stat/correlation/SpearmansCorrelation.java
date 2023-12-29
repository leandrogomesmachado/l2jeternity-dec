package org.apache.commons.math.stat.correlation;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.ranking.NaturalRanking;
import org.apache.commons.math.stat.ranking.RankingAlgorithm;

public class SpearmansCorrelation {
   private final RealMatrix data;
   private final RankingAlgorithm rankingAlgorithm;
   private final PearsonsCorrelation rankCorrelation;

   public SpearmansCorrelation(RealMatrix dataMatrix, RankingAlgorithm rankingAlgorithm) {
      this.data = dataMatrix.copy();
      this.rankingAlgorithm = rankingAlgorithm;
      this.rankTransform(this.data);
      this.rankCorrelation = new PearsonsCorrelation(this.data);
   }

   public SpearmansCorrelation(RealMatrix dataMatrix) {
      this(dataMatrix, new NaturalRanking());
   }

   public SpearmansCorrelation() {
      this.data = null;
      this.rankingAlgorithm = new NaturalRanking();
      this.rankCorrelation = null;
   }

   public RealMatrix getCorrelationMatrix() {
      return this.rankCorrelation.getCorrelationMatrix();
   }

   public PearsonsCorrelation getRankCorrelation() {
      return this.rankCorrelation;
   }

   public RealMatrix computeCorrelationMatrix(RealMatrix matrix) {
      RealMatrix matrixCopy = matrix.copy();
      this.rankTransform(matrixCopy);
      return new PearsonsCorrelation().computeCorrelationMatrix(matrixCopy);
   }

   public RealMatrix computeCorrelationMatrix(double[][] matrix) {
      return this.computeCorrelationMatrix(new BlockRealMatrix(matrix));
   }

   public double correlation(double[] xArray, double[] yArray) throws IllegalArgumentException {
      if (xArray.length != yArray.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, xArray.length, yArray.length);
      } else if (xArray.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, xArray.length, 2);
      } else {
         return new PearsonsCorrelation().correlation(this.rankingAlgorithm.rank(xArray), this.rankingAlgorithm.rank(yArray));
      }
   }

   private void rankTransform(RealMatrix matrix) {
      for(int i = 0; i < matrix.getColumnDimension(); ++i) {
         matrix.setColumn(i, this.rankingAlgorithm.rank(matrix.getColumn(i)));
      }
   }
}
