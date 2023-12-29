package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerVehicleMounted extends Condition {
   private final boolean _val;

   public ConditionPlayerVehicleMounted(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return true;
      } else {
         return env.getPlayer().isInVehicle() == this._val;
      }
   }
}
