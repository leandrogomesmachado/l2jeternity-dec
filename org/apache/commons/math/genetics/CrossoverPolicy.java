package org.apache.commons.math.genetics;

public interface CrossoverPolicy {
   ChromosomePair crossover(Chromosome var1, Chromosome var2);
}
