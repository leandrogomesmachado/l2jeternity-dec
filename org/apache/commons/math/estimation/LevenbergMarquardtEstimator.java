package org.apache.commons.math.estimation;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

@Deprecated
public class LevenbergMarquardtEstimator extends AbstractEstimator implements Serializable {
   private static final long serialVersionUID = -5705952631533171019L;
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

   public LevenbergMarquardtEstimator() {
      this.setMaxCostEval(1000);
      this.setInitialStepBoundFactor(100.0);
      this.setCostRelativeTolerance(1.0E-10);
      this.setParRelativeTolerance(1.0E-10);
      this.setOrthoTolerance(1.0E-10);
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

   @Override
   public void estimate(EstimationProblem problem) throws EstimationException {
      this.initializeEstimate(problem);
      this.solvedCols = FastMath.min(this.rows, this.cols);
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
      double[] work1 = new double[this.cols];
      double[] work2 = new double[this.cols];
      double[] work3 = new double[this.cols];
      this.updateResidualsAndCost();
      this.lmPar = 0.0;
      boolean firstIteration = true;

      while(true) {
         this.updateJacobian();
         this.qrDecomposition();
         this.qTy(this.residuals);

         for(int k = 0; k < this.solvedCols; ++k) {
            int pk = this.permutation[k];
            this.jacobian[k * this.cols + pk] = this.diagR[pk];
         }

         if (firstIteration) {
            xNorm = 0.0;

            for(int k = 0; k < this.cols; ++k) {
               double dk = this.jacNorm[k];
               if (dk == 0.0) {
                  dk = 1.0;
               }

               double xk = dk * this.parameters[k].getEstimate();
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
                  int index = pj;

                  for(int i = 0; i <= j; ++i) {
                     sum += this.jacobian[index] * this.residuals[i];
                     index += this.cols;
                  }

                  maxCosine = FastMath.max(maxCosine, FastMath.abs(sum) / (s * this.cost));
               }
            }
         }

         if (maxCosine <= this.orthoTolerance) {
            return;
         }

         for(int j = 0; j < this.cols; ++j) {
            diag[j] = FastMath.max(diag[j], this.jacNorm[j]);
         }

         double ratio = 0.0;

         while(ratio < 1.0E-4) {
            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               oldX[pj] = this.parameters[pj].getEstimate();
            }

            double previousCost = this.cost;
            double[] tmpVec = this.residuals;
            this.residuals = oldRes;
            oldRes = tmpVec;
            this.determineLMParameter(tmpVec, delta, diag, work1, work2, work3);
            double lmNorm = 0.0;

            for(int j = 0; j < this.solvedCols; ++j) {
               int pj = this.permutation[j];
               this.lmDir[pj] = -this.lmDir[pj];
               this.parameters[pj].setEstimate(oldX[pj] + this.lmDir[pj]);
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
               int index = pj;

               for(int i = 0; i <= j; ++i) {
                  work1[i] += this.jacobian[index] * dirJ;
                  index += this.cols;
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
                  double xK = diag[k] * this.parameters[k].getEstimate();
                  xNorm += xK * xK;
               }

               xNorm = FastMath.sqrt(xNorm);
            } else {
               this.cost = previousCost;

               for(int j = 0; j < this.solvedCols; ++j) {
                  int pj = this.permutation[j];
                  this.parameters[pj].setEstimate(oldX[pj]);
               }

               double[] var48 = this.residuals;
               this.residuals = tmpVec;
               oldRes = var48;
            }

            if (FastMath.abs(actRed) <= this.costRelativeTolerance && preRed <= this.costRelativeTolerance && ratio <= 2.0
               || delta <= this.parRelativeTolerance * xNorm) {
               return;
            }

            if (FastMath.abs(actRed) <= 2.2204E-16 && preRed <= 2.2204E-16 && ratio <= 2.0) {
               throw new EstimationException(
                  "cost relative tolerance is too small ({0}), no further reduction in the sum of squares is possible", this.costRelativeTolerance
               );
            }

            if (delta <= 2.2204E-16 * xNorm) {
               throw new EstimationException(
                  "parameters relative tolerance is too small ({0}), no further improvement in the approximate solution is possible",
                  this.parRelativeTolerance
               );
            }

            if (maxCosine <= 2.2204E-16) {
               throw new EstimationException("orthogonality tolerance is too small ({0}), solution is orthogonal to the jacobian", this.orthoTolerance);
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
         int index = pk;

         for(int i = 0; i < k; ++i) {
            this.lmDir[this.permutation[i]] -= ypk * this.jacobian[index];
            index += this.cols;
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
               int index = pj;

               for(int i = 0; i < j; ++i) {
                  sum += this.jacobian[index] * work1[this.permutation[i]];
                  index += this.cols;
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
            int index = pj;

            for(int i = 0; i <= j; ++i) {
               sum += this.jacobian[index] * qy[i];
               index += this.cols;
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
                  work1[this.permutation[i]] -= this.jacobian[i * this.cols + pj] * tmp;
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
            this.jacobian[i * this.cols + pj] = this.jacobian[j * this.cols + this.permutation[i]];
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
               double rkk = this.jacobian[k * this.cols + pk];
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

               this.jacobian[k * this.cols + pk] = cos * rkk + sin * lmDiag[k];
               double temp = cos * work[k] + sin * qtbpj;
               qtbpj = -sin * work[k] + cos * qtbpj;
               work[k] = temp;

               for(int i = k + 1; i < this.solvedCols; ++i) {
                  double rik = this.jacobian[i * this.cols + pk];
                  double temp2 = cos * rik + sin * lmDiag[i];
                  lmDiag[i] = -sin * rik + cos * lmDiag[i];
                  this.jacobian[i * this.cols + pk] = temp2;
               }
            }
         }

         int index = j * this.cols + this.permutation[j];
         lmDiag[j] = this.jacobian[index];
         this.jacobian[index] = this.lmDir[j];
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
               sum += this.jacobian[i * this.cols + pj] * work[i];
            }

            work[j] = (work[j] - sum) / lmDiag[j];
         }
      }

      for(int j = 0; j < this.lmDir.length; ++j) {
         this.lmDir[this.permutation[j]] = work[j];
      }
   }

