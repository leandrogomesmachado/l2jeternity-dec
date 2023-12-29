package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.RecipeBookItemList;

public final class RequestRecipeItemDelete extends GameClientPacket {
   private int _recipeID;

   @Override
   protected void readImpl() {
      this._recipeID = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         RecipeList rp = RecipeParser.getInstance().getRecipeList(this._recipeID);
         if (rp != null) {
            activeChar.unregisterRecipeList(this._recipeID);
            RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), (int)activeChar.getMaxMp());
            if (rp.isDwarvenRecipe()) {
               response.addRecipes(activeChar.getDwarvenRecipeBook());
            } else {
               response.addRecipes(activeChar.getCommonRecipeBook());
            }

            activeChar.sendPacket(response);
         }
      }
   }
}
