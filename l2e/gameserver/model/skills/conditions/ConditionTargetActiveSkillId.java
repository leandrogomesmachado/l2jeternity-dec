package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetActiveSkillId extends Condition {
   private final int _skillId;
   private final int _skillLevel;

   public ConditionTargetActiveSkillId(int skillId) {
      this._skillId = skillId;
      this._skillLevel = -1;
   }

   public ConditionTargetActiveSkillId(int skillId, int skillLevel) {
      this._skillId = skillId;
      this._skillLevel = skillLevel;
   }

   @Override
   public boolean testImpl(Env env) {
      for(Skill sk : env.getTarget().getAllSkills()) {
         if (sk != null && sk.getId() == this._skillId && (this._skillLevel == -1 || this._skillLevel <= sk.getLevel())) {
            return true;
         }
      }

      return false;
   }
}
