package l2e.gameserver.model.skills.options;

import l2e.gameserver.model.holders.SkillHolder;

public class OptionsSkillHolder extends SkillHolder {
   private final OptionsSkillType _type;
   private final double _chance;

   public OptionsSkillHolder(int skillId, int skillLvl, double chance, OptionsSkillType type) {
      super(skillId, skillLvl);
      this._chance = chance;
      this._type = type;
   }

   public OptionsSkillType getSkillType() {
      return this._type;
   }

   public double getChance() {
      return this._chance;
   }
}
