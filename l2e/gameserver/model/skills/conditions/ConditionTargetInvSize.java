package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetInvSize extends Condition {
   private final int _size;

   public ConditionTargetInvSize(int size) {
      this._size = size;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature targetObj = env.getTarget();
      if (targetObj != null && targetObj.isPlayer()) {
         Player target = targetObj.getActingPlayer();
         return target.getInventory().getSize(false) <= target.getInventoryLimit() - this._size;
      } else {
         return false;
      }
   }
}
