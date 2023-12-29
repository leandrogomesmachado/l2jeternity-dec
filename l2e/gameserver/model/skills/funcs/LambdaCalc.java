package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.stats.Env;

public final class LambdaCalc extends Lambda {
   public Func[] funcs = new Func[0];

   @Override
   public double calc(Env env) {
      double saveValue = env.getValue();

      double var11;
      try {
         env.setValue(0.0);

         for(Func f : this.funcs) {
            f.calc(env);
         }

         var11 = env.getValue();
      } finally {
         env.setValue(saveValue);
      }

      return var11;
   }

   public void addFunc(Func f) {
      int len = this.funcs.length;
      Func[] dest = new Func[len + 1];
      System.arraycopy(this.funcs, 0, dest, 0, len);
      dest[len] = f;
      this.funcs = dest;
   }
}
