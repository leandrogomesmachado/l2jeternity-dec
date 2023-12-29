package l2e.commons.util.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class BaseSendablePacket {
   private static final Logger _log = Logger.getLogger(BaseSendablePacket.class.getName());
   private final ByteArrayOutputStream _bao = new ByteArrayOutputStream();

   protected BaseSendablePacket() {
   }

   protected void writeD(int value) {
      this._bao.write(value & 0xFF);
      this._bao.write(value >> 8 & 0xFF);
      this._bao.write(value >> 16 & 0xFF);
      this._bao.write(value >> 24 & 0xFF);
   }

   protected void writeH(int value) {
      this._bao.write(value & 0xFF);
      this._bao.write(value >> 8 & 0xFF);
   }

   protected void writeC(int value) {
      this._bao.write(value & 0xFF);
   }

   protected void writeF(double org) {
      long value = Double.doubleToRawLongBits(org);
      this._bao.write((int)(value & 255L));
      this._bao.write((int)(value >> 8 & 255L));
      this._bao.write((int)(value >> 16 & 255L));
      this._bao.write((int)(value >> 24 & 255L));
      this._bao.write((int)(value >> 32 & 255L));
      this._bao.write((int)(value >> 40 & 255L));
      this._bao.write((int)(value >> 48 & 255L));
      this._bao.write((int)(value >> 56 & 255L));
   }

   protected void writeS(String text) {
      try {
         if (text != null) {
            this._bao.write(text.getBytes("UTF-16LE"));
         }
      } catch (Exception var3) {
         _log.warning(this.getClass().getSimpleName() + ": " + var3.getMessage());
      }

      this._bao.write(0);
      this._bao.write(0);
   }

   protected void writeB(byte[] array) {
      try {
         this._bao.write(array);
      } catch (IOException var3) {
         _log.warning(this.getClass().getSimpleName() + ": " + var3.getMessage());
      }
   }

   protected void writeQ(long value) {
      this._bao.write((int)(value & 255L));
      this._bao.write((int)(value >> 8 & 255L));
      this._bao.write((int)(value >> 16 & 255L));
      this._bao.write((int)(value >> 24 & 255L));
      this._bao.write((int)(value >> 32 & 255L));
      this._bao.write((int)(value >> 40 & 255L));
      this._bao.write((int)(value >> 48 & 255L));
      this._bao.write((int)(value >> 56 & 255L));
   }

   public int getLength() {
      return this._bao.size() + 2;
   }

   public byte[] getBytes() {
      this.writeD(0);
      int padding = this._bao.size() % 8;
      if (padding != 0) {
         for(int i = padding; i < 8; ++i) {
            this.writeC(0);
         }
      }

      return this._bao.toByteArray();
   }

   public abstract byte[] getContent() throws IOException;
}
