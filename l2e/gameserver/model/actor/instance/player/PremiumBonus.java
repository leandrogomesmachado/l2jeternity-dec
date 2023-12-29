package l2e.gameserver.model.actor.instance.player;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.VipManager;
import l2e.gameserver.model.actor.templates.player.vip.VipTemplate;

public class PremiumBonus {
   private int _premiumId = 0;
   private double _rateXp = 1.0;
   private double _rateSp = 1.0;
   private double _rateFishing = 1.0;
   private double _dropSiege = 1.0;
   private double _dropElementStones = 1.0;
   private double _dropSealStones = 1.0;
   private double _questRewardRate = 1.0;
   private double _questDropRate = 1.0;
   private double _dropAdena = 1.0;
   private double _dropItems = 1.0;
   private double _dropRaids = 1.0;
   private double _dropEpics = 1.0;
   private double _dropSpoil = 1.0;
   private double _weight = 1.0;
   private double _fame = 1.0;
   private double _reflectionReduce = 1.0;
   private int _craftChance = 0;
   private int _masterWorkChance = 0;
   private int _enchantChance = 0;
   private double _modNobleStonesMinCount = Config.RATE_NOBLE_STONES_COUNT_MIN;
   private double _modSealStonesMinCount = Config.RATE_SEAL_STONES_COUNT_MIN;
   private double _modLifeStonesMinCount = Config.RATE_LIFE_STONES_COUNT_MIN;
   private double _modEnchantScrollsMinCount = Config.RATE_ENCHANT_SCROLLS_COUNT_MIN;
   private double _modForgottenScrollsMinCount = Config.RATE_FORGOTTEN_SCROLLS_COUNT_MIN;
   private double _modMaterialsMinCount = Config.RATE_KEY_MATHETIRALS_COUNT_MIN;
   private double _modRepicesMinCount = Config.RATE_RECEPIES_COUNT_MIN;
   private double _modBeltsMinCount = Config.RATE_BELTS_COUNT_MIN;
   private double _modBraceletsMinCount = Config.RATE_BRACELETS_COUNT_MIN;
   private double _modCloaksMinCount = Config.RATE_CLOAKS_COUNT_MIN;
   private double _modCodexBooksMinCount = Config.RATE_CODEX_BOOKS_COUNT_MIN;
   private double _modAttStonesMinCount = Config.RATE_ATTRIBUTE_STONES_COUNT_MIN;
   private double _modAttCrystalsMinCount = Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN;
   private double _modAttJewelsMinCount = Config.RATE_ATTRIBUTE_JEWELS_COUNT_MIN;
   private double _modAttEnergyMinCount = Config.RATE_ATTRIBUTE_ENERGY_COUNT_MIN;
   private double _modWeaponsMinCount = Config.RATE_WEAPONS_COUNT_MIN;
   private double _modArmorsMinCount = Config.RATE_ARMOR_COUNT_MIN;
   private double _modAccessoryesMinCount = Config.RATE_ACCESSORY_COUNT_MIN;
   private double _modNobleStonesMaxCount = Config.RATE_NOBLE_STONES_COUNT_MAX;
   private double _modSealStonesMaxCount = Config.RATE_SEAL_STONES_COUNT_MAX;
   private double _modLifeStonesMaxCount = Config.RATE_LIFE_STONES_COUNT_MAX;
   private double _modEnchantScrollsMaxCount = Config.RATE_ENCHANT_SCROLLS_COUNT_MAX;
   private double _modForgottenScrollsMaxCount = Config.RATE_FORGOTTEN_SCROLLS_COUNT_MAX;
   private double _modMaterialsMaxCount = Config.RATE_KEY_MATHETIRALS_COUNT_MAX;
   private double _modRepicesMaxCount = Config.RATE_RECEPIES_COUNT_MAX;
   private double _modBeltsMaxCount = Config.RATE_BELTS_COUNT_MAX;
   private double _modBraceletsMaxCount = Config.RATE_BRACELETS_COUNT_MAX;
   private double _modCloaksMaxCount = Config.RATE_CLOAKS_COUNT_MAX;
   private double _modCodexBooksMaxCount = Config.RATE_CODEX_BOOKS_COUNT_MAX;
   private double _modAttStonesMaxCount = Config.RATE_ATTRIBUTE_STONES_COUNT_MAX;
   private double _modAttCrystalsMaxCount = Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX;
   private double _modAttJewelsMaxCount = Config.RATE_ATTRIBUTE_JEWELS_COUNT_MAX;
   private double _modAttEnergyMaxCount = Config.RATE_ATTRIBUTE_ENERGY_COUNT_MAX;
   private double _modWeaponsMaxCount = Config.RATE_WEAPONS_COUNT_MAX;
   private double _modArmorsMaxCount = Config.RATE_ARMOR_COUNT_MAX;
   private double _modAccessoryesMaxCount = Config.RATE_ACCESSORY_COUNT_MAX;
   private int _maxSpoilItemsFromOneGroup = Config.MAX_SPOIL_ITEMS_FROM_ONE_GROUP;
   private int _maxDropItemsFromOneGroup = Config.MAX_DROP_ITEMS_FROM_ONE_GROUP;
   private int _maxRaidDropItemsFromOneGroup = Config.MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS;
   private boolean _isOnlineType;
   private boolean _isPersonal;
   private long _onlineTime;
   private boolean _isActive;
   private VipTemplate _vipTemplate = null;

