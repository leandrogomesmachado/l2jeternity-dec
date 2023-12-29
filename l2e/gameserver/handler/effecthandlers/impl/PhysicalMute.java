package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class PhysicalMute extends Effect {
   public PhysicalMute(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.PHYSICAL_MUTE;
   }

   @Override
   public boolean onStart() {
      this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_MUTED);
      return true;
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.PSYCHICAL_MUTED.getMask();
   }
}
