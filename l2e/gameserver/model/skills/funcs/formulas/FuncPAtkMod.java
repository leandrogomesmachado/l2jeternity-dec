package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncPAtkMod extends Func {
   private static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

   public static Func getInstance() {
      return _fpa_instance;
   }

   private FuncPAtkMod() {
      super(Stats.POWER_ATTACK, 48, null);
   }

   @Override
   public void calc(Env env) {
      if (env.getCharacter().isPlayer()) {
         env.mulValue(BaseStats.STR.calcBonus(env.getPlayer()) * env.getPlayer().getLevelMod());
      } else {
         env.mulValue(BaseStats.STR.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod());
      }
   }
}
