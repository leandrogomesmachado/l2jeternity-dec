package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerWeight extends Condition {
   private final int _weight;

   public ConditionPlayerWeight(int weight) {
      this._weight = weight;
   }

   @Override
   public boolean testImpl(Env env) {
      Player player = env.getPlayer();
      if (player != null && player.getMaxLoad() > 0) {
         int weightproc = (player.getCurrentLoad() - player.getBonusWeightPenalty()) * 100 / player.getMaxLoad();
         return weightproc < this._weight || player.getDietMode();
      } else {
         return true;
      }
   }
}
