package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.stats.Env;

public class ConditionChangeWeapon extends Condition {
   private final boolean _required;

   public ConditionChangeWeapon(boolean required) {
      this._required = required;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         if (this._required) {
            Weapon weaponItem = env.getPlayer().getActiveWeaponItem();
            if (weaponItem == null) {
               return false;
            }

            if (weaponItem.getChangeWeaponId() == 0) {
               return false;
            }

            if (env.getPlayer().isEnchanting()) {
               return false;
            }
         }

         return true;
      }
   }
}
