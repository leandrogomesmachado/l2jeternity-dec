package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncAtkEvasion extends Func {
   private static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

   public static Func getInstance() {
      return _fae_instance;
   }

   private FuncAtkEvasion() {
      super(Stats.EVASION_RATE, 16, null);
   }

   @Override
   public void calc(Env env) {
      int level = env.getCharacter().getLevel();
      if (env.getCharacter().isPlayer()) {
         env.addValue(Math.sqrt((double)env.getCharacter().getDEX()) * 6.0 + (double)level);
         if (level > 77) {
            env.addValue((double)(level - 77));
         }

         if (level > 69) {
            env.addValue((double)(level - 69));
         }
      } else {
         env.addValue(Math.sqrt((double)env.getCharacter().getDEX()) * 6.0 + (double)level);
         if (level > 69) {
            env.addValue((double)(level - 69 + 2));
         }
      }
   }
}
