package org.apache.commons.math.genetics;

import java.util.ArrayList;
import java.util.List;

public abstract class BinaryChromosome extends AbstractListChromosome<Integer> {
   public BinaryChromosome(List<Integer> representation) {
      super(representation);
   }

   public BinaryChromosome(Integer[] representation) {
      super(representation);
   }

   @Override
   protected void checkValidity(List<Integer> chromosomeRepresentation) throws InvalidRepresentationException {
      for(int i : chromosomeRepresentation) {
         if (i < 0 || i > 1) {
            throw new InvalidRepresentationException("Elements can be only 0 or 1.");
         }
      }
   }

   public static List<Integer> randomBinaryRepresentation(int length) {
      List<Integer> rList = new ArrayList<>(length);

      for(int j = 0; j < length; ++j) {
         rList.add(GeneticAlgorithm.getRandomGenerator().nextInt(2));
      }

      return rList;
   }

   @Override
   protected boolean isSame(Chromosome another) {
      if (!(another instanceof BinaryChromosome)) {
         return false;
      } else {
         BinaryChromosome anotherBc = (BinaryChromosome)another;
         if (this.getLength() != anotherBc.getLength()) {
            return false;
         } else {
            for(int i = 0; i < this.getRepresentation().size(); ++i) {
               if (!this.getRepresentation().get(i).equals(anotherBc.getRepresentation().get(i))) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
