package l2e.loginserver.network.communication.gameserverpackets;

import l2e.loginserver.accounts.SessionManager;
import l2e.loginserver.network.SessionKey;
import l2e.loginserver.network.communication.ReceivablePacket;
import l2e.loginserver.network.communication.loginserverpackets.PlayerAuthResponse;

public class PlayerAuthRequest extends ReceivablePacket {
   private String _account;
   private int _playOkId1;
   private int _playOkId2;
   private int _loginOkId1;
   private int _loginOkId2;

   @Override
   protected void readImpl() {
      this._account = this.readS();
      this._playOkId1 = this.readD();
      this._playOkId2 = this.readD();
      this._loginOkId1 = this.readD();
      this._loginOkId2 = this.readD();
   }

   @Override
   protected void runImpl() {
      SessionKey skey = new SessionKey(this._loginOkId1, this._loginOkId2, this._playOkId1, this._playOkId2);
      SessionManager.Session session = SessionManager.getInstance().closeSession(skey);
      if (session != null && session.getAccount().getLogin().equals(this._account)) {
         this.sendPacket(new PlayerAuthResponse(session, session.getSessionKey().equals(skey), session.getIP()));
      } else {
         this.sendPacket(new PlayerAuthResponse(this._account));
      }
   }
}
