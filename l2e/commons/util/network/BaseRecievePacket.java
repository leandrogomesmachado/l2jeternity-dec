package l2e.commons.util.network;

import java.util.logging.Logger;

public abstract class BaseRecievePacket {
   private static final Logger _log = Logger.getLogger(BaseRecievePacket.class.getName());
   private final byte[] _decrypt;
   private int _off;

   public BaseRecievePacket(byte[] decrypt) {
      this._decrypt = decrypt;
      this._off = 1;
   }

   public int readD() {
      int result = this._decrypt[this._off++] & 255;
      result |= this._decrypt[this._off++] << 8 & 0xFF00;
      result |= this._decrypt[this._off++] << 16 & 0xFF0000;
      return result | this._decrypt[this._off++] << 24 & 0xFF000000;
   }

   public int readC() {
      return this._decrypt[this._off++] & 0xFF;
   }

   public int readH() {
      int result = this._decrypt[this._off++] & 255;
      return result | this._decrypt[this._off++] << 8 & 0xFF00;
   }

   public double readF() {
      long result = (long)(this._decrypt[this._off++] & 255);
      result |= ((long)this._decrypt[this._off++] & 255L) << 8;
      result |= ((long)this._decrypt[this._off++] & 255L) << 16;
      result |= ((long)this._decrypt[this._off++] & 255L) << 24;
      result |= ((long)this._decrypt[this._off++] & 255L) << 32;
      result |= ((long)this._decrypt[this._off++] & 255L) << 40;
      result |= ((long)this._decrypt[this._off++] & 255L) << 48;
      result |= ((long)this._decrypt[this._off++] & 255L) << 56;
      return Double.longBitsToDouble(result);
   }

   public String readS() {
      String result = null;

      try {
         result = new String(this._decrypt, this._off, this._decrypt.length - this._off, "UTF-16LE");
         result = result.substring(0, result.indexOf(0));
         this._off += result.length() * 2 + 2;
      } catch (Exception var3) {
         _log.warning(this.getClass().getSimpleName() + ": " + var3.getMessage());
      }

      return result;
   }

   public final byte[] readB(int length) {
      byte[] result = new byte[length];
      System.arraycopy(this._decrypt, this._off, result, 0, length);
      this._off += length;
      return result;
   }

   public long readQ() {
      long result = (long)(this._decrypt[this._off++] & 255);
      result |= ((long)this._decrypt[this._off++] & 255L) << 8;
      result |= ((long)this._decrypt[this._off++] & 255L) << 16;
      result |= ((long)this._decrypt[this._off++] & 255L) << 24;
      result |= ((long)this._decrypt[this._off++] & 255L) << 32;
      result |= ((long)this._decrypt[this._off++] & 255L) << 40;
      result |= ((long)this._decrypt[this._off++] & 255L) << 48;
      return result | ((long)this._decrypt[this._off++] & 255L) << 56;
   }
}
