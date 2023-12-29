package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public final class Flag extends Effect {
   public Flag(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         this.getEffected().updatePvPFlag(1);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onActionTime() {
      this.getEffected().updatePvPFlag(1);
      return super.onActionTime();
   }

   @Override
   public void onExit() {
      this.getEffected().getActingPlayer().updatePvPFlag(0);
   }
}
