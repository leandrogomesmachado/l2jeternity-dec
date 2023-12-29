package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.player.PremiumBonus;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import l2e.gameserver.model.service.premium.PremiumGift;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrPremiumState;

public class PremuimAccountTask implements Runnable {
   private final Player _player;

   public PremuimAccountTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         PremiumBonus bonus = this._player.getPremiumBonus();
         int premiumId = bonus.getPremiumId();
         bonus.setPremiumId(0);
         bonus.setRateXp(1.0);
         bonus.setRateSp(1.0);
         bonus.setDropSiege(1.0);
         bonus.setDropElementStones(1.0);
         bonus.setDropSealStones(1.0);
         bonus.setQuestRewardRate(1.0);
         bonus.setQuestDropRate(1.0);
         bonus.setDropAdena(1.0);
         bonus.setDropItems(1.0);
         bonus.setDropRaid(1.0);
         bonus.setDropEpic(1.0);
         bonus.setDropSpoil(1.0);
         bonus.setWeight(1.0);
         bonus.setCraftChance(0);
         bonus.setMasterWorkChance(0);
         bonus.setEnchantChance(0);
         bonus.setFishingRate(1.0);
         bonus.setFameBonus(1.0);
         bonus.setReflectionReduce(1.0);
         bonus.setNobleStonesMinCount(Config.RATE_NOBLE_STONES_COUNT_MIN);
         bonus.setNobleStonesMaxCount(Config.RATE_NOBLE_STONES_COUNT_MAX);
         bonus.setSealStonesMinCount(Config.RATE_SEAL_STONES_COUNT_MIN);
         bonus.setSealStonesMaxCount(Config.RATE_SEAL_STONES_COUNT_MAX);
         bonus.setLifeStonesMinCount(Config.RATE_LIFE_STONES_COUNT_MIN);
         bonus.setLifeStonesMaxCount(Config.RATE_LIFE_STONES_COUNT_MAX);
         bonus.setEnchantScrollsMinCount(Config.RATE_ENCHANT_SCROLLS_COUNT_MIN);
         bonus.setEnchantScrollsMaxCount(Config.RATE_ENCHANT_SCROLLS_COUNT_MAX);
         bonus.setForgottenScrollsMinCount(Config.RATE_FORGOTTEN_SCROLLS_COUNT_MIN);
         bonus.setForgottenScrollsMaxCount(Config.RATE_FORGOTTEN_SCROLLS_COUNT_MAX);
         bonus.setMaterialsMinCount(Config.RATE_KEY_MATHETIRALS_COUNT_MIN);
         bonus.setMaterialsMaxCount(Config.RATE_KEY_MATHETIRALS_COUNT_MAX);
         bonus.setRepicesMinCount(Config.RATE_RECEPIES_COUNT_MIN);
         bonus.setRepicesMaxCount(Config.RATE_RECEPIES_COUNT_MAX);
         bonus.setBeltsMinCount(Config.RATE_BELTS_COUNT_MIN);
         bonus.setBeltsMaxCount(Config.RATE_BELTS_COUNT_MAX);
         bonus.setBraceletsMinCount(Config.RATE_BRACELETS_COUNT_MIN);
         bonus.setBraceletsMaxCount(Config.RATE_BRACELETS_COUNT_MAX);
         bonus.setCloaksMinCount(Config.RATE_CLOAKS_COUNT_MIN);
         bonus.setCloaksMaxCount(Config.RATE_CLOAKS_COUNT_MAX);
         bonus.setCodexMinCount(Config.RATE_CODEX_BOOKS_COUNT_MIN);
         bonus.setCodexMaxCount(Config.RATE_CODEX_BOOKS_COUNT_MAX);
         bonus.setAttStonesMinCount(Config.RATE_ATTRIBUTE_STONES_COUNT_MIN);
         bonus.setAttStonesMaxCount(Config.RATE_ATTRIBUTE_STONES_COUNT_MAX);
         bonus.setAttCrystalsMinCount(Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN);
         bonus.setAttCrystalsMaxCount(Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX);
         bonus.setAttJewelsMinCount(Config.RATE_ATTRIBUTE_JEWELS_COUNT_MIN);
         bonus.setAttJewelsMaxCount(Config.RATE_ATTRIBUTE_JEWELS_COUNT_MAX);
         bonus.setAttEnergyMinCount(Config.RATE_ATTRIBUTE_ENERGY_COUNT_MIN);
         bonus.setAttEnergyMaxCount(Config.RATE_ATTRIBUTE_ENERGY_COUNT_MAX);
         bonus.setWeaponsMinCount(Config.RATE_WEAPONS_COUNT_MIN);
         bonus.setWeaponsMaxCount(Config.RATE_WEAPONS_COUNT_MAX);
         bonus.setArmorsMinCount(Config.RATE_ARMOR_COUNT_MIN);
         bonus.setArmorsMaxCount(Config.RATE_ARMOR_COUNT_MAX);
         bonus.setAccessoryesMinCount(Config.RATE_ACCESSORY_COUNT_MIN);
         bonus.setAccessoryesMaxCount(Config.RATE_ACCESSORY_COUNT_MAX);
         bonus.setMaxSpoilItemsFromOneGroup(Config.MAX_SPOIL_ITEMS_FROM_ONE_GROUP);
         bonus.setMaxDropItemsFromOneGroup(Config.MAX_DROP_ITEMS_FROM_ONE_GROUP);
         bonus.setMaxRaidDropItemsFromOneGroup(Config.MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS);
         bonus.setOnlineType(false);
         bonus.setActivate(false);
         if (bonus.isPersonal()) {
            CharacterPremiumDAO.getInstance().disablePersonal(this._player);
            bonus.setIsPersonal(false);
         } else {
            CharacterPremiumDAO.getInstance().disable(this._player);
         }

         this._player.sendPacket(new ExBrPremiumState(this._player.getObjectId(), 0));
         this._player.sendPacket(SystemMessageId.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED);
         if (Config.PC_BANG_ENABLED && Config.PC_BANG_ONLY_FOR_PREMIUM) {
            this._player.stopPcBangPointsTask();
         }

         PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(premiumId);
         if (template != null) {
            for(PremiumGift gift : template.getGifts()) {
               if (gift != null && gift.isRemovable()) {
                  if (this._player.getInventory().getItemByItemId(gift.getId()) != null) {
                     this._player.getInventory().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium");
                  } else if (this._player.getWarehouse().getItemByItemId(gift.getId()) != null) {
                     this._player.getWarehouse().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium");
                  }
               }
            }
         }

         if (this._player.isInParty()) {
            this._player.getParty().recalculatePartyData();
         }

         if (FarmSettings.ALLOW_AUTO_FARM && FarmSettings.PREMIUM_FARM_FREE && !this._player.getFarmSystem().isActiveFarmTask()) {
            this._player.getFarmSystem().stopFarmTask(false);
         }
      }
   }
}
