package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Buff extends Effect {
   public Buff(Env env, EffectTemplate template) {
      super(env, template);
   }

   public Buff(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return !this.getSkill().isPassive();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }

   @Override
   public boolean onActionTime() {
      return this.getSkill().isPassive() || this.getSkill().isToggle();
   }
}
