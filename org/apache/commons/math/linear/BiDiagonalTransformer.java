package org.apache.commons.math.linear;

import org.apache.commons.math.util.FastMath;

class BiDiagonalTransformer {
   private final double[][] householderVectors;
   private final double[] main;
   private final double[] secondary;
   private RealMatrix cachedU;
   private RealMatrix cachedB;
   private RealMatrix cachedV;

   public BiDiagonalTransformer(RealMatrix matrix) {
      int m = matrix.getRowDimension();
      int n = matrix.getColumnDimension();
      int p = FastMath.min(m, n);
      this.householderVectors = matrix.getData();
      this.main = new double[p];
      this.secondary = new double[p - 1];
      this.cachedU = null;
      this.cachedB = null;
      this.cachedV = null;
      if (m >= n) {
         this.transformToUpperBiDiagonal();
      } else {
         this.transformToLowerBiDiagonal();
      }
   }

   public RealMatrix getU() {
      if (this.cachedU == null) {
         int m = this.householderVectors.length;
         int n = this.householderVectors[0].length;
         int p = this.main.length;
         int diagOffset = m >= n ? 0 : 1;
         double[] diagonal = m >= n ? this.main : this.secondary;
         this.cachedU = MatrixUtils.createRealMatrix(m, m);

         for(int k = m - 1; k >= p; --k) {
            this.cachedU.setEntry(k, k, 1.0);
         }

         for(int k = p - 1; k >= diagOffset; --k) {
            double[] hK = this.householderVectors[k];
            this.cachedU.setEntry(k, k, 1.0);
            if (hK[k - diagOffset] != 0.0) {
               for(int j = k; j < m; ++j) {
                  double alpha = 0.0;

                  for(int i = k; i < m; ++i) {
                     alpha -= this.cachedU.getEntry(i, j) * this.householderVectors[i][k - diagOffset];
                  }

                  alpha /= diagonal[k - diagOffset] * hK[k - diagOffset];

                  for(int i = k; i < m; ++i) {
                     this.cachedU.addToEntry(i, j, -alpha * this.householderVectors[i][k - diagOffset]);
                  }
               }
            }
         }

         if (diagOffset > 0) {
            this.cachedU.setEntry(0, 0, 1.0);
         }
      }

      return this.cachedU;
   }

   public RealMatrix getB() {
      if (this.cachedB == null) {
         int m = this.householderVectors.length;
         int n = this.householderVectors[0].length;
         this.cachedB = MatrixUtils.createRealMatrix(m, n);

         for(int i = 0; i < this.main.length; ++i) {
            this.cachedB.setEntry(i, i, this.main[i]);
            if (m < n) {
               if (i > 0) {
                  this.cachedB.setEntry(i, i - 1, this.secondary[i - 1]);
               }
            } else if (i < this.main.length - 1) {
               this.cachedB.setEntry(i, i + 1, this.secondary[i]);
            }
         }
      }

      return this.cachedB;
   }

