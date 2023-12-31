package org.apache.commons.math.genetics;

import java.util.ArrayList;
import java.util.List;

public class OnePointCrossover<T> implements CrossoverPolicy {
   @Override
   public ChromosomePair crossover(Chromosome first, Chromosome second) {
      if (first instanceof AbstractListChromosome && second instanceof AbstractListChromosome) {
         return this.crossover((AbstractListChromosome<T>)first, (AbstractListChromosome<T>)second);
      } else {
         throw new IllegalArgumentException("One point crossover works on FixedLengthChromosomes only.");
      }
   }

   private ChromosomePair crossover(AbstractListChromosome<T> first, AbstractListChromosome<T> second) {
      int length = first.getLength();
      if (length != second.getLength()) {
         throw new IllegalArgumentException("Both chromosomes must have same lengths.");
      } else {
         List<T> parent1Rep = first.getRepresentation();
         List<T> parent2Rep = second.getRepresentation();
         ArrayList<T> child1Rep = new ArrayList<>(first.getLength());
         ArrayList<T> child2Rep = new ArrayList<>(second.getLength());
         int crossoverIndex = 1 + GeneticAlgorithm.getRandomGenerator().nextInt(length - 2);

         for(int i = 0; i < crossoverIndex; ++i) {
            child1Rep.add(parent1Rep.get(i));
            child2Rep.add(parent2Rep.get(i));
         }

         for(int i = crossoverIndex; i < length; ++i) {
            child1Rep.add(parent2Rep.get(i));
            child2Rep.add(parent1Rep.get(i));
         }

         return new ChromosomePair(first.newFixedLengthChromosome(child1Rep), second.newFixedLengthChromosome(child2Rep));
      }
   }
}
