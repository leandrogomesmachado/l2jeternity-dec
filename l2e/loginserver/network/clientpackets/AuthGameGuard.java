package l2e.loginserver.network.clientpackets;

import l2e.loginserver.network.LoginClient;
import l2e.loginserver.network.serverpackets.GGAuth;
import l2e.loginserver.network.serverpackets.LoginFail;

public class AuthGameGuard extends LoginClientPacket {
   private int _sessionId;

   @Override
   protected void readImpl() {
      this._sessionId = this.readD();
   }

   @Override
   protected void runImpl() {
      LoginClient client = this.getClient();
      if (this._sessionId != 0 && this._sessionId != client.getSessionId()) {
         client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      } else {
         client.setState(LoginClient.LoginClientState.AUTHED_GG);
         client.sendPacket(new GGAuth(client.getSessionId()));
      }
   }
}
