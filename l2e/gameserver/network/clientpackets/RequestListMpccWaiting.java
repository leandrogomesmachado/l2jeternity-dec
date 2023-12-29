package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExListMpccWaiting;

public class RequestListMpccWaiting extends GameClientPacket {
   private int _listId;
   private int _locationId;
   private boolean _allLevels;

   @Override
   protected void readImpl() {
      this._listId = this.readD();
      this._locationId = this.readD();
      this._allLevels = this.readD() == 1;
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.sendPacket(new ExListMpccWaiting(player, this._listId, this._locationId, this._allLevels));
      }
   }
}
