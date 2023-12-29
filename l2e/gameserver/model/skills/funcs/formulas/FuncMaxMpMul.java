package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMaxMpMul extends Func {
   private static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

   public static Func getInstance() {
      return _fmmm_instance;
   }

   private FuncMaxMpMul() {
      super(Stats.MAX_MP, 32, null);
   }

   @Override
   public void calc(Env env) {
      env.mulValue(BaseStats.MEN.calcBonus(env.getCharacter()));
   }
}
