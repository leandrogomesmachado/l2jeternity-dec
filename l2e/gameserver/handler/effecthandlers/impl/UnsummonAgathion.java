package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class UnsummonAgathion extends Effect {
   public UnsummonAgathion(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      Player player = this.getEffector().getActingPlayer();
      if (player != null) {
         player.setAgathionId(0);
         player.broadcastUserInfo(true);

         for(Effect e : player.getAllEffects()) {
            if (e != null && e.getSkill().hasEffectType(EffectType.ENERGY_DAM_OVER_TIME)) {
               e.exit();
            }
         }
      }

      return true;
   }
}
