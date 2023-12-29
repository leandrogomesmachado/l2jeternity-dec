package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class EnlargeAbnormalSlot extends Effect {
   public EnlargeAbnormalSlot(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean onActionTime() {
      return this.getSkill().isPassive();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.ENLARGE_ABNORMAL_SLOT;
   }

   @Override
   public boolean onStart() {
      return this.getEffector() != null && this.getEffected() != null && this.getEffected().isPlayer();
   }
}
