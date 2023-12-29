package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class ExSpawnEmitter extends GameServerPacket {
   private final int _playerObjectId;
   private final int _npcObjectId;

   public ExSpawnEmitter(int playerObjectId, int npcObjectId) {
      this._playerObjectId = playerObjectId;
      this._npcObjectId = npcObjectId;
   }

   public ExSpawnEmitter(Player player, Npc npc) {
      this(player.getObjectId(), npc.getObjectId());
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._npcObjectId);
      this.writeD(this._playerObjectId);
      this.writeD(0);
   }
}
