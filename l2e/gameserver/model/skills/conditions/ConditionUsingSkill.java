package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public final class ConditionUsingSkill extends Condition {
   private final int _skillId;

   public ConditionUsingSkill(int skillId) {
      this._skillId = skillId;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getSkill() == null) {
         return false;
      } else {
         return env.getSkill().getId() == this._skillId;
      }
   }
}
