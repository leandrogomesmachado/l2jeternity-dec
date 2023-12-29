package jonelo.jacksum.algorithm;

public class Xor8 extends AbstractChecksum {
   public Xor8() {
      this.value = 0L;
   }

   public void update(byte var1) {
      this.value ^= (long)(var1 & 255);
      ++this.length;
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }
}
