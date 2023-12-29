package gnu.trove.impl.unmodifiable;

import gnu.trove.set.TIntSet;
import java.io.Serializable;

public class TUnmodifiableIntSet extends TUnmodifiableIntCollection implements TIntSet, Serializable {
   private static final long serialVersionUID = -9215047833775013803L;

   public TUnmodifiableIntSet(TIntSet s) {
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
