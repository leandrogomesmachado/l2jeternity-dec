package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncPAtkSpeed extends Func {
   private static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

   public static Func getInstance() {
      return _fas_instance;
   }

   private FuncPAtkSpeed() {
      super(Stats.POWER_ATTACK_SPEED, 32, null);
   }

   @Override
   public void calc(Env env) {
      env.mulValue(BaseStats.DEX.calcBonus(env.getCharacter()));
   }
}
