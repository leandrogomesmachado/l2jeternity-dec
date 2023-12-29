package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestPrivateStoreSellManageList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isAlikeDead() || player.isInOlympiadMode()) {
            this.sendActionFailed();
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
