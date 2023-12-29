package l2e.gameserver.model.service.premium;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;

public class PremiumTemplate {
   private final int _id;
   private long _time;
   private final String _nameEn;
   private final String _nameRu;
   private final String _icon;
   private final List<PremiumGift> _list = new ArrayList<>();
   private final List<PremiumPrice> _price = new ArrayList<>();
   private double _exp = 1.0;
   private double _sp = 1.0;
   private double _epaulette = 1.0;
   private double _adena = 1.0;
   private double _spoil = 1.0;
   private double _items = 1.0;
   private double _dropRaids = 1.0;
   private double _dropEpics = 1.0;
   private double _questReward = 1.0;
   private double _questDrop = 1.0;
   private double _fishing = 1.0;
   private double _elementStones = 1.0;
   private double _sealStones = 1.0;
   private double _weight = 1.0;
   private double _fame = 1.0;
   private double _reflectionReduce = 1.0;
   private int _craft = 0;
   private int _masterwork = 0;
   private int _enchant = 0;
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
   private final boolean _isOnlineType;
   private final boolean _isPersonal;

   public PremiumTemplate(int id, String nameEn, String nameRu, String icon, boolean isOnlineType, boolean isPersonal) {
      this._id = id;
      this._nameEn = nameEn;
      this._nameRu = nameRu;
      this._icon = icon;
      this._isOnlineType = isOnlineType;
      this._isPersonal = isPersonal;
   }

   public int getId() {
      return this._id;
   }

   public long getTime() {
      return this._time;
   }

   public void setTime(long time) {
      this._time = time;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }

   public String getIcon() {
      return this._icon;
   }

   public boolean isOnlineType() {
      return this._isOnlineType;
   }

   public boolean isPersonal() {
      return this._isPersonal;
   }

   public double getExp() {
      return this._exp;
   }

   public double getSp() {
      return this._sp;
   }

   public double getEpaulette() {
      return this._epaulette;
   }

   public double getAdena() {
      return this._adena;
   }

   public double getSpoil() {
      return this._spoil;
   }

   public double getItems() {
      return this._items;
   }

   public double getElementStones() {
      return this._elementStones;
   }

   public double getWeight() {
      return this._weight;
   }

   public int getCraftChance() {
      return this._craft;
   }

   public int getMasterWorkChance() {
      return this._masterwork;
   }

   public int getEnchantChance() {
      return this._enchant;
   }

   public double getQuestReward() {
      return this._questReward;
   }

   public double getQuestDrop() {
      return this._questDrop;
   }

   public double getFishing() {
      return this._fishing;
   }

   public double getDropRaids() {
      return this._dropRaids;
   }

   public double getDropEpics() {
      return this._dropEpics;
   }

   public double getFameBonus() {
      return this._fame;
   }

   public double getReflectionReduce() {
      return this._reflectionReduce;
   }

   public double getSealStones() {
      return this._sealStones;
   }

