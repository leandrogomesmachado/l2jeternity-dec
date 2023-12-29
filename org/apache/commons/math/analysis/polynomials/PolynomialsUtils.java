package org.apache.commons.math.analysis.polynomials;

import java.util.ArrayList;
import org.apache.commons.math.fraction.BigFraction;
import org.apache.commons.math.util.FastMath;

public class PolynomialsUtils {
   private static final ArrayList<BigFraction> CHEBYSHEV_COEFFICIENTS = new ArrayList<>();
   private static final ArrayList<BigFraction> HERMITE_COEFFICIENTS = new ArrayList<>();
   private static final ArrayList<BigFraction> LAGUERRE_COEFFICIENTS = new ArrayList<>();
   private static final ArrayList<BigFraction> LEGENDRE_COEFFICIENTS = new ArrayList<>();

   private PolynomialsUtils() {
   }

   public static PolynomialFunction createChebyshevPolynomial(int degree) {
      return buildPolynomial(degree, CHEBYSHEV_COEFFICIENTS, new PolynomialsUtils.RecurrenceCoefficientsGenerator() {
         private final BigFraction[] coeffs = new BigFraction[]{BigFraction.ZERO, BigFraction.TWO, BigFraction.ONE};

         @Override
         public BigFraction[] generate(int k) {
            return this.coeffs;
         }
      });
   }

   public static PolynomialFunction createHermitePolynomial(int degree) {
      return buildPolynomial(degree, HERMITE_COEFFICIENTS, new PolynomialsUtils.RecurrenceCoefficientsGenerator() {
         @Override
         public BigFraction[] generate(int k) {
            return new BigFraction[]{BigFraction.ZERO, BigFraction.TWO, new BigFraction(2 * k)};
         }
      });
   }

   public static PolynomialFunction createLaguerrePolynomial(int degree) {
      return buildPolynomial(degree, LAGUERRE_COEFFICIENTS, new PolynomialsUtils.RecurrenceCoefficientsGenerator() {
         @Override
         public BigFraction[] generate(int k) {
            int kP1 = k + 1;
            return new BigFraction[]{new BigFraction(2 * k + 1, kP1), new BigFraction(-1, kP1), new BigFraction(k, kP1)};
         }
      });
   }

   public static PolynomialFunction createLegendrePolynomial(int degree) {
      return buildPolynomial(degree, LEGENDRE_COEFFICIENTS, new PolynomialsUtils.RecurrenceCoefficientsGenerator() {
         @Override
         public BigFraction[] generate(int k) {
            int kP1 = k + 1;
            return new BigFraction[]{BigFraction.ZERO, new BigFraction(k + kP1, kP1), new BigFraction(k, kP1)};
         }
      });
   }

   private static PolynomialFunction buildPolynomial(
      int degree, ArrayList<BigFraction> coefficients, PolynomialsUtils.RecurrenceCoefficientsGenerator generator
   ) {
      int maxDegree = (int)FastMath.floor(FastMath.sqrt((double)(2 * coefficients.size()))) - 1;
      synchronized(PolynomialsUtils.class) {
         if (degree > maxDegree) {
            computeUpToDegree(degree, maxDegree, generator, coefficients);
         }
      }

      int start = degree * (degree + 1) / 2;
      double[] a = new double[degree + 1];

      for(int i = 0; i <= degree; ++i) {
         a[i] = coefficients.get(start + i).doubleValue();
      }

      return new PolynomialFunction(a);
   }

   private static void computeUpToDegree(
      int degree, int maxDegree, PolynomialsUtils.RecurrenceCoefficientsGenerator generator, ArrayList<BigFraction> coefficients
   ) {
      int startK = (maxDegree - 1) * maxDegree / 2;

      for(int k = maxDegree; k < degree; ++k) {
         int startKm1 = startK;
         startK += k;
         BigFraction[] ai = generator.generate(k);
         BigFraction ck = coefficients.get(startK);
         BigFraction ckm1 = coefficients.get(startKm1);
         coefficients.add(ck.multiply(ai[0]).subtract(ckm1.multiply(ai[2])));

         for(int i = 1; i < k; ++i) {
            BigFraction ckPrev = ck;
            ck = coefficients.get(startK + i);
            ckm1 = coefficients.get(startKm1 + i);
            coefficients.add(ck.multiply(ai[0]).add(ckPrev.multiply(ai[1])).subtract(ckm1.multiply(ai[2])));
         }

         BigFraction var12 = coefficients.get(startK + k);
         coefficients.add(var12.multiply(ai[0]).add(ck.multiply(ai[1])));
         coefficients.add(var12.multiply(ai[1]));
      }
   }

   static {
      CHEBYSHEV_COEFFICIENTS.add(BigFraction.ONE);
      CHEBYSHEV_COEFFICIENTS.add(BigFraction.ZERO);
      CHEBYSHEV_COEFFICIENTS.add(BigFraction.ONE);
      HERMITE_COEFFICIENTS.add(BigFraction.ONE);
      HERMITE_COEFFICIENTS.add(BigFraction.ZERO);
      HERMITE_COEFFICIENTS.add(BigFraction.TWO);
      LAGUERRE_COEFFICIENTS.add(BigFraction.ONE);
      LAGUERRE_COEFFICIENTS.add(BigFraction.ONE);
      LAGUERRE_COEFFICIENTS.add(BigFraction.MINUS_ONE);
      LEGENDRE_COEFFICIENTS.add(BigFraction.ONE);
      LEGENDRE_COEFFICIENTS.add(BigFraction.ZERO);
      LEGENDRE_COEFFICIENTS.add(BigFraction.ONE);
   }

   private interface RecurrenceCoefficientsGenerator {
      BigFraction[] generate(int var1);
   }
}
