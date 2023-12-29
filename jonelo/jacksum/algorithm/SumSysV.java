package jonelo.jacksum.algorithm;

public class SumSysV extends AbstractChecksum {
   public SumSysV() {
      this.separator = " ";
   }

   public void update(int var1) {
      this.value += (long)(var1 & 0xFF);
      ++this.length;
   }

   public void update(byte var1) {
      this.value += (long)(var1 & 255);
      ++this.length;
   }

   public long getValue() {
      long var1 = (this.value & 65535L) + ((this.value & -1L) >> 16 & 65535L);
      this.value = (var1 & 65535L) + (var1 >> 16);
      return this.value;
   }

   public String toString() {
      long var1 = (this.length + 511L) / 512L;
      return this.getFormattedValue()
         + this.separator
         + var1
         + this.separator
         + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "")
         + this.filename;
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
