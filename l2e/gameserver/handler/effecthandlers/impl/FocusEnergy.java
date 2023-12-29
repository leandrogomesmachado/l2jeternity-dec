package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class FocusEnergy extends Effect {
   public FocusEnergy(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean calcSuccess() {
      return true;
   }

   @Override
   public boolean onStart() {
      if (!this.getEffected().isPlayer()) {
         return false;
      } else {
         this.getEffected().getActingPlayer().increaseCharges(1, (int)this.calc());
         return true;
      }
   }
}
