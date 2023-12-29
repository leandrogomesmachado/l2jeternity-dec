package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class BlockResurrection extends Effect {
   public BlockResurrection(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BLOCK_RESURRECTION;
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.BLOCK_RESURRECTION.getMask();
   }
}
