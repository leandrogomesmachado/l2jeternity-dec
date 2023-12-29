package jonelo.jacksum.algorithm;

public class Adler32 extends AbstractChecksum {
   private java.util.zip.Adler32 adler32 = null;

   public Adler32() {
      this.adler32 = new java.util.zip.Adler32();
   }

   public void reset() {
      this.adler32.reset();
      this.length = 0L;
   }

   public void update(byte[] var1, int var2, int var3) {
      this.adler32.update(var1, var2, var3);
      this.length += (long)var3;
   }

   public void update(int var1) {
      this.adler32.update(var1);
      ++this.length;
   }

   public long getValue() {
      return this.adler32.getValue();
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 24 & 255L)), (byte)((int)(var1 >> 16 & 255L)), (byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
