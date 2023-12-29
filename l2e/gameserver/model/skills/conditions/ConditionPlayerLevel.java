package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerLevel extends Condition {
   private final int _level;

   public ConditionPlayerLevel(int level) {
      this._level = level;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getCharacter().getLevel() >= this._level;
   }
}
