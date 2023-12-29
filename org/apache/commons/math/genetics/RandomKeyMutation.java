package org.apache.commons.math.genetics;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class RandomKeyMutation implements MutationPolicy {
   @Override
   public Chromosome mutate(Chromosome original) {
      if (!(original instanceof RandomKey)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.RANDOMKEY_MUTATION_WRONG_CLASS, original.getClass().getSimpleName());
      } else {
         RandomKey<?> originalRk = (RandomKey)original;
         List<Double> repr = originalRk.getRepresentation();
         int rInd = GeneticAlgorithm.getRandomGenerator().nextInt(repr.size());
         List<Double> newRepr = new ArrayList<>(repr);
         newRepr.set(rInd, GeneticAlgorithm.getRandomGenerator().nextDouble());
         return originalRk.newFixedLengthChromosome(newRepr);
      }
   }
}
