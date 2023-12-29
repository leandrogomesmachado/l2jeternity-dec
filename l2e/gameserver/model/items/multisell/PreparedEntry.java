package l2e.gameserver.model.items.multisell;

import java.util.ArrayList;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PreparedEntry extends Entry {
   private long _taxAmount = 0L;

   public PreparedEntry(Entry template, ItemInstance item, boolean applyTaxes, boolean maintainEnchantment, double taxRate) {
      this._entryId = template.getEntryId() * 100000;
      if (maintainEnchantment && item != null) {
         this._entryId += item.getEnchantLevel();
      }

      ItemInfo info = null;
      long adenaAmount = 0L;
      this._ingredients = new ArrayList<>(template.getIngredients().size());

      for(Ingredient ing : template.getIngredients()) {
         if (ing.getId() == 57) {
            if (ing.isTaxIngredient()) {
               if (applyTaxes) {
                  this._taxAmount += Math.round((double)ing.getCount() * taxRate);
               }
            } else {
               adenaAmount += ing.getCount();
            }
         } else if (maintainEnchantment && item != null && ing.isArmorOrWeapon()) {
            info = new ItemInfo(item);
            Ingredient newIngredient = ing.getCopy();
            newIngredient.setItemInfo(info);
            this._ingredients.add(newIngredient);
         } else {
            Ingredient newIngredient = ing.getCopy();
            this._ingredients.add(newIngredient);
         }
      }

      adenaAmount += this._taxAmount;
      if (adenaAmount > 0L) {
         this._ingredients.add(new Ingredient(57, adenaAmount, 0, -1, null, null, false, false));
      }

      this._products = new ArrayList<>(template.getProducts().size());

      for(Ingredient ing : template.getProducts()) {
         if (!ing.isStackable()) {
            this._stackable = false;
         }

         Ingredient newProduct = ing.getCopy();
         if (maintainEnchantment && ing.isArmorOrWeapon()) {
            newProduct.setItemInfo(info);
         }

         this._products.add(newProduct);
      }
   }

   @Override
   public final long getTaxAmount() {
      return this._taxAmount;
   }
}
