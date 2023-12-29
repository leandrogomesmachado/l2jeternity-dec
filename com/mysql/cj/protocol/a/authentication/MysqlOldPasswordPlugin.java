package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.StringUtils;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MysqlOldPasswordPlugin implements AuthenticationPlugin<NativePacketPayload> {
   private Protocol<NativePacketPayload> protocol;
   private String password = null;

   @Override
   public void init(Protocol<NativePacketPayload> prot) {
      this.protocol = prot;
   }

   @Override
   public void destroy() {
      this.password = null;
   }

   @Override
   public String getProtocolPluginName() {
      return "mysql_old_password";
   }

   @Override
   public boolean requiresConfidentiality() {
      return false;
   }

   @Override
   public boolean isReusable() {
      return true;
   }

   @Override
   public void setAuthenticationParameters(String user, String password) {
      this.password = password;
   }

   public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
      toServer.clear();
      NativePacketPayload bresp = null;
      String pwd = this.password;
      if (fromServer != null && pwd != null && pwd.length() != 0) {
         bresp = new NativePacketPayload(
            StringUtils.getBytes(
               newCrypt(
                  pwd,
                  fromServer.readString(NativeConstants.StringSelfDataType.STRING_TERM, null).substring(0, 8),
                  this.protocol.getPasswordCharacterEncoding()
               )
            )
         );
         bresp.setPosition(bresp.getPayloadLength());
         bresp.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
         bresp.setPosition(0);
      } else {
         bresp = new NativePacketPayload(new byte[0]);
      }

      toServer.add(bresp);
      return true;
   }

   private static String newCrypt(String password, String seed, String encoding) {
      if (password != null && password.length() != 0) {
         long[] pw = newHash(seed.getBytes());
         long[] msg = hashPre41Password(password, encoding);
         long max = 1073741823L;
         long seed1 = (pw[0] ^ msg[0]) % max;
         long seed2 = (pw[1] ^ msg[1]) % max;
         char[] chars = new char[seed.length()];

         for(int i = 0; i < seed.length(); ++i) {
            seed1 = (seed1 * 3L + seed2) % max;
            seed2 = (seed1 + seed2 + 33L) % max;
            double d = (double)seed1 / (double)max;
            byte b = (byte)((int)Math.floor(d * 31.0 + 64.0));
            chars[i] = (char)b;
         }

         seed1 = (seed1 * 3L + seed2) % max;
         seed2 = (seed1 + seed2 + 33L) % max;
         double d = (double)seed1 / (double)max;
         byte b = (byte)((int)Math.floor(d * 31.0));

         for(int i = 0; i < seed.length(); ++i) {
            chars[i] ^= (char)b;
         }

         return new String(chars);
      } else {
         return password;
      }
   }

   private static long[] hashPre41Password(String password, String encoding) {
      try {
         return newHash(password.replaceAll("\\s", "").getBytes(encoding));
      } catch (UnsupportedEncodingException var3) {
         return new long[0];
      }
   }

   private static long[] newHash(byte[] password) {
      long nr = 1345345333L;
      long add = 7L;
      long nr2 = 305419889L;

      for(byte b : password) {
         long tmp = (long)(255 & b);
         nr ^= ((nr & 63L) + add) * tmp + (nr << 8);
         nr2 += nr2 << 8 ^ nr;
         add += tmp;
      }

      return new long[]{nr & 2147483647L, nr2 & 2147483647L};
   }
}
