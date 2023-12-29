package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ManaDamOverTime extends Effect {
   public ManaDamOverTime(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.MANA_DMG_OVER_TIME;
   }

   @Override
   public boolean onActionTime() {
      Creature target = this.getEffected();
      if (target == null) {
         return false;
      } else if (!target.isHealBlocked() && !target.isDead()) {
         double manaDam;
         if (this.getSkill().isToggle()) {
            manaDam = this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
         } else {
            manaDam = this.calc();
         }

         if (manaDam > this.getEffected().getCurrentMp() && this.getSkill().isToggle()) {
            this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            return false;
         } else {
            this.getEffected().reduceCurrentMp(manaDam);
            return this.getSkill().isToggle();
         }
      } else {
         return false;
      }
   }
}
