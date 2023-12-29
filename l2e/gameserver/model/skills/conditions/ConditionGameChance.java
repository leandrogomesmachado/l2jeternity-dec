package l2e.gameserver.model.skills.conditions;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.stats.Env;

public class ConditionGameChance extends Condition {
   private final int _chance;

   public ConditionGameChance(int chance) {
      this._chance = chance;
   }

   @Override
   public boolean testImpl(Env env) {
      return Rnd.get(100) < this._chance;
   }
}
