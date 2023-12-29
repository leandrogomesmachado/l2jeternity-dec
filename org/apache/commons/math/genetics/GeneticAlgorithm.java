package org.apache.commons.math.genetics;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;

public class GeneticAlgorithm {
   private static RandomGenerator randomGenerator = new JDKRandomGenerator();
   private final CrossoverPolicy crossoverPolicy;
   private final double crossoverRate;
   private final MutationPolicy mutationPolicy;
   private final double mutationRate;
   private final SelectionPolicy selectionPolicy;
   private int generationsEvolved = 0;

   public GeneticAlgorithm(
      CrossoverPolicy crossoverPolicy, double crossoverRate, MutationPolicy mutationPolicy, double mutationRate, SelectionPolicy selectionPolicy
   ) {
      if (crossoverRate < 0.0 || crossoverRate > 1.0) {
         throw new IllegalArgumentException("crossoverRate must be between 0 and 1");
      } else if (!(mutationRate < 0.0) && !(mutationRate > 1.0)) {
         this.crossoverPolicy = crossoverPolicy;
         this.crossoverRate = crossoverRate;
         this.mutationPolicy = mutationPolicy;
         this.mutationRate = mutationRate;
         this.selectionPolicy = selectionPolicy;
      } else {
         throw new IllegalArgumentException("mutationRate must be between 0 and 1");
      }
   }

   public static synchronized void setRandomGenerator(RandomGenerator random) {
      randomGenerator = random;
   }

   public static synchronized RandomGenerator getRandomGenerator() {
      return randomGenerator;
   }

   public Population evolve(Population initial, StoppingCondition condition) {
      Population current = initial;

      for(this.generationsEvolved = 0; !condition.isSatisfied(current); ++this.generationsEvolved) {
         current = this.nextGeneration(current);
      }

      return current;
   }

   public Population nextGeneration(Population current) {
      Population nextGeneration = current.nextGeneration();
      RandomGenerator randGen = getRandomGenerator();

      while(nextGeneration.getPopulationSize() < nextGeneration.getPopulationLimit()) {
         ChromosomePair pair = this.getSelectionPolicy().select(current);
         if (randGen.nextDouble() < this.getCrossoverRate()) {
            pair = this.getCrossoverPolicy().crossover(pair.getFirst(), pair.getSecond());
         }

         if (randGen.nextDouble() < this.getMutationRate()) {
            pair = new ChromosomePair(this.getMutationPolicy().mutate(pair.getFirst()), this.getMutationPolicy().mutate(pair.getSecond()));
         }

         nextGeneration.addChromosome(pair.getFirst());
         if (nextGeneration.getPopulationSize() < nextGeneration.getPopulationLimit()) {
            nextGeneration.addChromosome(pair.getSecond());
         }
      }

      return nextGeneration;
   }

   public CrossoverPolicy getCrossoverPolicy() {
      return this.crossoverPolicy;
   }

   public double getCrossoverRate() {
      return this.crossoverRate;
   }

   public MutationPolicy getMutationPolicy() {
      return this.mutationPolicy;
   }

   public double getMutationRate() {
      return this.mutationRate;
   }

   public SelectionPolicy getSelectionPolicy() {
      return this.selectionPolicy;
   }

   public int getGenerationsEvolved() {
      return this.generationsEvolved;
   }
}
