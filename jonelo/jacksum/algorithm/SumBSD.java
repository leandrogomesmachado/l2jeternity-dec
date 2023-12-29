package jonelo.jacksum.algorithm;

import jonelo.jacksum.util.Service;

public class SumBSD extends AbstractChecksum {
   public SumBSD() {
      this.separator = " ";
   }

   public void update(byte var1) {
      this.value = (this.value >> 1) + ((this.value & 1L) << 15);
      this.value += (long)(var1 & 255);
      this.value &= 65535L;
      ++this.length;
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }

   public String toString() {
      long var1 = (this.length + 1023L) / 1024L;
      return (this.getEncoding().length() == 0 ? Service.decformat(this.getValue(), "00000") : this.getFormattedValue())
         + this.separator
         + Service.right(var1, 5)
         + this.separator
         + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "")
         + this.getFilename();
   }

   public byte[] getByteArray() {
      long var1 = this.getValue();
      return new byte[]{(byte)((int)(var1 >> 8 & 255L)), (byte)((int)(var1 & 255L))};
   }
}
