package l2e.gameserver.model.skills.conditions;

import l2e.commons.util.Util;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetRace extends Condition {
   private final Race[] _races;

   public ConditionTargetRace(Race[] races) {
      this._races = races;
   }

   @Override
   public boolean testImpl(Env env) {
      return !env.getTarget().isPlayer() ? false : Util.contains(this._races, env.getTarget().getActingPlayer().getRace());
   }
}
