package l2e.gameserver.network.clientpackets;

import l2e.gameserver.RecipeController;
import l2e.gameserver.model.actor.Player;

public final class RequestRecipeItemMakeSelf extends GameClientPacket {
   private int _id;

   @Override
   protected void readImpl() {
      this._id = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else if (activeChar.getPrivateStoreType() != 0) {
            activeChar.sendMessage("You cannot create items while trading.");
         } else if (activeChar.isInCraftMode()) {
            activeChar.sendMessage("You are currently in Craft Mode.");
         } else {
            RecipeController.getInstance().requestMakeItem(activeChar, this._id);
         }
      }
   }
}
