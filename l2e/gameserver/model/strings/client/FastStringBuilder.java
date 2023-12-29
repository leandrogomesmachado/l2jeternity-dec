package l2e.gameserver.model.strings.client;

final class FastStringBuilder {
   private final char[] _array;
   private int _len;

   public FastStringBuilder(int capacity) {
      this._array = new char[capacity];
   }

   public final void append(String text) {
      text.getChars(0, text.length(), this._array, this._len);
      this._len += text.length();
   }

   @Override
   public final String toString() {
      return new String(this._array);
   }
}
