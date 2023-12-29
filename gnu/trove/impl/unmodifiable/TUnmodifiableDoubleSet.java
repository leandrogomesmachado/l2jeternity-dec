package gnu.trove.impl.unmodifiable;

import gnu.trove.set.TDoubleSet;
import java.io.Serializable;

public class TUnmodifiableDoubleSet extends TUnmodifiableDoubleCollection implements TDoubleSet, Serializable {
   private static final long serialVersionUID = -9215047833775013803L;

   public TUnmodifiableDoubleSet(TDoubleSet s) {
      super(s);
   }

   @Override
   public boolean equals(Object o) {
      return o == this || this.c.equals(o);
   }

   @Override
   public int hashCode() {
      return this.c.hashCode();
   }
}
