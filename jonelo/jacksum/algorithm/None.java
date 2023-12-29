package jonelo.jacksum.algorithm;

import java.io.File;
import java.io.IOException;

public class None extends AbstractChecksum {
   public None() {
      this.encoding = "hex";
   }

   public void reset() {
      this.length = 0L;
   }

   public String toString() {
      return this.length + this.separator + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "") + this.getFilename();
   }

   public String getFormattedValue() {
      return "";
   }

   public long readFile(String var1, boolean var2) throws IOException {
      this.filename = var1;
      if (this.isTimestampWanted()) {
         this.setTimestamp(var1);
      }

      File var3 = new File(var1);
      this.length = var3.length();
      return this.length;
   }
}
