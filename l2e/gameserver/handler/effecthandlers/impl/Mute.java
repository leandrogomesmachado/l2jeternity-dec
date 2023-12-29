package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Mute extends Effect {
   public Mute(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.MUTE;
   }

   @Override
   public boolean onStart() {
      this.getEffected().abortCast();
      this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_MUTED);
      return true;
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.MUTED.getMask();
   }
}
