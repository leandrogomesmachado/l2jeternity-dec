package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerSouls extends Condition {
   private final int _souls;

   public ConditionPlayerSouls(int souls) {
      this._souls = souls;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getPlayer() != null && env.getPlayer().getChargedSouls() >= this._souls;
   }
}
