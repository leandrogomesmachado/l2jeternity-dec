package jonelo.jacksum.algorithm;

public class Read extends AbstractChecksum {
   public Read() {
      this.encoding = "hex";
   }

   public void reset() {
      this.length = 0L;
   }

   public void update(byte[] var1, int var2, int var3) {
      this.length += (long)var3;
   }

   public void update(byte[] var1) {
      this.length += (long)var1.length;
   }

   public void update(int var1) {
      ++this.length;
   }

   public void update(byte var1) {
      ++this.length;
   }

   public String toString() {
      return this.length + this.separator + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "") + this.getFilename();
   }

   public String getFormattedValue() {
      return "";
   }
}
