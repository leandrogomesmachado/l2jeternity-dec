package org.apache.commons.math.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.NonMonotonousSequenceException;
import org.apache.commons.math.exception.util.Localizable;
import org.apache.commons.math.exception.util.LocalizedFormats;

public final class MathUtils {
   public static final double EPSILON = 1.110223E-16F;
   public static final double SAFE_MIN = Double.MIN_NORMAL;
   public static final double TWO_PI = Math.PI * 2;
   private static final byte NB = -1;
   private static final short NS = -1;
   private static final byte PB = 1;
   private static final short PS = 1;
   private static final byte ZB = 0;
   private static final short ZS = 0;
   private static final int NAN_GAP = 4194304;
   private static final long SGN_MASK = Long.MIN_VALUE;
   private static final int SGN_MASK_FLOAT = Integer.MIN_VALUE;
   private static final long[] FACTORIALS = new long[]{
      1L,
      1L,
      2L,
      6L,
      24L,
      120L,
      720L,
      5040L,
      40320L,
      362880L,
      3628800L,
      39916800L,
      479001600L,
      6227020800L,
      87178291200L,
      1307674368000L,
      20922789888000L,
      355687428096000L,
      6402373705728000L,
      121645100408832000L,
      2432902008176640000L
   };

   private MathUtils() {
   }

