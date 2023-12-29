package l2e.gameserver.model.actor.templates;

import l2e.gameserver.data.parser.RecipeParser;

public class ManufactureItemTemplate {
   private final int _recipeId;
   private final long _cost;
   private final boolean _isDwarven;

   public ManufactureItemTemplate(int recipeId, long cost) {
      this._recipeId = recipeId;
      this._cost = cost;
      this._isDwarven = RecipeParser.getInstance().getRecipeList(this._recipeId).isDwarvenRecipe();
   }

   public int getRecipeId() {
      return this._recipeId;
   }

   public long getCost() {
      return this._cost;
   }

   public boolean isDwarven() {
      return this._isDwarven;
   }
}
