package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.network.communication.SendablePacket;

public class KickPlayer extends SendablePacket {
   private final String _account;

   public KickPlayer(String login) {
      this._account = login;
   }

   @Override
   protected void writeImpl() {
      this.writeC(3);
      this.writeS(this._account);
   }
}
