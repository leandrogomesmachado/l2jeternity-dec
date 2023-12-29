package l2e.commons.util.crypt;

import java.io.IOException;
import l2e.commons.util.Rnd;

public class LoginCrypt {
   private static final byte[] STATIC_BLOWFISH_KEY = new byte[]{107, 96, -53, 91, -126, -50, -112, -79, -52, 43, 108, 85, 108, 108, 108, 108};
   private static final NewCrypt _STATIC_CRYPT = new NewCrypt(STATIC_BLOWFISH_KEY);
   private NewCrypt _crypt = null;
   private boolean _static = true;

   public void setKey(byte[] key) {
      this._crypt = new NewCrypt(key);
   }

   public boolean decrypt(byte[] raw, int offset, int size) throws IOException {
      if (size % 8 != 0) {
         throw new IOException("size have to be multiple of 8");
      } else if (offset + size > raw.length) {
         throw new IOException("raw array too short for size starting from offset");
      } else {
         this._crypt.decrypt(raw, offset, size);
         return NewCrypt.verifyChecksum(raw, offset, size);
      }
   }

   public int encrypt(byte[] raw, int offset, int size) throws IOException {
      size += 4;
      if (this._static) {
         size += 4;
         size += 8 - size % 8;
         if (offset + size > raw.length) {
            throw new IOException("packet too long");
         }

         NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
         _STATIC_CRYPT.crypt(raw, offset, size);
         this._static = false;
      } else {
         size += 8 - size % 8;
         if (offset + size > raw.length) {
            throw new IOException("packet too long");
         }

         NewCrypt.appendChecksum(raw, offset, size);
         this._crypt.crypt(raw, offset, size);
      }

      return size;
   }
}
