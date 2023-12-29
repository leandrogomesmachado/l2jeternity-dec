package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class NoblesseBless extends Effect {
   public NoblesseBless(Env env, EffectTemplate template) {
      super(env, template);
   }

   public NoblesseBless(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return true;
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.NOBLESS_BLESSING.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NOBLESSE_BLESSING;
   }

   @Override
   public boolean onStart() {
      return this.getEffector() != null && this.getEffected() != null && this.getEffected().isPlayable();
   }
}
