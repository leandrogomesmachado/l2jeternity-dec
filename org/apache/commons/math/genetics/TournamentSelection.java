package org.apache.commons.math.genetics;

import java.util.ArrayList;
import java.util.List;

public class TournamentSelection implements SelectionPolicy {
   private int arity;

   public TournamentSelection(int arity) {
      this.arity = arity;
   }

   @Override
   public ChromosomePair select(Population population) {
      return new ChromosomePair(this.tournament((ListPopulation)population), this.tournament((ListPopulation)population));
   }

   private Chromosome tournament(ListPopulation population) {
      if (population.getPopulationSize() < this.arity) {
         throw new IllegalArgumentException("Tournament arity cannot be bigger than population size.");
      } else {
         ListPopulation tournamentPopulation = new ListPopulation(this.arity) {
            @Override
            public Population nextGeneration() {
               return null;
            }
         };
         List<Chromosome> chromosomes = new ArrayList<>(population.getChromosomes());

         for(int i = 0; i < this.arity; ++i) {
            int rind = GeneticAlgorithm.getRandomGenerator().nextInt(chromosomes.size());
            tournamentPopulation.addChromosome(chromosomes.get(rind));
            chromosomes.remove(rind);
         }

         return tournamentPopulation.getFittestChromosome();
      }
   }

   public int getArity() {
      return this.arity;
   }

   public void setArity(int arity) {
      this.arity = arity;
   }
}
