package l2e.gameserver.model;

import l2e.gameserver.model.actor.templates.RecipeStatTemplate;
import l2e.gameserver.model.actor.templates.RecipeTemplate;
import l2e.gameserver.model.stats.StatsSet;

public class RecipeList {
   private RecipeTemplate[] _recipes = new RecipeTemplate[0];
   private RecipeStatTemplate[] _statUse = new RecipeStatTemplate[0];
   private RecipeStatTemplate[] _altStatChange = new RecipeStatTemplate[0];
   private final int _id;
   private final int _level;
   private final int _recipeId;
   private final String _recipeName;
   private final int _successRate;
   private final int _itemId;
   private final int _count;
   private int _rareItemId;
   private int _rareCount;
   private int _rarity;
   private final boolean _isDwarvenRecipe;

   public RecipeList(StatsSet set, boolean haveRare) {
      this._id = set.getInteger("id");
      this._level = set.getInteger("craftLevel");
      this._recipeId = set.getInteger("recipeId");
      this._recipeName = set.getString("recipeName");
      this._successRate = set.getInteger("successRate");
      this._itemId = set.getInteger("itemId");
      this._count = set.getInteger("count");
      if (haveRare) {
         this._rareItemId = set.getInteger("rareItemId");
         this._rareCount = set.getInteger("rareCount");
         this._rarity = set.getInteger("rarity");
      }

      this._isDwarvenRecipe = set.getBool("isDwarvenRecipe");
   }

   public void addRecipe(RecipeTemplate recipe) {
      int len = this._recipes.length;
      RecipeTemplate[] tmp = new RecipeTemplate[len + 1];
      System.arraycopy(this._recipes, 0, tmp, 0, len);
      tmp[len] = recipe;
      this._recipes = tmp;
   }

   public void addStatUse(RecipeStatTemplate statUse) {
      int len = this._statUse.length;
      RecipeStatTemplate[] tmp = new RecipeStatTemplate[len + 1];
      System.arraycopy(this._statUse, 0, tmp, 0, len);
      tmp[len] = statUse;
      this._statUse = tmp;
   }

   public void addAltStatChange(RecipeStatTemplate statChange) {
      int len = this._altStatChange.length;
      RecipeStatTemplate[] tmp = new RecipeStatTemplate[len + 1];
      System.arraycopy(this._altStatChange, 0, tmp, 0, len);
      tmp[len] = statChange;
      this._altStatChange = tmp;
   }

   public int getId() {
      return this._id;
   }

   public int getLevel() {
      return this._level;
   }

   public int getRecipeId() {
      return this._recipeId;
   }

   public String getRecipeName() {
      return this._recipeName;
   }

   public int getSuccessRate() {
      return this._successRate;
   }

   public boolean isConsumable() {
      return this._itemId >= 1463 && this._itemId <= 1467
         || this._itemId >= 2509 && this._itemId <= 2514
         || this._itemId >= 3947 && this._itemId <= 3952
         || this._itemId >= 1341 && this._itemId <= 1345;
   }

   public int getItemId() {
      return this._itemId;
   }

   public int getCount() {
      return this._count;
   }

   public int getRareItemId() {
      return this._rareItemId;
   }

   public int getRareCount() {
      return this._rareCount;
   }

   public int getRarity() {
      return this._rarity;
   }

   public boolean isDwarvenRecipe() {
      return this._isDwarvenRecipe;
   }

   public RecipeTemplate[] getRecipes() {
      return this._recipes;
   }

   public RecipeStatTemplate[] getStatUse() {
      return this._statUse;
   }

   public RecipeStatTemplate[] getAltStatChange() {
      return this._altStatChange;
   }
}
