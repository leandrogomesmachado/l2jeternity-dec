package jonelo.jacksum.algorithm;

public class Sum32 extends Sum8 {
   public Sum32() {
      this.value = 0L;
   }

   public long getValue() {
      return this.value % 4294967296L;
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 24 & 255L)), (byte)((int)(var1 >> 16 & 255L)), (byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
