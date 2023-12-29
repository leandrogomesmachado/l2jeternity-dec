package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class VitalityPointUp extends Effect {
   public VitalityPointUp(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer()) {
         this.getEffected().getActingPlayer().updateVitalityPoints((double)((float)this.calc()), false, false);
         this.getEffected().getActingPlayer().sendUserInfo();
         return true;
      } else {
         return false;
      }
   }
}
