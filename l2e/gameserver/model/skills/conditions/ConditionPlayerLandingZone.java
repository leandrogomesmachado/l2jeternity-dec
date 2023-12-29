package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;

public class ConditionPlayerLandingZone extends Condition {
   private final boolean _val;

   public ConditionPlayerLandingZone(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getCharacter().isInsideZone(ZoneId.LANDING) == this._val;
   }
}
