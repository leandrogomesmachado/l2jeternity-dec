package l2e.gameserver.model.skills.funcs.formulas;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncHenna extends Func {
   private static final Map<Stats, FuncHenna> _fh_instance = new HashMap<>();

   public static Func getInstance(Stats st) {
      if (!_fh_instance.containsKey(st)) {
         _fh_instance.put(st, new FuncHenna(st));
      }

      return _fh_instance.get(st);
   }

   private FuncHenna(Stats stat) {
      super(stat, 16, null);
   }

   @Override
   public void calc(Env env) {
      Player pc = env.getPlayer();
      if (pc != null) {
         switch(this.stat) {
            case STAT_STR:
               env.addValue((double)pc.getHennaStatSTR());
               break;
            case STAT_CON:
               env.addValue((double)pc.getHennaStatCON());
               break;
            case STAT_DEX:
               env.addValue((double)pc.getHennaStatDEX());
               break;
            case STAT_INT:
               env.addValue((double)pc.getHennaStatINT());
               break;
            case STAT_WIT:
               env.addValue((double)pc.getHennaStatWIT());
               break;
            case STAT_MEN:
               env.addValue((double)pc.getHennaStatMEN());
         }
      }
   }
}
