package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerMp extends Condition {
   private final int _mp;

   public ConditionPlayerMp(int mp) {
      this._mp = mp;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getCharacter().getCurrentMp() * 100.0 / env.getCharacter().getMaxMp() <= (double)this._mp;
   }
}
