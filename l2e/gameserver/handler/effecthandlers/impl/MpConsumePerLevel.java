package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class MpConsumePerLevel extends Effect {
   public MpConsumePerLevel(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.MP_CONSUME_PER_LEVEL;
   }

   @Override
   public boolean onActionTime() {
      Creature target = this.getEffected();
      if (target == null) {
         return false;
      } else if (target.isDead()) {
         return false;
      } else {
         double base = this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
         double consume = this.getAbnormalTime() > 0 ? (double)(this.getEffected().getLevel() - 1) / 7.5 * base * (double)this.getAbnormalTime() : base;
         if (consume > this.getEffected().getCurrentMp()) {
            this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            return false;
         } else {
            this.getEffected().reduceCurrentMp(consume);
            return this.getSkill().isToggle();
         }
      }
   }
}
