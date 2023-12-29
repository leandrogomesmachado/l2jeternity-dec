package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class PrivateStoreBuyMsg extends GameServerPacket {
   private final int _objId;
   private String _storeMsg;

   public PrivateStoreBuyMsg(Player player) {
      this._objId = player.getObjectId();
      if (player.getBuyList() != null) {
         this._storeMsg = player.getBuyList().getTitle();
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeS(this._storeMsg);
   }
}
