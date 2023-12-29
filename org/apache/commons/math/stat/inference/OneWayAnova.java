package org.apache.commons.math.stat.inference;

import java.util.Collection;
import org.apache.commons.math.MathException;

public interface OneWayAnova {
   double anovaFValue(Collection<double[]> var1) throws IllegalArgumentException, MathException;

   double anovaPValue(Collection<double[]> var1) throws IllegalArgumentException, MathException;

   boolean anovaTest(Collection<double[]> var1, double var2) throws IllegalArgumentException, MathException;
}
