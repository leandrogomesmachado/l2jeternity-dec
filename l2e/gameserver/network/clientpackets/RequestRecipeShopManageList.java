package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.RecipeShopManageList;

public final class RequestRecipeShopManageList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isAlikeDead()) {
            this.sendActionFailed();
         } else {
            if (player.getPrivateStoreType() != 0) {
               player.setPrivateStoreType(0);
               player.broadcastCharInfo();
               if (player.isSitting()) {
                  player.standUp();
               }
            }

            player.sendPacket(new RecipeShopManageList(player, true));
         }
      }
   }
}
