package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public class ExBrGamePoint extends GameServerPacket {
   private final int _objId;
   private long _points;

   public ExBrGamePoint(Player player) {
      this._objId = player.getObjectId();
      if (Config.GAME_POINT_ITEM_ID == -1) {
         this._points = player.getGamePoints();
      } else {
         this._points = player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -100);
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._objId);
      this.writeQ(this._points);
      this.writeD(0);
   }
}
