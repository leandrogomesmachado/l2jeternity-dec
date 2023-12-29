package l2e.loginserver.network.clientpackets;

import l2e.loginserver.network.LoginClient;
import l2e.loginserver.network.SessionKey;
import l2e.loginserver.network.serverpackets.LoginFail;
import l2e.loginserver.network.serverpackets.ServerList;

public class RequestServerList extends LoginClientPacket {
   private int _loginOkID1;
   private int _loginOkID2;
   protected int _unk;

   @Override
   protected void readImpl() {
      this._loginOkID1 = this.readD();
      this._loginOkID2 = this.readD();
      this._unk = this.readC();
   }

   @Override
   protected void runImpl() {
      LoginClient client = this.getClient();
      SessionKey skey = client.getSessionKey();
      if (skey != null && skey.checkLoginPair(this._loginOkID1, this._loginOkID2)) {
         client.sendPacket(new ServerList(client.getAccount()));
      } else {
         client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      }
   }
}
