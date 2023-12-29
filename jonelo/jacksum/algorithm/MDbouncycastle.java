package jonelo.jacksum.algorithm;

import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.Digest;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.digests.GOST3411Digest;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.digests.RIPEMD256Digest;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.digests.RIPEMD320Digest;

public class MDbouncycastle extends AbstractChecksum {
   private Digest md = null;
   private boolean virgin = true;
   private byte[] digest = null;

   public MDbouncycastle(String var1) throws NoSuchAlgorithmException {
      this.length = 0L;
      this.filename = null;
      this.separator = " ";
      this.encoding = "hex";
      this.virgin = true;
      if (var1.equalsIgnoreCase("gost")) {
         this.md = new GOST3411Digest();
      } else if (var1.equalsIgnoreCase("ripemd256")) {
         this.md = new RIPEMD256Digest();
      } else {
         if (!var1.equalsIgnoreCase("ripemd320")) {
            throw new NoSuchAlgorithmException(var1 + " is an unknown algorithm.");
         }

         this.md = new RIPEMD320Digest();
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
         this.digest = new byte[this.md.getDigestSize()];
         this.md.doFinal(this.digest, 0);
         this.virgin = false;
      }

      byte[] var1 = new byte[this.digest.length];
      System.arraycopy(this.digest, 0, var1, 0, this.digest.length);
      return var1;
   }
}
