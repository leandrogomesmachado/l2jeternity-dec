package jonelo.jacksum.algorithm;

public class Sum24 extends Sum8 {
   public Sum24() {
      this.value = 0L;
   }

   public long getValue() {
      return this.value % 16777216L;
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 16 & 255L)), (byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
