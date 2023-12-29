package org.apache.commons.math.random;

public class GaussianRandomGenerator implements NormalizedRandomGenerator {
   private final RandomGenerator generator;

   public GaussianRandomGenerator(RandomGenerator generator) {
      this.generator = generator;
   }

   @Override
   public double nextNormalizedDouble() {
      return this.generator.nextGaussian();
   }
}