   public int getPremiumId() {
      return this._premiumId;
   }

   public void setPremiumId(int premiumId) {
      this._premiumId = premiumId;
   }

   public double getRateXp() {
      return this._rateXp * (this._vipTemplate != null ? this._vipTemplate.getExpRate() : 1.0);
   }

   public void setRateXp(double rateXp) {
      this._rateXp = rateXp;
   }

   public double getRateSp() {
      return this._rateSp * (this._vipTemplate != null ? this._vipTemplate.getSpRate() : 1.0);
   }

   public void setRateSp(double rateSp) {
      this._rateSp = rateSp;
   }

   public double getQuestRewardRate() {
      return this._questRewardRate;
   }

   public void setQuestRewardRate(double questRewardRate) {
      this._questRewardRate = questRewardRate;
   }

   public double getQuestDropRate() {
      return this._questDropRate;
   }

   public void setQuestDropRate(double questDropRate) {
      this._questDropRate = questDropRate;
   }

   public double getDropAdena() {
      return this._dropAdena * (this._vipTemplate != null ? this._vipTemplate.getAdenaRate() : 1.0);
   }

   public void setDropAdena(double dropAdena) {
      this._dropAdena = dropAdena;
   }

   public double getDropItems() {
      return this._dropItems * (this._vipTemplate != null ? this._vipTemplate.getDropRate() : 1.0);
   }

   public void setDropItems(double dropItems) {
      this._dropItems = dropItems;
   }

   public double getDropSpoil() {
      return this._dropSpoil * (this._vipTemplate != null ? this._vipTemplate.getSpoilRate() : 1.0);
   }

   public void setDropSpoil(double dropSpoil) {
      this._dropSpoil = dropSpoil;
   }

   public double getDropSiege() {
      return this._dropSiege * (this._vipTemplate != null ? this._vipTemplate.getEpRate() : 1.0);
   }

   public void setDropSiege(double dropSiege) {
      this._dropSiege = dropSiege;
   }

   public double getDropElementStones() {
      return this._dropElementStones;
   }

   public void setDropElementStones(double ElementStones) {
      this._dropElementStones = ElementStones;
   }

   public double getWeight() {
      return this._weight;
   }

   public void setWeight(double weight) {
      this._weight = weight;
   }

   public int getCraftChance() {
      return this._craftChance;
   }

   public void setCraftChance(int craftChance) {
      this._craftChance = craftChance;
   }

   public int getMasterWorkChance() {
      return this._masterWorkChance;
   }

   public void setMasterWorkChance(int masterWorkChance) {
      this._masterWorkChance = masterWorkChance;
   }

   public double getFishingRate() {
      return this._rateFishing;
   }

   public void setFishingRate(double fishingRate) {
      this._rateFishing = fishingRate;
   }

   public double getDropRaids() {
      return this._dropRaids * (this._vipTemplate != null ? this._vipTemplate.getDropRaidRate() : 1.0);
   }

   public void setDropRaid(double dropRaids) {
      this._dropRaids = dropRaids;
   }

   public double getDropEpics() {
      return this._dropEpics * (this._vipTemplate != null ? this._vipTemplate.getDropRaidRate() : 1.0);
   }

   public void setDropEpic(double dropEpics) {
      this._dropEpics = dropEpics;
   }

   public int getEnchantChance() {
      return this._enchantChance + (this._vipTemplate != null ? this._vipTemplate.getEnchantChance() : 0);
   }

   public void setEnchantChance(int enchantChance) {
      this._enchantChance = enchantChance;
   }

   public double getFameBonus() {
      return this._fame;
   }

   public void setFameBonus(double fame) {
      this._fame = fame;
   }

