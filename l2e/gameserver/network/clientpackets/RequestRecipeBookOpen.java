package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.RecipeController;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestRecipeBookOpen extends GameClientPacket {
   private boolean _isDwarvenCraft;

   @Override
   protected void readImpl() {
      this._isDwarvenCraft = this.readD() == 0;
      if (Config.DEBUG) {
         _log.info("RequestRecipeBookOpen : " + (this._isDwarvenCraft ? "dwarvenCraft" : "commonCraft"));
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow()) {
            activeChar.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING);
         } else if (activeChar.getActiveRequester() != null) {
            activeChar.sendMessage("You may not alter your recipe book while trading.");
         } else {
            RecipeController.getInstance().requestBookOpen(activeChar, this._isDwarvenCraft);
         }
      }
   }
}
