package l2e.gameserver.handler.effecthandlers.impl;

import java.util.List;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.reward.RewardItem;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Sweeper extends Effect {
   public Sweeper(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() != null && this.getEffected() != null && this.getEffector().isPlayer() && this.getEffected().isAttackable()) {
         Player player = this.getEffector().getActingPlayer();
         Attackable monster = (Attackable)this.getEffected();
         if (!monster.checkSpoilOwner(player, false)) {
            return false;
         } else {
            List<RewardItem> items = monster.takeSweep();
            if (items == null) {
               return false;
            } else if (!player.getInventory().checkInventorySlotsAndWeight(monster.getSpoilLootItems(), false, false)) {
               return false;
            } else {
               for(RewardItem item : items) {
                  if (!player.isInParty() || player.getParty().getLootDistribution() != 2 && !BotFunctions.getInstance().isAutoSpoilEnable(player)) {
                     player.addItem("Sweeper", item._itemId, item._count, this.getEffected(), true);
                  } else {
                     player.getParty().distributeItem(player, item._itemId, item._count, true, monster);
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }
}
