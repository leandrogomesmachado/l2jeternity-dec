package jonelo.jacksum.algorithm;

public class Crc32Mpeg2 extends Cksum {
   public void reset() {
      this.value = -1;
      this.length = 0L;
   }

   public long getValue() {
      return (long)this.value & 4294967295L;
   }
}
