package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.ExRegenMax;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class HealOverTime extends Effect {
   public HealOverTime(Env env, EffectTemplate template) {
      super(env, template);
   }

   public HealOverTime(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.HEAL_OVER_TIME;
   }

   @Override
   public boolean onStart() {
      Creature target = this.getEffected();
      if (target != null && !target.isDead() && !target.isHealBlocked() && !target.isInvul()) {
         if (this.getEffected().isPlayer() && this.getEffectTemplate().getTotalTickCount() > 0) {
            this.getEffected()
               .sendPacket(
                  new ExRegenMax(
                     this.calc(),
                     this.getEffectTemplate().getTotalTickCount() * this.getEffectTemplate().getAbnormalTime(),
                     this.getEffectTemplate().getTotalTickCount()
                  )
               );
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onActionTime() {
      if (!this.getEffected().isDead() && !this.getEffected().isDoor()) {
         double hp = this.getEffected().getCurrentHp();
         double maxhp = (double)this.getEffected().getMaxRecoverableHp();
         if (hp >= maxhp) {
            return false;
         } else {
            if (this.getSkill().isToggle()) {
               hp += this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
            } else {
               hp += this.calc();
            }

            hp = Math.min(hp, maxhp);
            this.getEffected().setCurrentHp(hp);
            StatusUpdate suhp = new StatusUpdate(this.getEffected());
            suhp.addAttribute(9, (int)hp);
            this.getEffected().sendPacket(suhp);
            return this.getSkill().isToggle();
         }
      } else {
         return false;
      }
   }
}
