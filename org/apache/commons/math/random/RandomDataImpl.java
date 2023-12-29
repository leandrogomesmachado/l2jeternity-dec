package org.apache.commons.math.random;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Collection;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BetaDistributionImpl;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.CauchyDistributionImpl;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.apache.commons.math.distribution.IntegerDistribution;
import org.apache.commons.math.distribution.PascalDistributionImpl;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.distribution.WeibullDistributionImpl;
import org.apache.commons.math.distribution.ZipfDistributionImpl;
import org.apache.commons.math.exception.MathInternalError;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class RandomDataImpl implements RandomData, Serializable {
   private static final long serialVersionUID = -626730818244969716L;
   private RandomGenerator rand = null;
   private SecureRandom secRand = null;

   public RandomDataImpl() {
   }

   public RandomDataImpl(RandomGenerator rand) {
      this.rand = rand;
   }

   @Override
   public String nextHexString(int len) {
      if (len <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
      } else {
         RandomGenerator ran = this.getRan();
         StringBuilder outBuffer = new StringBuilder();
         byte[] randomBytes = new byte[len / 2 + 1];
         ran.nextBytes(randomBytes);

         for(int i = 0; i < randomBytes.length; ++i) {
            Integer c = Integer.valueOf(randomBytes[i]);
            String hex = Integer.toHexString(c + 128);
            if (hex.length() == 1) {
               hex = "0" + hex;
            }

            outBuffer.append(hex);
         }

         return outBuffer.toString().substring(0, len);
      }
   }

   @Override
   public int nextInt(int lower, int upper) {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         double r = this.getRan().nextDouble();
         return (int)(r * (double)upper + (1.0 - r) * (double)lower + r);
      }
   }

   @Override
   public long nextLong(long lower, long upper) {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         double r = this.getRan().nextDouble();
         return (long)(r * (double)upper + (1.0 - r) * (double)lower + r);
      }
   }

   @Override
   public String nextSecureHexString(int len) {
      if (len <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
      } else {
         SecureRandom secRan = this.getSecRan();
         MessageDigest alg = null;

         try {
            alg = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException var12) {
            throw new MathInternalError(var12);
         }

         alg.reset();
         int numIter = len / 40 + 1;
         StringBuilder outBuffer = new StringBuilder();

         for(int iter = 1; iter < numIter + 1; ++iter) {
            byte[] randomBytes = new byte[40];
            secRan.nextBytes(randomBytes);
            alg.update(randomBytes);
            byte[] hash = alg.digest();

            for(int i = 0; i < hash.length; ++i) {
               Integer c = Integer.valueOf(hash[i]);
               String hex = Integer.toHexString(c + 128);
               if (hex.length() == 1) {
                  hex = "0" + hex;
               }

               outBuffer.append(hex);
            }
         }

         return outBuffer.toString().substring(0, len);
      }
   }

   @Override
   public int nextSecureInt(int lower, int upper) {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         SecureRandom sec = this.getSecRan();
         return lower + (int)(sec.nextDouble() * (double)(upper - lower + 1));
      }
   }

   @Override
   public long nextSecureLong(long lower, long upper) {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         SecureRandom sec = this.getSecRan();
         return lower + (long)(sec.nextDouble() * (double)(upper - lower + 1L));
      }
   }

   @Override
   public long nextPoisson(double mean) {
      if (mean <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, mean);
      } else {
         RandomGenerator generator = this.getRan();
         double pivot = 40.0;
         if (mean < 40.0) {
            double p = FastMath.exp(-mean);
            long n = 0L;
            double r = 1.0;

            for(double rnd = 1.0; (double)n < 1000.0 * mean; ++n) {
               rnd = generator.nextDouble();
               r *= rnd;
               if (!(r >= p)) {
                  return n;
               }
            }

            return n;
         } else {
            double lambda = FastMath.floor(mean);
            double lambdaFractional = mean - lambda;
            double logLambda = FastMath.log(lambda);
            double logLambdaFactorial = MathUtils.factorialLog((int)lambda);
            long y2 = lambdaFractional < Double.MIN_VALUE ? 0L : this.nextPoisson(lambdaFractional);
            double delta = FastMath.sqrt(lambda * FastMath.log(32.0 * lambda / Math.PI + 1.0));
            double halfDelta = delta / 2.0;
            double twolpd = 2.0 * lambda + delta;
            double a1 = FastMath.sqrt(Math.PI * twolpd) * FastMath.exp(0.0 * lambda);
            double a2 = twolpd / delta * FastMath.exp(-delta * (1.0 + delta) / twolpd);
            double aSum = a1 + a2 + 1.0;
            double p1 = a1 / aSum;
            double p2 = a2 / aSum;
            double c1 = 1.0 / (8.0 * lambda);
            double x = 0.0;
            double y = 0.0;
            double v = 0.0;
            int a = 0;
            double t = 0.0;
            double qr = 0.0;
            double qa = 0.0;

            while(true) {
               double u = this.nextUniform(0.0, 1.0);
               if (u <= p1) {
                  double n = this.nextGaussian(0.0, 1.0);
                  x = n * FastMath.sqrt(lambda + halfDelta) - 0.5;
                  if (x > delta || x < -lambda) {
                     continue;
                  }

                  y = x < 0.0 ? FastMath.floor(x) : FastMath.ceil(x);
                  double e = this.nextExponential(1.0);
                  v = -e - n * n / 2.0 + c1;
               } else {
                  if (u > p1 + p2) {
                     y = lambda;
                     break;
                  }

                  x = delta + twolpd / delta * this.nextExponential(1.0);
                  y = FastMath.ceil(x);
                  v = -this.nextExponential(1.0) - delta * (x + 1.0) / twolpd;
               }

               a = x < 0.0 ? 1 : 0;
               t = y * (y + 1.0) / (2.0 * lambda);
               if (v < -t && a == 0) {
                  y = lambda + y;
                  break;
               }

               qr = t * ((2.0 * y + 1.0) / (6.0 * lambda) - 1.0);
               qa = qr - t * t / (3.0 * (lambda + (double)a * (y + 1.0)));
               if (v < qa) {
                  y = lambda + y;
                  break;
               }

               if (!(v > qr) && v < y * logLambda - MathUtils.factorialLog((int)(y + lambda)) + logLambdaFactorial) {
                  y = lambda + y;
                  break;
               }
            }

            return y2 + (long)y;
         }
      }
   }

   @Override
   public double nextGaussian(double mu, double sigma) {
      if (sigma <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.STANDARD_DEVIATION, sigma);
      } else {
         return sigma * this.getRan().nextGaussian() + mu;
      }
   }

   @Override
   public double nextExponential(double mean) {
      if (mean <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, mean);
      } else {
         RandomGenerator generator = this.getRan();
         double unif = generator.nextDouble();

         while(unif == 0.0) {
            unif = generator.nextDouble();
         }

         return -mean * FastMath.log(unif);
      }
   }

   @Override
   public double nextUniform(double lower, double upper) {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         RandomGenerator generator = this.getRan();
         double u = generator.nextDouble();

         while(u <= 0.0) {
            u = generator.nextDouble();
         }

         return lower + u * (upper - lower);
      }
   }

   public double nextBeta(double alpha, double beta) throws MathException {
      return this.nextInversionDeviate(new BetaDistributionImpl(alpha, beta));
   }

   public int nextBinomial(int numberOfTrials, double probabilityOfSuccess) throws MathException {
      return this.nextInversionDeviate(new BinomialDistributionImpl(numberOfTrials, probabilityOfSuccess));
   }

   public double nextCauchy(double median, double scale) throws MathException {
      return this.nextInversionDeviate(new CauchyDistributionImpl(median, scale));
   }

   public double nextChiSquare(double df) throws MathException {
      return this.nextInversionDeviate(new ChiSquaredDistributionImpl(df));
   }

   public double nextF(double numeratorDf, double denominatorDf) throws MathException {
      return this.nextInversionDeviate(new FDistributionImpl(numeratorDf, denominatorDf));
   }

   public double nextGamma(double shape, double scale) throws MathException {
      return this.nextInversionDeviate(new GammaDistributionImpl(shape, scale));
   }

   public int nextHypergeometric(int populationSize, int numberOfSuccesses, int sampleSize) throws MathException {
      return this.nextInversionDeviate(new HypergeometricDistributionImpl(populationSize, numberOfSuccesses, sampleSize));
   }

   public int nextPascal(int r, double p) throws MathException {
      return this.nextInversionDeviate(new PascalDistributionImpl(r, p));
   }

   public double nextT(double df) throws MathException {
      return this.nextInversionDeviate(new TDistributionImpl(df));
   }

   public double nextWeibull(double shape, double scale) throws MathException {
      return this.nextInversionDeviate(new WeibullDistributionImpl(shape, scale));
   }

   public int nextZipf(int numberOfElements, double exponent) throws MathException {
      return this.nextInversionDeviate(new ZipfDistributionImpl(numberOfElements, exponent));
   }

   private RandomGenerator getRan() {
      if (this.rand == null) {
         this.rand = new JDKRandomGenerator();
         this.rand.setSeed(System.currentTimeMillis());
      }

      return this.rand;
   }

   private SecureRandom getSecRan() {
      if (this.secRand == null) {
         this.secRand = new SecureRandom();
         this.secRand.setSeed(System.currentTimeMillis());
      }

      return this.secRand;
   }

   public void reSeed(long seed) {
      if (this.rand == null) {
         this.rand = new JDKRandomGenerator();
      }

      this.rand.setSeed(seed);
   }

   public void reSeedSecure() {
      if (this.secRand == null) {
         this.secRand = new SecureRandom();
      }

      this.secRand.setSeed(System.currentTimeMillis());
   }

   public void reSeedSecure(long seed) {
      if (this.secRand == null) {
         this.secRand = new SecureRandom();
      }

      this.secRand.setSeed(seed);
   }

   public void reSeed() {
      if (this.rand == null) {
         this.rand = new JDKRandomGenerator();
      }

      this.rand.setSeed(System.currentTimeMillis());
   }

   public void setSecureAlgorithm(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
      this.secRand = SecureRandom.getInstance(algorithm, provider);
   }

   @Override
   public int[] nextPermutation(int n, int k) {
      if (k > n) {
         throw new NumberIsTooLargeException(LocalizedFormats.PERMUTATION_EXCEEDS_N, k, n, true);
      } else if (k == 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.PERMUTATION_SIZE, k);
      } else {
         int[] index = this.getNatural(n);
         this.shuffle(index, n - k);
         int[] result = new int[k];

         for(int i = 0; i < k; ++i) {
            result[i] = index[n - i - 1];
         }

         return result;
      }
   }

   @Override
   public Object[] nextSample(Collection<?> c, int k) {
      int len = c.size();
      if (k > len) {
         throw new NumberIsTooLargeException(LocalizedFormats.SAMPLE_SIZE_EXCEEDS_COLLECTION_SIZE, k, len, true);
      } else if (k <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, k);
      } else {
         Object[] objects = c.toArray();
         int[] index = this.nextPermutation(len, k);
         Object[] result = new Object[k];

         for(int i = 0; i < k; ++i) {
            result[i] = objects[index[i]];
         }

         return result;
      }
   }

   public double nextInversionDeviate(ContinuousDistribution distribution) throws MathException {
      return distribution.inverseCumulativeProbability(this.nextUniform(0.0, 1.0));
   }

   public int nextInversionDeviate(IntegerDistribution distribution) throws MathException {
      double target = this.nextUniform(0.0, 1.0);
      int glb = distribution.inverseCumulativeProbability(target);
      return distribution.cumulativeProbability(glb) == 1.0 ? glb : glb + 1;
   }

   private void shuffle(int[] list, int end) {
      int target = 0;

      for(int i = list.length - 1; i >= end; --i) {
         if (i == 0) {
            target = 0;
         } else {
            target = this.nextInt(0, i);
         }

         int temp = list[target];
         list[target] = list[i];
         list[i] = temp;
      }
   }

   private int[] getNatural(int n) {
      int[] natural = new int[n];
      int i = 0;

      while(i < n) {
         natural[i] = i++;
      }

      return natural;
   }
}
