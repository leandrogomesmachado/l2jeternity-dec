package l2e.gameserver.network.serverpackets;

import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Player;

public class ExDominionWarStart extends GameServerPacket {
   private final int _objId;
   private final int _terId;
   private final boolean _isDisguised;

   public ExDominionWarStart(Player player) {
      this._objId = player.getObjectId();
      this._terId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
      this._isDisguised = TerritoryWarManager.getInstance().isDisguised(this._objId);
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objId);
      this.writeD(1);
      this.writeD(this._terId);
      this.writeD(this._isDisguised ? 1 : 0);
      this.writeD(this._isDisguised ? this._terId : 0);
   }
}
