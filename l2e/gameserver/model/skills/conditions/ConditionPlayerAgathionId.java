package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerAgathionId extends Condition {
   private final int _agathionId;

   public ConditionPlayerAgathionId(int agathionId) {
      this._agathionId = agathionId;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getPlayer() != null && env.getPlayer().getAgathionId() == this._agathionId;
   }
}
