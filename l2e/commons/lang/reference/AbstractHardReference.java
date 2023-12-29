package l2e.commons.lang.reference;

public class AbstractHardReference<T> implements HardReference<T> {
   private T _reference;

   public AbstractHardReference(T reference) {
      this._reference = reference;
   }

   @Override
   public T get() {
      return this._reference;
   }

   @Override
   public void clear() {
      this._reference = null;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (!(o instanceof AbstractHardReference)) {
         return false;
      } else {
         return ((AbstractHardReference)o).get() == null ? false : ((AbstractHardReference)o).get().equals(this.get());
      }
   }
}
