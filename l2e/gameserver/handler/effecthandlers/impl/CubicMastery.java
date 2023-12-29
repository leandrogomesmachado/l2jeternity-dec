package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class CubicMastery extends Effect {
   public CubicMastery(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CUBIC_MASTERY;
   }

   @Override
   public boolean onActionTime() {
      return this.getSkill().isPassive();
   }

   @Override
   public boolean onStart() {
      return this.getEffector() != null && this.getEffected() != null && this.getEffected().isPlayer();
   }
}
