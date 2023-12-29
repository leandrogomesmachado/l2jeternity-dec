package org.netcon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class BaseWritePacket {
   private final ByteArrayOutputStream _bao = new ByteArrayOutputStream();

   protected BaseWritePacket() {
   }

   protected final void writeC(int value) {
      this._bao.write(value & 0xFF);
   }

   protected final void writeH(int value) {
      this._bao.write(value & 0xFF);
      this._bao.write(value >> 8 & 0xFF);
   }

   protected final void writeD(int value) {
      this._bao.write(value & 0xFF);
      this._bao.write(value >> 8 & 0xFF);
      this._bao.write(value >> 16 & 0xFF);
      this._bao.write(value >> 24 & 0xFF);
   }

   protected final void writeF(double value) {
      this.writeQ(Double.doubleToRawLongBits(value));
   }

   protected final void writeQ(long value) {
      this._bao.write((byte)((int)(value & 255L)));
      this._bao.write((byte)((int)(value >> 8 & 255L)));
      this._bao.write((byte)((int)(value >> 16 & 255L)));
      this._bao.write((byte)((int)(value >> 24 & 255L)));
      this._bao.write((byte)((int)(value >> 32 & 255L)));
      this._bao.write((byte)((int)(value >> 40 & 255L)));
      this._bao.write((byte)((int)(value >> 48 & 255L)));
      this._bao.write((byte)((int)(value >> 56 & 255L)));
   }

   protected final void writeS(String text) {
      try {
         if (text != null) {
            this._bao.write(text.getBytes("UTF-16LE"));
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      this._bao.write(0);
      this._bao.write(0);
   }

   protected final void writeB(byte[] array) {
      try {
         this._bao.write(array);
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   public final byte[] getContent() {
      this.writeD(0);
      int padding = this._bao.size() % 8;
      if (padding != 0) {
         for(int i = padding; i < 8; ++i) {
            this.writeC(0);
         }
      }

      return this._bao.toByteArray();
   }
}
