package org.apache.commons.math.linear;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class SingularValueDecompositionImpl implements SingularValueDecomposition {
   private int m;
   private int n;
   private EigenDecomposition eigenDecomposition;
   private double[] singularValues;
   private RealMatrix cachedU;
   private RealMatrix cachedUt;
   private RealMatrix cachedS;
   private RealMatrix cachedV;
   private RealMatrix cachedVt;

   public SingularValueDecompositionImpl(RealMatrix matrix) throws InvalidMatrixException {
      this.m = matrix.getRowDimension();
      this.n = matrix.getColumnDimension();
      this.cachedU = null;
      this.cachedS = null;
      this.cachedV = null;
      this.cachedVt = null;
      double[][] localcopy = matrix.getData();
      double[][] matATA = new double[this.n][this.n];

      for(int i = 0; i < this.n; ++i) {
         for(int j = i; j < this.n; ++j) {
            matATA[i][j] = 0.0;

            for(int k = 0; k < this.m; ++k) {
               matATA[i][j] += localcopy[k][i] * localcopy[k][j];
            }

            matATA[j][i] = matATA[i][j];
         }
      }

      double[][] matAAT = new double[this.m][this.m];

      for(int i = 0; i < this.m; ++i) {
         for(int j = i; j < this.m; ++j) {
            matAAT[i][j] = 0.0;

            for(int k = 0; k < this.n; ++k) {
               matAAT[i][j] += localcopy[i][k] * localcopy[j][k];
            }

            matAAT[j][i] = matAAT[i][j];
         }
      }

      int p;
      if (this.m >= this.n) {
         p = this.n;
         this.eigenDecomposition = new EigenDecompositionImpl(new Array2DRowRealMatrix(matATA), 1.0);
         this.singularValues = this.eigenDecomposition.getRealEigenvalues();
         this.cachedV = this.eigenDecomposition.getV();
         this.eigenDecomposition = new EigenDecompositionImpl(new Array2DRowRealMatrix(matAAT), 1.0);
         this.cachedU = this.eigenDecomposition.getV().getSubMatrix(0, this.m - 1, 0, p - 1);
      } else {
         p = this.m;
         this.eigenDecomposition = new EigenDecompositionImpl(new Array2DRowRealMatrix(matAAT), 1.0);
         this.singularValues = this.eigenDecomposition.getRealEigenvalues();
         this.cachedU = this.eigenDecomposition.getV();
         this.eigenDecomposition = new EigenDecompositionImpl(new Array2DRowRealMatrix(matATA), 1.0);
         this.cachedV = this.eigenDecomposition.getV().getSubMatrix(0, this.n - 1, 0, p - 1);
      }

      for(int i = 0; i < p; ++i) {
         this.singularValues[i] = FastMath.sqrt(FastMath.abs(this.singularValues[i]));
      }

      for(int i = 0; i < p; ++i) {
         RealVector tmp = this.cachedU.getColumnVector(i);
         double product = matrix.operate(this.cachedV.getColumnVector(i)).dotProduct(tmp);
         if (product < 0.0) {
            this.cachedU.setColumnVector(i, tmp.mapMultiply(-1.0));
         }
      }
   }

   @Override
   public RealMatrix getU() throws InvalidMatrixException {
      return this.cachedU;
   }

   @Override
   public RealMatrix getUT() throws InvalidMatrixException {
      if (this.cachedUt == null) {
         this.cachedUt = this.getU().transpose();
      }

      return this.cachedUt;
   }

   @Override
   public RealMatrix getS() throws InvalidMatrixException {
      if (this.cachedS == null) {
         this.cachedS = MatrixUtils.createRealDiagonalMatrix(this.singularValues);
      }

      return this.cachedS;
   }

   @Override
   public double[] getSingularValues() throws InvalidMatrixException {
      return (double[])this.singularValues.clone();
   }

   @Override
   public RealMatrix getV() throws InvalidMatrixException {
      return this.cachedV;
   }

   @Override
   public RealMatrix getVT() throws InvalidMatrixException {
      if (this.cachedVt == null) {
         this.cachedVt = this.getV().transpose();
      }

      return this.cachedVt;
   }

   @Override
   public RealMatrix getCovariance(double minSingularValue) {
      int p = this.singularValues.length;
      int dimension = 0;

      while(dimension < p && this.singularValues[dimension] >= minSingularValue) {
         ++dimension;
      }

      if (dimension == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.TOO_LARGE_CUTOFF_SINGULAR_VALUE, minSingularValue, this.singularValues[0]);
      } else {
         final double[][] data = new double[dimension][p];
         this.getVT().walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
            @Override
            public void visit(int row, int column, double value) {
               data[row][column] = value / SingularValueDecompositionImpl.this.singularValues[row];
            }
         }, 0, dimension - 1, 0, p - 1);
         RealMatrix jv = new Array2DRowRealMatrix(data, false);
         return jv.transpose().multiply(jv);
      }
   }

   @Override
   public double getNorm() throws InvalidMatrixException {
      return this.singularValues[0];
   }

   @Override
   public double getConditionNumber() throws InvalidMatrixException {
      return this.singularValues[0] / this.singularValues[this.singularValues.length - 1];
   }

   @Override
   public int getRank() throws IllegalStateException {
      double threshold = (double)FastMath.max(this.m, this.n) * FastMath.ulp(this.singularValues[0]);

      for(int i = this.singularValues.length - 1; i >= 0; --i) {
         if (this.singularValues[i] > threshold) {
            return i + 1;
         }
      }

      return 0;
   }

   @Override
   public DecompositionSolver getSolver() {
      return new SingularValueDecompositionImpl.Solver(this.singularValues, this.getUT(), this.getV(), this.getRank() == Math.max(this.m, this.n));
   }

   private static class Solver implements DecompositionSolver {
      private final RealMatrix pseudoInverse;
      private boolean nonSingular;

      private Solver(double[] singularValues, RealMatrix uT, RealMatrix v, boolean nonSingular) {
         double[][] suT = uT.getData();

         for(int i = 0; i < singularValues.length; ++i) {
            double a;
            if (singularValues[i] > 0.0) {
               a = 1.0 / singularValues[i];
            } else {
               a = 0.0;
            }

            double[] suTi = suT[i];

            for(int j = 0; j < suTi.length; ++j) {
               suTi[j] *= a;
            }
         }

         this.pseudoInverse = v.multiply(new Array2DRowRealMatrix(suT, false));
         this.nonSingular = nonSingular;
      }

      @Override
      public double[] solve(double[] b) throws IllegalArgumentException {
         return this.pseudoInverse.operate(b);
      }

      @Override
      public RealVector solve(RealVector b) throws IllegalArgumentException {
         return this.pseudoInverse.operate(b);
      }

      @Override
      public RealMatrix solve(RealMatrix b) throws IllegalArgumentException {
         return this.pseudoInverse.multiply(b);
      }

      @Override
      public boolean isNonSingular() {
         return this.nonSingular;
      }

      @Override
      public RealMatrix getInverse() {
         return this.pseudoInverse;
      }
   }
}
