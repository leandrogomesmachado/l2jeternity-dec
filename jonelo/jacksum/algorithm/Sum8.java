package jonelo.jacksum.algorithm;

public class Sum8 extends AbstractChecksum {
   public Sum8() {
      this.value = 0L;
   }

   public void reset() {
      this.value = 0L;
      this.length = 0L;
   }

   public void update(byte var1) {
      this.value += (long)(var1 & 255);
      ++this.length;
   }

   public void update(int var1) {
      this.value += (long)(var1 & 0xFF);
      ++this.length;
   }

   public long getValue() {
      return this.value % 256L;
   }

   public byte[] getByteArray() {
      return new byte[]{(byte)((int)(this.getValue() & 255L))};
   }
}
