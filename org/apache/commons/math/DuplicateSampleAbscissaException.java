package org.apache.commons.math;

import org.apache.commons.math.exception.util.LocalizedFormats;

public class DuplicateSampleAbscissaException extends MathException {
   private static final long serialVersionUID = -2271007547170169872L;

   public DuplicateSampleAbscissaException(double abscissa, int i1, int i2) {
      super(LocalizedFormats.DUPLICATED_ABSCISSA, abscissa, i1, i2);
   }

   public double getDuplicateAbscissa() {
      return this.getArguments()[0];
   }
}
