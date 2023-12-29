package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Disarm extends Effect {
   public Disarm(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.DISARMED.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.DISARM;
   }

   @Override
   public boolean onStart() {
      if (!this.getEffected().isPlayer()) {
         return false;
      } else {
         this.getEffected().getActingPlayer().disarmWeapons();
         return true;
      }
   }
}
