package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionTargetPercentHp extends Condition {
   private final double _hp;

   public ConditionTargetPercentHp(int hp) {
      this._hp = (double)hp / 100.0;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getTarget() != null && env.getTarget().getCurrentHpRatio() <= this._hp;
   }
}
