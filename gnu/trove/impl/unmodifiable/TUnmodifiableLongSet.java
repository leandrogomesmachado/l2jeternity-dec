package gnu.trove.impl.unmodifiable;

import gnu.trove.set.TLongSet;
import java.io.Serializable;

public class TUnmodifiableLongSet extends TUnmodifiableLongCollection implements TLongSet, Serializable {
   private static final long serialVersionUID = -9215047833775013803L;

   public TUnmodifiableLongSet(TLongSet s) {
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
