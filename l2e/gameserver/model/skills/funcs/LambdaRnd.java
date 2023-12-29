package l2e.gameserver.model.skills.funcs;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.stats.Env;

public final class LambdaRnd extends Lambda {
   private final Lambda _max;
   private final boolean _linear;

   public LambdaRnd(Lambda max, boolean linear) {
      this._max = max;
      this._linear = linear;
   }

   @Override
   public double calc(Env env) {
      return this._linear ? this._max.calc(env) * Rnd.nextDouble() : this._max.calc(env) * Rnd.nextGaussian();
   }
}
