package gnu.trove.impl.sync;

import gnu.trove.set.TFloatSet;

public class TSynchronizedFloatSet extends TSynchronizedFloatCollection implements TFloatSet {
   private static final long serialVersionUID = 487447009682186044L;

   public TSynchronizedFloatSet(TFloatSet s) {
      super(s);
   }

   public TSynchronizedFloatSet(TFloatSet s, Object mutex) {
      super(s, mutex);
   }

   @Override
   public boolean equals(Object o) {
      synchronized(this.mutex) {
         return this.c.equals(o);
      }
   }

   @Override
   public int hashCode() {
      synchronized(this.mutex) {
         return this.c.hashCode();
      }
   }
}
