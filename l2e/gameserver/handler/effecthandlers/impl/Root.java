package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Root extends Effect {
   public Root(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.ROOTED.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.ROOT;
   }

   @Override
   public boolean onStart() {
      this.getEffected().stopMove(null);
      this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_ROOTED);
      return super.onStart();
   }

   @Override
   public void onExit() {
      if (!this.getEffected().isPlayer()) {
         this.getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
      }

      super.onExit();
   }
}
