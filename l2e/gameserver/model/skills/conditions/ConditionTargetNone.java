package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionTargetNone extends Condition {
   @Override
   public boolean testImpl(Env env) {
      return env.getTarget() == null;
   }
}
