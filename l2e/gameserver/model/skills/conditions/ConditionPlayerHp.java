package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerHp extends Condition {
   private final int _hp;

   public ConditionPlayerHp(int hp) {
      this._hp = hp;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getCharacter() != null && env.getCharacter().getCurrentHp() * 100.0 / env.getCharacter().getMaxHp() <= (double)this._hp;
   }
}
