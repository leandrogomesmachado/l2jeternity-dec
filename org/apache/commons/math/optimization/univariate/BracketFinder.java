package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.optimization.GoalType;

public class BracketFinder {
   private static final double EPS_MIN = 1.0E-21;
   private static final double GOLD = 1.618034;
   private final double growLimit;
   private final int maxIterations;
   private int iterations;
   private int evaluations;
   private double lo;
   private double hi;
   private double mid;
   private double fLo;
   private double fHi;
   private double fMid;

   public BracketFinder() {
      this(100.0, 50);
   }

   public BracketFinder(double growLimit, int maxIterations) {
      if (growLimit <= 0.0) {
         throw new NotStrictlyPositiveException(growLimit);
      } else if (maxIterations <= 0) {
         throw new NotStrictlyPositiveException(maxIterations);
      } else {
         this.growLimit = growLimit;
         this.maxIterations = maxIterations;
      }
   }

   public void search(UnivariateRealFunction func, GoalType goal, double xA, double xB) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.reset();
      boolean isMinim = goal == GoalType.MINIMIZE;
      double fA = this.eval(func, xA);
      double fB = this.eval(func, xB);
      if (isMinim ? fA < fB : fA > fB) {
         double tmp = xA;
         xA = xB;
         xB = tmp;
         tmp = fA;
         fA = fB;
         fB = tmp;
      }

      double xC = xB + 1.618034 * (xB - xA);

      double fC;
      double fW;
      for(fC = this.eval(func, xC); isMinim ? fC < fB : fC > fB; fC = fW) {
         if (++this.iterations > this.maxIterations) {
            throw new MaxIterationsExceededException(this.maxIterations);
         }

         double tmp1 = (xB - xA) * (fB - fC);
         double tmp2 = (xB - xC) * (fB - fA);
         double val = tmp2 - tmp1;
         double denom = Math.abs(val) < 1.0E-21 ? 2.0E-21 : 2.0 * val;
         double w = xB - ((xB - xC) * tmp2 - (xB - xA) * tmp1) / denom;
         double wLim = xB + this.growLimit * (xC - xB);
         if ((w - xC) * (xB - w) > 0.0) {
            fW = this.eval(func, w);
            if (isMinim ? fW < fC : fW > fC) {
               xA = xB;
               xB = w;
               fA = fB;
               fB = fW;
               break;
            }

            if (isMinim ? fW > fB : fW < fB) {
               xC = w;
               fC = fW;
               break;
            }

            w = xC + 1.618034 * (xC - xB);
            fW = this.eval(func, w);
         } else if ((w - wLim) * (wLim - xC) >= 0.0) {
            w = wLim;
            fW = this.eval(func, wLim);
         } else if ((w - wLim) * (xC - w) > 0.0) {
            fW = this.eval(func, w);
            if (isMinim ? fW < fC : fW > fC) {
               xB = xC;
               xC = w;
               w += 1.618034 * (w - xB);
               fB = fC;
               fC = fW;
               fW = this.eval(func, w);
            }
         } else {
            w = xC + 1.618034 * (xC - xB);
            fW = this.eval(func, w);
         }

         xA = xB;
         xB = xC;
         xC = w;
         fA = fB;
         fB = fC;
      }

      this.lo = xA;
      this.mid = xB;
      this.hi = xC;
      this.fLo = fA;
      this.fMid = fB;
      this.fHi = fC;
   }

   public int getIterations() {
      return this.iterations;
   }

   public int getEvaluations() {
      return this.evaluations;
   }

   public double getLo() {
      return this.lo;
   }

   public double getFLow() {
      return this.fLo;
   }

   public double getHi() {
      return this.hi;
   }

   public double getFHi() {
      return this.fHi;
   }

   public double getMid() {
      return this.mid;
   }

   public double getFMid() {
      return this.fMid;
   }

   private double eval(UnivariateRealFunction f, double x) throws FunctionEvaluationException {
      ++this.evaluations;
      return f.value(x);
   }

   private void reset() {
      this.iterations = 0;
      this.evaluations = 0;
   }
}
