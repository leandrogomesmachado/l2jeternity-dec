package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerCloakStatus extends Condition {
   private final int _val;

   public ConditionPlayerCloakStatus(int val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getPlayer() != null && env.getPlayer().getInventory().getCloakStatus() >= this._val;
   }
}
