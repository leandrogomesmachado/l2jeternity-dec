package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class CpDamPercent extends Effect {
   public CpDamPercent(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CPDAMPERCENT;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer()) {
         if (this.getEffected().isPlayer() && this.getEffected().isFakeDeathNow()) {
            this.getEffected().stopFakeDeath(true);
         }

         int damage = (int)(this.getEffected().getCurrentCp() * this.calc() / 100.0);
         if (!this.getEffected().isRaid() && Formulas.calcAtkBreak(this.getEffected(), false)) {
            this.getEffected().breakAttack();
            this.getEffected().breakCast();
         }

         if (damage > 0) {
            this.getEffected().setCurrentCp(this.getEffected().getCurrentCp() - (double)damage);
            if (this.getEffected() != this.getEffector()) {
               this.getEffector().sendDamageMessage(this.getEffected(), damage, this.getSkill(), false, false, false);
               this.getEffected().notifyDamageReceived((double)damage, this.getEffector(), this.getSkill(), false, false);
            }
         }

         Formulas.calcDamageReflected(this.getEffector(), this.getEffected(), this.getSkill(), false);
         return true;
      } else {
         return false;
      }
   }
}
