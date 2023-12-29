package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;

@Deprecated
public interface HasDensity<P> {
   double density(P var1) throws MathException;
}
