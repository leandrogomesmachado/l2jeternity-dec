package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ManaHealOverTime extends Effect {
   public ManaHealOverTime(Env env, EffectTemplate template) {
      super(env, template);
   }

   public ManaHealOverTime(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.MANA_HEAL_OVER_TIME;
   }

   @Override
   public boolean onActionTime() {
      Creature target = this.getEffected();
      if (target == null) {
         return false;
      } else if (!target.isHealBlocked() && !target.isDead()) {
         double mp = this.getEffected().getCurrentMp();
         double maxmp = (double)this.getEffected().getMaxRecoverableMp();
         if (mp >= maxmp) {
            return false;
         } else {
            if (this.getSkill().isToggle()) {
               mp += this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
            } else {
               mp += this.calc();
            }

            mp = Math.min(mp, maxmp);
            this.getEffected().setCurrentMp(mp);
            return this.getSkill().isToggle();
         }
      } else {
         return false;
      }
   }
}
