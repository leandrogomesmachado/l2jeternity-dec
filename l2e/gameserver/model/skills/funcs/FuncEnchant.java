package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncEnchant extends Func {
   public FuncEnchant(Stats pStat, int pOrder, Object owner, Lambda lambda) {
      super(pStat, pOrder, owner);
   }

   @Override
   public void calc(Env env) {
      if (this.cond == null || this.cond.test(env)) {
         ItemInstance item = (ItemInstance)this.funcOwner;
         int enchant = item.getEnchantLevel();
         if (enchant > 0) {
            int overenchant = 0;
            if (enchant > 3) {
               overenchant = enchant - 3;
               enchant = 3;
            }

            if (env.getPlayer() != null) {
               Player player = env.getPlayer();
               if (player.isInOlympiadMode() && Config.ALT_OLY_ENCHANT_LIMIT >= 0 && enchant + overenchant > Config.ALT_OLY_ENCHANT_LIMIT) {
                  if (Config.ALT_OLY_ENCHANT_LIMIT > 3) {
                     overenchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
                  } else {
                     overenchant = 0;
                     enchant = Config.ALT_OLY_ENCHANT_LIMIT;
                  }
               }
            }

            if (this.stat == Stats.MAGIC_DEFENCE || this.stat == Stats.POWER_DEFENCE) {
               env.addValue((double)(enchant + 3 * overenchant));
            } else if (this.stat == Stats.MAGIC_ATTACK) {
               switch(item.getItem().getItemGradeSPlus()) {
                  case 0:
                  case 1:
                     env.addValue((double)(2 * enchant + 4 * overenchant));
                     break;
                  case 2:
                  case 3:
                  case 4:
                     env.addValue((double)(3 * enchant + 6 * overenchant));
                     break;
                  case 5:
                     env.addValue((double)(4 * enchant + 8 * overenchant));
               }
            } else {
               if (item.isWeapon()) {
                  WeaponType type = (WeaponType)item.getItemType();
                  switch(item.getItem().getItemGradeSPlus()) {
                     case 0:
                     case 1:
                        switch(type) {
                           case BOW:
                           case CROSSBOW:
                              env.addValue((double)(4 * enchant + 8 * overenchant));
                              return;
                           default:
                              env.addValue((double)(2 * enchant + 4 * overenchant));
                              return;
                        }
                     case 2:
                     case 3:
                        switch(type) {
                           case BOW:
                           case CROSSBOW:
                              env.addValue((double)(6 * enchant + 12 * overenchant));
                              return;
                           case BIGSWORD:
                           case BIGBLUNT:
                           case DUAL:
                           case DUALFIST:
                           case ANCIENTSWORD:
                           case DUALDAGGER:
                              env.addValue((double)(4 * enchant + 8 * overenchant));
                              return;
                           default:
                              env.addValue((double)(3 * enchant + 6 * overenchant));
                              return;
                        }
                     case 4:
                        switch(type) {
                           case BOW:
                           case CROSSBOW:
                              env.addValue((double)(8 * enchant + 16 * overenchant));
                              return;
                           case BIGSWORD:
                           case BIGBLUNT:
                           case DUAL:
                           case DUALFIST:
                           case ANCIENTSWORD:
                           case DUALDAGGER:
                              env.addValue((double)(5 * enchant + 10 * overenchant));
                              return;
                           default:
                              env.addValue((double)(4 * enchant + 8 * overenchant));
                              return;
                        }
                     case 5:
                        switch(type) {
                           case BOW:
                           case CROSSBOW:
                              env.addValue((double)(10 * enchant + 20 * overenchant));
                              break;
                           case BIGSWORD:
                           case BIGBLUNT:
                           case DUAL:
                           case DUALFIST:
                           case ANCIENTSWORD:
                           case DUALDAGGER:
                              env.addValue((double)(6 * enchant + 12 * overenchant));
                              break;
                           default:
                              env.addValue((double)(5 * enchant + 10 * overenchant));
                        }
                  }
               }
            }
         }
      }
   }
}
