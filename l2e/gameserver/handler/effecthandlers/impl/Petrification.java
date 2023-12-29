package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Petrification extends Effect {
   public Petrification(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.PETRIFICATION;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startAbnormalEffect(AbnormalEffect.HOLD_2);
      this.getEffected().startParalyze();
      this.getEffected().setIsInvul(true);
      return super.onStart();
   }

   @Override
   public void onExit() {
      this.getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_2);
      if (!this.getEffected().isHealBlocked()) {
         this.getEffected().setIsInvul(false);
      }

      if (!this.getEffected().isPlayer()) {
         this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
      }
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.PARALYZED.getMask() | EffectFlag.INVUL.getMask();
   }
}
