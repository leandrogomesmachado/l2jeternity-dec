package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMAtkCritical extends Func {
   private static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

   public static Func getInstance() {
      return _fac_instance;
   }

   private FuncMAtkCritical() {
      super(Stats.MCRITICAL_RATE, 9, null);
   }

   @Override
   public void calc(Env env) {
      Creature p = env.getCharacter();
      if (p.isPlayer()) {
         if (p.getActiveWeaponInstance() != null) {
            env.mulValue(BaseStats.WIT.calcBonus(p) * 10.0);
         }
      } else {
         env.mulValue(BaseStats.WIT.calcBonus(p) * 10.0);
      }
   }
}