   public double getReflectionReduce() {
      return this._reflectionReduce;
   }

   public void setReflectionReduce(double reflectionReduce) {
      this._reflectionReduce = reflectionReduce;
   }

   public boolean isOnlineType() {
      return this._isOnlineType;
   }

   public void setOnlineType(boolean isOnlineType) {
      this._isOnlineType = isOnlineType;
   }

   public long getOnlineTime() {
      return this._onlineTime;
   }

   public void setOnlineTime(long onlineTime) {
      this._onlineTime = onlineTime;
   }

   public boolean isActive() {
      return this._isActive;
   }

   public void setActivate(boolean isActive) {
      this._isActive = isActive;
   }

   public double getDropSealStones() {
      return this._dropSealStones;
   }

   public void setDropSealStones(double SealStones) {
      this._dropSealStones = SealStones;
   }

   public double getMinNobleStonesCount() {
      return this._modNobleStonesMinCount;
   }

   public void setNobleStonesMinCount(double value) {
      this._modNobleStonesMinCount = value;
   }

   public double getMaxNobleStonesCount() {
      return this._modNobleStonesMaxCount;
   }

   public void setNobleStonesMaxCount(double value) {
      this._modNobleStonesMaxCount = value;
   }

   public double getMinSealStonesCount() {
      return this._modSealStonesMinCount;
   }

   public void setSealStonesMinCount(double value) {
      this._modSealStonesMinCount = value;
   }

   public double getMaxSealStonesCount() {
      return this._modSealStonesMaxCount;
   }

   public void setSealStonesMaxCount(double value) {
      this._modSealStonesMaxCount = value;
   }

   public double getMinLifeStonesCount() {
      return this._modLifeStonesMinCount;
   }

   public void setLifeStonesMinCount(double value) {
      this._modLifeStonesMinCount = value;
   }

   public double getMaxLifeStonesCount() {
      return this._modLifeStonesMaxCount;
   }

   public void setLifeStonesMaxCount(double value) {
      this._modLifeStonesMaxCount = value;
   }

   public double getMinEnchantScrollsCount() {
      return this._modEnchantScrollsMinCount;
   }

   public void setEnchantScrollsMinCount(double value) {
      this._modEnchantScrollsMinCount = value;
   }

   public double getMaxEnchantScrollsCount() {
      return this._modEnchantScrollsMaxCount;
   }

   public void setEnchantScrollsMaxCount(double value) {
      this._modEnchantScrollsMaxCount = value;
   }

   public double getMinForgottenScrollsCount() {
      return this._modForgottenScrollsMinCount;
   }

   public void setForgottenScrollsMinCount(double value) {
      this._modForgottenScrollsMinCount = value;
   }

   public double getMaxForgottenScrollsCount() {
      return this._modForgottenScrollsMaxCount;
   }

   public void setForgottenScrollsMaxCount(double value) {
      this._modForgottenScrollsMaxCount = value;
   }

   public double getMinMaterialsCount() {
      return this._modMaterialsMinCount;
   }

   public void setMaterialsMinCount(double value) {
      this._modMaterialsMinCount = value;
   }

   public double getMaxMaterialsCount() {
      return this._modMaterialsMaxCount;
   }

   public void setMaterialsMaxCount(double value) {
      this._modMaterialsMaxCount = value;
   }

   public double getMinRepicesCount() {
      return this._modRepicesMinCount;
   }

   public void setRepicesMinCount(double value) {
      this._modRepicesMinCount = value;
   }

   public double getMaxRepicesCount() {
      return this._modRepicesMaxCount;
   }

   public void setRepicesMaxCount(double value) {
      this._modRepicesMaxCount = value;
   }

   public double getMinBeltsCount() {
      return this._modBeltsMinCount;
   }

   public void setBeltsMinCount(double value) {
      this._modBeltsMinCount = value;
   }

   public double getMaxBeltsCount() {
      return this._modBeltsMaxCount;
   }

   public void setBeltsMaxCount(double value) {
      this._modBeltsMaxCount = value;
   }

   public double getMinBraceletsCount() {
      return this._modBraceletsMinCount;
   }

   public void setBraceletsMinCount(double value) {
      this._modBraceletsMinCount = value;
   }

   public double getMaxBraceletsCount() {
      return this._modBraceletsMaxCount;
   }

   public void setBraceletsMaxCount(double value) {
      this._modBraceletsMaxCount = value;
   }

   public double getMinCloaksCount() {
      return this._modCloaksMinCount;
   }

