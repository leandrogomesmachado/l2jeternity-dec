package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.RecipeController;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class OpenCommonRecipeBook extends Effect {
   public OpenCommonRecipeBook(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean calcSuccess() {
      return true;
   }

   @Override
   public boolean onStart() {
      if (!this.getEffector().isPlayer()) {
         return false;
      } else {
         Player player = this.getEffector().getActingPlayer();
         if (player.getPrivateStoreType() != 0) {
            player.sendPacket(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING);
            return false;
         } else {
            RecipeController.getInstance().requestBookOpen(player, false);
            return true;
         }
      }
   }
}
