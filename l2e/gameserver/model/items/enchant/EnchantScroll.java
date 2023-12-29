package l2e.gameserver.model.items.enchant;

import java.util.logging.Level;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.EnchantItemGroupsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;

public final class EnchantScroll extends EnchantItem {
   private final boolean _isBlessed;
   private final boolean _isSafe;
   private final int _scrollGroupId;

   public EnchantScroll(StatsSet set) {
      super(set);
      this._isBlessed = set.getBool("isBlessed", false);
      this._isSafe = set.getBool("isSafe", false);
      this._scrollGroupId = set.getInteger("scrollGroupId", 0);
   }

   public boolean isBlessed() {
      return this._isBlessed;
   }

   public boolean isSafe() {
      return this._isSafe;
   }

   public int getScrollGroupId() {
      return this._scrollGroupId;
   }

   public boolean isValid(ItemInstance enchantItem, EnchantItem supportItem) {
      return supportItem == null || supportItem.isValid(enchantItem) && !this.isBlessed() ? super.isValid(enchantItem) : false;
   }

   public double getChance(Player player, ItemInstance enchantItem) {
      if (EnchantItemGroupsParser.getInstance().getScrollGroup(this._scrollGroupId) == null) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Unexistent enchant scroll group specified for enchant scroll: " + this.getId());
         return -1.0;
      } else {
         EnchantItemGroup group = EnchantItemGroupsParser.getInstance().getItemGroup(enchantItem.getItem(), this._scrollGroupId);
         if (group == null) {
            _log.log(
               Level.WARNING, this.getClass().getSimpleName() + ": Couldn't find enchant item group for scroll: " + this.getId() + " requested by: " + player
            );
            return -1.0;
         } else {
            double chance = group.getChance(enchantItem.getEnchantLevel());
            if (chance <= 0.0) {
               return -1.0;
            } else {
               double bonusRate = this.getBonusRate();
               double finalChance;
               if (Config.CUSTOM_ENCHANT_ITEMS_ENABLED) {
                  if (Config.ENCHANT_ITEMS_ID.containsKey(enchantItem.getItem().getId())) {
                     if (enchantItem.getEnchantLevel() < 3) {
                        finalChance = 100.0 + bonusRate;
                     } else {
                        finalChance = (double)Config.ENCHANT_ITEMS_ID.get(enchantItem.getItem().getId()).floatValue() + bonusRate;
                     }
                  } else {
                     finalChance = chance + bonusRate;
                  }
               } else {
                  finalChance = chance + bonusRate;
               }

               return finalChance <= 0.0 ? -1.0 : finalChance;
            }
         }
      }
   }

   public EnchantResultType calculateSuccess(Player player, ItemInstance enchantItem, EnchantItem supportItem) {
      if (!this.isValid(enchantItem, supportItem)) {
         return EnchantResultType.ERROR;
      } else if (EnchantItemGroupsParser.getInstance().getScrollGroup(this._scrollGroupId) == null) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Unexistent enchant scroll group specified for enchant scroll: " + this.getId());
         return EnchantResultType.ERROR;
      } else {
         EnchantItemGroup group = EnchantItemGroupsParser.getInstance().getItemGroup(enchantItem.getItem(), this._scrollGroupId);
         if (group == null) {
            _log.log(
               Level.WARNING, this.getClass().getSimpleName() + ": Couldn't find enchant item group for scroll: " + this.getId() + " requested by: " + player
            );
            return EnchantResultType.ERROR;
         } else {
            double chance = group.getChance(enchantItem.getEnchantLevel());
            if (chance <= 0.0) {
               return EnchantResultType.ERROR;
            } else {
               double bonusRate = this.getBonusRate();
               double supportBonusRate = supportItem != null && !this._isBlessed ? supportItem.getBonusRate() : 0.0;
               double finalChance;
               if (Config.CUSTOM_ENCHANT_ITEMS_ENABLED) {
                  if (Config.ENCHANT_ITEMS_ID.containsKey(enchantItem.getItem().getId())) {
                     if (enchantItem.getEnchantLevel() < 3) {
                        finalChance = 100.0 + bonusRate + supportBonusRate;
                     } else {
                        finalChance = (double)Config.ENCHANT_ITEMS_ID.get(enchantItem.getItem().getId()).floatValue() + bonusRate + supportBonusRate;
                     }
                  } else {
                     finalChance = chance + bonusRate + supportBonusRate;
                  }
               } else {
                  finalChance = chance + bonusRate + supportBonusRate;
               }

               if (finalChance <= 0.0) {
                  return EnchantResultType.ERROR;
               } else {
                  double random = 100.0 * Rnd.nextDouble();
                  boolean success = random < finalChance + (double)player.getPremiumBonus().getEnchantChance();
                  return success ? EnchantResultType.SUCCESS : EnchantResultType.FAILURE;
               }
            }
         }
      }
   }
}
