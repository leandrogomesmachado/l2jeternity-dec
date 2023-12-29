package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class PrivateStoreSellMsg extends GameServerPacket {
   private final int _objId;
   private String _storeMsg;

   public PrivateStoreSellMsg(Player player) {
      this._objId = player.getObjectId();
      if (player.getSellList() != null || player.isSellingBuffs()) {
         this._storeMsg = player.getSellList().getTitle();
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeS(this._storeMsg);
   }
}
