package org.apache.commons.math.genetics;

import java.util.ArrayList;
import java.util.List;

public class BinaryMutation implements MutationPolicy {
   @Override
   public Chromosome mutate(Chromosome original) {
      if (!(original instanceof BinaryChromosome)) {
         throw new IllegalArgumentException("Binary mutation works on BinaryChromosome only.");
      } else {
         BinaryChromosome origChrom = (BinaryChromosome)original;
         List<Integer> newRepr = new ArrayList<>(origChrom.getRepresentation());
         int geneIndex = GeneticAlgorithm.getRandomGenerator().nextInt(origChrom.getLength());
         newRepr.set(geneIndex, origChrom.getRepresentation().get(geneIndex) == 0 ? 1 : 0);
         Chromosome newChrom = origChrom.newFixedLengthChromosome(newRepr);
         return newChrom;
      }
   }
}
