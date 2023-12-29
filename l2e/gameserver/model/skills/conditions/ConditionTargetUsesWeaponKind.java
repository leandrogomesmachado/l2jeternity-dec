package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetUsesWeaponKind extends Condition {
   private final int _weaponMask;

   public ConditionTargetUsesWeaponKind(int weaponMask) {
      this._weaponMask = weaponMask;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getTarget() == null) {
         return false;
      } else {
         Weapon item = env.getTarget().getActiveWeaponItem();
         if (item == null) {
            return false;
         } else {
            return (item.getItemType().mask() & this._weaponMask) != 0;
         }
      }
   }
}
