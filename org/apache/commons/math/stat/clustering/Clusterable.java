package org.apache.commons.math.stat.clustering;

import java.util.Collection;

public interface Clusterable<T> {
   double distanceFrom(T var1);

   T centroidOf(Collection<T> var1);
}
