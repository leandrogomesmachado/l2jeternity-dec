package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;

public interface IntegerDistribution extends DiscreteDistribution {
   double probability(int var1);

   double cumulativeProbability(int var1) throws MathException;

   double cumulativeProbability(int var1, int var2) throws MathException;

   int inverseCumulativeProbability(double var1) throws MathException;
}
