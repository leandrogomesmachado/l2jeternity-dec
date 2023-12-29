package l2e.gameserver.model.reward;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;

public class RewardItemRates {
   public static double getMinCountModifier(Player player, Item item, boolean allowModifier) {
      double modifier = 1.0;
      if (!allowModifier) {
         return modifier;
      } else {
         if (item.isNobleStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinNobleStonesCount() : Config.RATE_NOBLE_STONES_COUNT_MIN;
         }

         if (item.isLifeStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinLifeStonesCount() : Config.RATE_LIFE_STONES_COUNT_MIN;
         }

         if (item.isEnchantScroll()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinEnchantScrollsCount() : Config.RATE_ENCHANT_SCROLLS_COUNT_MIN;
         }

         if (item.isForgottenScroll()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinForgottenScrollsCount() : Config.RATE_FORGOTTEN_SCROLLS_COUNT_MIN;
         }

         if (item.isKeyMatherial()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinMaterialsCount() : Config.RATE_KEY_MATHETIRALS_COUNT_MIN;
         }

         if (item.isRecipe()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinRepicesCount() : Config.RATE_RECEPIES_COUNT_MIN;
         }

         if (item.isBelt()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinBeltsCount() : Config.RATE_BELTS_COUNT_MIN;
         }

         if (item.isBracelet()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinBraceletsCount() : Config.RATE_BRACELETS_COUNT_MIN;
         }

         if (item.isCloak()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinCloaksCount() : Config.RATE_CLOAKS_COUNT_MIN;
         }

         if (item.isCodexBook()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinCodexCount() : Config.RATE_CODEX_BOOKS_COUNT_MIN;
         }

         if (item.isAttributeStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinAttStonesCount() : Config.RATE_ATTRIBUTE_STONES_COUNT_MIN;
         }

         if (item.isAttributeCrystal()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinAttCrystalsCount() : Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN;
         }

         if (item.isAttributeJewel()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinAttJewelsCount() : Config.RATE_ATTRIBUTE_JEWELS_COUNT_MIN;
         }

         if (item.isAttributeEnergy()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinAttEnergyCount() : Config.RATE_ATTRIBUTE_ENERGY_COUNT_MIN;
         }

         if (item.isWeapon()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinWeaponsCount() : Config.RATE_WEAPONS_COUNT_MIN;
         }

         if (item.isArmor()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinArmorsCount() : Config.RATE_ARMOR_COUNT_MIN;
         }

         if (item.isAccessory()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinAccessoryesCount() : Config.RATE_ACCESSORY_COUNT_MIN;
         }

         if (item.isSealStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMinSealStonesCount() : Config.RATE_SEAL_STONES_COUNT_MIN;
            if (player != null) {
               modifier *= player.isInParty() && Config.PREMIUM_PARTY_RATE
                  ? player.getParty().getDropSealStones()
                  : player.getPremiumBonus().getDropSealStones();
            }
         }

         return modifier;
      }
   }

   public static double getMaxCountModifier(Player player, Item item, boolean allowModifier) {
      double modifier = 1.0;
      if (!allowModifier) {
         return modifier;
      } else {
         if (item.isNobleStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxNobleStonesCount() : Config.RATE_NOBLE_STONES_COUNT_MAX;
         }

         if (item.isLifeStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxLifeStonesCount() : Config.RATE_LIFE_STONES_COUNT_MAX;
         }

         if (item.isEnchantScroll()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxEnchantScrollsCount() : Config.RATE_ENCHANT_SCROLLS_COUNT_MAX;
         }

         if (item.isForgottenScroll()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxForgottenScrollsCount() : Config.RATE_FORGOTTEN_SCROLLS_COUNT_MAX;
         }

         if (item.isKeyMatherial()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxMaterialsCount() : Config.RATE_KEY_MATHETIRALS_COUNT_MAX;
         }

         if (item.isRecipe()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxRepicesCount() : Config.RATE_RECEPIES_COUNT_MAX;
         }

         if (item.isBelt()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxBeltsCount() : Config.RATE_BELTS_COUNT_MAX;
         }

         if (item.isBracelet()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxBraceletsCount() : Config.RATE_BRACELETS_COUNT_MAX;
         }

         if (item.isCloak()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxCloaksCount() : Config.RATE_CLOAKS_COUNT_MAX;
         }

         if (item.isCodexBook()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxCodexCount() : Config.RATE_CODEX_BOOKS_COUNT_MAX;
         }

         if (item.isAttributeStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxAttStonesCount() : Config.RATE_ATTRIBUTE_STONES_COUNT_MAX;
            if (player != null) {
               modifier *= player.isInParty() && Config.PREMIUM_PARTY_RATE
                  ? player.getParty().getDropElementStones()
                  : player.getPremiumBonus().getDropElementStones();
            }
         }

         if (item.isAttributeCrystal()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxAttCrystalsCount() : Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX;
            if (player != null) {
               modifier *= player.isInParty() && Config.PREMIUM_PARTY_RATE
                  ? player.getParty().getDropElementStones()
                  : player.getPremiumBonus().getDropElementStones();
            }
         }

         if (item.isAttributeJewel()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxAttJewelsCount() : Config.RATE_ATTRIBUTE_JEWELS_COUNT_MAX;
         }

         if (item.isAttributeEnergy()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxAttEnergyCount() : Config.RATE_ATTRIBUTE_ENERGY_COUNT_MAX;
         }

         if (item.isWeapon()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxWeaponsCount() : Config.RATE_WEAPONS_COUNT_MAX;
         }

         if (item.isArmor()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxArmorsCount() : Config.RATE_ARMOR_COUNT_MAX;
         }

         if (item.isAccessory()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxAccessoryesCount() : Config.RATE_ACCESSORY_COUNT_MAX;
         }

         if (item.isSealStone()) {
            modifier *= player.hasPremiumBonus() ? player.getPremiumBonus().getMaxSealStonesCount() : Config.RATE_SEAL_STONES_COUNT_MAX;
            if (player != null) {
               modifier *= player.isInParty() && Config.PREMIUM_PARTY_RATE
                  ? player.getParty().getDropSealStones()
                  : player.getPremiumBonus().getDropSealStones();
            }
         }

         return modifier;
      }
   }
}
