package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import java.util.List;

public class MysqlNativePasswordPlugin implements AuthenticationPlugin<NativePacketPayload> {
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
      return "mysql_native_password";
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
            Security.scramble411(pwd, fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_TERM), this.protocol.getPasswordCharacterEncoding())
         );
      } else {
         bresp = new NativePacketPayload(new byte[0]);
      }

      toServer.add(bresp);
      return true;
   }
}
