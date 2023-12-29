package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionTargetPercentMp extends Condition {
   private final double _mp;

   public ConditionTargetPercentMp(int mp) {
      this._mp = (double)mp / 100.0;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getTarget() != null && env.getTarget().getCurrentMpRatio() <= this._mp;
   }
}
