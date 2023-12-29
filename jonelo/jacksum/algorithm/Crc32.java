package jonelo.jacksum.algorithm;

import java.util.zip.CRC32;

public class Crc32 extends AbstractChecksum {
   private CRC32 crc32 = null;

   public Crc32() {
      this.crc32 = new CRC32();
   }

   public void reset() {
      this.crc32.reset();
      this.length = 0L;
   }

   public void update(byte[] var1, int var2, int var3) {
      this.crc32.update(var1, var2, var3);
      this.length += (long)var3;
   }

   public void update(int var1) {
      this.crc32.update(var1);
      ++this.length;
   }

   public void update(byte var1) {
      this.update(var1 & 255);
   }

   public long getValue() {
      return this.crc32.getValue();
   }

   public byte[] getByteArray() {
      long var1 = this.crc32.getValue();
      return new byte[]{(byte)((int)(var1 >> 24 & 255L)), (byte)((int)(var1 >> 16 & 255L)), (byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
