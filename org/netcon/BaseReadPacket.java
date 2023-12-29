package org.netcon;

public abstract class BaseReadPacket implements Runnable {
   private final byte[] _data;
   private int _off;

   protected BaseReadPacket(byte[] data) {
      this._data = data;
      this._off = 2;
   }

   protected final int readC() {
      return this._data[this._off++] & 0xFF;
   }

   protected final int readH() {
      int result = this._data[this._off++] & 255;
      return result | this._data[this._off++] << 8 & 0xFF00;
   }

   protected final int readD() {
      int result = this._data[this._off++] & 255;
      result |= this._data[this._off++] << 8 & 0xFF00;
      result |= this._data[this._off++] << 16 & 0xFF0000;
      return result | this._data[this._off++] << 24 & 0xFF000000;
   }

   protected final double readF() {
      long result = (long)(this._data[this._off++] & 255);
      result |= (long)(this._data[this._off++] << 8 & 0xFF00);
      result |= (long)(this._data[this._off++] << 16 & 0xFF0000);
      result |= (long)(this._data[this._off++] << 24 & 0xFF000000);
      result |= (long)(this._data[this._off++] << 32 & 0);
      result |= (long)(this._data[this._off++] << 40 & 0);
      result |= (long)(this._data[this._off++] << 48 & 0);
      result |= (long)(this._data[this._off++] << 56 & 0);
      return Double.longBitsToDouble(result);
   }

   protected final long readQ() {
      int value1 = this._data[this._off++] & 255
         | this._data[this._off++] << 8 & 0xFF00
         | this._data[this._off++] << 16 & 0xFF0000
         | this._data[this._off++] << 24 & 0xFF000000;
      int value2 = this._data[this._off++] & 255
         | this._data[this._off++] << 8 & 0xFF00
         | this._data[this._off++] << 16 & 0xFF0000
         | this._data[this._off++] << 24 & 0xFF000000;
      return (long)value1 & 4294967295L | ((long)value2 & 4294967295L) << 32;
   }

   protected final byte[] readB(int length) {
      byte[] result = new byte[length];

      for(int i = 0; i < length; ++i) {
         result[i] = this._data[this._off + i];
      }

      this._off += length;
      return result;
   }

   protected final String readS() {
      String result = null;

      try {
         result = new String(this._data, this._off, this._data.length - this._off, "UTF-16LE");
         result = result.substring(0, result.indexOf(0));
         this._off += result.length() * 2 + 2;
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return result;
   }
}
