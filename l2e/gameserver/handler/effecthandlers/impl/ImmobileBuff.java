package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ImmobileBuff extends Buff {
   public ImmobileBuff(Env env, EffectTemplate template) {
      super(env, template);
   }

   public ImmobileBuff(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }

   @Override
   public boolean onStart() {
      this.getEffected().setIsImmobilized(true);
      return super.onStart();
   }

   @Override
   public void onExit() {
      this.getEffected().setIsImmobilized(false);
      super.onExit();
   }
}
