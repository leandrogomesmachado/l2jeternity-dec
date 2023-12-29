package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public final class ConditionPlayerPledgeClass extends Condition {
   private final int _pledgeClass;

   public ConditionPlayerPledgeClass(int pledgeClass) {
      this._pledgeClass = pledgeClass;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() != null && env.getPlayer().getClan() != null) {
         return this._pledgeClass == -1 ? env.getPlayer().isClanLeader() : env.getPlayer().getPledgeClass() >= this._pledgeClass;
      } else {
         return false;
      }
   }
}
