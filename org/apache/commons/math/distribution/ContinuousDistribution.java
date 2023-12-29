package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;

public interface ContinuousDistribution extends Distribution {
   double inverseCumulativeProbability(double var1) throws MathException;
}
