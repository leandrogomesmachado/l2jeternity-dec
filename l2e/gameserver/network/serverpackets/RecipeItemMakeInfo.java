package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Player;

public class RecipeItemMakeInfo extends GameServerPacket {
   private final int _id;
   private final Player _activeChar;
   private final boolean _success;

   public RecipeItemMakeInfo(int id, Player player, boolean success) {
      this._id = id;
      this._activeChar = player;
      this._success = success;
   }

   public RecipeItemMakeInfo(int id, Player player) {
      this._id = id;
      this._activeChar = player;
      this._success = true;
   }

   @Override
   protected final void writeImpl() {
      RecipeList recipe = RecipeParser.getInstance().getRecipeList(this._id);
      if (recipe != null) {
         this.writeD(this._id);
         this.writeD(recipe.isDwarvenRecipe() ? 0 : 1);
         this.writeD((int)this._activeChar.getCurrentMp());
         this.writeD((int)this._activeChar.getMaxMp());
         this.writeD(this._success ? 1 : 0);
      } else {
         _log.info("Character: " + this.getClient().getActiveChar() + ": Requested unexisting recipe with id = " + this._id);
      }
   }
}
