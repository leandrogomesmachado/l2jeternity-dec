package l2e.gameserver.model.items.multisell;

import java.util.ArrayList;
import java.util.List;

public class Entry {
   protected int _entryId;
   protected boolean _stackable = true;
   protected List<Ingredient> _products;
   protected List<Ingredient> _ingredients;

   public Entry(int entryId) {
      this._entryId = entryId;
      this._products = new ArrayList<>();
      this._ingredients = new ArrayList<>();
   }

   protected Entry() {
   }

   public final void setEntryId(int id) {
      this._entryId = id;
   }

   public final int getEntryId() {
      return this._entryId;
   }

   public final void addProduct(Ingredient product) {
      this._products.add(product);
      if (!product.isStackable()) {
         this._stackable = false;
      }
   }

   public final List<Ingredient> getProducts() {
      return this._products;
   }

   public final void addIngredient(Ingredient ingredient) {
      this._ingredients.add(ingredient);
   }

   public final List<Ingredient> getIngredients() {
      return this._ingredients;
   }

   public final boolean isStackable() {
      return this._stackable;
   }

   public long getTaxAmount() {
      return 0L;
   }
}
