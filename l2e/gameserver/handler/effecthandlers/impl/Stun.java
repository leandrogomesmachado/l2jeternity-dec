package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Stun extends Effect {
   public Stun(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.STUNNED.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.STUN;
   }

   @Override
   public void onExit() {
      if (this.getSkill().getId() == 5008) {
         this.getEffected().setIsDanceStun(false);
      }

      this.getEffected().setIsStuned(false);
   }

   @Override
   public boolean onStart() {
      if (this.getSkill().getId() == 5008) {
         this.getEffected().setIsDanceStun(true);
      }

      this.getEffected().abortAttack();
      this.getEffected().abortCast();
      this.getEffected().stopMove(null);
      this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
      if (!this.getEffected().isSummon()) {
         this.getEffected().getAI().setIntention(CtrlIntention.IDLE);
      }

      this.getEffected().setIsStuned(true);
      return true;
   }

   @Override
   public boolean onActionTime() {
      return false;
   }
}
