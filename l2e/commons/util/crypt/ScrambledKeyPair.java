package l2e.commons.util.crypt;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

public class ScrambledKeyPair {
   private static Logger _log = Logger.getLogger(ScrambledKeyPair.class.getName());
   public KeyPair _pair;
   public byte[] _scrambledModulus;

   public ScrambledKeyPair(KeyPair pPair) {
      this._pair = pPair;
      this._scrambledModulus = this.scrambleModulus(((RSAPublicKey)this._pair.getPublic()).getModulus());
   }

   private byte[] scrambleModulus(BigInteger modulus) {
      byte[] scrambledMod = modulus.toByteArray();
      if (scrambledMod.length == 129 && scrambledMod[0] == 0) {
         byte[] temp = new byte[128];
         System.arraycopy(scrambledMod, 1, temp, 0, 128);
         scrambledMod = temp;
      }

      for(int i = 0; i < 4; ++i) {
         byte temp = scrambledMod[0 + i];
         scrambledMod[0 + i] = scrambledMod[77 + i];
         scrambledMod[77 + i] = temp;
      }

      for(int i = 0; i < 64; ++i) {
         scrambledMod[i] ^= scrambledMod[64 + i];
      }

      for(int i = 0; i < 4; ++i) {
         scrambledMod[13 + i] ^= scrambledMod[52 + i];
      }

      for(int i = 0; i < 64; ++i) {
         scrambledMod[64 + i] ^= scrambledMod[i];
      }

      _log.fine("Modulus was scrambled");
      return scrambledMod;
   }
}
