package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestPrivateStoreBuyManageList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.tryOpenPrivateBuyStore();
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
