package org.apache.commons.math.random;

import org.apache.commons.math.util.FastMath;

public class UnitSphereRandomVectorGenerator implements RandomVectorGenerator {
   private final RandomGenerator rand;
   private final int dimension;

   public UnitSphereRandomVectorGenerator(int dimension, RandomGenerator rand) {
      this.dimension = dimension;
      this.rand = rand;
   }

   public UnitSphereRandomVectorGenerator(int dimension) {
      this(dimension, new MersenneTwister());
   }

   @Override
   public double[] nextVector() {
      double[] v = new double[this.dimension];

      double normSq;
      do {
         normSq = 0.0;

         for(int i = 0; i < this.dimension; ++i) {
            double comp = 2.0 * this.rand.nextDouble() - 1.0;
            v[i] = comp;
            normSq += comp * comp;
         }
      } while(normSq > 1.0);

      double f = 1.0 / FastMath.sqrt(normSq);

      for(int i = 0; i < this.dimension; ++i) {
         v[i] *= f;
      }

      return v;
   }
}
