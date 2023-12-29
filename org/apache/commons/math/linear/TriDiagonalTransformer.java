package org.apache.commons.math.linear;

import java.util.Arrays;
import org.apache.commons.math.util.FastMath;

class TriDiagonalTransformer {
   private final double[][] householderVectors;
   private final double[] main;
   private final double[] secondary;
   private RealMatrix cachedQ;
   private RealMatrix cachedQt;
   private RealMatrix cachedT;

   public TriDiagonalTransformer(RealMatrix matrix) throws InvalidMatrixException {
      if (!matrix.isSquare()) {
         throw new NonSquareMatrixException(matrix.getRowDimension(), matrix.getColumnDimension());
      } else {
         int m = matrix.getRowDimension();
         this.householderVectors = matrix.getData();
         this.main = new double[m];
         this.secondary = new double[m - 1];
         this.cachedQ = null;
         this.cachedQt = null;
         this.cachedT = null;
         this.transform();
      }
   }

   public RealMatrix getQ() {
      if (this.cachedQ == null) {
         this.cachedQ = this.getQT().transpose();
      }

      return this.cachedQ;
   }

   public RealMatrix getQT() {
      if (this.cachedQt == null) {
         int m = this.householderVectors.length;
         this.cachedQt = MatrixUtils.createRealMatrix(m, m);

         for(int k = m - 1; k >= 1; --k) {
            double[] hK = this.householderVectors[k - 1];
            double inv = 1.0 / (this.secondary[k - 1] * hK[k]);
            this.cachedQt.setEntry(k, k, 1.0);
            if (hK[k] != 0.0) {
               double beta = 1.0 / this.secondary[k - 1];
               this.cachedQt.setEntry(k, k, 1.0 + beta * hK[k]);

               for(int i = k + 1; i < m; ++i) {
                  this.cachedQt.setEntry(k, i, beta * hK[i]);
               }

               for(int j = k + 1; j < m; ++j) {
                  beta = 0.0;

                  for(int i = k + 1; i < m; ++i) {
                     beta += this.cachedQt.getEntry(j, i) * hK[i];
                  }

                  beta *= inv;
                  this.cachedQt.setEntry(j, k, beta * hK[k]);

                  for(int i = k + 1; i < m; ++i) {
                     this.cachedQt.addToEntry(j, i, beta * hK[i]);
                  }
               }
            }
         }

         this.cachedQt.setEntry(0, 0, 1.0);
      }

      return this.cachedQt;
   }

   public RealMatrix getT() {
      if (this.cachedT == null) {
         int m = this.main.length;
         this.cachedT = MatrixUtils.createRealMatrix(m, m);

         for(int i = 0; i < m; ++i) {
            this.cachedT.setEntry(i, i, this.main[i]);
            if (i > 0) {
               this.cachedT.setEntry(i, i - 1, this.secondary[i - 1]);
            }

            if (i < this.main.length - 1) {
               this.cachedT.setEntry(i, i + 1, this.secondary[i]);
            }
         }
      }

      return this.cachedT;
   }

   double[][] getHouseholderVectorsRef() {
      return this.householderVectors;
   }

   double[] getMainDiagonalRef() {
      return this.main;
   }

   double[] getSecondaryDiagonalRef() {
      return this.secondary;
   }

   private void transform() {
      int m = this.householderVectors.length;
      double[] z = new double[m];

      for(int k = 0; k < m - 1; ++k) {
         double[] hK = this.householderVectors[k];
         this.main[k] = hK[k];
         double xNormSqr = 0.0;

         for(int j = k + 1; j < m; ++j) {
            double c = hK[j];
            xNormSqr += c * c;
         }

         double a = hK[k + 1] > 0.0 ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
         this.secondary[k] = a;
         if (a != 0.0) {
            hK[k + 1] -= a;
            double beta = -1.0 / (a * hK[k + 1]);
            Arrays.fill(z, k + 1, m, 0.0);

            for(int i = k + 1; i < m; ++i) {
               double[] hI = this.householderVectors[i];
               double hKI = hK[i];
               double zI = hI[i] * hKI;

               for(int j = i + 1; j < m; ++j) {
                  double hIJ = hI[j];
                  zI += hIJ * hK[j];
                  z[j] += hIJ * hKI;
               }

               z[i] = beta * (z[i] + zI);
            }

            double gamma = 0.0;

            for(int i = k + 1; i < m; ++i) {
               gamma += z[i] * hK[i];
            }

            gamma *= beta / 2.0;

            for(int i = k + 1; i < m; ++i) {
               z[i] -= gamma * hK[i];
            }

            for(int i = k + 1; i < m; ++i) {
               double[] hI = this.householderVectors[i];

               for(int j = i; j < m; ++j) {
                  hI[j] -= hK[i] * z[j] + z[i] * hK[j];
               }
            }
         }
      }

      this.main[m - 1] = this.householderVectors[m - 1][m - 1];
   }
}
