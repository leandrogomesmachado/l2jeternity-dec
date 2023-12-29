package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncAtkAccuracy extends Func {
   private static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

   public static Func getInstance() {
      return _faa_instance;
   }

   private FuncAtkAccuracy() {
      super(Stats.ACCURACY_COMBAT, 16, null);
   }

   @Override
   public void calc(Env env) {
      int level = env.getCharacter().getLevel();
      env.addValue(Math.sqrt((double)env.getCharacter().getDEX()) * 6.0 + (double)level);
      if (level > 77) {
         env.addValue((double)(level - 76));
      }

      if (level > 69) {
         env.addValue((double)(level - 69));
      }
   }
}
