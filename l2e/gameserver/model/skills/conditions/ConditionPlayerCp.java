package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerCp extends Condition {
   private final int _cp;

   public ConditionPlayerCp(int cp) {
      this._cp = cp;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getCharacter() != null && env.getCharacter().getCurrentCp() * 100.0 / env.getCharacter().getMaxCp() >= (double)this._cp;
   }
}
