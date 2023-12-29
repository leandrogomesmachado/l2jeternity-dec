package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.communication.SendablePacket;

public class PlayerAuthRequest extends SendablePacket {
   private final String _account;
   private final int _playOkID1;
   private final int _playOkID2;
   private final int _loginOkID1;
   private final int _loginOkID2;

   public PlayerAuthRequest(GameClient client) {
      this._account = client.getLogin();
      this._playOkID1 = client.getSessionId().playOkID1;
      this._playOkID2 = client.getSessionId().playOkID2;
      this._loginOkID1 = client.getSessionId().loginOkID1;
      this._loginOkID2 = client.getSessionId().loginOkID2;
   }

   @Override
   protected void writeImpl() {
      this.writeC(2);
      this.writeS(this._account);
      this.writeD(this._playOkID1);
      this.writeD(this._playOkID2);
      this.writeD(this._loginOkID1);
      this.writeD(this._loginOkID2);
   }
}
