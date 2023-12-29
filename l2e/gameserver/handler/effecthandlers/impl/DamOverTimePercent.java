package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class DamOverTimePercent extends Effect {
   private final boolean _canKill;

   public DamOverTimePercent(Env env, EffectTemplate template) {
      super(env, template);
      this._canKill = template.hasParameters() && template.getParameters().getBool("canKill", false);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.DMG_OVER_TIME_PERCENT;
   }

   @Override
   public boolean onActionTime() {
      if (this.getEffected().isDead()) {
         return false;
      } else {
         double damage;
         if (this.getSkill().isToggle()) {
            damage = this.getEffected().getCurrentHp() * this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
         } else {
            damage = this.getEffected().getCurrentHp() * this.calc();
         }

         if (damage >= this.getEffected().getCurrentHp() - 1.0) {
            if (this.getSkill().isToggle()) {
               this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
               return false;
            }

            if (!this._canKill) {
               if (this.getEffected().getCurrentHp() <= 1.0) {
                  return this.getSkill().isToggle();
               }

               damage = this.getEffected().getCurrentHp() - 1.0;
            }
         }

         this.getEffected().reduceCurrentHpByDOT(damage, this.getEffector(), this.getSkill());
         this.getEffected().notifyDamageReceived(damage, this.getEffector(), this.getSkill(), false, true);
         return this.getSkill().isToggle();
      }
   }
}
