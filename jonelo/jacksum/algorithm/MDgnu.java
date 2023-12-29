package jonelo.jacksum.algorithm;

import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.adapt.gnu.crypto.hash.HashFactory;
import jonelo.jacksum.adapt.gnu.crypto.hash.IMessageDigest;

public class MDgnu extends AbstractChecksum {
   private IMessageDigest md = null;
   private boolean virgin = true;
   private byte[] digest = null;

   public MDgnu(String var1) throws NoSuchAlgorithmException {
      this.length = 0L;
      this.filename = null;
      this.separator = " ";
      this.encoding = "hex";
      this.md = HashFactory.getInstance(var1);
      if (this.md == null) {
         throw new NoSuchAlgorithmException(var1 + " is an unknown algorithm.");
      } else {
         this.virgin = true;
      }
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
