package l2e.gameserver.model.holders;

import l2e.gameserver.model.items.instance.ItemInstance;

public class PetItemHolder {
   private final ItemInstance _item;

   public PetItemHolder(ItemInstance item) {
      this._item = item;
   }

   public ItemInstance getItem() {
      return this._item;
   }
}
