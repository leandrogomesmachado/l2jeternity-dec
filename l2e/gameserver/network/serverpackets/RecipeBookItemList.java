package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.RecipeList;

public class RecipeBookItemList extends GameServerPacket {
   private RecipeList[] _recipes;
   private final boolean _isDwarvenCraft;
   private final int _maxMp;

   public RecipeBookItemList(boolean isDwarvenCraft, int maxMp) {
      this._isDwarvenCraft = isDwarvenCraft;
      this._maxMp = maxMp;
   }

   public void addRecipes(RecipeList[] recipeBook) {
      this._recipes = recipeBook;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._isDwarvenCraft ? 0 : 1);
      this.writeD(this._maxMp);
      if (this._recipes == null) {
         this.writeD(0);
      } else {
         this.writeD(this._recipes.length);

         for(int i = 0; i < this._recipes.length; ++i) {
            this.writeD(this._recipes[i].getId());
            this.writeD(i + 1);
         }
      }
   }
}
