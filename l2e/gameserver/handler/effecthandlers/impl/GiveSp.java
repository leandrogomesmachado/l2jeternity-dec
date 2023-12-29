package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class GiveSp extends Effect {
   public GiveSp(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.GIVE_SP;
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() != null
         && this.getEffected() != null
         && this.getEffector().isPlayer()
         && this.getEffected().isPlayer()
         && !this.getEffected().isAlikeDead()) {
         this.getEffector().getActingPlayer().addExpAndSp(0L, (int)this.calc());
         return true;
      } else {
         return false;
      }
   }
}
