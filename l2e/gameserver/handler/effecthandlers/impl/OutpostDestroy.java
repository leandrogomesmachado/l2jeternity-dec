package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class OutpostDestroy extends Effect {
   public OutpostDestroy(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean isInstant() {
      return true;
   }

   @Override
   public boolean onStart() {
      Player player = this.getEffector().getActingPlayer();
      if (player.isClanLeader() && player.getClan().getCastleId() > 0) {
         if (TerritoryWarManager.getInstance().isTWInProgress()) {
            TerritoryWarManager.getInstance().removeHQForClan(player.getClan());
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }
}
