package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerCharges extends Condition {
   private final int _charges;

   public ConditionPlayerCharges(int charges) {
      this._charges = charges;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getPlayer() != null && env.getPlayer().getCharges() >= this._charges;
   }
}
