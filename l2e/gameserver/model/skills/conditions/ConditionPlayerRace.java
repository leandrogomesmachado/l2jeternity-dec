package l2e.gameserver.model.skills.conditions;

import l2e.commons.util.Util;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerRace extends Condition {
   private final Race[] _races;

   public ConditionPlayerRace(Race[] races) {
      this._races = races;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getCharacter() != null && env.getCharacter().isPlayer() ? Util.contains(this._races, env.getPlayer().getRace()) : false;
   }
}
