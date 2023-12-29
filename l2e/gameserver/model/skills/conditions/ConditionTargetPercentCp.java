package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionTargetPercentCp extends Condition {
   private final double _cp;

   public ConditionTargetPercentCp(int cp) {
      this._cp = (double)cp / 100.0;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getTarget() != null && env.getTarget().getCurrentCpRatio() <= this._cp;
   }
}
