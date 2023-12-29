package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class CallSkills extends Effect {
   private final String _skills;
   private final List<Skill> _skillList;

   public CallSkills(Env env, EffectTemplate template) {
      super(env, template);
      this._skills = template.getParameters().getString("skills", null);
      if (this._skills != null && !this._skills.isEmpty()) {
         this._skillList = new ArrayList<>();

         for(String ngtStack : this._skills.split(";")) {
            String[] ngt = ngtStack.split(",");
            Skill skill = SkillsParser.getInstance().getInfo(Integer.parseInt(ngt[0]), Integer.parseInt(ngt[1]));
            if (skill != null) {
               this._skillList.add(skill);
            }
         }
      } else {
         this._skillList = null;
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && !this.getEffected().isDead() && this._skillList != null && !this._skillList.isEmpty()) {
         for(Skill skill : this._skillList) {
            if (skill != null) {
               skill.getEffects(this.getEffected(), this.getEffected(), false);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onActionTime() {
      return false;
   }
}