   private void qrDecomposition() throws EstimationException {
      for(int k = 0; k < this.cols; ++k) {
         this.permutation[k] = k;
         double norm2 = 0.0;

         for(int index = k; index < this.jacobian.length; index += this.cols) {
            double akk = this.jacobian[index];
            norm2 += akk * akk;
         }

         this.jacNorm[k] = FastMath.sqrt(norm2);
      }

      for(int k = 0; k < this.cols; ++k) {
         int nextColumn = -1;
         double ak2 = Double.NEGATIVE_INFINITY;

         for(int i = k; i < this.cols; ++i) {
            double norm2 = 0.0;
            int iDiag = k * this.cols + this.permutation[i];

            for(int index = iDiag; index < this.jacobian.length; index += this.cols) {
               double aki = this.jacobian[index];
               norm2 += aki * aki;
            }

            if (Double.isInfinite(norm2) || Double.isNaN(norm2)) {
               throw new EstimationException(LocalizedFormats.UNABLE_TO_PERFORM_QR_DECOMPOSITION_ON_JACOBIAN, this.rows, this.cols);
            }

            if (norm2 > ak2) {
               nextColumn = i;
               ak2 = norm2;
            }
         }

         if (ak2 == 0.0) {
            this.rank = k;
            return;
         }

         int pk = this.permutation[nextColumn];
         this.permutation[nextColumn] = this.permutation[k];
         this.permutation[k] = pk;
         int kDiag = k * this.cols + pk;
         double akk = this.jacobian[kDiag];
         double alpha = akk > 0.0 ? -FastMath.sqrt(ak2) : FastMath.sqrt(ak2);
         double betak = 1.0 / (ak2 - akk * alpha);
         this.beta[pk] = betak;
         this.diagR[pk] = alpha;
         this.jacobian[kDiag] -= alpha;

         for(int dk = this.cols - 1 - k; dk > 0; --dk) {
            int dkp = this.permutation[k + dk] - pk;
            double gamma = 0.0;

            for(int index = kDiag; index < this.jacobian.length; index += this.cols) {
               gamma += this.jacobian[index] * this.jacobian[index + dkp];
            }

            gamma *= betak;

            for(int index = kDiag; index < this.jacobian.length; index += this.cols) {
               this.jacobian[index + dkp] -= gamma * this.jacobian[index];
            }
         }
      }

      this.rank = this.solvedCols;
   }

   private void qTy(double[] y) {
      for(int k = 0; k < this.cols; ++k) {
         int pk = this.permutation[k];
         int kDiag = k * this.cols + pk;
         double gamma = 0.0;
         int index = kDiag;

         for(int i = k; i < this.rows; ++i) {
            gamma += this.jacobian[index] * y[i];
            index += this.cols;
         }

         gamma *= this.beta[pk];
         index = kDiag;

         for(int i = k; i < this.rows; ++i) {
            y[i] -= gamma * this.jacobian[index];
            index += this.cols;
         }
      }
   }
}
