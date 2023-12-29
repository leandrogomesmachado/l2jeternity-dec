package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.stats.Env;

public final class ConditionSlotItemId extends ConditionInventory {
   private final int _itemId;
   private final int _enchantLevel;

   public ConditionSlotItemId(int slot, int itemId, int enchantLevel) {
      super(slot);
      this._itemId = itemId;
      this._enchantLevel = enchantLevel;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         Inventory inv = env.getPlayer().getInventory();
         ItemInstance item = inv.getPaperdollItem(this._slot);
         if (item == null) {
            return this._itemId == 0;
         } else {
            return item.getId() == this._itemId && item.getEnchantLevel() >= this._enchantLevel;
         }
      }
   }
}
