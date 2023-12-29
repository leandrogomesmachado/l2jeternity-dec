package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class ZipfDistributionImpl extends AbstractIntegerDistribution implements ZipfDistribution, Serializable {
   private static final long serialVersionUID = -140627372283420404L;
   private int numberOfElements;
   private double exponent;

   public ZipfDistributionImpl(int numberOfElements, double exponent) throws IllegalArgumentException {
      this.setNumberOfElementsInternal(numberOfElements);
      this.setExponentInternal(exponent);
   }

   @Override
   public int getNumberOfElements() {
      return this.numberOfElements;
   }

   @Deprecated
   @Override
   public void setNumberOfElements(int n) {
      this.setNumberOfElementsInternal(n);
   }

   private void setNumberOfElementsInternal(int n) throws IllegalArgumentException {
      if (n <= 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, n, 0);
      } else {
         this.numberOfElements = n;
      }
   }

   @Override
   public double getExponent() {
      return this.exponent;
   }

   @Deprecated
   @Override
   public void setExponent(double s) {
      this.setExponentInternal(s);
   }

   private void setExponentInternal(double s) throws IllegalArgumentException {
      if (s <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_EXPONENT, s);
      } else {
         this.exponent = s;
      }
   }

   @Override
   public double probability(int x) {
      return x > 0 && x <= this.numberOfElements
         ? 1.0 / FastMath.pow((double)x, this.exponent) / this.generalizedHarmonic(this.numberOfElements, this.exponent)
         : 0.0;
   }

   @Override
   public double cumulativeProbability(int x) {
      if (x <= 0) {
         return 0.0;
      } else {
         return x >= this.numberOfElements ? 1.0 : this.generalizedHarmonic(x, this.exponent) / this.generalizedHarmonic(this.numberOfElements, this.exponent);
      }
   }

   @Override
   protected int getDomainLowerBound(double p) {
      return 0;
   }

   @Override
   protected int getDomainUpperBound(double p) {
      return this.numberOfElements;
   }

   private double generalizedHarmonic(int n, double m) {
      double value = 0.0;

      for(int k = n; k > 0; --k) {
         value += 1.0 / FastMath.pow((double)k, m);
      }

      return value;
   }

   public int getSupportLowerBound() {
      return 1;
   }

   public int getSupportUpperBound() {
      return this.getNumberOfElements();
   }

   protected double getNumericalMean() {
      int N = this.getNumberOfElements();
      double s = this.getExponent();
      double Hs1 = this.generalizedHarmonic(N, s - 1.0);
      double Hs = this.generalizedHarmonic(N, s);
      return Hs1 / Hs;
   }

   protected double getNumericalVariance() {
      int N = this.getNumberOfElements();
      double s = this.getExponent();
      double Hs2 = this.generalizedHarmonic(N, s - 2.0);
      double Hs1 = this.generalizedHarmonic(N, s - 1.0);
      double Hs = this.generalizedHarmonic(N, s);
      return Hs2 / Hs - Hs1 * Hs1 / (Hs * Hs);
   }
}
