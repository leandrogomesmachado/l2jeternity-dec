package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ProtectionBlessing extends Effect {
   public ProtectionBlessing(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.PROTECTION_BLESSING.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.PROTECTION_BLESSING;
   }

   @Override
   public boolean onStart() {
      return this.getEffector() != null && this.getEffected() != null && this.getEffected().isPlayer();
   }
}
