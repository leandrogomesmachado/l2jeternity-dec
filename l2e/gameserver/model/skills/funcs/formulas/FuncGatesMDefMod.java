package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncGatesMDefMod extends Func {
   private static final FuncGatesMDefMod _fmm_instance = new FuncGatesMDefMod();

   public static Func getInstance() {
      return _fmm_instance;
   }

   private FuncGatesMDefMod() {
      super(Stats.MAGIC_DEFENCE, 32, null);
   }

   @Override
   public void calc(Env env) {
      if (SevenSigns.getInstance().getSealOwner(3) == 2) {
         env.mulValue(Config.ALT_SIEGE_DAWN_GATES_MDEF_MULT);
      } else if (SevenSigns.getInstance().getSealOwner(3) == 1) {
         env.mulValue(Config.ALT_SIEGE_DUSK_GATES_MDEF_MULT);
      }
   }
}
