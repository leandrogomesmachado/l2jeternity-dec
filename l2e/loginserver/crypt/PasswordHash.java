package l2e.loginserver.crypt;

import java.util.logging.Level;
import java.util.logging.Logger;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

public class PasswordHash {
   private static final Logger _log = Logger.getLogger(PasswordHash.class.getName());
   private final String _name;

   public PasswordHash(String name) {
      this._name = name;
   }

   public boolean compare(String password, String expected) {
      try {
         return this.encrypt(password).equals(expected);
      } catch (Exception var4) {
         _log.log(Level.WARNING, this._name + ": encryption error!", (Throwable)var4);
         return false;
      }
   }

   public String encrypt(String password) throws Exception {
      AbstractChecksum checksum = JacksumAPI.getChecksumInstance(this._name);
      checksum.setEncoding("BASE64");
      checksum.update(password.getBytes());
      return checksum.format("#CHECKSUM");
   }
}
