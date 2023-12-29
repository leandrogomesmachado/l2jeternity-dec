package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ChairSit extends GameServerPacket {
   private final Player _activeChar;
   private final int _staticObjectId;

   public ChairSit(Player player, int staticObjectId) {
      this._activeChar = player;
      this._staticObjectId = staticObjectId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._staticObjectId);
   }
}
