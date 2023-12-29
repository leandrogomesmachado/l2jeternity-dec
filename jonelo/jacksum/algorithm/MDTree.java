package jonelo.jacksum.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.adapt.com.bitzi.util.TigerTree;

public class MDTree extends AbstractChecksum {
   private MessageDigest md = null;
   private boolean virgin = true;
   private byte[] digest = null;

   public MDTree(String var1) throws NoSuchAlgorithmException {
      this.length = 0L;
      this.filename = null;
      this.separator = " ";
      this.encoding = "base32";
      this.virgin = true;
      this.md = new TigerTree(var1);
   }

   public void reset() {
      this.md.reset();
      this.length = 0L;
      this.virgin = true;
   }

   public void update(byte[] var1, int var2, int var3) {
      this.md.update(var1, var2, var3);
      this.length += (long)var3;
   }

   public void update(byte var1) {
      this.md.update(var1);
      ++this.length;
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }

   public String toString() {
      return this.getFormattedValue() + this.separator + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "") + this.getFilename();
   }

   public byte[] getByteArray() {
      if (this.virgin) {
         this.digest = this.md.digest();
         this.virgin = false;
      }

      byte[] var1 = new byte[this.digest.length];
      System.arraycopy(this.digest, 0, var1, 0, this.digest.length);
      return var1;
   }
}
