package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class Relax extends Effect {
   public Relax(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.RELAXING;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().getActingPlayer().sitDown(false);
      } else {
         this.getEffected().getAI().setIntention(CtrlIntention.REST);
      }

      return super.onStart();
   }

   @Override
   public boolean onActionTime() {
      if (this.getEffected().isDead()) {
         return false;
      } else if (this.getEffected().isPlayer() && !this.getEffected().getActingPlayer().isSitting()) {
         return false;
      } else if (this.getEffected().getCurrentHp() + 1.0 > (double)this.getEffected().getMaxRecoverableHp() && this.getSkill().isToggle()) {
         this.getEffected().sendPacket(SystemMessageId.SKILL_DEACTIVATED_HP_FULL);
         return false;
      } else {
         double manaDam = this.calc();
         if (manaDam > this.getEffected().getCurrentMp() && this.getSkill().isToggle()) {
            this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            return false;
         } else {
            this.getEffected().reduceCurrentMp(manaDam);
            return true;
         }
      }
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.RELAXING.getMask();
   }
}
