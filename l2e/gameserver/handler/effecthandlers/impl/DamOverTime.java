package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class DamOverTime extends Effect {
   private final boolean _canKill;
   private int _doubleTick;

   public DamOverTime(Env env, EffectTemplate template) {
      super(env, template);
      this._canKill = template.hasParameters() && template.getParameters().getBool("canKill", false);
      this._doubleTick = 1;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.DMG_OVER_TIME;
   }

   @Override
   public boolean onActionTime() {
      if (this.getEffected().isDead()) {
         return false;
      } else {
         double damage;
         if (this.getSkill().isToggle()) {
            damage = this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
         } else {
            damage = this.calc();
         }

         if (this.getSkill().getId() == 4082 && this.getTickCount() > 3600) {
            damage = damage * (double)this.getTickCount() / 100.0 / 2.0 * (double)this._doubleTick;
            ++this._doubleTick;
         }

         if (damage >= this.getEffected().getCurrentHp() - 1.0) {
            if (this.getSkill().isToggle()) {
               this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
               return false;
            }

            if (!this._canKill) {
               if (this.getEffected().getCurrentHp() <= 1.0) {
                  return true;
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
