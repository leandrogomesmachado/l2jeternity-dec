package org.apache.commons.math.genetics;

public interface Population extends Iterable<Chromosome> {
   int getPopulationSize();

   int getPopulationLimit();

   Population nextGeneration();

   void addChromosome(Chromosome var1);

   Chromosome getFittestChromosome();
}
