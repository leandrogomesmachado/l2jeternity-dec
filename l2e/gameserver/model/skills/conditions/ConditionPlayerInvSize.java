package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerInvSize extends Condition {
   private final int _size;

   public ConditionPlayerInvSize(int size) {
      this._size = size;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() != null) {
         return env.getPlayer().getInventory().getSize(false) <= env.getPlayer().getInventoryLimit() - this._size;
      } else {
         return true;
      }
   }
}
