package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMAtkMod extends Func {
   private static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

   public static Func getInstance() {
      return _fma_instance;
   }

   private FuncMAtkMod() {
      super(Stats.MAGIC_ATTACK, 32, null);
   }

   @Override
   public void calc(Env env) {
      double lvlMod = env.getCharacter().isPlayer() ? BaseStats.INT.calcBonus(env.getPlayer()) : BaseStats.INT.calcBonus(env.getCharacter());
      double intMod = env.getCharacter().isPlayer() ? env.getPlayer().getLevelMod() : env.getCharacter().getLevelMod();
      env.mulValue(Math.pow(lvlMod, 2.0) * Math.pow(intMod, 2.0));
   }
}