   public void setCloaksMinCount(double value) {
      this._modCloaksMinCount = value;
   }

   public double getMaxCloaksCount() {
      return this._modCloaksMaxCount;
   }

   public void setCloaksMaxCount(double value) {
      this._modCloaksMaxCount = value;
   }

   public double getMinCodexCount() {
      return this._modCodexBooksMinCount;
   }

   public void setCodexMinCount(double value) {
      this._modCodexBooksMinCount = value;
   }

   public double getMaxCodexCount() {
      return this._modCodexBooksMaxCount;
   }

   public void setCodexMaxCount(double value) {
      this._modCodexBooksMaxCount = value;
   }

   public double getMinAttStonesCount() {
      return this._modAttStonesMinCount;
   }

   public void setAttStonesMinCount(double value) {
      this._modAttStonesMinCount = value;
   }

   public double getMaxAttStonesCount() {
      return this._modAttStonesMaxCount;
   }

   public void setAttStonesMaxCount(double value) {
      this._modAttStonesMaxCount = value;
   }

   public double getMinAttCrystalsCount() {
      return this._modAttCrystalsMinCount;
   }

   public void setAttCrystalsMinCount(double value) {
      this._modAttCrystalsMinCount = value;
   }

   public double getMaxAttCrystalsCount() {
      return this._modAttCrystalsMaxCount;
   }

   public void setAttCrystalsMaxCount(double value) {
      this._modAttCrystalsMaxCount = value;
   }

   public double getMinAttJewelsCount() {
      return this._modAttJewelsMinCount;
   }

   public void setAttJewelsMinCount(double value) {
      this._modAttJewelsMinCount = value;
   }

   public double getMaxAttJewelsCount() {
      return this._modAttJewelsMaxCount;
   }

   public void setAttJewelsMaxCount(double value) {
      this._modAttJewelsMaxCount = value;
   }

   public double getMinAttEnergyCount() {
      return this._modAttEnergyMinCount;
   }

   public void setAttEnergyMinCount(double value) {
      this._modAttEnergyMinCount = value;
   }

   public double getMaxAttEnergyCount() {
      return this._modAttEnergyMaxCount;
   }

   public void setAttEnergyMaxCount(double value) {
      this._modAttEnergyMaxCount = value;
   }

   public double getMinWeaponsCount() {
      return this._modWeaponsMinCount;
   }

   public void setWeaponsMinCount(double value) {
      this._modWeaponsMinCount = value;
   }

   public double getMaxWeaponsCount() {
      return this._modWeaponsMaxCount;
   }

   public void setWeaponsMaxCount(double value) {
      this._modWeaponsMaxCount = value;
   }

   public double getMinArmorsCount() {
      return this._modArmorsMinCount;
   }

   public void setArmorsMinCount(double value) {
      this._modArmorsMinCount = value;
   }

   public double getMaxArmorsCount() {
      return this._modArmorsMaxCount;
   }

   public void setArmorsMaxCount(double value) {
      this._modArmorsMaxCount = value;
   }

   public double getMinAccessoryesCount() {
      return this._modAccessoryesMinCount;
   }

   public void setAccessoryesMinCount(double value) {
      this._modAccessoryesMinCount = value;
   }

   public double getMaxAccessoryesCount() {
      return this._modAccessoryesMaxCount;
   }

   public void setAccessoryesMaxCount(double value) {
      this._modAccessoryesMaxCount = value;
   }

   public void setMaxSpoilItemsFromOneGroup(int value) {
      this._maxSpoilItemsFromOneGroup = value;
   }

   public int getMaxSpoilItemsFromOneGroup() {
      return this._maxSpoilItemsFromOneGroup;
   }

   public void setMaxDropItemsFromOneGroup(int value) {
      this._maxDropItemsFromOneGroup = value;
   }

   public int getMaxDropItemsFromOneGroup() {
      return this._maxDropItemsFromOneGroup;
   }

   public void setMaxRaidDropItemsFromOneGroup(int value) {
      this._maxRaidDropItemsFromOneGroup = value;
   }

   public int getMaxRaidDropItemsFromOneGroup() {
      return this._maxRaidDropItemsFromOneGroup;
   }

   public void setIsPersonal(boolean personal) {
      this._isPersonal = personal;
   }

   public boolean isPersonal() {
      return this._isPersonal;
   }

   public void setVipTemplate(int level) {
      this._vipTemplate = VipManager.getInstance().getVipLevel(level);
   }

   public VipTemplate getVipTemplate() {
      return this._vipTemplate;
   }
}
