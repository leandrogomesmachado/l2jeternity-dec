package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Betray extends Effect {
   public Betray(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BETRAY;
   }

   @Override
   public boolean onStart() {
      if (this.getEffector().isPlayer() && this.getEffected().isSummon()) {
         Player targetOwner = this.getEffected().getActingPlayer();
         this.getEffected().getAI().setIntention(CtrlIntention.ATTACK, targetOwner);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      this.getEffected().getAI().setIntention(CtrlIntention.IDLE);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.BETRAYED.getMask();
   }
}
