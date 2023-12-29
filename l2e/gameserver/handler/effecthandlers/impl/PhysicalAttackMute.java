package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class PhysicalAttackMute extends Effect {
   public PhysicalAttackMute(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.PSYCHICAL_ATTACK_MUTED.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.PHYSICAL_ATTACK_MUTE;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startPhysicalAttackMuted();
      return true;
   }
}
