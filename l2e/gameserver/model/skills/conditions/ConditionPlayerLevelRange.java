package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerLevelRange extends Condition {
   private final int[] _levels;

   public ConditionPlayerLevelRange(int[] levels) {
      this._levels = levels;
   }

   @Override
   public boolean testImpl(Env env) {
      int level = env.getCharacter().getLevel();
      return level >= this._levels[0] && level <= this._levels[1];
   }
}
