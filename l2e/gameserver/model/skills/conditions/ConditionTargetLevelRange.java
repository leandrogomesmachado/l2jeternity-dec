package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionTargetLevelRange extends Condition {
   private final int[] _levels;

   public ConditionTargetLevelRange(int[] levels) {
      this._levels = levels;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getTarget() == null) {
         return false;
      } else {
         int level = env.getTarget().getLevel();
         return level >= this._levels[0] && level <= this._levels[1];
      }
   }
}
