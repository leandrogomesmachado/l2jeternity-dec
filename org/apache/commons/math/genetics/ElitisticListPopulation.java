package org.apache.commons.math.genetics;

import java.util.Collections;
import java.util.List;
import org.apache.commons.math.util.FastMath;

public class ElitisticListPopulation extends ListPopulation {
   private double elitismRate = 0.9;

   public ElitisticListPopulation(List<Chromosome> chromosomes, int populationLimit, double elitismRate) {
      super(chromosomes, populationLimit);
      this.elitismRate = elitismRate;
   }

   public ElitisticListPopulation(int populationLimit, double elitismRate) {
      super(populationLimit);
      this.elitismRate = elitismRate;
   }

   @Override
   public Population nextGeneration() {
      ElitisticListPopulation nextGeneration = new ElitisticListPopulation(this.getPopulationLimit(), this.getElitismRate());
      List<Chromosome> oldChromosomes = this.getChromosomes();
      Collections.sort(oldChromosomes);
      int boundIndex = (int)FastMath.ceil((1.0 - this.getElitismRate()) * (double)oldChromosomes.size());

      for(int i = boundIndex; i < oldChromosomes.size(); ++i) {
         nextGeneration.addChromosome(oldChromosomes.get(i));
      }

      return nextGeneration;
   }

   public void setElitismRate(double elitismRate) {
      if (!(elitismRate < 0.0) && !(elitismRate > 1.0)) {
         this.elitismRate = elitismRate;
      } else {
         throw new IllegalArgumentException("Elitism rate has to be in [0,1]");
      }
   }

   public double getElitismRate() {
      return this.elitismRate;
   }
}
