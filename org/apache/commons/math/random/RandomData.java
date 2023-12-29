package org.apache.commons.math.random;

import java.util.Collection;

public interface RandomData {
   String nextHexString(int var1);

   int nextInt(int var1, int var2);

   long nextLong(long var1, long var3);

   String nextSecureHexString(int var1);

   int nextSecureInt(int var1, int var2);

   long nextSecureLong(long var1, long var3);

   long nextPoisson(double var1);

   double nextGaussian(double var1, double var3);

   double nextExponential(double var1);

   double nextUniform(double var1, double var3);

   int[] nextPermutation(int var1, int var2);

   Object[] nextSample(Collection<?> var1, int var2);
}
