package l2e.gameserver.model.holders;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.skills.Skill;

public class SkillHolder {
   private final int _skillId;
   private final int _skillLvl;

   public SkillHolder(int skillId, int skillLvl) {
      this._skillId = skillId;
      this._skillLvl = skillLvl;
   }

   public SkillHolder(Skill skill) {
      this._skillId = skill.getId();
      this._skillLvl = skill.getLevel();
   }

   public final int getId() {
      return this._skillId;
   }

   public final int getLvl() {
      return this._skillLvl;
   }

   public final Skill getSkill() {
      return SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
   }

   @Override
   public String toString() {
      return "[SkillId: " + this._skillId + " Level: " + this._skillLvl + "]";
   }
}
