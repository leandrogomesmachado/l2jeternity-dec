package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMul extends Func {
   private final Lambda _lambda;

   public FuncMul(Stats pStat, int pOrder, Object owner, Lambda lambda) {
      super(pStat, pOrder, owner);
      this._lambda = lambda;
   }

   @Override
   public void calc(Env env) {
      if (this.cond == null || this.cond.test(env)) {
         env.mulValue(this._lambda.calc(env));
      }
   }
}
