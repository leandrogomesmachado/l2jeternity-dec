package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetWeight extends Condition {
   private final int _weight;

   public ConditionTargetWeight(int weight) {
      this._weight = weight;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature targetObj = env.getTarget();
      if (targetObj != null && targetObj.isPlayer()) {
         Player target = targetObj.getActingPlayer();
         if (!target.getDietMode() && target.getMaxLoad() > 0) {
            int weightproc = (target.getCurrentLoad() - target.getBonusWeightPenalty()) * 100 / target.getMaxLoad();
            return weightproc < this._weight;
         }
      }

      return false;
   }
}