   public static int addAndCheck(int x, int y) {
      long s = (long)x + (long)y;
      if (s >= -2147483648L && s <= 2147483647L) {
         return (int)s;
      } else {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, x, y);
      }
   }

   public static long addAndCheck(long a, long b) {
      return addAndCheck(a, b, LocalizedFormats.OVERFLOW_IN_ADDITION);
   }

   private static long addAndCheck(long a, long b, Localizable pattern) {
      long ret;
      if (a > b) {
         ret = addAndCheck(b, a, pattern);
      } else if (a < 0L) {
         if (b < 0L) {
            if (Long.MIN_VALUE - b > a) {
               throw MathRuntimeException.createArithmeticException(pattern, a, b);
            }

            ret = a + b;
         } else {
            ret = a + b;
         }
      } else {
         if (a > Long.MAX_VALUE - b) {
            throw MathRuntimeException.createArithmeticException(pattern, a, b);
         }

         ret = a + b;
      }

      return ret;
   }

   public static long binomialCoefficient(int n, int k) {
      checkBinomial(n, k);
      if (n == k || k == 0) {
         return 1L;
      } else if (k == 1 || k == n - 1) {
         return (long)n;
      } else if (k > n / 2) {
         return binomialCoefficient(n, n - k);
      } else {
         long result = 1L;
         if (n <= 61) {
            int i = n - k + 1;

            for(int j = 1; j <= k; ++j) {
               result = result * (long)i / (long)j;
               ++i;
            }
         } else if (n <= 66) {
            int i = n - k + 1;

            for(int j = 1; j <= k; ++j) {
               long d = (long)gcd(i, j);
               result = result / ((long)j / d) * ((long)i / d);
               ++i;
            }
         } else {
            int i = n - k + 1;

            for(int j = 1; j <= k; ++j) {
               long d = (long)gcd(i, j);
               result = mulAndCheck(result / ((long)j / d), (long)i / d);
               ++i;
            }
         }

         return result;
      }
   }

   public static double binomialCoefficientDouble(int n, int k) {
      checkBinomial(n, k);
      if (n == k || k == 0) {
         return 1.0;
      } else if (k == 1 || k == n - 1) {
         return (double)n;
      } else if (k > n / 2) {
         return binomialCoefficientDouble(n, n - k);
      } else if (n < 67) {
         return (double)binomialCoefficient(n, k);
      } else {
         double result = 1.0;

         for(int i = 1; i <= k; ++i) {
            result *= (double)(n - k + i) / (double)i;
         }

         return FastMath.floor(result + 0.5);
      }
   }

   public static double binomialCoefficientLog(int n, int k) {
      checkBinomial(n, k);
      if (n == k || k == 0) {
         return 0.0;
      } else if (k == 1 || k == n - 1) {
         return FastMath.log((double)n);
      } else if (n < 67) {
         return FastMath.log((double)binomialCoefficient(n, k));
      } else if (n < 1030) {
         return FastMath.log(binomialCoefficientDouble(n, k));
      } else if (k > n / 2) {
         return binomialCoefficientLog(n, n - k);
      } else {
         double logSum = 0.0;

         for(int i = n - k + 1; i <= n; ++i) {
            logSum += FastMath.log((double)i);
         }

         for(int i = 2; i <= k; ++i) {
            logSum -= FastMath.log((double)i);
         }

         return logSum;
      }
   }

   private static void checkBinomial(int n, int k) throws IllegalArgumentException {
      if (n < k) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.BINOMIAL_INVALID_PARAMETERS_ORDER, n, k);
      } else if (n < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.BINOMIAL_NEGATIVE_PARAMETER, n);
      }
   }

   public static int compareTo(double x, double y, double eps) {
      if (equals(x, y, eps)) {
         return 0;
      } else {
         return x < y ? -1 : 1;
      }
   }

   public static double cosh(double x) {
      return (FastMath.exp(x) + FastMath.exp(-x)) / 2.0;
   }

   @Deprecated
   public static boolean equals(float x, float y) {
      return Float.isNaN(x) && Float.isNaN(y) || x == y;
   }

   public static boolean equalsIncludingNaN(float x, float y) {
      return Float.isNaN(x) && Float.isNaN(y) || equals(x, y, 1);
   }

   public static boolean equals(float x, float y, float eps) {
      return equals(x, y, 1) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equalsIncludingNaN(float x, float y, float eps) {
      return equalsIncludingNaN(x, y) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equals(float x, float y, int maxUlps) {
      assert maxUlps > 0 && maxUlps < 4194304;

      int xInt = Float.floatToIntBits(x);
      int yInt = Float.floatToIntBits(y);
      if (xInt < 0) {
         xInt = Integer.MIN_VALUE - xInt;
      }

      if (yInt < 0) {
         yInt = Integer.MIN_VALUE - yInt;
      }

      boolean isEqual = FastMath.abs(xInt - yInt) <= maxUlps;
      return isEqual && !Float.isNaN(x) && !Float.isNaN(y);
   }

   public static boolean equalsIncludingNaN(float x, float y, int maxUlps) {
      return Float.isNaN(x) && Float.isNaN(y) || equals(x, y, maxUlps);
   }

   @Deprecated
   public static boolean equals(float[] x, float[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!equals(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static boolean equalsIncludingNaN(float[] x, float[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!equalsIncludingNaN(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static boolean equals(double x, double y) {
      return Double.isNaN(x) && Double.isNaN(y) || x == y;
   }

   public static boolean equalsIncludingNaN(double x, double y) {
      return Double.isNaN(x) && Double.isNaN(y) || equals(x, y, 1);
   }

   public static boolean equals(double x, double y, double eps) {
      return equals(x, y) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equalsIncludingNaN(double x, double y, double eps) {
      return equalsIncludingNaN(x, y) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equals(double x, double y, int maxUlps) {
      assert maxUlps > 0 && maxUlps < 4194304;

      long xInt = Double.doubleToLongBits(x);
      long yInt = Double.doubleToLongBits(y);
      if (xInt < 0L) {
         xInt = Long.MIN_VALUE - xInt;
      }

      if (yInt < 0L) {
         yInt = Long.MIN_VALUE - yInt;
      }

      return FastMath.abs(xInt - yInt) <= (long)maxUlps;
   }

   public static boolean equalsIncludingNaN(double x, double y, int maxUlps) {
      return Double.isNaN(x) && Double.isNaN(y) || equals(x, y, maxUlps);
   }

   public static boolean equals(double[] x, double[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!equals(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static boolean equalsIncludingNaN(double[] x, double[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!equalsIncludingNaN(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static long factorial(int n) {
      if (n < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
      } else if (n > 20) {
         throw new ArithmeticException("factorial value is too large to fit in a long");
      } else {
         return FACTORIALS[n];
      }
   }

   public static double factorialDouble(int n) {
      if (n < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
      } else {
         return n < 21 ? (double)factorial(n) : FastMath.floor(FastMath.exp(factorialLog(n)) + 0.5);
      }
   }

   public static double factorialLog(int n) {
      if (n < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
      } else if (n < 21) {
         return FastMath.log((double)factorial(n));
      } else {
         double logSum = 0.0;

         for(int i = 2; i <= n; ++i) {
            logSum += FastMath.log((double)i);
         }

         return logSum;
      }
   }

   public static int gcd(int p, int q) {
      int u = p;
      int v = q;
      if (p != 0 && q != 0) {
         if (p > 0) {
            u = -p;
         }

         if (q > 0) {
            v = -q;
         }

         int k;
         for(k = 0; (u & 1) == 0 && (v & 1) == 0 && k < 31; ++k) {
            u /= 2;
            v /= 2;
         }

         if (k == 31) {
            throw MathRuntimeException.createArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, p, q);
         } else {
            int t = (u & 1) == 1 ? v : -(u / 2);

            while(true) {
               while((t & 1) != 0) {
                  if (t > 0) {
                     u = -t;
                  } else {
                     v = t;
                  }

                  t = (v - u) / 2;
                  if (t == 0) {
                     return -u * (1 << k);
                  }
               }

               t /= 2;
            }
         }
      } else if (p != Integer.MIN_VALUE && q != Integer.MIN_VALUE) {
         return FastMath.abs(p) + FastMath.abs(q);
      } else {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, p, q);
      }
   }

   public static long gcd(long p, long q) {
      long u = p;
      long v = q;
      if (p != 0L && q != 0L) {
         if (p > 0L) {
            u = -p;
         }

         if (q > 0L) {
            v = -q;
         }

         int k;
         for(k = 0; (u & 1L) == 0L && (v & 1L) == 0L && k < 63; ++k) {
            u /= 2L;
            v /= 2L;
         }

         if (k == 63) {
            throw MathRuntimeException.createArithmeticException(LocalizedFormats.GCD_OVERFLOW_64_BITS, p, q);
         } else {
            long t = (u & 1L) == 1L ? v : -(u / 2L);

            while(true) {
               while((t & 1L) != 0L) {
                  if (t > 0L) {
                     u = -t;
                  } else {
                     v = t;
                  }

                  t = (v - u) / 2L;
                  if (t == 0L) {
                     return -u * (1L << k);
                  }
               }

               t /= 2L;
            }
         }
      } else if (p != Long.MIN_VALUE && q != Long.MIN_VALUE) {
         return FastMath.abs(p) + FastMath.abs(q);
      } else {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.GCD_OVERFLOW_64_BITS, p, q);
      }
   }

   public static int hash(double value) {
      return new Double(value).hashCode();
   }

   public static int hash(double[] value) {
      return Arrays.hashCode(value);
   }

   public static byte indicator(byte x) {
      return (byte)(x >= 0 ? 1 : -1);
   }

   public static double indicator(double x) {
      if (Double.isNaN(x)) {
         return Double.NaN;
      } else {
         return x >= 0.0 ? 1.0 : -1.0;
      }
   }

   public static float indicator(float x) {
      if (Float.isNaN(x)) {
         return Float.NaN;
      } else {
         return x >= 0.0F ? 1.0F : -1.0F;
      }
   }

   public static int indicator(int x) {
      return x >= 0 ? 1 : -1;
   }

   public static long indicator(long x) {
      return x >= 0L ? 1L : -1L;
   }

   public static short indicator(short x) {
      return (short)(x >= 0 ? 1 : -1);
   }

   public static int lcm(int a, int b) {
      if (a != 0 && b != 0) {
         int lcm = FastMath.abs(mulAndCheck(a / gcd(a, b), b));
         if (lcm == Integer.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(LocalizedFormats.LCM_OVERFLOW_32_BITS, a, b);
         } else {
            return lcm;
         }
      } else {
         return 0;
      }
   }

   public static long lcm(long a, long b) {
      if (a != 0L && b != 0L) {
         long lcm = FastMath.abs(mulAndCheck(a / gcd(a, b), b));
         if (lcm == Long.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(LocalizedFormats.LCM_OVERFLOW_64_BITS, a, b);
         } else {
            return lcm;
         }
      } else {
         return 0L;
      }
   }

   public static double log(double base, double x) {
      return FastMath.log(x) / FastMath.log(base);
   }

   public static int mulAndCheck(int x, int y) {
      long m = (long)x * (long)y;
      if (m >= -2147483648L && m <= 2147483647L) {
         return (int)m;
      } else {
         throw new ArithmeticException("overflow: mul");
      }
   }

   public static long mulAndCheck(long a, long b) {
      String msg = "overflow: multiply";
      long ret;
      if (a > b) {
         ret = mulAndCheck(b, a);
      } else if (a < 0L) {
         if (b < 0L) {
            if (a < Long.MAX_VALUE / b) {
               throw new ArithmeticException(msg);
            }

            ret = a * b;
         } else if (b > 0L) {
            if (Long.MIN_VALUE / b > a) {
               throw new ArithmeticException(msg);
            }

            ret = a * b;
         } else {
            ret = 0L;
         }
      } else if (a > 0L) {
         if (a > Long.MAX_VALUE / b) {
            throw new ArithmeticException(msg);
         }

         ret = a * b;
      } else {
         ret = 0L;
      }

      return ret;
   }

   @Deprecated
   public static double nextAfter(double d, double direction) {
      if (Double.isNaN(d) || Double.isInfinite(d)) {
         return d;
      } else if (d == 0.0) {
         return direction < 0.0 ? -Double.MIN_VALUE : Double.MIN_VALUE;
      } else {
         long bits = Double.doubleToLongBits(d);
         long sign = bits & Long.MIN_VALUE;
         long exponent = bits & 9218868437227405312L;
         long mantissa = bits & 4503599627370495L;
         if (d * (direction - d) >= 0.0) {
            return mantissa == 4503599627370495L
               ? Double.longBitsToDouble(sign | exponent + 4503599627370496L)
               : Double.longBitsToDouble(sign | exponent | mantissa + 1L);
         } else {
            return mantissa == 0L
               ? Double.longBitsToDouble(sign | exponent - 4503599627370496L | 4503599627370495L)
               : Double.longBitsToDouble(sign | exponent | mantissa - 1L);
         }
      }
   }

   @Deprecated
   public static double scalb(double d, int scaleFactor) {
      return FastMath.scalb(d, scaleFactor);
   }

   public static double normalizeAngle(double a, double center) {
      return a - (Math.PI * 2) * FastMath.floor((a + Math.PI - center) / (Math.PI * 2));
   }

   public static double[] normalizeArray(double[] values, double normalizedSum) throws ArithmeticException, IllegalArgumentException {
      if (Double.isInfinite(normalizedSum)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE);
      } else if (Double.isNaN(normalizedSum)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NORMALIZE_NAN);
      } else {
         double sum = 0.0;
         int len = values.length;
         double[] out = new double[len];

         for(int i = 0; i < len; ++i) {
            if (Double.isInfinite(values[i])) {
               throw MathRuntimeException.createArithmeticException(LocalizedFormats.INFINITE_ARRAY_ELEMENT, values[i], i);
            }

            if (!Double.isNaN(values[i])) {
               sum += values[i];
            }
         }

         if (sum == 0.0) {
            throw MathRuntimeException.createArithmeticException(LocalizedFormats.ARRAY_SUMS_TO_ZERO);
         } else {
            for(int i = 0; i < len; ++i) {
               if (Double.isNaN(values[i])) {
                  out[i] = Double.NaN;
               } else {
                  out[i] = values[i] * normalizedSum / sum;
               }
            }

            return out;
         }
      }
   }

   public static double round(double x, int scale) {
      return round(x, scale, 4);
   }

   public static double round(double x, int scale, int roundingMethod) {
      try {
         return new BigDecimal(Double.toString(x)).setScale(scale, roundingMethod).doubleValue();
      } catch (NumberFormatException var5) {
         return Double.isInfinite(x) ? x : Double.NaN;
      }
   }

   public static float round(float x, int scale) {
      return round(x, scale, 4);
   }

   public static float round(float x, int scale, int roundingMethod) {
      float sign = indicator(x);
      float factor = (float)FastMath.pow(10.0, (double)scale) * sign;
      return (float)roundUnscaled((double)(x * factor), (double)sign, roundingMethod) / factor;
   }

   private static double roundUnscaled(double unscaled, double sign, int roundingMethod) {
      switch(roundingMethod) {
         case 0:
            unscaled = FastMath.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            break;
         case 1:
            unscaled = FastMath.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            break;
         case 2:
            if (sign == -1.0) {
               unscaled = FastMath.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            } else {
               unscaled = FastMath.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            }
            break;
         case 3:
            if (sign == -1.0) {
               unscaled = FastMath.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            } else {
               unscaled = FastMath.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            }
            break;
         case 4:
            unscaled = nextAfter(unscaled, Double.POSITIVE_INFINITY);
            double fraction = unscaled - FastMath.floor(unscaled);
            if (fraction >= 0.5) {
               unscaled = FastMath.ceil(unscaled);
            } else {
               unscaled = FastMath.floor(unscaled);
            }
            break;
         case 5:
            unscaled = nextAfter(unscaled, Double.NEGATIVE_INFINITY);
            double fraction = unscaled - FastMath.floor(unscaled);
            if (fraction > 0.5) {
               unscaled = FastMath.ceil(unscaled);
            } else {
               unscaled = FastMath.floor(unscaled);
            }
            break;
         case 6:
            double fraction = unscaled - FastMath.floor(unscaled);
            if (fraction > 0.5) {
               unscaled = FastMath.ceil(unscaled);
            } else if (fraction < 0.5) {
               unscaled = FastMath.floor(unscaled);
            } else if (FastMath.floor(unscaled) / 2.0 == FastMath.floor(Math.floor(unscaled) / 2.0)) {
               unscaled = FastMath.floor(unscaled);
            } else {
               unscaled = FastMath.ceil(unscaled);
            }
            break;
         case 7:
            if (unscaled != FastMath.floor(unscaled)) {
               throw new ArithmeticException("Inexact result from rounding");
            }
            break;
         default:
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.INVALID_ROUNDING_METHOD,
               roundingMethod,
               "ROUND_CEILING",
               2,
               "ROUND_DOWN",
               1,
               "ROUND_FLOOR",
               3,
               "ROUND_HALF_DOWN",
               5,
               "ROUND_HALF_EVEN",
               6,
               "ROUND_HALF_UP",
               4,
               "ROUND_UNNECESSARY",
               7,
               "ROUND_UP",
               0
            );
      }

      return unscaled;
   }

   public static byte sign(byte x) {
      return (byte)(x == 0 ? 0 : (x > 0 ? 1 : -1));
   }

   public static double sign(double x) {
      if (Double.isNaN(x)) {
         return Double.NaN;
      } else {
         return x == 0.0 ? 0.0 : (x > 0.0 ? 1.0 : -1.0);
      }
   }

   public static float sign(float x) {
      if (Float.isNaN(x)) {
         return Float.NaN;
      } else {
         return x == 0.0F ? 0.0F : (x > 0.0F ? 1.0F : -1.0F);
      }
   }

   public static int sign(int x) {
      return x == 0 ? 0 : (x > 0 ? 1 : -1);
   }

   public static long sign(long x) {
      return x == 0L ? 0L : (x > 0L ? 1L : -1L);
   }

   public static short sign(short x) {
      return (short)(x == 0 ? 0 : (x > 0 ? 1 : -1));
   }

   public static double sinh(double x) {
      return (FastMath.exp(x) - FastMath.exp(-x)) / 2.0;
   }

   public static int subAndCheck(int x, int y) {
      long s = (long)x - (long)y;
      if (s >= -2147483648L && s <= 2147483647L) {
         return (int)s;
      } else {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, x, y);
      }
   }

   public static long subAndCheck(long a, long b) {
      String msg = "overflow: subtract";
      long ret;
      if (b == Long.MIN_VALUE) {
         if (a >= 0L) {
            throw new ArithmeticException(msg);
         }

         ret = a - b;
      } else {
         ret = addAndCheck(a, -b, LocalizedFormats.OVERFLOW_IN_ADDITION);
      }

      return ret;
   }

   public static int pow(int k, int e) throws IllegalArgumentException {
      if (e < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         int result = 1;

         for(int k2p = k; e != 0; e >>= 1) {
            if ((e & 1) != 0) {
               result *= k2p;
            }

            k2p *= k2p;
         }

         return result;
      }
   }

   public static int pow(int k, long e) throws IllegalArgumentException {
      if (e < 0L) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         int result = 1;

         for(int k2p = k; e != 0L; e >>= 1) {
            if ((e & 1L) != 0L) {
               result *= k2p;
            }

            k2p *= k2p;
         }

         return result;
      }
   }

   public static long pow(long k, int e) throws IllegalArgumentException {
      if (e < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         long result = 1L;

         for(long k2p = k; e != 0; e >>= 1) {
            if ((e & 1) != 0) {
               result *= k2p;
            }

            k2p *= k2p;
         }

         return result;
      }
   }

   public static long pow(long k, long e) throws IllegalArgumentException {
      if (e < 0L) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         long result = 1L;

         for(long k2p = k; e != 0L; e >>= 1) {
            if ((e & 1L) != 0L) {
               result *= k2p;
            }

            k2p *= k2p;
         }

         return result;
      }
   }

   public static BigInteger pow(BigInteger k, int e) throws IllegalArgumentException {
      if (e < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         return k.pow(e);
      }
   }

   public static BigInteger pow(BigInteger k, long e) throws IllegalArgumentException {
      if (e < 0L) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         BigInteger result = BigInteger.ONE;

         for(BigInteger k2p = k; e != 0L; e >>= 1) {
            if ((e & 1L) != 0L) {
               result = result.multiply(k2p);
            }

            k2p = k2p.multiply(k2p);
         }

         return result;
      }
   }

   public static BigInteger pow(BigInteger k, BigInteger e) throws IllegalArgumentException {
      if (e.compareTo(BigInteger.ZERO) < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POWER_NEGATIVE_PARAMETERS, k, e);
      } else {
         BigInteger result = BigInteger.ONE;

         for(BigInteger k2p = k; !BigInteger.ZERO.equals(e); e = e.shiftRight(1)) {
            if (e.testBit(0)) {
               result = result.multiply(k2p);
            }

            k2p = k2p.multiply(k2p);
         }

         return result;
      }
   }

   public static double distance1(double[] p1, double[] p2) {
      double sum = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         sum += FastMath.abs(p1[i] - p2[i]);
      }

      return sum;
   }

   public static int distance1(int[] p1, int[] p2) {
      int sum = 0;

      for(int i = 0; i < p1.length; ++i) {
         sum += FastMath.abs(p1[i] - p2[i]);
      }

      return sum;
   }

   public static double distance(double[] p1, double[] p2) {
      double sum = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         double dp = p1[i] - p2[i];
         sum += dp * dp;
      }

      return FastMath.sqrt(sum);
   }

   public static double distance(int[] p1, int[] p2) {
      double sum = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         double dp = (double)(p1[i] - p2[i]);
         sum += dp * dp;
      }

      return FastMath.sqrt(sum);
   }

   public static double distanceInf(double[] p1, double[] p2) {
      double max = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         max = FastMath.max(max, FastMath.abs(p1[i] - p2[i]));
      }

      return max;
   }

   public static int distanceInf(int[] p1, int[] p2) {
      int max = 0;

      for(int i = 0; i < p1.length; ++i) {
         max = FastMath.max(max, FastMath.abs(p1[i] - p2[i]));
      }

      return max;
   }

   public static void checkOrder(double[] val, MathUtils.OrderDirection dir, boolean strict) {
      double previous = val[0];
      boolean ok = true;
      int max = val.length;

      for(int i = 1; i < max; ++i) {
         switch(dir) {
            case INCREASING:
               if (strict) {
                  if (val[i] <= previous) {
                     ok = false;
                  }
               } else if (val[i] < previous) {
                  ok = false;
               }
               break;
            case DECREASING:
               if (strict) {
                  if (val[i] >= previous) {
                     ok = false;
                  }
               } else if (val[i] > previous) {
                  ok = false;
               }
               break;
            default:
               throw new IllegalArgumentException();
         }

         if (!ok) {
            throw new NonMonotonousSequenceException(val[i], previous, i, dir, strict);
         }

         previous = val[i];
      }
   }

   public static void checkOrder(double[] val) {
      checkOrder(val, MathUtils.OrderDirection.INCREASING, true);
   }

   @Deprecated
   public static void checkOrder(double[] val, int dir, boolean strict) {
      if (dir > 0) {
         checkOrder(val, MathUtils.OrderDirection.INCREASING, strict);
      } else {
         checkOrder(val, MathUtils.OrderDirection.DECREASING, strict);
      }
   }

   public static double safeNorm(double[] v) {
      double rdwarf = 3.834E-20;
      double rgiant = 1.304E19;
      double s1 = 0.0;
      double s2 = 0.0;
      double s3 = 0.0;
      double x1max = 0.0;
      double x3max = 0.0;
      double floatn = (double)v.length;
      double agiant = rgiant / floatn;

      for(int i = 0; i < v.length; ++i) {
         double xabs = Math.abs(v[i]);
         if (!(xabs < rdwarf) && !(xabs > agiant)) {
            s2 += xabs * xabs;
         } else if (xabs > rdwarf) {
            if (xabs > x1max) {
               double r = x1max / xabs;
               s1 = 1.0 + s1 * r * r;
               x1max = xabs;
            } else {
               double r = xabs / x1max;
               s1 += r * r;
            }
         } else if (xabs > x3max) {
            double r = x3max / xabs;
            s3 = 1.0 + s3 * r * r;
            x3max = xabs;
         } else if (xabs != 0.0) {
            double r = xabs / x3max;
            s3 += r * r;
         }
      }

      double norm;
      if (s1 != 0.0) {
         norm = x1max * Math.sqrt(s1 + s2 / x1max / x1max);
      } else if (s2 == 0.0) {
         norm = x3max * Math.sqrt(s3);
      } else if (s2 >= x3max) {
         norm = Math.sqrt(s2 * (1.0 + x3max / s2 * x3max * s3));
      } else {
         norm = Math.sqrt(x3max * (s2 / x3max + x3max * s3));
      }

      return norm;
   }

   public static enum OrderDirection {
      INCREASING,
      DECREASING;
   }
}
