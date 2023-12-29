package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Hide extends Effect {
   public Hide(Env env, EffectTemplate template) {
      super(env, template);
   }

   public Hide(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.HIDE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer()) {
         Player activeChar = this.getEffected().getActingPlayer();
         activeChar.setInvisible(true);
         activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);
         if (activeChar.getAI().getNextIntention() != null && activeChar.getAI().getNextIntention().getCtrlIntention() == CtrlIntention.ATTACK) {
            activeChar.getAI().setIntention(CtrlIntention.IDLE);
         }
      }

      return true;
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayer()) {
         Player activeChar = this.getEffected().getActingPlayer();
         if (!activeChar.inObserverMode()) {
            activeChar.setInvisible(false);
         }

         activeChar.stopAbnormalEffect(AbnormalEffect.STEALTH);
      }
   }
}
