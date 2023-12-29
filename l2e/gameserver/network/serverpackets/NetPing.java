package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class NetPing extends GameServerPacket {
   private final int _clientId;

   public NetPing(Player cha) {
      this._clientId = cha.getObjectId();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._clientId);
   }
}
