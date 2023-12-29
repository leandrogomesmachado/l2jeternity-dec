package org.apache.commons.math.optimization.general;

import java.util.Arrays;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.util.FastMath;

public class LevenbergMarquardtOptimizer extends AbstractLeastSquaresOptimizer {
   private int solvedCols;
   private double[] diagR;
   private double[] jacNorm;
   private double[] beta;
   private int[] permutation;
   private int rank;
   private double lmPar;
   private double[] lmDir;
   private double initialStepBoundFactor;
   private double costRelativeTolerance;
   private double parRelativeTolerance;
   private double orthoTolerance;
   private double qrRankingThreshold;

   public LevenbergMarquardtOptimizer() {
      this.setMaxIterations(1000);
      this.setConvergenceChecker(null);
      this.setInitialStepBoundFactor(100.0);
      this.setCostRelativeTolerance(1.0E-10);
      this.setParRelativeTolerance(1.0E-10);
      this.setOrthoTolerance(1.0E-10);
      this.setQRRankingThreshold(Double.MIN_NORMAL);
   }

   public void setInitialStepBoundFactor(double initialStepBoundFactor) {
      this.initialStepBoundFactor = initialStepBoundFactor;
   }

   public void setCostRelativeTolerance(double costRelativeTolerance) {
      this.costRelativeTolerance = costRelativeTolerance;
   }

   public void setParRelativeTolerance(double parRelativeTolerance) {
      this.parRelativeTolerance = parRelativeTolerance;
   }

   public void setOrthoTolerance(double orthoTolerance) {
      this.orthoTolerance = orthoTolerance;
   }

   public void setQRRankingThreshold(double threshold) {
      this.qrRankingThreshold = threshold;
   }

   @Override
   protected VectorialPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      this.solvedCols = Math.min(this.rows, this.cols);
      this.diagR = new double[this.cols];
      this.jacNorm = new double[this.cols];
      this.beta = new double[this.cols];
      this.permutation = new int[this.cols];
      this.lmDir = new double[this.cols];
      double delta = 0.0;
      double xNorm = 0.0;
      double[] diag = new double[this.cols];
      double[] oldX = new double[this.cols];
      double[] oldRes = new double[this.rows];
      double[] oldObj = new double[this.rows];
      double[] qtf = new double[this.rows];
      double[] work1 = new double[this.cols];
      double[] work2 = new double[this.cols];
      double[] work3 = new double[this.cols];
      this.updateResidualsAndCost();
      this.lmPar = 0.0;
      boolean firstIteration = true;
      VectorialPointValuePair current = new VectorialPointValuePair(this.point, this.objective);

      while(true) {
         for(int i = 0; i < this.rows; ++i) {
            qtf[i] = this.wresiduals[i];
         }

         this.incrementIterationsCounter();
         VectorialPointValuePair previous = current;
         this.updateJacobian();
         this.qrDecomposition();
         this.qTy(qtf);

         for(int k = 0; k < this.solvedCols; ++k) {
            int pk = this.permutation[k];
            this.wjacobian[k][pk] = this.diagR[pk];
         }

         if (firstIteration) {
            xNorm = 0.0;

            for(int k = 0; k < this.cols; ++k) {
               double dk = this.jacNorm[k];
               if (dk == 0.0) {
                  dk = 1.0;
               }

               double xk = dk * this.point[k];
               xNorm += xk * xk;
               diag[k] = dk;
            }

            xNorm = FastMath.sqrt(xNorm);
            delta = xNorm == 0.0 ? this.initialStepBoundFactor : this.initialStepBoundFactor * xNorm;
         }

         double maxCosine = 0.0;
         if (this.cost != 0.0) {
            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               double s = this.jacNorm[pj];
               if (s != 0.0) {
                  double sum = 0.0;

                  for(int i = 0; i <= j; ++i) {
                     sum += this.wjacobian[i][pj] * qtf[i];
                  }

                  maxCosine = FastMath.max(maxCosine, FastMath.abs(sum) / (s * this.cost));
               }
            }
         }

         if (maxCosine <= this.orthoTolerance) {
            this.updateResidualsAndCost();
            return new VectorialPointValuePair(this.point, this.objective);
         }

         for(int j = 0; j < this.cols; ++j) {
            diag[j] = FastMath.max(diag[j], this.jacNorm[j]);
         }

         double ratio = 0.0;

         while(ratio < 1.0E-4) {
            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               oldX[pj] = this.point[pj];
            }

