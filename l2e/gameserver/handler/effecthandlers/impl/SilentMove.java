package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class SilentMove extends Effect {
   public SilentMove(Env env, EffectTemplate template) {
      super(env, template);
   }

   public SilentMove(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return !this.getSkill().isToggle();
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.SILENT_MOVE.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onActionTime() {
      if (this.getEffected().isDead()) {
         return false;
      } else {
         double manaDam;
         if (this.getSkill().isToggle()) {
            manaDam = this.calc() * (double)this.getEffectTemplate().getTotalTickCount();
         } else {
            manaDam = this.calc();
         }

         if (manaDam > this.getEffected().getCurrentMp()) {
            this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            return false;
         } else {
            this.getEffected().reduceCurrentMp(manaDam);
            return this.getSkill().isToggle();
         }
      }
   }
}
