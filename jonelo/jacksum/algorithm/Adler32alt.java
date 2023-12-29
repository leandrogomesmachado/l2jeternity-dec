package jonelo.jacksum.algorithm;

public class Adler32alt extends AbstractChecksum {
   private static final long BASE = 65521L;

   public Adler32alt() {
      this.reset();
   }

   public void reset() {
      this.value = 1L;
      this.length = 0L;
   }

   public void update(byte[] var1, int var2, int var3) {
      long var4 = this.value & 65535L;
      long var6 = this.value >> 16 & 65535L;

      for(int var8 = var2; var8 < var3 + var2; ++var8) {
         var4 = (var4 + (long)(var1[var8] & 255)) % 65521L;
         var6 = (var6 + var4) % 65521L;
      }

      this.value = var6 << 16 | var4;
      this.length += (long)var3;
   }

   public void update(byte var1) {
      this.update(new byte[]{var1}, 0, 1);
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 24 & 255L)), (byte)((int)(var1 >> 16 & 255L)), (byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
