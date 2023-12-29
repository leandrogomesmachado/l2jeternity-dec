package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class AbortCast extends Effect {
   private final int _chance;

   public AbortCast(Env env, EffectTemplate template) {
      super(env, template);
      this._chance = template.getParameters().getInteger("chance", 100);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() == null || this.getEffected() == this.getEffector()) {
         return false;
      } else if (this.getEffected().isRaid()) {
         return false;
      } else if (!Formulas.calcProbability((double)this._chance, this.getEffector(), this.getEffected(), this.getSkill(), true)) {
         return false;
      } else {
         this.getEffected().abortCast();
         return true;
      }
   }
}
