package l2e.gameserver.model.actor.templates;

public class RecipeTemplate {
   private int _itemId;
   private int _quantity;

   public RecipeTemplate(int itemId, int quantity) {
      this._itemId = itemId;
      this._quantity = quantity;
   }

   public int getId() {
      return this._itemId;
   }

   public int getQuantity() {
      return this._quantity;
   }
}
