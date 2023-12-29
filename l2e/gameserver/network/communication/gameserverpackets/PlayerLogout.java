package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class PlayerLogout extends SendablePacket {
   private final String _account;

   public PlayerLogout(String account) {
      this._account = account;
   }

   @Override
   protected void writeImpl() {
      this.writeC(4);
      this.writeS(this._account);
   }
}