            double previousCost = this.cost;
            double[] tmpVec = this.residuals;
            this.residuals = oldRes;
            oldRes = tmpVec;
            tmpVec = this.objective;
            this.objective = oldObj;
            oldObj = tmpVec;
            this.determineLMParameter(qtf, delta, diag, work1, work2, work3);
            double lmNorm = 0.0;

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               this.lmDir[pj] = -this.lmDir[pj];
               this.point[pj] = oldX[pj] + this.lmDir[pj];
               double s = diag[pj] * this.lmDir[pj];
               lmNorm += s * s;
            }

            lmNorm = FastMath.sqrt(lmNorm);
            if (firstIteration) {
               delta = FastMath.min(delta, lmNorm);
            }

            this.updateResidualsAndCost();
            double actRed = -1.0;
            if (0.1 * this.cost < previousCost) {
               double r = this.cost / previousCost;
               actRed = 1.0 - r * r;
            }

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               double dirJ = this.lmDir[pj];
               work1[j] = 0.0;

               for(int i = 0; i <= j; ++i) {
                  work1[i] += this.wjacobian[i][pj] * dirJ;
               }
            }

            double coeff1 = 0.0;

            for(int j = 0; j < this.solvedCols; ++j) {
               coeff1 += work1[j] * work1[j];
            }

            double pc2 = previousCost * previousCost;
            coeff1 /= pc2;
            double coeff2 = this.lmPar * lmNorm * lmNorm / pc2;
            double preRed = coeff1 + 2.0 * coeff2;
            double dirDer = -(coeff1 + coeff2);
            ratio = preRed == 0.0 ? 0.0 : actRed / preRed;
            if (ratio <= 0.25) {
               double tmp = actRed < 0.0 ? 0.5 * dirDer / (dirDer + 0.5 * actRed) : 0.5;
               if (0.1 * this.cost >= previousCost || tmp < 0.1) {
                  tmp = 0.1;
               }

               delta = tmp * FastMath.min(delta, 10.0 * lmNorm);
               this.lmPar /= tmp;
            } else if (this.lmPar == 0.0 || ratio >= 0.75) {
               delta = 2.0 * lmNorm;
               this.lmPar *= 0.5;
            }

            if (ratio >= 1.0E-4) {
               firstIteration = false;
               xNorm = 0.0;

               for(int k = 0; k < this.cols; ++k) {
                  double xK = diag[k] * this.point[k];
                  xNorm += xK * xK;
               }

               xNorm = FastMath.sqrt(xNorm);
               current = new VectorialPointValuePair(this.point, this.objective);
               if (this.checker != null && this.checker.converged(this.getIterations(), previous, current)) {
                  return current;
               }
            } else {
               this.cost = previousCost;

               for(int j = 0; j < this.solvedCols; ++j) {
                  int pj = this.permutation[j];
                  this.point[pj] = oldX[pj];
               }

               double[] var53 = this.residuals;
               this.residuals = tmpVec;
               oldRes = var53;
               double[] var54 = this.objective;
               this.objective = tmpVec;
               oldObj = var54;
            }

            if (this.checker == null
               && (
                  FastMath.abs(actRed) <= this.costRelativeTolerance && preRed <= this.costRelativeTolerance && ratio <= 2.0
                     || delta <= this.parRelativeTolerance * xNorm
               )) {
               return current;
            }

            if (FastMath.abs(actRed) <= 2.2204E-16 && preRed <= 2.2204E-16 && ratio <= 2.0) {
               throw new OptimizationException(LocalizedFormats.TOO_SMALL_COST_RELATIVE_TOLERANCE, this.costRelativeTolerance);
            }

            if (delta <= 2.2204E-16 * xNorm) {
               throw new OptimizationException(LocalizedFormats.TOO_SMALL_PARAMETERS_RELATIVE_TOLERANCE, this.parRelativeTolerance);
            }

            if (maxCosine <= 2.2204E-16) {
               throw new OptimizationException(LocalizedFormats.TOO_SMALL_ORTHOGONALITY_TOLERANCE, this.orthoTolerance);
            }
         }
      }
   }

   private void determineLMParameter(double[] qy, double delta, double[] diag, double[] work1, double[] work2, double[] work3) {
      for(int j = 0; j < this.rank; ++j) {
         this.lmDir[this.permutation[j]] = qy[j];
      }

      for(int j = this.rank; j < this.cols; ++j) {
         this.lmDir[this.permutation[j]] = 0.0;
      }

      for(int k = this.rank - 1; k >= 0; --k) {
         int pk = this.permutation[k];
         double ypk = this.lmDir[pk] / this.diagR[pk];

         for(int i = 0; i < k; ++i) {
            this.lmDir[this.permutation[i]] -= ypk * this.wjacobian[i][pk];
         }

         this.lmDir[pk] = ypk;
      }

      double dxNorm = 0.0;

      for(int j = 0; j < this.solvedCols; ++j) {
         int pj = this.permutation[j];
         double s = diag[pj] * this.lmDir[pj];
         work1[pj] = s;
         dxNorm += s * s;
      }

      dxNorm = FastMath.sqrt(dxNorm);
      double fp = dxNorm - delta;
      if (fp <= 0.1 * delta) {
         this.lmPar = 0.0;
      } else {
         double parl = 0.0;
         if (this.rank == this.solvedCols) {
            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               work1[pj] *= diag[pj] / dxNorm;
            }

            double sum2 = 0.0;

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               double sum = 0.0;

               for(int i = 0; i < j; ++i) {
                  sum += this.wjacobian[i][pj] * work1[this.permutation[i]];
               }

               double s = (work1[pj] - sum) / this.diagR[pj];
               work1[pj] = s;
               sum2 += s * s;
            }

            parl = fp / (delta * sum2);
         }

         double sum2 = 0.0;

         for(int j = 0; j < this.solvedCols; ++j) {
            int pj = this.permutation[j];
            double sum = 0.0;

            for(int i = 0; i <= j; ++i) {
               sum += this.wjacobian[i][pj] * qy[i];
            }

            sum /= diag[pj];
            sum2 += sum * sum;
         }

         double gNorm = FastMath.sqrt(sum2);
         double paru = gNorm / delta;
         if (paru == 0.0) {
            paru = 2.2251E-308 / FastMath.min(delta, 0.1);
         }

         this.lmPar = FastMath.min(paru, FastMath.max(this.lmPar, parl));
         if (this.lmPar == 0.0) {
            this.lmPar = gNorm / dxNorm;
         }

         for(int countdown = 10; countdown >= 0; --countdown) {
            if (this.lmPar == 0.0) {
               this.lmPar = FastMath.max(2.2251E-308, 0.001 * paru);
            }

            double sPar = FastMath.sqrt(this.lmPar);

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               work1[pj] = sPar * diag[pj];
            }

            this.determineLMDirection(qy, work1, work2, work3);
            dxNorm = 0.0;

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               double s = diag[pj] * this.lmDir[pj];
               work3[pj] = s;
               dxNorm += s * s;
            }

            dxNorm = FastMath.sqrt(dxNorm);
            double previousFP = fp;
            fp = dxNorm - delta;
            if (FastMath.abs(fp) <= 0.1 * delta || parl == 0.0 && fp <= previousFP && previousFP < 0.0) {
               return;
            }

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               work1[pj] = work3[pj] * diag[pj] / dxNorm;
            }

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               work1[pj] /= work2[j];
               double tmp = work1[pj];

               for(int i = j + 1; i < this.solvedCols; ++i) {
                  work1[this.permutation[i]] -= this.wjacobian[i][pj] * tmp;
               }
            }

            sum2 = 0.0;

            for(int j = 0; j < this.solvedCols; ++j) {
               double s = work1[this.permutation[j]];
               sum2 += s * s;
            }

            double correction = fp / (delta * sum2);
            if (fp > 0.0) {
               parl = FastMath.max(parl, this.lmPar);
            } else if (fp < 0.0) {
               paru = FastMath.min(paru, this.lmPar);
            }

            this.lmPar = FastMath.max(parl, this.lmPar + correction);
         }
      }
   }

   private void determineLMDirection(double[] qy, double[] diag, double[] lmDiag, double[] work) {
      for(int j = 0; j < this.solvedCols; ++j) {
         int pj = this.permutation[j];

         for(int i = j + 1; i < this.solvedCols; ++i) {
            this.wjacobian[i][pj] = this.wjacobian[j][this.permutation[i]];
         }

         this.lmDir[j] = this.diagR[pj];
         work[j] = qy[j];
      }

      for(int j = 0; j < this.solvedCols; ++j) {
         int pj = this.permutation[j];
         double dpj = diag[pj];
         if (dpj != 0.0) {
            Arrays.fill(lmDiag, j + 1, lmDiag.length, 0.0);
         }

         lmDiag[j] = dpj;
         double qtbpj = 0.0;

         for(int k = j; k < this.solvedCols; ++k) {
            int pk = this.permutation[k];
            if (lmDiag[k] != 0.0) {
               double rkk = this.wjacobian[k][pk];
               double sin;
               double cos;
               if (FastMath.abs(rkk) < FastMath.abs(lmDiag[k])) {
                  double cotan = rkk / lmDiag[k];
                  sin = 1.0 / FastMath.sqrt(1.0 + cotan * cotan);
                  cos = sin * cotan;
               } else {
                  double tan = lmDiag[k] / rkk;
                  cos = 1.0 / FastMath.sqrt(1.0 + tan * tan);
                  sin = cos * tan;
               }

               this.wjacobian[k][pk] = cos * rkk + sin * lmDiag[k];
               double temp = cos * work[k] + sin * qtbpj;
               qtbpj = -sin * work[k] + cos * qtbpj;
               work[k] = temp;

               for(int i = k + 1; i < this.solvedCols; ++i) {
                  double rik = this.wjacobian[i][pk];
                  double temp2 = cos * rik + sin * lmDiag[i];
                  lmDiag[i] = -sin * rik + cos * lmDiag[i];
                  this.wjacobian[i][pk] = temp2;
               }
            }
         }

         lmDiag[j] = this.wjacobian[j][this.permutation[j]];
         this.wjacobian[j][this.permutation[j]] = this.lmDir[j];
      }

      int nSing = this.solvedCols;

      for(int j = 0; j < this.solvedCols; ++j) {
         if (lmDiag[j] == 0.0 && nSing == this.solvedCols) {
            nSing = j;
         }

         if (nSing < this.solvedCols) {
            work[j] = 0.0;
         }
      }

      if (nSing > 0) {
         for(int j = nSing - 1; j >= 0; --j) {
            int pj = this.permutation[j];
            double sum = 0.0;

            for(int i = j + 1; i < nSing; ++i) {
               sum += this.wjacobian[i][pj] * work[i];
            }

            work[j] = (work[j] - sum) / lmDiag[j];
         }
      }

      for(int j = 0; j < this.lmDir.length; ++j) {
         this.lmDir[this.permutation[j]] = work[j];
      }
   }

   private void qrDecomposition() throws OptimizationException {
      for(int k = 0; k < this.cols; ++k) {
         this.permutation[k] = k;
         double norm2 = 0.0;

         for(int i = 0; i < this.wjacobian.length; ++i) {
            double akk = this.wjacobian[i][k];
            norm2 += akk * akk;
         }

         this.jacNorm[k] = FastMath.sqrt(norm2);
      }

      for(int k = 0; k < this.cols; ++k) {
         int nextColumn = -1;
         double ak2 = Double.NEGATIVE_INFINITY;

         for(int i = k; i < this.cols; ++i) {
            double norm2 = 0.0;

            for(int j = k; j < this.wjacobian.length; ++j) {
               double aki = this.wjacobian[j][this.permutation[i]];
               norm2 += aki * aki;
            }

            if (Double.isInfinite(norm2) || Double.isNaN(norm2)) {
               throw new OptimizationException(LocalizedFormats.UNABLE_TO_PERFORM_QR_DECOMPOSITION_ON_JACOBIAN, this.rows, this.cols);
            }

            if (norm2 > ak2) {
               nextColumn = i;
               ak2 = norm2;
            }
         }

         if (ak2 <= this.qrRankingThreshold) {
            this.rank = k;
            return;
         }

         int pk = this.permutation[nextColumn];
         this.permutation[nextColumn] = this.permutation[k];
         this.permutation[k] = pk;
         double akk = this.wjacobian[k][pk];
         double alpha = akk > 0.0 ? -FastMath.sqrt(ak2) : FastMath.sqrt(ak2);
         double betak = 1.0 / (ak2 - akk * alpha);
         this.beta[pk] = betak;
         this.diagR[pk] = alpha;
         this.wjacobian[k][pk] -= alpha;

         for(int dk = this.cols - 1 - k; dk > 0; --dk) {
            double gamma = 0.0;

            for(int j = k; j < this.wjacobian.length; ++j) {
               gamma += this.wjacobian[j][pk] * this.wjacobian[j][this.permutation[k + dk]];
            }

            gamma *= betak;

            for(int j = k; j < this.wjacobian.length; ++j) {
               this.wjacobian[j][this.permutation[k + dk]] -= gamma * this.wjacobian[j][pk];
            }
         }
      }

      this.rank = this.solvedCols;
   }

   private void qTy(double[] y) {
      for(int k = 0; k < this.cols; ++k) {
         int pk = this.permutation[k];
         double gamma = 0.0;

         for(int i = k; i < this.rows; ++i) {
            gamma += this.wjacobian[i][pk] * y[i];
         }

         gamma *= this.beta[pk];

         for(int i = k; i < this.rows; ++i) {
            y[i] -= gamma * this.wjacobian[i][pk];
         }
      }
   }
}
