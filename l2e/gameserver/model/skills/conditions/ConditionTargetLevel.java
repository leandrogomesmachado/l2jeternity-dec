package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionTargetLevel extends Condition {
   private final int _level;

   public ConditionTargetLevel(int level) {
      this._level = level;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getTarget() == null) {
         return false;
      } else {
         return env.getTarget().getLevel() >= this._level;
      }
   }
}
