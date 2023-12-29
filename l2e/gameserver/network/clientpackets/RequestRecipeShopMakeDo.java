package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.RecipeController;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public final class RequestRecipeShopMakeDo extends GameClientPacket {
   private int _id;
   private int _recipeId;
   protected long _unknow;

   @Override
   protected void readImpl() {
      this._id = this.readD();
      this._recipeId = this.readD();
      this._unknow = this.readQ();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else {
            Player manufacturer = World.getInstance().getPlayer(this._id);
            if (manufacturer != null) {
               if (manufacturer.getReflectionId() == activeChar.getReflectionId() || activeChar.getReflectionId() == -1) {
                  if (activeChar.getPrivateStoreType() != 0) {
                     activeChar.sendMessage("You cannot create items while trading.");
                  } else if (manufacturer.getPrivateStoreType() == 5) {
                     if (!activeChar.isInCraftMode() && !manufacturer.isInCraftMode()) {
                        if (Util.checkIfInRange(150, activeChar, manufacturer, true)) {
                           RecipeController.getInstance().requestManufactureItem(manufacturer, this._recipeId, activeChar);
                        }
                     } else {
                        activeChar.sendMessage("You are currently in Craft Mode.");
                     }
                  }
               }
            }
         }
      }
   }
}
