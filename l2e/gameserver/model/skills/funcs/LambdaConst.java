package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.stats.Env;

public final class LambdaConst extends Lambda {
   private final double _value;

   public LambdaConst(double value) {
      this._value = value;
   }

   @Override
   public double calc(Env env) {
      return this._value;
   }
}
