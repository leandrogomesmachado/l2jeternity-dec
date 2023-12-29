package org.apache.commons.math.util;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class DefaultTransformer implements NumberTransformer, Serializable {
   private static final long serialVersionUID = 4019938025047800455L;

   @Override
   public double transform(Object o) throws MathException {
      if (o == null) {
         throw new MathException(LocalizedFormats.OBJECT_TRANSFORMATION);
      } else if (o instanceof Number) {
         return ((Number)o).doubleValue();
      } else {
         try {
            return Double.valueOf(o.toString());
         } catch (NumberFormatException var3) {
            throw new MathException(var3, LocalizedFormats.CANNOT_TRANSFORM_TO_DOUBLE, var3.getMessage());
         }
      }
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return other == null ? false : other instanceof DefaultTransformer;
      }
   }

   @Override
   public int hashCode() {
      return 401993047;
   }
}
