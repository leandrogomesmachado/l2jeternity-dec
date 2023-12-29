package l2e.loginserver.network.clientpackets;

import l2e.loginserver.Config;
import l2e.loginserver.GameServerManager;
import l2e.loginserver.accounts.Account;
import l2e.loginserver.network.LoginClient;
import l2e.loginserver.network.ProxyServer;
import l2e.loginserver.network.SessionKey;
import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.serverpackets.LoginFail;
import l2e.loginserver.network.serverpackets.PlayOk;

public class RequestServerLogin extends LoginClientPacket {
   private int _loginOkID1;
   private int _loginOkID2;
   private int _serverId;

   @Override
   protected void readImpl() {
      this._loginOkID1 = this.readD();
      this._loginOkID2 = this.readD();
      this._serverId = this.readC();
   }

   @Override
   protected void runImpl() {
      LoginClient client = this.getClient();
      if (!client.isPasswordCorrect()) {
         client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
      } else {
         SessionKey skey = client.getSessionKey();
         if ((skey == null || !skey.checkLoginPair(this._loginOkID1, this._loginOkID2)) && Config.SHOW_LICENCE) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
         } else {
            Account account = client.getAccount();
            GameServer gs = GameServerManager.getInstance().getGameServerById(this._serverId);
            if (gs == null) {
               ProxyServer ps = GameServerManager.getInstance().getProxyServerById(this._serverId);
               if (ps != null) {
                  gs = GameServerManager.getInstance().getGameServerById(ps.getOrigServerId());
               }
            }

            if (gs != null
               && gs.isAuthed()
               && (!gs.isGmOnly() || account.getAccessLevel() >= 5)
               && (gs.getOnline() < gs.getMaxPlayers() || account.getAccessLevel() >= 1)) {
               account.setLastServer(this._serverId);
               account.update();
               client.close(new PlayOk(skey, this._serverId));
            } else {
               client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
            }
         }
      }
   }
}
