package org.apache.commons.math.linear;

import java.util.Iterator;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface RealVector {
   RealVector mapToSelf(UnivariateRealFunction var1) throws FunctionEvaluationException;

   RealVector map(UnivariateRealFunction var1) throws FunctionEvaluationException;

   Iterator<RealVector.Entry> iterator();

   Iterator<RealVector.Entry> sparseIterator();

   RealVector copy();

   RealVector add(RealVector var1);

   RealVector add(double[] var1);

   RealVector subtract(RealVector var1);

   RealVector subtract(double[] var1);

   RealVector mapAdd(double var1);

   RealVector mapAddToSelf(double var1);

   RealVector mapSubtract(double var1);

   RealVector mapSubtractToSelf(double var1);

   RealVector mapMultiply(double var1);

   RealVector mapMultiplyToSelf(double var1);

   RealVector mapDivide(double var1);

   RealVector mapDivideToSelf(double var1);

   @Deprecated
   RealVector mapPow(double var1);

   @Deprecated
   RealVector mapPowToSelf(double var1);

   @Deprecated
   RealVector mapExp();

   @Deprecated
   RealVector mapExpToSelf();

   @Deprecated
   RealVector mapExpm1();

   @Deprecated
   RealVector mapExpm1ToSelf();

   @Deprecated
   RealVector mapLog();

   @Deprecated
   RealVector mapLogToSelf();

   @Deprecated
   RealVector mapLog10();

   @Deprecated
   RealVector mapLog10ToSelf();

   @Deprecated
   RealVector mapLog1p();

   @Deprecated
   RealVector mapLog1pToSelf();

   @Deprecated
   RealVector mapCosh();

   @Deprecated
   RealVector mapCoshToSelf();

   @Deprecated
   RealVector mapSinh();

   @Deprecated
   RealVector mapSinhToSelf();

   @Deprecated
   RealVector mapTanh();

   @Deprecated
   RealVector mapTanhToSelf();

   @Deprecated
   RealVector mapCos();

   @Deprecated
   RealVector mapCosToSelf();

   @Deprecated
   RealVector mapSin();

   @Deprecated
   RealVector mapSinToSelf();

   @Deprecated
   RealVector mapTan();

   @Deprecated
   RealVector mapTanToSelf();

   @Deprecated
   RealVector mapAcos();

   @Deprecated
   RealVector mapAcosToSelf();

   @Deprecated
   RealVector mapAsin();

   @Deprecated
   RealVector mapAsinToSelf();

   @Deprecated
   RealVector mapAtan();

   @Deprecated
   RealVector mapAtanToSelf();

   @Deprecated
   RealVector mapInv();

   @Deprecated
   RealVector mapInvToSelf();

   @Deprecated
   RealVector mapAbs();

   @Deprecated
   RealVector mapAbsToSelf();

   @Deprecated
   RealVector mapSqrt();

   @Deprecated
   RealVector mapSqrtToSelf();

   @Deprecated
   RealVector mapCbrt();

   @Deprecated
   RealVector mapCbrtToSelf();

   @Deprecated
   RealVector mapCeil();

   @Deprecated
   RealVector mapCeilToSelf();

   @Deprecated
   RealVector mapFloor();

   @Deprecated
   RealVector mapFloorToSelf();

   @Deprecated
   RealVector mapRint();

   @Deprecated
   RealVector mapRintToSelf();

   @Deprecated
   RealVector mapSignum();

   @Deprecated
   RealVector mapSignumToSelf();

   @Deprecated
   RealVector mapUlp();

   @Deprecated
   RealVector mapUlpToSelf();

   RealVector ebeMultiply(RealVector var1);

   RealVector ebeMultiply(double[] var1);

   RealVector ebeDivide(RealVector var1);

   RealVector ebeDivide(double[] var1);

   double[] getData();

   double dotProduct(RealVector var1);

   double dotProduct(double[] var1);

   double getNorm();

   double getL1Norm();

   double getLInfNorm();

   double getDistance(RealVector var1);

   double getDistance(double[] var1);

   double getL1Distance(RealVector var1);

   double getL1Distance(double[] var1);

   double getLInfDistance(RealVector var1);

   double getLInfDistance(double[] var1);

   RealVector unitVector();

   void unitize();

   RealVector projection(RealVector var1);

   RealVector projection(double[] var1);

   RealMatrix outerProduct(RealVector var1);

   RealMatrix outerProduct(double[] var1);

   double getEntry(int var1);

   void setEntry(int var1, double var2);

   int getDimension();

   RealVector append(RealVector var1);

   RealVector append(double var1);

   RealVector append(double[] var1);

   RealVector getSubVector(int var1, int var2);

   void setSubVector(int var1, RealVector var2);

   void setSubVector(int var1, double[] var2);

   void set(double var1);

   double[] toArray();

   boolean isNaN();

   boolean isInfinite();

   public abstract static class Entry {
      private int index;

      public abstract double getValue();

      public abstract void setValue(double var1);

      public int getIndex() {
         return this.index;
      }

      public void setIndex(int index) {
         this.index = index;
      }
   }
}
