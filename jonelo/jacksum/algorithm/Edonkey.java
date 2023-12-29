package jonelo.jacksum.algorithm;

import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.adapt.gnu.crypto.hash.HashFactory;
import jonelo.jacksum.adapt.gnu.crypto.hash.IMessageDigest;

public class Edonkey extends AbstractChecksum {
   private static final String AUX_ALGORITHM = "md4";
   private IMessageDigest md4 = null;
   private IMessageDigest md4final = null;
   private boolean virgin = true;
   private static final int BLOCKSIZE = 9728000;
   private byte[] edonkeyHash = new byte[16];
   private byte[] digest = null;

   public Edonkey() throws NoSuchAlgorithmException {
      this.separator = " ";
      this.encoding = "hex";
      this.md4 = HashFactory.getInstance("md4");
      if (this.md4 == null) {
         throw new NoSuchAlgorithmException("md4 is an unknown algorithm.");
      } else {
         this.md4final = HashFactory.getInstance("md4");
         this.virgin = true;
      }
   }

   public void reset() {
      this.md4.reset();
      this.md4final.reset();
      this.length = 0L;
      this.virgin = true;
   }

   public void update(byte var1) {
      this.md4.update(var1);
      ++this.length;
      if (this.length % 9728000L == 0L) {
         System.arraycopy(this.md4.digest(), 0, this.edonkeyHash, 0, 16);
         this.md4final.update(this.edonkeyHash, 0, 16);
         this.md4.reset();
      }
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }

   public void update(byte[] var1, int var2, int var3) {
      int var4 = var3 - var2;
      int var5 = (int)(this.length % 9728000L);
      int var6 = 9728000 - var5;
      if (var6 > var4) {
         this.md4.update(var1, var2, var3);
         this.length += (long)var3;
      } else if (var6 == var4) {
         this.md4.update(var1, var2, var3);
         this.length += (long)var3;
         System.arraycopy(this.md4.digest(), 0, this.edonkeyHash, 0, 16);
         this.md4final.update(this.edonkeyHash, 0, 16);
         this.md4.reset();
      } else if (var6 < var4) {
         this.md4.update(var1, var2, var6);
         this.length += (long)var6;
         System.arraycopy(this.md4.digest(), 0, this.edonkeyHash, 0, 16);
         this.md4final.update(this.edonkeyHash, 0, 16);
         this.md4.reset();
         this.md4.update(var1, var2 + var6, var4 - var6);
         this.length += (long)(var4 - var6);
      }
   }

   public String toString() {
      return this.getFormattedValue() + this.separator + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "") + this.getFilename();
   }

   public byte[] getByteArray() {
      if (this.virgin) {
         if (this.length < 9728000L) {
            System.arraycopy(this.md4.digest(), 0, this.edonkeyHash, 0, 16);
         } else {
            IMessageDigest var1 = (IMessageDigest)this.md4final.clone();
            var1.update(this.md4.digest(), 0, 16);
            System.arraycopy(var1.digest(), 0, this.edonkeyHash, 0, 16);
         }

         this.virgin = false;
         this.digest = this.edonkeyHash;
      }

      byte[] var2 = new byte[this.digest.length];
      System.arraycopy(this.digest, 0, var2, 0, this.digest.length);
      return var2;
   }
}
