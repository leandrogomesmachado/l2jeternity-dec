package l2e.gameserver.model;

import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class TempItem {
   private final int _itemId;
   private int _quantity;
   private final int _referencePrice;
   private final Item _item;

   public TempItem(ItemInstance item, int quantity) {
      this._itemId = item.getId();
      this._quantity = quantity;
      this._item = item.getItem();
      this._referencePrice = item.getReferencePrice();
   }

   public int getQuantity() {
      return this._quantity;
   }

   public void setQuantity(int quantity) {
      this._quantity = quantity;
   }

   public int getReferencePrice() {
      return this._referencePrice;
   }

   public int getId() {
      return this._itemId;
   }

   public Item getItem() {
      return this._item;
   }
}
