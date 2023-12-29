package l2e.gameserver.model.skills.funcs.formulas;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.parser.ArmorSetsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ArmorSetTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncArmorSet extends Func {
   private static final Map<Stats, FuncArmorSet> _fh_instance = new HashMap<>();

   public static Func getInstance(Stats st) {
      if (!_fh_instance.containsKey(st)) {
         _fh_instance.put(st, new FuncArmorSet(st));
      }

      return _fh_instance.get(st);
   }

   private FuncArmorSet(Stats stat) {
      super(stat, 16, null);
   }

   @Override
   public void calc(Env env) {
      Player player = env.getPlayer();
      if (player != null) {
         ItemInstance chest = player.getChestArmorInstance();
         if (chest != null) {
            ArmorSetTemplate set = ArmorSetsParser.getInstance().getSet(chest.getId());
            if (set != null && set.containAll(player)) {
               switch(this.stat) {
                  case STAT_STR:
                     env.addValue((double)set.getSTR());
                     break;
                  case STAT_DEX:
                     env.addValue((double)set.getDEX());
                     break;
                  case STAT_INT:
                     env.addValue((double)set.getINT());
                     break;
                  case STAT_MEN:
                     env.addValue((double)set.getMEN());
                     break;
                  case STAT_CON:
                     env.addValue((double)set.getCON());
                     break;
                  case STAT_WIT:
                     env.addValue((double)set.getWIT());
               }
            }
         }
      }
   }
}
