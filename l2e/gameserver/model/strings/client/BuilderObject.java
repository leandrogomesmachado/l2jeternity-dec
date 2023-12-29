package l2e.gameserver.model.strings.client;

final class BuilderObject extends Builder {
   private final int _index;

   BuilderObject(int id) {
      if (id >= 1 && id <= 9) {
         this._index = id - 1;
      } else {
         throw new RuntimeException("Illegal Id: " + id);
      }
   }

   @Override
   public final String toString(Object param) {
      return param == null ? "null" : param.toString();
   }

   @Override
   public final String toString(Object... params) {
      return params != null && params.length != 0 ? params[0].toString() : "null";
   }

   @Override
   public final int getIndex() {
      return this._index;
   }

   @Override
   public final String toString() {
      return "[PARAM-" + (this._index + 1) + "]";
   }
}
