package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.RecipeShopItemInfo;

public final class RequestRecipeShopMakeInfo extends GameClientPacket {
   private int _playerObjectId;
   private int _recipeId;

   @Override
   protected void readImpl() {
      this._playerObjectId = this.readD();
      this._recipeId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isActionsDisabled()) {
            player.sendActionFailed();
         } else {
            Player shop = World.getInstance().getPlayer(this._playerObjectId);
            if (shop != null && shop.getPrivateStoreType() == 5) {
               player.sendPacket(new RecipeShopItemInfo(shop, this._recipeId));
            }
         }
      }
   }
}
