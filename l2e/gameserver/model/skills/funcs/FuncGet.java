package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncGet extends Func {
   public FuncGet(Stats stat, int order, Object owner, double value) {
      super(stat, order, owner, value);
   }

   @Override
   public void calc(Env env) {
      env._value = this.value;
   }
}
