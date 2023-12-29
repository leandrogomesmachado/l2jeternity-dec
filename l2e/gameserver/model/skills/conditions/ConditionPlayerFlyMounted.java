package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerFlyMounted extends Condition {
   private final boolean _val;

   public ConditionPlayerFlyMounted(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getPlayer() != null ? env.getPlayer().isFlyingMounted() == this._val : true;
   }
}
