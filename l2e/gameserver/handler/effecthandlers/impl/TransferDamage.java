package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class TransferDamage extends Effect {
   public TransferDamage(Env env, EffectTemplate template) {
      super(env, template);
   }

   public TransferDamage(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.DAMAGE_TRANSFER;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayable() && this.getEffector().isPlayer()) {
         ((Playable)this.getEffected()).setTransferDamageTo(this.getEffector().getActingPlayer());
      }

      return true;
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayable() && this.getEffector().isPlayer()) {
         ((Playable)this.getEffected()).setTransferDamageTo(null);
      }
   }
}
