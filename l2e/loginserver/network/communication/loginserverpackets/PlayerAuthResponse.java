package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.accounts.Account;
import l2e.loginserver.accounts.SessionManager;
import l2e.loginserver.network.SessionKey;
import l2e.loginserver.network.communication.SendablePacket;

public class PlayerAuthResponse extends SendablePacket {
   private final String _login;
   private final boolean _authed;
   private int _playOkID1;
   private int _playOkID2;
   private int _loginOkID1;
   private int _loginOkID2;
   private String _ip;

   public PlayerAuthResponse(SessionManager.Session session, boolean authed, String ip) {
      Account account = session.getAccount();
      this._login = account.getLogin();
      this._authed = authed;
      if (authed) {
         SessionKey skey = session.getSessionKey();
         this._playOkID1 = skey._playOkID1;
         this._playOkID2 = skey._playOkID2;
         this._loginOkID1 = skey._loginOkID1;
         this._loginOkID2 = skey._loginOkID2;
         this._ip = ip;
      }
   }

   public PlayerAuthResponse(String account) {
      this._login = account;
      this._authed = false;
   }

   @Override
   protected void writeImpl() {
      this.writeC(2);
      this.writeS(this._login);
      this.writeC(this._authed ? 1 : 0);
      if (this._authed) {
         this.writeD(this._playOkID1);
         this.writeD(this._playOkID2);
         this.writeD(this._loginOkID1);
         this.writeD(this._loginOkID2);
         this.writeS(this._ip);
      }
   }
}
