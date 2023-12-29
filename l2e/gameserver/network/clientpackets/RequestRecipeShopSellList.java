package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.RecipeShopSellList;

public final class RequestRecipeShopSellList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (player != null) {
         if (!player.isActionsDisabled() && player.getTarget() != null && player.getTarget().isPlayer()) {
            player.sendPacket(new RecipeShopSellList(player, player.getTarget().getActingPlayer()));
         } else {
            this.sendActionFailed();
         }
      }
   }
}
