package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ChangeHairColor extends Effect {
   public ChangeHairColor(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() != null
         && this.getEffected() != null
         && this.getEffector().isPlayer()
         && this.getEffected().isPlayer()
         && !this.getEffected().isAlikeDead()) {
         Player player = this.getEffector().getActingPlayer();
         player.getAppearance().setHairColor((int)this.calc());
         player.broadcastUserInfo(true);
         return true;
      } else {
         return false;
      }
   }
}
