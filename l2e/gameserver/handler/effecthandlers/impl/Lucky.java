package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Lucky extends Effect {
   public Lucky(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean onStart() {
      return this.getEffector() != null && this.getEffected() != null && this.getEffected().isPlayer();
   }

   @Override
   public boolean onActionTime() {
      return this.getSkill().isPassive();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.LUCKY;
   }
}