   public RealMatrix getV() {
      if (this.cachedV == null) {
         int m = this.householderVectors.length;
         int n = this.householderVectors[0].length;
         int p = this.main.length;
         int diagOffset = m >= n ? 1 : 0;
         double[] diagonal = m >= n ? this.secondary : this.main;
         this.cachedV = MatrixUtils.createRealMatrix(n, n);

         for(int k = n - 1; k >= p; --k) {
            this.cachedV.setEntry(k, k, 1.0);
         }

         for(int k = p - 1; k >= diagOffset; --k) {
            double[] hK = this.householderVectors[k - diagOffset];
            this.cachedV.setEntry(k, k, 1.0);
            if (hK[k] != 0.0) {
               for(int j = k; j < n; ++j) {
                  double beta = 0.0;

                  for(int i = k; i < n; ++i) {
                     beta -= this.cachedV.getEntry(i, j) * hK[i];
                  }

                  beta /= diagonal[k - diagOffset] * hK[k];

                  for(int i = k; i < n; ++i) {
                     this.cachedV.addToEntry(i, j, -beta * hK[i]);
                  }
               }
            }
         }

         if (diagOffset > 0) {
            this.cachedV.setEntry(0, 0, 1.0);
         }
      }

      return this.cachedV;
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

   boolean isUpperBiDiagonal() {
      return this.householderVectors.length >= this.householderVectors[0].length;
   }

   private void transformToUpperBiDiagonal() {
      int m = this.householderVectors.length;
      int n = this.householderVectors[0].length;

      for(int k = 0; k < n; ++k) {
         double xNormSqr = 0.0;

         for(int i = k; i < m; ++i) {
            double c = this.householderVectors[i][k];
            xNormSqr += c * c;
         }

         double[] hK = this.householderVectors[k];
         double a = hK[k] > 0.0 ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
         this.main[k] = a;
         if (a != 0.0) {
            hK[k] -= a;

            for(int j = k + 1; j < n; ++j) {
               double alpha = 0.0;

               for(int i = k; i < m; ++i) {
                  double[] hI = this.householderVectors[i];
                  alpha -= hI[j] * hI[k];
               }

               alpha /= a * this.householderVectors[k][k];

               for(int i = k; i < m; ++i) {
                  double[] hI = this.householderVectors[i];
                  hI[j] -= alpha * hI[k];
               }
            }
         }

         if (k < n - 1) {
            xNormSqr = 0.0;

            for(int j = k + 1; j < n; ++j) {
               double c = hK[j];
               xNormSqr += c * c;
            }

            double b = hK[k + 1] > 0.0 ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
            this.secondary[k] = b;
            if (b != 0.0) {
               hK[k + 1] -= b;

               for(int i = k + 1; i < m; ++i) {
                  double[] hI = this.householderVectors[i];
                  double beta = 0.0;

                  for(int j = k + 1; j < n; ++j) {
                     beta -= hI[j] * hK[j];
                  }

                  beta /= b * hK[k + 1];

                  for(int j = k + 1; j < n; ++j) {
                     hI[j] -= beta * hK[j];
                  }
               }
            }
         }
      }
   }

   private void transformToLowerBiDiagonal() {
      int m = this.householderVectors.length;
      int n = this.householderVectors[0].length;

      for(int k = 0; k < m; ++k) {
         double[] hK = this.householderVectors[k];
         double xNormSqr = 0.0;

         for(int j = k; j < n; ++j) {
            double c = hK[j];
            xNormSqr += c * c;
         }

         double a = hK[k] > 0.0 ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
         this.main[k] = a;
         if (a != 0.0) {
            hK[k] -= a;

            for(int i = k + 1; i < m; ++i) {
               double[] hI = this.householderVectors[i];
               double alpha = 0.0;

               for(int j = k; j < n; ++j) {
                  alpha -= hI[j] * hK[j];
               }

               alpha /= a * this.householderVectors[k][k];

               for(int j = k; j < n; ++j) {
                  hI[j] -= alpha * hK[j];
               }
            }
         }

         if (k < m - 1) {
            double[] hKp1 = this.householderVectors[k + 1];
            xNormSqr = 0.0;

            for(int i = k + 1; i < m; ++i) {
               double c = this.householderVectors[i][k];
               xNormSqr += c * c;
            }

            double b = hKp1[k] > 0.0 ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
            this.secondary[k] = b;
            if (b != 0.0) {
               hKp1[k] -= b;

               for(int j = k + 1; j < n; ++j) {
                  double beta = 0.0;

                  for(int i = k + 1; i < m; ++i) {
                     double[] hI = this.householderVectors[i];
                     beta -= hI[j] * hI[k];
                  }

                  beta /= b * hKp1[k];

                  for(int i = k + 1; i < m; ++i) {
                     double[] hI = this.householderVectors[i];
                     hI[j] -= beta * hI[k];
                  }
               }
            }
         }
      }
   }
}