   public void setRate(PremiumRates key, String value) {
      String[] param = value.split("-");
      switch(key) {
         case ADENA:
            this._adena = Double.parseDouble(value);
            break;
         case DROP_RAID:
            this._dropRaids = Double.parseDouble(value);
            break;
         case DROP_EPIC:
            this._dropEpics = Double.parseDouble(value);
            break;
         case FISHING:
            this._fishing = Double.parseDouble(value);
            break;
         case QUEST_REWARD:
            this._questReward = Double.parseDouble(value);
            break;
         case QUEST_DROP:
            this._questDrop = Double.parseDouble(value);
            break;
         case CRAFT:
            this._craft = Integer.parseInt(value);
            break;
         case DROP:
            this._items = Double.parseDouble(value);
            break;
         case ELEMENT_STONES:
            this._elementStones = Double.parseDouble(value);
            break;
         case SEAL_STONES:
            this._sealStones = Double.parseDouble(value);
            break;
         case EXP:
            this._exp = Double.parseDouble(value);
            break;
         case MASTERWORK_CRAFT:
            this._masterwork = Integer.parseInt(value);
            break;
         case ENCHANT:
            this._enchant = Integer.parseInt(value);
            break;
         case SIEGE:
            this._epaulette = Double.parseDouble(value);
            break;
         case SP:
            this._sp = Double.parseDouble(value);
            break;
         case SPOIL:
            this._spoil = Double.parseDouble(value);
            break;
         case WEIGHT_LIMIT:
            this._weight = Double.parseDouble(value);
            break;
         case FAME:
            this._fame = Double.parseDouble(value);
            break;
         case REFLECTION_REDUCE:
            this._reflectionReduce = Double.parseDouble(value);
            break;
         case MODIFIER_NOBLE_STONES:
            if (param.length == 2) {
               this._modNobleStonesMinCount = Double.parseDouble(param[0]);
               this._modNobleStonesMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_SEAL_STONES:
            if (param.length == 2) {
               this._modSealStonesMinCount = Double.parseDouble(param[0]);
               this._modSealStonesMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_LIFE_STONES:
            if (param.length == 2) {
               this._modLifeStonesMinCount = Double.parseDouble(param[0]);
               this._modLifeStonesMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ENCHANT_SCROLLS:
            if (param.length == 2) {
               this._modEnchantScrollsMinCount = Double.parseDouble(param[0]);
               this._modEnchantScrollsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_FORGOTTEN_SCROLLS:
            if (param.length == 2) {
               this._modForgottenScrollsMinCount = Double.parseDouble(param[0]);
               this._modForgottenScrollsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_MATERIALS:
            if (param.length == 2) {
               this._modMaterialsMinCount = Double.parseDouble(param[0]);
               this._modMaterialsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_RECIPES:
            if (param.length == 2) {
               this._modRepicesMinCount = Double.parseDouble(param[0]);
               this._modRepicesMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_BELTS:
            if (param.length == 2) {
               this._modBeltsMinCount = Double.parseDouble(param[0]);
               this._modBeltsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_BRACELETS:
            if (param.length == 2) {
               this._modBraceletsMinCount = Double.parseDouble(param[0]);
               this._modBraceletsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_CLOAKS:
            if (param.length == 2) {
               this._modCloaksMinCount = Double.parseDouble(param[0]);
               this._modCloaksMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_CODEX:
            if (param.length == 2) {
               this._modCodexBooksMinCount = Double.parseDouble(param[0]);
               this._modCodexBooksMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ATT_STONES:
            if (param.length == 2) {
               this._modAttStonesMinCount = Double.parseDouble(param[0]);
               this._modAttStonesMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ATT_CRYSTALS:
            if (param.length == 2) {
               this._modAttCrystalsMinCount = Double.parseDouble(param[0]);
               this._modAttCrystalsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ATT_JEWELS:
            if (param.length == 2) {
               this._modAttJewelsMinCount = Double.parseDouble(param[0]);
               this._modAttJewelsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ATT_ENERGY:
            if (param.length == 2) {
               this._modAttEnergyMinCount = Double.parseDouble(param[0]);
               this._modAttEnergyMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_WEAPONS:
            if (param.length == 2) {
               this._modWeaponsMinCount = Double.parseDouble(param[0]);
               this._modWeaponsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ARMORS:
            if (param.length == 2) {
               this._modArmorsMinCount = Double.parseDouble(param[0]);
               this._modArmorsMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MODIFIER_ACCESSORYES:
            if (param.length == 2) {
               this._modAccessoryesMinCount = Double.parseDouble(param[0]);
               this._modAccessoryesMaxCount = Double.parseDouble(param[1]);
            }
            break;
         case MAX_SPOIL_PER_ONE_GROUP:
            this._maxSpoilItemsFromOneGroup = Integer.parseInt(value);
            break;
         case MAX_DROP_PER_ONE_GROUP:
            this._maxDropItemsFromOneGroup = Integer.parseInt(value);
            break;
         case MAX_DROP_RAID_PER_ONE_GROUP:
            this._maxRaidDropItemsFromOneGroup = Integer.parseInt(value);
      }
   }

   public int getMaxSpoilItemsFromOneGroup() {
      return this._maxSpoilItemsFromOneGroup;
   }

   public int getMaxDropItemsFromOneGroup() {
      return this._maxDropItemsFromOneGroup;
   }

   public int getMaxRaidDropItemsFromOneGroup() {
      return this._maxRaidDropItemsFromOneGroup;
   }

   public double getMinSealStonesCount() {
      return this._modSealStonesMinCount;
   }

   public double getMaxSealStonesCount() {
      return this._modSealStonesMaxCount;
   }

   public double getMinLifeStonesCount() {
      return this._modLifeStonesMinCount;
   }

   public double getMaxLifeStonesCount() {
      return this._modLifeStonesMaxCount;
   }

   public double getMinEnchantScrollsCount() {
      return this._modEnchantScrollsMinCount;
   }

   public double getMaxEnchantScrollsCount() {
      return this._modEnchantScrollsMaxCount;
   }

   public double getMinForgottenScrollsCount() {
      return this._modForgottenScrollsMinCount;
   }

   public double getMaxForgottenScrollsCount() {
      return this._modForgottenScrollsMaxCount;
   }

   public double getMinMaterialsCount() {
      return this._modMaterialsMinCount;
   }

   public double getMaxMaterialsCount() {
      return this._modMaterialsMaxCount;
   }

   public double getMinRepicesCount() {
      return this._modRepicesMinCount;
   }

   public double getMaxRepicesCount() {
      return this._modRepicesMaxCount;
   }

   public double getMinBeltsCount() {
      return this._modBeltsMinCount;
   }

   public double getMaxBeltsCount() {
      return this._modBeltsMaxCount;
   }

   public double getMinBraceletsCount() {
      return this._modBraceletsMinCount;
   }

   public double getMaxBraceletsCount() {
      return this._modBraceletsMaxCount;
   }

   public double getMinCloaksCount() {
      return this._modCloaksMinCount;
   }

   public double getMaxCloaksCount() {
      return this._modCloaksMaxCount;
   }

   public double getMinCodexCount() {
      return this._modCodexBooksMinCount;
   }

   public double getMaxCodexCount() {
      return this._modCodexBooksMaxCount;
   }

   public double getMinAttStonesCount() {
      return this._modAttStonesMinCount;
   }

   public double getMaxAttStonesCount() {
      return this._modAttStonesMaxCount;
   }

   public double getMinAttCrystalsCount() {
      return this._modAttCrystalsMinCount;
   }

   public double getMaxAttCrystalsCount() {
      return this._modAttCrystalsMaxCount;
   }

   public double getMinAttJewelsCount() {
      return this._modAttJewelsMinCount;
   }

   public double getMaxAttJewelsCount() {
      return this._modAttJewelsMaxCount;
   }

   public double getMinAttEnergyCount() {
      return this._modAttEnergyMinCount;
   }

   public double getMaxAttEnergyCount() {
      return this._modAttEnergyMaxCount;
   }

   public double getMinWeaponsCount() {
      return this._modWeaponsMinCount;
   }

   public double getMaxWeaponsCount() {
      return this._modWeaponsMaxCount;
   }

   public double getMinArmorsCount() {
      return this._modArmorsMinCount;
   }

   public double getMaxArmorsCount() {
      return this._modArmorsMaxCount;
   }

   public double getMinAccessoryesCount() {
      return this._modAccessoryesMinCount;
   }

   public double getMaxAccessoryesCount() {
      return this._modAccessoryesMaxCount;
   }

   public double getMinNobleStonesCount() {
      return this._modNobleStonesMinCount;
   }

   public double getMaxNobleStonesCount() {
      return this._modNobleStonesMaxCount;
   }

   public List<PremiumGift> getGifts() {
      return this._list;
   }

   public void addGift(PremiumGift gift) {
      this._list.add(gift);
   }

   public List<PremiumPrice> getPriceList() {
      return this._price;
   }

   public void addPrice(PremiumPrice price) {
      this._price.add(price);
   }
}
