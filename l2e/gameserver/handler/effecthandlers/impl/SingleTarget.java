package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class SingleTarget extends Effect {
   public SingleTarget(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.SINGLE_TARGET.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SINGLE_TARGET;
   }

   @Override
   public boolean onActionTime() {
      if (this.getEffected().isDead()) {
         return false;
      } else {
         double manaDam = this.calc() * (double)this.getTickCount();
         if (manaDam > this.getEffected().getCurrentMp() && this.getSkill().isToggle()) {
            this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            return false;
         } else {
            this.getEffected().reduceCurrentMp(manaDam);
            return true;
         }
      }
   }
}
