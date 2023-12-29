package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionWithSkill extends Condition {
   private final boolean _skill;

   public ConditionWithSkill(boolean skill) {
      this._skill = skill;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getSkill() != null == this._skill;
   }
}
