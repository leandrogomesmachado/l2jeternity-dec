package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerEnergy extends Condition {
   private final boolean _agathionEnergy;

   public ConditionPlayerEnergy(boolean agathionEnergy) {
      this._agathionEnergy = agathionEnergy;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() != null) {
         ItemInstance item = env.getPlayer().getInventory().getPaperdollItem(15);
         if (item != null) {
            if (!item.isEnergyItem()) {
               return true;
            }

            return item.getAgathionEnergy() > 0;
         }
      }

      return !this._agathionEnergy;
   }
}
