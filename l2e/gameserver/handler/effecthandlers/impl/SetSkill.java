package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class SetSkill extends Effect {
   private final int _skillId;
   private final int _skillLvl;

   public SetSkill(Env env, EffectTemplate template) {
      super(env, template);
      this._skillId = template.getParameters().getInteger("skillId", 0);
      this._skillLvl = template.getParameters().getInteger("skillLvl", 1);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         Skill skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
         if (skill == null) {
            return false;
         } else {
            this.getEffected().getActingPlayer().addSkill(skill, true);
            return true;
         }
      } else {
         return false;
      }
   }
}
