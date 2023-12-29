package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;

public interface Distribution {
   double cumulativeProbability(double var1) throws MathException;

   double cumulativeProbability(double var1, double var3) throws MathException;
}
