package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.stats.Env;

public final class ConditionSlotItemType extends ConditionInventory {
   private final int _mask;

   public ConditionSlotItemType(int slot, int mask) {
      super(slot);
      this._mask = mask;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         Inventory inv = env.getPlayer().getInventory();
         ItemInstance item = inv.getPaperdollItem(this._slot);
         if (item == null) {
            return false;
         } else {
            return (item.getItem().getItemMask() & this._mask) != 0;
         }
      }
   }
}
