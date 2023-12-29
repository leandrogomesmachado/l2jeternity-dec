package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.ExArrays;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public final class IgnoreSkills extends Effect {
   private final int[] _skills;

   public IgnoreSkills(Env env, EffectTemplate template) {
      super(env, template);
      int[] skills = null;
      int i = 1;

      while(true) {
         int skillId = template.getParameters().getInteger("skillId" + i, 0);
         int skillLvl = template.getParameters().getInteger("skillLvl" + i, 0);
         if (skillId == 0) {
            if (skills == null) {
               throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Without parameters!");
            } else {
               this._skills = skills;
               return;
            }
         }

         skills = ExArrays.push(skills, SkillsParser.getSkillHashCode(skillId, skillLvl));
         ++i;
      }
   }

   @Override
   public boolean onStart() {
      Creature effected = this.getEffected();

      for(int skillHashCode : this._skills) {
         effected.addInvulAgainst(SkillsParser.getId(skillHashCode), SkillsParser.getLvl(skillHashCode));
      }

      return true;
   }

   @Override
   public void onExit() {
      Creature effected = this.getEffected();

      for(int skillHashCode : this._skills) {
         effected.removeInvulAgainst(SkillsParser.getId(skillHashCode), SkillsParser.getLvl(skillHashCode));
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }
}
