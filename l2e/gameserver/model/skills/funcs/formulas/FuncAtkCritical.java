package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncAtkCritical extends Func {
   private static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

   public static Func getInstance() {
      return _fac_instance;
   }

   private FuncAtkCritical() {
      super(Stats.CRITICAL_RATE, 9, null);
   }

   @Override
   public void calc(Env env) {
      env.mulValue(BaseStats.DEX.calcBonus(env.getCharacter()) * 10.0);
      env.setBaseValue(env.getValue());
   }
}
