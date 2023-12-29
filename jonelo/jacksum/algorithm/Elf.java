package jonelo.jacksum.algorithm;

public class Elf extends AbstractChecksum {
   protected long ghash;

   public Elf() {
      this.reset();
   }

   public void reset() {
      this.value = 0L;
      this.length = 0L;
   }

   public void update(byte var1) {
      this.value = (this.value << 4) + (long)(var1 & 255);
      long var2 = this.value & 4026531840L;
      if (var2 != 0L) {
         this.value ^= var2 >>> 24;
      }

      this.value &= ~var2;
      ++this.length;
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 24 & 255L)), (byte)((int)(var1 >> 16 & 255L)), (byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
