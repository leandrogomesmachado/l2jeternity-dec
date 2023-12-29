package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class CancelAll extends Effect {
   private final int _rate;

   public CancelAll(Env env, EffectTemplate template) {
      super(env, template);
      this._rate = template.getParameters().getInteger("rate", 0);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CANCEL_ALL;
   }

   @Override
   public boolean onStart() {
      if (Rnd.get(100) <= this._rate) {
         this.getEffected().stopAllEffects();
      }

      return false;
   }
}
