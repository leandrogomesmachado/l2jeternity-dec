package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncGatesPDefMod extends Func {
   private static final FuncGatesPDefMod _fmm_instance = new FuncGatesPDefMod();

   public static Func getInstance() {
      return _fmm_instance;
   }

   private FuncGatesPDefMod() {
      super(Stats.POWER_DEFENCE, 32, null);
   }

   @Override
   public void calc(Env env) {
      if (SevenSigns.getInstance().getSealOwner(3) == 2) {
         env.mulValue(Config.ALT_SIEGE_DAWN_GATES_PDEF_MULT);
      } else if (SevenSigns.getInstance().getSealOwner(3) == 1) {
         env.mulValue(Config.ALT_SIEGE_DUSK_GATES_PDEF_MULT);
      }
   }
}
