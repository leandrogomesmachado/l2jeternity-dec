package l2e.gameserver.network.communication.gameserverpackets;

import l2e.gameserver.network.communication.SendablePacket;

public class PlayerInGame extends SendablePacket {
   private final String _account;

   public PlayerInGame(String account) {
      this._account = account;
   }

   @Override
   protected void writeImpl() {
      this.writeC(3);
      this.writeS(this._account);
   }
}
