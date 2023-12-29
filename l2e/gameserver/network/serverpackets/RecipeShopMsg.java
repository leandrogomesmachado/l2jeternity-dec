package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class RecipeShopMsg extends GameServerPacket {
   private final Player _activeChar;

   public RecipeShopMsg(Player player) {
      this._activeChar = player;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeS(this._activeChar.getStoreName());
   }
}
