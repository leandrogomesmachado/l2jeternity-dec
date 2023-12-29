package org.apache.commons.math.fraction;

import java.io.Serializable;
import java.math.BigInteger;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class Fraction extends Number implements FieldElement<Fraction>, Comparable<Fraction>, Serializable {
   public static final Fraction TWO = new Fraction(2, 1);
   public static final Fraction ONE = new Fraction(1, 1);
   public static final Fraction ZERO = new Fraction(0, 1);
   public static final Fraction FOUR_FIFTHS = new Fraction(4, 5);
   public static final Fraction ONE_FIFTH = new Fraction(1, 5);
   public static final Fraction ONE_HALF = new Fraction(1, 2);
   public static final Fraction ONE_QUARTER = new Fraction(1, 4);
   public static final Fraction ONE_THIRD = new Fraction(1, 3);
   public static final Fraction THREE_FIFTHS = new Fraction(3, 5);
   public static final Fraction THREE_QUARTERS = new Fraction(3, 4);
   public static final Fraction TWO_FIFTHS = new Fraction(2, 5);
   public static final Fraction TWO_QUARTERS = new Fraction(2, 4);
   public static final Fraction TWO_THIRDS = new Fraction(2, 3);
   public static final Fraction MINUS_ONE = new Fraction(-1, 1);
   private static final long serialVersionUID = 3698073679419233275L;
   private final int denominator;
   private final int numerator;

   public Fraction(double value) throws FractionConversionException {
      this(value, 1.0E-5, 100);
   }

   public Fraction(double value, double epsilon, int maxIterations) throws FractionConversionException {
      this(value, epsilon, Integer.MAX_VALUE, maxIterations);
   }

   public Fraction(double value, int maxDenominator) throws FractionConversionException {
      this(value, 0.0, maxDenominator, 100);
   }

   private Fraction(double value, double epsilon, int maxDenominator, int maxIterations) throws FractionConversionException {
      long overflow = 2147483647L;
      double r0 = value;
      long a0 = (long)FastMath.floor(value);
      if (a0 > overflow) {
         throw new FractionConversionException(value, a0, 1L);
      } else if (FastMath.abs((double)a0 - value) < epsilon) {
         this.numerator = (int)a0;
         this.denominator = 1;
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
               this.numerator = (int)p2;
               this.denominator = (int)q2;
            } else {
               this.numerator = (int)p1;
               this.denominator = (int)q1;
            }
         }
      }
   }

   public Fraction(int num) {
      this(num, 1);
   }

   public Fraction(int num, int den) {
      if (den == 0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION, num, den);
      } else {
         if (den < 0) {
            if (num == Integer.MIN_VALUE || den == Integer.MIN_VALUE) {
               throw MathRuntimeException.createArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, num, den);
            }

            num = -num;
            den = -den;
         }

         int d = MathUtils.gcd(num, den);
         if (d > 1) {
            num /= d;
            den /= d;
         }

         if (den < 0) {
            num = -num;
            den = -den;
         }

         this.numerator = num;
         this.denominator = den;
      }
   }

   public Fraction abs() {
      Fraction ret;
      if (this.numerator >= 0) {
         ret = this;
      } else {
         ret = this.negate();
      }

      return ret;
   }

   public int compareTo(Fraction object) {
      long nOd = (long)this.numerator * (long)object.denominator;
      long dOn = (long)this.denominator * (long)object.numerator;
      return nOd < dOn ? -1 : (nOd > dOn ? 1 : 0);
   }

   @Override
   public double doubleValue() {
      return (double)this.numerator / (double)this.denominator;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof Fraction)) {
         return false;
      } else {
         Fraction rhs = (Fraction)other;
         return this.numerator == rhs.numerator && this.denominator == rhs.denominator;
      }
   }

   @Override
   public float floatValue() {
      return (float)this.doubleValue();
   }

   public int getDenominator() {
      return this.denominator;
   }

   public int getNumerator() {
      return this.numerator;
   }

   @Override
   public int hashCode() {
      return 37 * (629 + this.numerator) + this.denominator;
   }

   @Override
   public int intValue() {
      return (int)this.doubleValue();
   }

   @Override
   public long longValue() {
      return (long)this.doubleValue();
   }

   public Fraction negate() {
      if (this.numerator == Integer.MIN_VALUE) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, this.numerator, this.denominator);
      } else {
         return new Fraction(-this.numerator, this.denominator);
      }
   }

   public Fraction reciprocal() {
      return new Fraction(this.denominator, this.numerator);
   }

   public Fraction add(Fraction fraction) {
      return this.addSub(fraction, true);
   }

   public Fraction add(int i) {
      return new Fraction(this.numerator + i * this.denominator, this.denominator);
   }

   public Fraction subtract(Fraction fraction) {
      return this.addSub(fraction, false);
   }

   public Fraction subtract(int i) {
      return new Fraction(this.numerator - i * this.denominator, this.denominator);
   }

   private Fraction addSub(Fraction fraction, boolean isAdd) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION);
      } else if (this.numerator == 0) {
         return isAdd ? fraction : fraction.negate();
      } else if (fraction.numerator == 0) {
         return this;
      } else {
         int d1 = MathUtils.gcd(this.denominator, fraction.denominator);
         if (d1 == 1) {
            int uvp = MathUtils.mulAndCheck(this.numerator, fraction.denominator);
            int upv = MathUtils.mulAndCheck(fraction.numerator, this.denominator);
            return new Fraction(
               isAdd ? MathUtils.addAndCheck(uvp, upv) : MathUtils.subAndCheck(uvp, upv), MathUtils.mulAndCheck(this.denominator, fraction.denominator)
            );
         } else {
            BigInteger uvp = BigInteger.valueOf((long)this.numerator).multiply(BigInteger.valueOf((long)(fraction.denominator / d1)));
            BigInteger upv = BigInteger.valueOf((long)fraction.numerator).multiply(BigInteger.valueOf((long)(this.denominator / d1)));
            BigInteger t = isAdd ? uvp.add(upv) : uvp.subtract(upv);
            int tmodd1 = t.mod(BigInteger.valueOf((long)d1)).intValue();
            int d2 = tmodd1 == 0 ? d1 : MathUtils.gcd(tmodd1, d1);
            BigInteger w = t.divide(BigInteger.valueOf((long)d2));
            if (w.bitLength() > 31) {
               throw MathRuntimeException.createArithmeticException(LocalizedFormats.NUMERATOR_OVERFLOW_AFTER_MULTIPLY, w);
            } else {
               return new Fraction(w.intValue(), MathUtils.mulAndCheck(this.denominator / d1, fraction.denominator / d2));
            }
         }
      }
   }

   public Fraction multiply(Fraction fraction) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION);
      } else if (this.numerator != 0 && fraction.numerator != 0) {
         int d1 = MathUtils.gcd(this.numerator, fraction.denominator);
         int d2 = MathUtils.gcd(fraction.numerator, this.denominator);
         return getReducedFraction(
            MathUtils.mulAndCheck(this.numerator / d1, fraction.numerator / d2), MathUtils.mulAndCheck(this.denominator / d2, fraction.denominator / d1)
         );
      } else {
         return ZERO;
      }
   }

   public Fraction multiply(int i) {
      return new Fraction(this.numerator * i, this.denominator);
   }

   public Fraction divide(Fraction fraction) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION);
      } else if (fraction.numerator == 0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_FRACTION_TO_DIVIDE_BY, fraction.numerator, fraction.denominator);
      } else {
         return this.multiply(fraction.reciprocal());
      }
   }

   public Fraction divide(int i) {
      return new Fraction(this.numerator, this.denominator * i);
   }

   public static Fraction getReducedFraction(int numerator, int denominator) {
      if (denominator == 0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION, numerator, denominator);
      } else if (numerator == 0) {
         return ZERO;
      } else {
         if (denominator == Integer.MIN_VALUE && (numerator & 1) == 0) {
            numerator /= 2;
            denominator /= 2;
         }

         if (denominator < 0) {
            if (numerator == Integer.MIN_VALUE || denominator == Integer.MIN_VALUE) {
               throw MathRuntimeException.createArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, numerator, denominator);
            }

            numerator = -numerator;
            denominator = -denominator;
         }

         int gcd = MathUtils.gcd(numerator, denominator);
         numerator /= gcd;
         denominator /= gcd;
         return new Fraction(numerator, denominator);
      }
   }

   @Override
   public String toString() {
      String str = null;
      if (this.denominator == 1) {
         str = Integer.toString(this.numerator);
      } else if (this.numerator == 0) {
         str = "0";
      } else {
         str = this.numerator + " / " + this.denominator;
      }

      return str;
   }

   public FractionField getField() {
      return FractionField.getInstance();
   }
}
