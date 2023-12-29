package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerActiveSkillId extends Condition {
   private final int _skillId;
   private final int _skillLevel;

   public ConditionPlayerActiveSkillId(int skillId) {
      this._skillId = skillId;
      this._skillLevel = -1;
   }

   public ConditionPlayerActiveSkillId(int skillId, int skillLevel) {
      this._skillId = skillId;
      this._skillLevel = skillLevel;
   }

   @Override
   public boolean testImpl(Env env) {
      for(Skill sk : env.getCharacter().getAllSkills()) {
         if (sk != null && sk.getId() == this._skillId && (this._skillLevel == -1 || this._skillLevel <= sk.getLevel())) {
            return true;
         }
      }

      return false;
   }
}
