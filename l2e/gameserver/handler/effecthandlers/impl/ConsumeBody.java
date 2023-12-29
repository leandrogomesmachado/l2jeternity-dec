package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ConsumeBody extends Effect {
   public ConsumeBody(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() != null && this.getEffected() != null && this.getEffected().isNpc() && this.getEffected().isDead()) {
         ((Npc)this.getEffected()).endDecayTask();
         return true;
      } else {
         return false;
      }
   }
}
