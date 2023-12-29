package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Paralyze extends Effect {
   public Paralyze(Env env, EffectTemplate template) {
      super(env, template);
   }

   public Paralyze(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.PARALYZE;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startAbnormalEffect(AbnormalEffect.HOLD_1);
      this.getEffected().getAI().setIntention(CtrlIntention.IDLE, this.getEffector());
      this.getEffected().startParalyze();
      return super.onStart();
   }

   @Override
   public void onExit() {
      this.getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_1);
      if (!this.getEffected().isPlayer()) {
         this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
      }

      super.onExit();
   }

   @Override
   public boolean onActionTime() {
      return false;
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.PARALYZED.getMask();
   }
}
