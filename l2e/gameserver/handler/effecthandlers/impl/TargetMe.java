package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class TargetMe extends Effect {
   public TargetMe(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayable()) {
         if (this.getEffected() instanceof SiegeSummonInstance) {
            return false;
         } else {
            if (this.getEffected().getTarget() != this.getEffector()) {
               Player effector = this.getEffector().getActingPlayer();
               if (effector == null || effector.checkPvpSkill(this.getEffected(), this.getSkill())) {
                  this.getEffected().setTarget(this.getEffector());
               }
            }

            ((Playable)this.getEffected()).setLockedTarget(this.getEffector());
            return true;
         }
      } else {
         return this.getEffected().isAttackable() && !this.getEffected().isRaid();
      }
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayable()) {
         ((Playable)this.getEffected()).setLockedTarget(null);
      }
   }
}
