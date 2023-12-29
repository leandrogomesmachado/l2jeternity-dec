package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMaxHpMul extends Func {
   private static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

   public static Func getInstance() {
      return _fmhm_instance;
   }

   private FuncMaxHpMul() {
      super(Stats.MAX_HP, 32, null);
   }

   @Override
   public void calc(Env env) {
      env.mulValue(BaseStats.CON.calcBonus(env.getCharacter()));
   }
}
