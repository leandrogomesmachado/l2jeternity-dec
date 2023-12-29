package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ChameleonRest extends Effect {
   public ChameleonRest(Env env, EffectTemplate template) {
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
      } else if (this.getSkill().getSkillType() != SkillType.CONT) {
         return false;
      } else if (this.getEffected().isPlayer() && !this.getEffected().getActingPlayer().isSitting()) {
         return false;
      } else {
         double manaDam = this.calc();
         if (manaDam > this.getEffected().getCurrentMp()) {
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
      return EffectFlag.SILENT_MOVE.getMask() | EffectFlag.RELAXING.getMask();
   }
}
