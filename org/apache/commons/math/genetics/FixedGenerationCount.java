package org.apache.commons.math.genetics;

public class FixedGenerationCount implements StoppingCondition {
   private int numGenerations = 0;
   private final int maxGenerations;

   public FixedGenerationCount(int maxGenerations) {
      if (maxGenerations <= 0) {
         throw new IllegalArgumentException("The number of generations has to be >= 0");
      } else {
         this.maxGenerations = maxGenerations;
      }
   }

   @Override
   public boolean isSatisfied(Population population) {
      if (this.numGenerations < this.maxGenerations) {
         ++this.numGenerations;
         return false;
      } else {
         return true;
      }
   }

   public int getNumGenerations() {
      return this.numGenerations;
   }
}
