package org.apache.commons.math.genetics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public abstract class ListPopulation implements Population {
   private List<Chromosome> chromosomes;
   private int populationLimit;

   public ListPopulation(List<Chromosome> chromosomes, int populationLimit) {
      if (chromosomes.size() > populationLimit) {
         throw new NumberIsTooLargeException(LocalizedFormats.LIST_OF_CHROMOSOMES_BIGGER_THAN_POPULATION_SIZE, chromosomes.size(), populationLimit, false);
      } else if (populationLimit < 0) {
         throw new NotPositiveException(LocalizedFormats.POPULATION_LIMIT_NOT_POSITIVE, populationLimit);
      } else {
         this.chromosomes = chromosomes;
         this.populationLimit = populationLimit;
      }
   }

   public ListPopulation(int populationLimit) {
      if (populationLimit < 0) {
         throw new NotPositiveException(LocalizedFormats.POPULATION_LIMIT_NOT_POSITIVE, populationLimit);
      } else {
         this.populationLimit = populationLimit;
         this.chromosomes = new ArrayList<>(populationLimit);
      }
   }

   public void setChromosomes(List<Chromosome> chromosomes) {
      this.chromosomes = chromosomes;
   }

   public List<Chromosome> getChromosomes() {
      return this.chromosomes;
   }

   @Override
   public void addChromosome(Chromosome chromosome) {
      this.chromosomes.add(chromosome);
   }

   @Override
   public Chromosome getFittestChromosome() {
      Chromosome bestChromosome = this.chromosomes.get(0);

      for(Chromosome chromosome : this.chromosomes) {
         if (chromosome.compareTo(bestChromosome) > 0) {
            bestChromosome = chromosome;
         }
      }

      return bestChromosome;
   }

   @Override
   public int getPopulationLimit() {
      return this.populationLimit;
   }

   public void setPopulationLimit(int populationLimit) {
      this.populationLimit = populationLimit;
   }

   @Override
   public int getPopulationSize() {
      return this.chromosomes.size();
   }

   @Override
   public String toString() {
      return this.chromosomes.toString();
   }

   @Override
   public Iterator<Chromosome> iterator() {
      return this.chromosomes.iterator();
   }
}
