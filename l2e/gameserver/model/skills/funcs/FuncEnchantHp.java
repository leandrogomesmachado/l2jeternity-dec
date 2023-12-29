package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.data.parser.EnchantItemHPBonusParser;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncEnchantHp extends Func {
   public FuncEnchantHp(Stats pStat, int pOrder, Object owner, Lambda lambda) {
      super(pStat, pOrder, owner);
   }

   @Override
   public void calc(Env env) {
      if (this.cond == null || this.cond.test(env)) {
         ItemInstance item = (ItemInstance)this.funcOwner;
         if (item.getEnchantLevel() > 0) {
            env.addValue((double)EnchantItemHPBonusParser.getInstance().getHPBonus(item));
         }
      }
   }
}
