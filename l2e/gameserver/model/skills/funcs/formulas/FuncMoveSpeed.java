package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMoveSpeed extends Func {
   private static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

   public static Func getInstance() {
      return _fms_instance;
   }

   private FuncMoveSpeed() {
      super(Stats.MOVE_SPEED, 48, null);
   }

   @Override
   public void calc(Env env) {
      env.mulValue(BaseStats.DEX.calcBonus(env.getCharacter()));
   }
}
