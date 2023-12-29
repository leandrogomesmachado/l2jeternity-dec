package org.apache.commons.math.fraction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class BigFraction extends Number implements FieldElement<BigFraction>, Comparable<BigFraction>, Serializable {
   public static final BigFraction TWO = new BigFraction(2);
   public static final BigFraction ONE = new BigFraction(1);
   public static final BigFraction ZERO = new BigFraction(0);
   public static final BigFraction MINUS_ONE = new BigFraction(-1);
   public static final BigFraction FOUR_FIFTHS = new BigFraction(4, 5);
   public static final BigFraction ONE_FIFTH = new BigFraction(1, 5);
   public static final BigFraction ONE_HALF = new BigFraction(1, 2);
   public static final BigFraction ONE_QUARTER = new BigFraction(1, 4);
   public static final BigFraction ONE_THIRD = new BigFraction(1, 3);
   public static final BigFraction THREE_FIFTHS = new BigFraction(3, 5);
   public static final BigFraction THREE_QUARTERS = new BigFraction(3, 4);
   public static final BigFraction TWO_FIFTHS = new BigFraction(2, 5);
   public static final BigFraction TWO_QUARTERS = new BigFraction(2, 4);
   public static final BigFraction TWO_THIRDS = new BigFraction(2, 3);
   private static final long serialVersionUID = -5630213147331578515L;
   private static final BigInteger ONE_HUNDRED_DOUBLE = BigInteger.valueOf(100L);
   private final BigInteger numerator;
   private final BigInteger denominator;

   public BigFraction(BigInteger num) {
      this(num, BigInteger.ONE);
   }

   public BigFraction(BigInteger num, BigInteger den) {
      if (num == null) {
         throw new NullPointerException(LocalizedFormats.NUMERATOR.getSourceString());
      } else if (den == null) {
         throw new NullPointerException(LocalizedFormats.DENOMINATOR.getSourceString());
      } else if (BigInteger.ZERO.equals(den)) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_DENOMINATOR);
      } else {
         if (BigInteger.ZERO.equals(num)) {
            this.numerator = BigInteger.ZERO;
            this.denominator = BigInteger.ONE;
         } else {
            BigInteger gcd = num.gcd(den);
            if (BigInteger.ONE.compareTo(gcd) < 0) {
               num = num.divide(gcd);
               den = den.divide(gcd);
            }

            if (BigInteger.ZERO.compareTo(den) > 0) {
               num = num.negate();
               den = den.negate();
            }

            this.numerator = num;
            this.denominator = den;
         }
      }
   }

   public BigFraction(double value) throws IllegalArgumentException {
      if (Double.isNaN(value)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NAN_VALUE_CONVERSION);
      } else if (Double.isInfinite(value)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INFINITE_VALUE_CONVERSION);
      } else {
         long bits = Double.doubleToLongBits(value);
         long sign = bits & Long.MIN_VALUE;
         long exponent = bits & 9218868437227405312L;
         long m = bits & 4503599627370495L;
         if (exponent != 0L) {
            m |= 4503599627370496L;
         }

         if (sign != 0L) {
            m = -m;
         }

         int k;
         for(k = (int)(exponent >> 52) - 1075; (m & 9007199254740990L) != 0L && (m & 1L) == 0L; ++k) {
            m >>= 1;
         }

         if (k < 0) {
            this.numerator = BigInteger.valueOf(m);
            this.denominator = BigInteger.ZERO.flipBit(-k);
         } else {
            this.numerator = BigInteger.valueOf(m).multiply(BigInteger.ZERO.flipBit(k));
            this.denominator = BigInteger.ONE;
         }
      }
   }

   public BigFraction(double value, double epsilon, int maxIterations) throws FractionConversionException {
      this(value, epsilon, Integer.MAX_VALUE, maxIterations);
   }

   private BigFraction(double value, double epsilon, int maxDenominator, int maxIterations) throws FractionConversionException {
      long overflow = 2147483647L;
      double r0 = value;
      long a0 = (long)FastMath.floor(value);
      if (a0 > overflow) {
         throw new FractionConversionException(value, a0, 1L);
      } else if (FastMath.abs((double)a0 - value) < epsilon) {
         this.numerator = BigInteger.valueOf(a0);
         this.denominator = BigInteger.ONE;
      } else {
         long p0 = 1L;
         long q0 = 0L;
         long p1 = a0;
         long q1 = 1L;
         long p2 = 0L;
         long q2 = 1L;
         int n = 0;
         boolean stop = false;

         do {
            ++n;
            double r1 = 1.0 / (r0 - (double)a0);
            long a1 = (long)FastMath.floor(r1);
            p2 = a1 * p1 + p0;
            q2 = a1 * q1 + q0;
            if (p2 > overflow || q2 > overflow) {
               throw new FractionConversionException(value, p2, q2);
            }

            double convergent = (double)p2 / (double)q2;
            if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < (long)maxDenominator) {
               p0 = p1;
               p1 = p2;
               q0 = q1;
               q1 = q2;
               a0 = a1;
               r0 = r1;
            } else {
               stop = true;
            }
         } while(!stop);

         if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
         } else {
            if (q2 < (long)maxDenominator) {
               this.numerator = BigInteger.valueOf(p2);
               this.denominator = BigInteger.valueOf(q2);
            } else {
               this.numerator = BigInteger.valueOf(p1);
               this.denominator = BigInteger.valueOf(q1);
            }
         }
      }
   }

   public BigFraction(double value, int maxDenominator) throws FractionConversionException {
      this(value, 0.0, maxDenominator, 100);
   }

   public BigFraction(int num) {
      this(BigInteger.valueOf((long)num), BigInteger.ONE);
   }

   public BigFraction(int num, int den) {
      this(BigInteger.valueOf((long)num), BigInteger.valueOf((long)den));
   }

   public BigFraction(long num) {
      this(BigInteger.valueOf(num), BigInteger.ONE);
   }

   public BigFraction(long num, long den) {
      this(BigInteger.valueOf(num), BigInteger.valueOf(den));
   }

   public static BigFraction getReducedFraction(int numerator, int denominator) {
      return numerator == 0 ? ZERO : new BigFraction(numerator, denominator);
   }

   public BigFraction abs() {
      return BigInteger.ZERO.compareTo(this.numerator) <= 0 ? this : this.negate();
   }

   public BigFraction add(BigInteger bg) {
      return new BigFraction(this.numerator.add(this.denominator.multiply(bg)), this.denominator);
   }

   public BigFraction add(int i) {
      return this.add(BigInteger.valueOf((long)i));
   }

   public BigFraction add(long l) {
      return this.add(BigInteger.valueOf(l));
   }

   public BigFraction add(BigFraction fraction) {
      if (fraction == null) {
         throw new NullPointerException(LocalizedFormats.FRACTION.getSourceString());
      } else if (ZERO.equals(fraction)) {
         return this;
      } else {
         BigInteger num = null;
         BigInteger den = null;
         if (this.denominator.equals(fraction.denominator)) {
            num = this.numerator.add(fraction.numerator);
            den = this.denominator;
         } else {
            num = this.numerator.multiply(fraction.denominator).add(fraction.numerator.multiply(this.denominator));
            den = this.denominator.multiply(fraction.denominator);
         }

         return new BigFraction(num, den);
      }
   }

   public BigDecimal bigDecimalValue() {
      return new BigDecimal(this.numerator).divide(new BigDecimal(this.denominator));
   }

   public BigDecimal bigDecimalValue(int roundingMode) {
      return new BigDecimal(this.numerator).divide(new BigDecimal(this.denominator), roundingMode);
   }

   public BigDecimal bigDecimalValue(int scale, int roundingMode) {
      return new BigDecimal(this.numerator).divide(new BigDecimal(this.denominator), scale, roundingMode);
   }

   public int compareTo(BigFraction object) {
      BigInteger nOd = this.numerator.multiply(object.denominator);
      BigInteger dOn = this.denominator.multiply(object.numerator);
      return nOd.compareTo(dOn);
   }

   public BigFraction divide(BigInteger bg) {
      if (BigInteger.ZERO.equals(bg)) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_DENOMINATOR);
      } else {
         return new BigFraction(this.numerator, this.denominator.multiply(bg));
      }
   }

   public BigFraction divide(int i) {
      return this.divide(BigInteger.valueOf((long)i));
   }

   public BigFraction divide(long l) {
      return this.divide(BigInteger.valueOf(l));
   }

   public BigFraction divide(BigFraction fraction) {
      if (fraction == null) {
         throw new NullPointerException(LocalizedFormats.FRACTION.getSourceString());
      } else if (BigInteger.ZERO.equals(fraction.numerator)) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_DENOMINATOR);
      } else {
         return this.multiply(fraction.reciprocal());
      }
   }

   @Override
   public double doubleValue() {
      return this.numerator.doubleValue() / this.denominator.doubleValue();
   }

   @Override
   public boolean equals(Object other) {
      boolean ret = false;
      if (this == other) {
         ret = true;
      } else if (other instanceof BigFraction) {
         BigFraction rhs = ((BigFraction)other).reduce();
         BigFraction thisOne = this.reduce();
         ret = thisOne.numerator.equals(rhs.numerator) && thisOne.denominator.equals(rhs.denominator);
      }

      return ret;
   }

   @Override
   public float floatValue() {
      return this.numerator.floatValue() / this.denominator.floatValue();
   }

   public BigInteger getDenominator() {
      return this.denominator;
   }

   public int getDenominatorAsInt() {
      return this.denominator.intValue();
   }

   public long getDenominatorAsLong() {
      return this.denominator.longValue();
   }

   public BigInteger getNumerator() {
      return this.numerator;
   }

   public int getNumeratorAsInt() {
      return this.numerator.intValue();
   }

   public long getNumeratorAsLong() {
      return this.numerator.longValue();
   }

   @Override
   public int hashCode() {
      return 37 * (629 + this.numerator.hashCode()) + this.denominator.hashCode();
   }

   @Override
   public int intValue() {
      return this.numerator.divide(this.denominator).intValue();
   }

   @Override
   public long longValue() {
      return this.numerator.divide(this.denominator).longValue();
   }

   public BigFraction multiply(BigInteger bg) {
      if (bg == null) {
         throw new NullPointerException();
      } else {
         return new BigFraction(bg.multiply(this.numerator), this.denominator);
      }
   }

   public BigFraction multiply(int i) {
      return this.multiply(BigInteger.valueOf((long)i));
   }

   public BigFraction multiply(long l) {
      return this.multiply(BigInteger.valueOf(l));
   }

   public BigFraction multiply(BigFraction fraction) {
      if (fraction == null) {
         throw new NullPointerException(LocalizedFormats.FRACTION.getSourceString());
      } else {
         return !this.numerator.equals(BigInteger.ZERO) && !fraction.numerator.equals(BigInteger.ZERO)
            ? new BigFraction(this.numerator.multiply(fraction.numerator), this.denominator.multiply(fraction.denominator))
            : ZERO;
      }
   }

   public BigFraction negate() {
      return new BigFraction(this.numerator.negate(), this.denominator);
   }

   public double percentageValue() {
      return this.numerator.divide(this.denominator).multiply(ONE_HUNDRED_DOUBLE).doubleValue();
   }

   public BigFraction pow(int exponent) {
      return exponent < 0
         ? new BigFraction(this.denominator.pow(-exponent), this.numerator.pow(-exponent))
         : new BigFraction(this.numerator.pow(exponent), this.denominator.pow(exponent));
   }

   public BigFraction pow(long exponent) {
      return exponent < 0L
         ? new BigFraction(MathUtils.pow(this.denominator, -exponent), MathUtils.pow(this.numerator, -exponent))
         : new BigFraction(MathUtils.pow(this.numerator, exponent), MathUtils.pow(this.denominator, exponent));
   }

   public BigFraction pow(BigInteger exponent) {
      if (exponent.compareTo(BigInteger.ZERO) < 0) {
         BigInteger eNeg = exponent.negate();
         return new BigFraction(MathUtils.pow(this.denominator, eNeg), MathUtils.pow(this.numerator, eNeg));
      } else {
         return new BigFraction(MathUtils.pow(this.numerator, exponent), MathUtils.pow(this.denominator, exponent));
      }
   }

   public double pow(double exponent) {
      return FastMath.pow(this.numerator.doubleValue(), exponent) / FastMath.pow(this.denominator.doubleValue(), exponent);
   }

   public BigFraction reciprocal() {
      return new BigFraction(this.denominator, this.numerator);
   }

   public BigFraction reduce() {
      BigInteger gcd = this.numerator.gcd(this.denominator);
      return new BigFraction(this.numerator.divide(gcd), this.denominator.divide(gcd));
   }

   public BigFraction subtract(BigInteger bg) {
      if (bg == null) {
         throw new NullPointerException();
      } else {
         return new BigFraction(this.numerator.subtract(this.denominator.multiply(bg)), this.denominator);
      }
   }

   public BigFraction subtract(int i) {
      return this.subtract(BigInteger.valueOf((long)i));
   }

   public BigFraction subtract(long l) {
      return this.subtract(BigInteger.valueOf(l));
   }

   public BigFraction subtract(BigFraction fraction) {
      if (fraction == null) {
         throw new NullPointerException(LocalizedFormats.FRACTION.getSourceString());
      } else if (ZERO.equals(fraction)) {
         return this;
      } else {
         BigInteger num = null;
         BigInteger den = null;
         if (this.denominator.equals(fraction.denominator)) {
            num = this.numerator.subtract(fraction.numerator);
            den = this.denominator;
         } else {
            num = this.numerator.multiply(fraction.denominator).subtract(fraction.numerator.multiply(this.denominator));
            den = this.denominator.multiply(fraction.denominator);
         }

         return new BigFraction(num, den);
      }
   }

   @Override
   public String toString() {
      String str = null;
      if (BigInteger.ONE.equals(this.denominator)) {
         str = this.numerator.toString();
      } else if (BigInteger.ZERO.equals(this.numerator)) {
         str = "0";
      } else {
         str = this.numerator + " / " + this.denominator;
      }

      return str;
   }

   public BigFractionField getField() {
      return BigFractionField.getInstance();
   }
}
