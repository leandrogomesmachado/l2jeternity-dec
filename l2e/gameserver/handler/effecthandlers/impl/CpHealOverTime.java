package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class CpHealOverTime extends Effect {
   public CpHealOverTime(Env env, EffectTemplate template) {
      super(env, template);
   }

   public CpHealOverTime(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CPHEAL_OVER_TIME;
   }

   @Override
   public boolean onActionTime() {
      Creature target = this.getEffected();
      if (target == null) {
         return false;
      } else if (!target.isHealBlocked() && !target.isDead()) {
         double cp = this.getEffected().getCurrentCp();
         double maxcp = (double)this.getEffected().getMaxRecoverableCp();
         if (cp >= maxcp) {
            return false;
         } else {
            if (this.getSkill().isToggle()) {
               cp += this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
            } else {
               cp += this.calc();
            }

            cp = Math.min(cp, maxcp);
            this.getEffected().setCurrentCp(cp);
            return this.getSkill().isToggle();
         }
      } else {
         return false;
      }
   }
}
