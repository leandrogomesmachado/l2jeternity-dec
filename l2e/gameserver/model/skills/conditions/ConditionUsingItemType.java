package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.stats.Env;

public final class ConditionUsingItemType extends Condition {
   private final boolean _armor;
   private final int _mask;

   public ConditionUsingItemType(int mask) {
      this._mask = mask;
      this._armor = (this._mask & (ArmorType.MAGIC.mask() | ArmorType.LIGHT.mask() | ArmorType.HEAVY.mask())) != 0;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getCharacter() != null && env.getCharacter().isPlayer()) {
         Inventory inv = env.getPlayer().getInventory();
         if (this._armor) {
            ItemInstance chest = inv.getPaperdollItem(6);
            if (chest == null) {
               return false;
            } else {
               int chestMask = chest.getItem().getItemMask();
               if ((this._mask & chestMask) == 0) {
                  return false;
               } else {
                  int chestBodyPart = chest.getItem().getBodyPart();
                  if (chestBodyPart == 32768) {
                     return true;
                  } else {
                     ItemInstance legs = inv.getPaperdollItem(11);
                     if (legs == null) {
                        return false;
                     } else {
                        int legMask = legs.getItem().getItemMask();
                        return (this._mask & legMask) != 0;
                     }
                  }
               }
            }
         } else {
            return (this._mask & inv.getWearedMask()) != 0;
         }
      } else {
         return false;
      }
   }
}
