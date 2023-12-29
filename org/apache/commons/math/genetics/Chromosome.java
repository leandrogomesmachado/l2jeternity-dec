package org.apache.commons.math.genetics;

public abstract class Chromosome implements Comparable<Chromosome>, Fitness {
   private double fitness = Double.MIN_VALUE;

   public double getFitness() {
      if (this.fitness == Double.MIN_VALUE) {
         this.fitness = this.fitness();
      }

      return this.fitness;
   }

   public int compareTo(Chromosome another) {
      return Double.valueOf(this.getFitness()).compareTo(another.getFitness());
   }

   protected boolean isSame(Chromosome another) {
      return false;
   }

   protected Chromosome findSameChromosome(Population population) {
      for(Chromosome anotherChr : population) {
         if (this.isSame(anotherChr)) {
            return anotherChr;
         }
      }

      return null;
   }

   public void searchForFitnessUpdate(Population population) {
      Chromosome sameChromosome = this.findSameChromosome(population);
      if (sameChromosome != null) {
         this.fitness = sameChromosome.getFitness();
      }
   }
}
