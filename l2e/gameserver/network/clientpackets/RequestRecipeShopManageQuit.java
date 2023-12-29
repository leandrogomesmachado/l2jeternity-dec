package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestRecipeShopManageQuit extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.setPrivateStoreType(0);
         player.broadcastCharInfo();
         player.standUp();
      }
   }
}
