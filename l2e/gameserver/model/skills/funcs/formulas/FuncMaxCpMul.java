package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMaxCpMul extends Func {
   private static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

   public static Func getInstance() {
      return _fmcm_instance;
   }

   private FuncMaxCpMul() {
      super(Stats.MAX_CP, 32, null);
   }

   @Override
   public void calc(Env env) {
      env.mulValue(BaseStats.CON.calcBonus(env.getCharacter()));
   }
}
