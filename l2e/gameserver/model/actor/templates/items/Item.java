package l2e.gameserver.model.actor.templates.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ActionType;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.items.type.ItemType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.skills.conditions.ConditionLogicOr;
import l2e.gameserver.model.skills.conditions.ConditionPetType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class Item implements IIdentifiable {
   protected static final Logger _log = Logger.getLogger(Item.class.getName());
   public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
   public static final int TYPE1_SHIELD_ARMOR = 1;
   public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
   public static final int TYPE2_WEAPON = 0;
   public static final int TYPE2_SHIELD_ARMOR = 1;
   public static final int TYPE2_ACCESSORY = 2;
   public static final int TYPE2_QUEST = 3;
   public static final int TYPE2_MONEY = 4;
   public static final int TYPE2_OTHER = 5;
   public static final int STRIDER = 1;
   public static final int GROWN_UP_WOLF_GROUP = 2;
   public static final int HATCHLING_GROUP = 4;
   public static final int ALL_WOLF_GROUP = 8;
   public static final int BABY_PET_GROUP = 22;
   public static final int UPGRADE_BABY_PET_GROUP = 50;
   public static final int ITEM_EQUIP_PET_GROUP = 100;
   public static final int SLOT_NONE = 0;
   public static final int SLOT_UNDERWEAR = 1;
   public static final int SLOT_R_EAR = 2;
   public static final int SLOT_L_EAR = 4;
   public static final int SLOT_LR_EAR = 6;
   public static final int SLOT_NECK = 8;
   public static final int SLOT_R_FINGER = 16;
   public static final int SLOT_L_FINGER = 32;
   public static final int SLOT_LR_FINGER = 48;
   public static final int SLOT_HEAD = 64;
   public static final int SLOT_R_HAND = 128;
   public static final int SLOT_L_HAND = 256;
   public static final int SLOT_GLOVES = 512;
   public static final int SLOT_CHEST = 1024;
   public static final int SLOT_LEGS = 2048;
   public static final int SLOT_FEET = 4096;
   public static final int SLOT_BACK = 8192;
   public static final int SLOT_LR_HAND = 16384;
   public static final int SLOT_FULL_ARMOR = 32768;
   public static final int SLOT_HAIR = 65536;
   public static final int SLOT_ALLDRESS = 131072;
   public static final int SLOT_HAIR2 = 262144;
   public static final int SLOT_HAIRALL = 524288;
   public static final int SLOT_R_BRACELET = 1048576;
   public static final int SLOT_L_BRACELET = 2097152;
   public static final int SLOT_DECO = 4194304;
   public static final int SLOT_BELT = 268435456;
   public static final int SLOT_WOLF = -100;
   public static final int SLOT_HATCHLING = -101;
   public static final int SLOT_STRIDER = -102;
   public static final int SLOT_BABYPET = -103;
   public static final int SLOT_GREATWOLF = -104;
   public static final int SLOT_MULTI_ALLWEAPON = 16512;
   public static final int MATERIAL_STEEL = 0;
   public static final int MATERIAL_FINE_STEEL = 1;
   public static final int MATERIAL_BLOOD_STEEL = 2;
   public static final int MATERIAL_BRONZE = 3;
   public static final int MATERIAL_SILVER = 4;
   public static final int MATERIAL_GOLD = 5;
   public static final int MATERIAL_MITHRIL = 6;
   public static final int MATERIAL_ORIHARUKON = 7;
   public static final int MATERIAL_PAPER = 8;
   public static final int MATERIAL_WOOD = 9;
   public static final int MATERIAL_CLOTH = 10;
   public static final int MATERIAL_LEATHER = 11;
   public static final int MATERIAL_BONE = 12;
   public static final int MATERIAL_HORN = 13;
   public static final int MATERIAL_DAMASCUS = 14;
   public static final int MATERIAL_ADAMANTAITE = 15;
   public static final int MATERIAL_CHRYSOLITE = 16;
   public static final int MATERIAL_CRYSTAL = 17;
   public static final int MATERIAL_LIQUID = 18;
   public static final int MATERIAL_SCALE_OF_DRAGON = 19;
   public static final int MATERIAL_DYESTUFF = 20;
   public static final int MATERIAL_COBWEB = 21;
   public static final int MATERIAL_SEED = 22;
   public static final int MATERIAL_FISH = 23;
   public static final int MATERIAL_RUNE_XP = 24;
   public static final int MATERIAL_RUNE_SP = 25;
   public static final int MATERIAL_RUNE_PENALTY = 32;
   public static final int CRYSTAL_NONE = 0;
   public static final int CRYSTAL_D = 1;
   public static final int CRYSTAL_C = 2;
   public static final int CRYSTAL_B = 3;
   public static final int CRYSTAL_A = 4;
   public static final int CRYSTAL_S = 5;
   public static final int CRYSTAL_S80 = 6;
   public static final int CRYSTAL_S84 = 7;
   public static final int ITEM_ID_ADENA = 57;
   private static final int[] CRYSTAL_ITEM_ID = new int[]{0, 1458, 1459, 1460, 1461, 1462, 1462, 1462};
   private static final int[] CRYSTAL_ENCHANT_BONUS_ARMOR = new int[]{0, 11, 6, 11, 19, 25, 25, 25};
   private static final int[] CRYSTAL_ENCHANT_BONUS_WEAPON = new int[]{0, 90, 45, 67, 144, 250, 250, 250};
   private final int _itemId;
   private final int _displayId;
   private final String _nameEn;
   private final String _nameRu;
   private final String _icon;
   private final int _weight;
   private final boolean _stackable;
   private final int _materialType;
   private final int _crystalType;
   private final int _equipReuseDelay;
   private final int _duration;
   private final int _time;
   private final int _timeLimit;
   private final int _autoDestroyTime;
   private final int _bodyPart;
   private final int _referencePrice;
   private final int _crystalCount;
   private final boolean _sellable;
   private final boolean _dropable;
   private final boolean _destroyable;
   private final boolean _tradeable;
   private final boolean _depositable;
   private final int _enchantable;
   private final boolean _elementable;
   private final boolean _questItem;
   private final boolean _freightable;
   private final boolean _isOlyRestricted;
   private final boolean _isEventRestricted;
   private final boolean _for_npc;
   private final boolean _common;
   private final boolean _heroItem;
   private final boolean _pvpItem;
   private final boolean _ex_immediate_effect;
   private final int _defaultEnchantLevel;
   private final ActionType _defaultAction;
   private final String _showBoard;
   protected int _type1;
   protected int _type2;
   protected Elementals[] _elementals = null;
   protected FuncTemplate[] _funcTemplates;
   protected EffectTemplate[] _effectTemplates;
   protected List<Condition> _preConditions;
   private SkillHolder[] _SkillsHolder;
   private SkillHolder _unequipSkill = null;
   protected static final Func[] _emptyFunctionSet = new Func[0];
   protected static final Effect[] _emptyEffectSet = new Effect[0];
   private List<Quest> _questEvents;
   private final int _useSkillDisTime;
   private final int _reuseDelay;
   private final boolean _isReuseByCron;
   private final int _sharedReuseGroup;
   private final int _agathionMaxEnergy;
   private final int _premiumId;
   private final boolean _isCostume;
   private final int _itemConsumeCount;

   public String getItemsGrade(int itemGrade) {
      String grade = "";
      switch(itemGrade) {
         case 0:
            grade = "NONE";
            break;
         case 1:
            grade = "D";
            break;
         case 2:
            grade = "C";
            break;
         case 3:
            grade = "B";
            break;
         case 4:
            grade = "A";
            break;
         case 5:
            grade = "S";
            break;
         case 6:
            grade = "S80";
            break;
         case 7:
            grade = "S84";
      }

      return grade;
   }

   protected Item(StatsSet set) {
      this._itemId = set.getInteger("item_id");
      this._displayId = set.getInteger("displayId", this._itemId);
      this._nameEn = set.getString("nameEn");
      this._nameRu = set.getString("nameRu");
      this._icon = set.getString("icon", null);
      this._weight = set.getInteger("weight", 0);
      this._materialType = ItemsParser._materials.get(set.getString("material", "steel"));
      this._duration = set.getInteger("duration", -1);
      this._time = set.getInteger("time", -1);
      this._timeLimit = set.getInteger("timeLimit", 0);
      this._autoDestroyTime = set.getInteger("auto_destroy_time", -1) * 1000;
      this._bodyPart = ItemsParser._slots.get(set.getString("bodypart", "none"));
      this._referencePrice = set.getInteger("price", 0);
      this._crystalType = ItemsParser._crystalTypes.get(set.getString("crystal_type", "none"));
      this._crystalCount = set.getInteger("crystal_count", 0);
      this._equipReuseDelay = set.getInteger("equip_reuse_delay", 0) * 1000;
      this._stackable = set.getBool("is_stackable", false);
      this._sellable = set.getBool("is_sellable", true);
      this._dropable = set.getBool("is_dropable", true);
      this._destroyable = set.getBool("is_destroyable", true);
      this._tradeable = set.getBool("is_tradable", true);
      this._depositable = set.getBool("is_depositable", true);
      this._elementable = set.getBool("element_enabled", false);
      this._enchantable = set.getInteger("enchant_enabled", 0);
      this._questItem = set.getBool("is_questitem", false);
      this._freightable = set.getBool("is_freightable", false);
      this._isOlyRestricted = set.getBool("is_oly_restricted", false);
      this._isEventRestricted = set.getBool("isEventRestricted", false);
      this._for_npc = set.getBool("for_npc", false);
      this._isCostume = set.getBool("isCostume", false);
      this._showBoard = set.getString("showBoard", null);
      this._ex_immediate_effect = set.getBool("ex_immediate_effect", false);
      this._defaultAction = set.getEnum("default_action", ActionType.class, ActionType.none);
      this._useSkillDisTime = set.getInteger("useSkillDisTime", 0);
      this._defaultEnchantLevel = set.getInteger("enchanted", 0);
      this._reuseDelay = set.getInteger("reuse_delay", 0);
      this._isReuseByCron = set.getBool("is_cron_reuse", false);
      this._sharedReuseGroup = set.getInteger("shared_reuse_group", 0);
      this._agathionMaxEnergy = set.getInteger("agathion_energy", -1);
      this._premiumId = set.getInteger("premiumId", -1);
      String equip_condition = set.getString("equip_condition", null);
      if (equip_condition != null) {
         ConditionLogicOr cond = new ConditionLogicOr();
         if (equip_condition.contains("strider")) {
            cond.add(new ConditionPetType(1));
         }

         if (equip_condition.contains("grown_up_wolf_group")) {
            cond.add(new ConditionPetType(2));
         }

         if (equip_condition.contains("hatchling_group")) {
            cond.add(new ConditionPetType(4));
         }

         if (equip_condition.contains("all_wolf_group")) {
            cond.add(new ConditionPetType(8));
         }

         if (equip_condition.contains("baby_pet_group")) {
            cond.add(new ConditionPetType(22));
         }

         if (equip_condition.contains("upgrade_baby_pet_group")) {
            cond.add(new ConditionPetType(50));
         }

         if (equip_condition.contains("item_equip_pet_group")) {
            cond.add(new ConditionPetType(100));
         }

         if (cond.conditions.length > 0) {
            this.attach(cond);
         }
      }

      String skills = set.getString("item_skill", null);
      if (skills != null) {
         String[] skillsSplit = skills.split(";");
         this._SkillsHolder = new SkillHolder[skillsSplit.length];
         int used = 0;

         for(String element : skillsSplit) {
            try {
               String[] skillSplit = element.split("-");
               int id = Integer.parseInt(skillSplit[0]);
               int level = Integer.parseInt(skillSplit[1]);
               if (id == 0) {
                  _log.info(StringUtil.concat("Ignoring item_skill(", element, ") for item ", this.toString(), ". Skill id is 0!"));
               } else if (level == 0) {
                  _log.info(StringUtil.concat("Ignoring item_skill(", element, ") for item ", this.toString(), ". Skill level is 0!"));
               } else {
                  this._SkillsHolder[used] = new SkillHolder(id, level);
                  ++used;
               }
            } catch (Exception var14) {
               _log.warning(
                  StringUtil.concat(
                     "Failed to parse item_skill(", element, ") for item ", this.toString(), "! Format: SkillId0-SkillLevel0[;SkillIdN-SkillLevelN]"
                  )
               );
            }
         }

         if (used != this._SkillsHolder.length) {
            SkillHolder[] SkillsHolder = new SkillHolder[used];
            System.arraycopy(this._SkillsHolder, 0, SkillsHolder, 0, used);
            this._SkillsHolder = SkillsHolder;
         }
      }

      skills = set.getString("unequip_skill", null);
      if (skills != null) {
         String[] info = skills.split("-");
         if (info != null && info.length == 2) {
            int id = 0;
            int level = 0;

            try {
               id = Integer.parseInt(info[0]);
               level = Integer.parseInt(info[1]);
            } catch (Exception var13) {
               _log.info(StringUtil.concat("Couldnt parse ", skills, " in weapon unequip skills! item ", this.toString()));
            }

            if (id > 0 && level > 0) {
               this._unequipSkill = new SkillHolder(id, level);
            }
         }
      }

      this._common = this._itemId >= 11605 && this._itemId <= 12361;
      this._heroItem = this._itemId >= 6611 && this._itemId <= 6621 || this._itemId >= 9388 && this._itemId <= 9390 || this._itemId == 6842;
      this._pvpItem = this._itemId >= 10667 && this._itemId <= 10835
         || this._itemId >= 12852 && this._itemId <= 12977
         || this._itemId >= 14363 && this._itemId <= 14525
         || this._itemId == 14528
         || this._itemId == 14529
         || this._itemId == 14558
         || this._itemId >= 15913 && this._itemId <= 16024
         || this._itemId >= 16134 && this._itemId <= 16147
         || this._itemId == 16149
         || this._itemId == 16151
         || this._itemId == 16153
         || this._itemId == 16155
         || this._itemId == 16157
         || this._itemId == 16159
         || this._itemId >= 16168 && this._itemId <= 16176
         || this._itemId >= 16179 && this._itemId <= 16220;
      this._itemConsumeCount = set.getInteger("itemConsumeCount", 0);
   }

   public abstract ItemType getItemType();

   public boolean isMagicWeapon() {
      return false;
   }

   public int getEquipReuseDelay() {
      return this._equipReuseDelay;
   }

   public final int getDuration() {
      return this._duration;
   }

   public final int getTime() {
      return this._time;
   }

   public final int getTimeLimit() {
      return this._timeLimit;
   }

   public final int getAutoDestroyTime() {
      return this._autoDestroyTime;
   }

   @Override
   public final int getId() {
      return this._itemId;
   }

   public final int getDisplayId() {
      return this._displayId;
   }

   public abstract int getItemMask();

   public final int getMaterialType() {
      return this._materialType;
   }

   public final int getType2() {
      return this._type2;
   }

   public final int getWeight() {
      return this._weight;
   }

   public final boolean isCrystallizable() {
      return this._crystalType != 0 && this._crystalCount > 0;
   }

   public final int getCrystalType() {
      return this._crystalType;
   }

   public final int getCrystalItemId() {
      return CRYSTAL_ITEM_ID[this._crystalType];
   }

   public final int getItemGrade() {
      return this.getCrystalType();
   }

   public final int getItemGradeSPlus() {
      switch(this.getItemGrade()) {
         case 6:
         case 7:
            return 5;
         default:
            return this.getItemGrade();
      }
   }

   public final int getCrystalCount() {
      return this._crystalCount;
   }

   public final int getCrystalCount(int enchantLevel) {
      if (enchantLevel > 3) {
         switch(this._type2) {
            case 0:
               return this._crystalCount + CRYSTAL_ENCHANT_BONUS_WEAPON[this.getCrystalType()] * (2 * enchantLevel - 3);
            case 1:
            case 2:
               return this._crystalCount + CRYSTAL_ENCHANT_BONUS_ARMOR[this.getCrystalType()] * (3 * enchantLevel - 6);
            default:
               return this._crystalCount;
         }
      } else if (enchantLevel > 0) {
         switch(this._type2) {
            case 0:
               return this._crystalCount + CRYSTAL_ENCHANT_BONUS_WEAPON[this.getCrystalType()] * enchantLevel;
            case 1:
            case 2:
               return this._crystalCount + CRYSTAL_ENCHANT_BONUS_ARMOR[this.getCrystalType()] * enchantLevel;
            default:
               return this._crystalCount;
         }
      } else {
         return this._crystalCount;
      }
   }

   public final String getNameEn() {
      return this._nameEn;
   }

   public final String getNameRu() {
      return this._nameRu;
   }

   public final Elementals[] getElementals() {
      return this._elementals;
   }

   public Elementals getElemental(byte attribute) {
      if (this._elementals == null) {
         return null;
      } else {
         for(Elementals elm : this._elementals) {
            if (elm.getElement() == attribute) {
               return elm;
            }
         }

         return null;
      }
   }

   public void setElementals(Elementals element) {
      if (this._elementals == null) {
         this._elementals = new Elementals[1];
         this._elementals[0] = element;
      } else {
         Elementals elm = this.getElemental(element.getElement());
         if (elm != null) {
            elm.setValue(element.getValue());
         } else {
            Elementals[] array = new Elementals[this._elementals.length + 1];
            System.arraycopy(this._elementals, 0, array, 0, this._elementals.length);
            array[this._elementals.length] = element;
            this._elementals = array;
         }
      }
   }

   public final int getBodyPart() {
      return this._bodyPart;
   }

   public final int getType1() {
      return this._type1;
   }

   public final boolean isStackable() {
      return this._stackable;
   }

   public boolean isConsumable() {
      return false;
   }

   public boolean isEquipable() {
      return this.getBodyPart() != 0 && !(this.getItemType() instanceof EtcItemType);
   }

   public boolean isArrow() {
      return this.getItemType() == EtcItemType.ARROW;
   }

   public final int getReferencePrice() {
      return this.isConsumable() ? (int)((double)this._referencePrice * Config.RATE_CONSUMABLE_COST) : this._referencePrice;
   }

   public final boolean isSellable() {
      return this._sellable;
   }

   public final boolean isDropable() {
      return this._dropable;
   }

   public final boolean isDestroyable() {
      return this._destroyable;
   }

   public final boolean isTradeable() {
      return this._tradeable;
   }

   public final boolean isDepositable() {
      return this._depositable;
   }

   public final int isEnchantable() {
      return Arrays.binarySearch(Config.ENCHANT_BLACKLIST, this.getId()) < 0 ? this._enchantable : 0;
   }

   public final boolean isElementable() {
      return Config.ENCHANT_ELEMENT_ALL_ITEMS
         ? (this.isArmor() || this.isWeapon()) && this.getCrystalType() >= 5 && this.getBodyPart() != 256
         : this._elementable;
   }

   public final boolean isCommon() {
      return this._common;
   }

   public final boolean isHeroItem() {
      return this._heroItem;
   }

   public final boolean isPvpItem() {
      return this._pvpItem;
   }

   public boolean isPotion() {
      return this.getItemType() == EtcItemType.POTION;
   }

   public boolean isElixir() {
      return this.getItemType() == EtcItemType.ELIXIR;
   }

   public final Func[] getStatFuncs(ItemInstance item, Creature player) {
      if (this._funcTemplates != null && this._funcTemplates.length != 0) {
         ArrayList<Func> funcs = new ArrayList<>(this._funcTemplates.length);
         Env env = new Env();
         env.setCharacter(player);
         env.setTarget(player);
         env.setItem(item);

         for(FuncTemplate t : this._funcTemplates) {
            Func f = t.getFunc(env, item);
            if (f != null) {
               funcs.add(f);
            }
         }

         return funcs.isEmpty() ? _emptyFunctionSet : funcs.toArray(new Func[funcs.size()]);
      } else {
         return _emptyFunctionSet;
      }
   }

   public Effect[] getEffects(ItemInstance item, Creature player) {
      if (this._effectTemplates != null && this._effectTemplates.length != 0) {
         List<Effect> effects = new ArrayList<>(this._effectTemplates.length);
         Env env = new Env();
         env.setCharacter(player);
         env.setTarget(player);
         env.setItem(item);

         for(EffectTemplate et : this._effectTemplates) {
            Effect e = et.getEffect(env);
            if (e != null) {
               e.scheduleEffect(true);
               effects.add(e);
            }
         }

         return effects.isEmpty() ? _emptyEffectSet : effects.toArray(new Effect[effects.size()]);
      } else {
         return _emptyEffectSet;
      }
   }

   public void attach(FuncTemplate f) {
      switch(f.stat) {
         case FIRE_RES:
         case FIRE_POWER:
            this.setElementals(new Elementals((byte)0, (int)f.lambda.calc(null)));
            break;
         case WATER_RES:
         case WATER_POWER:
            this.setElementals(new Elementals((byte)1, (int)f.lambda.calc(null)));
            break;
         case WIND_RES:
         case WIND_POWER:
            this.setElementals(new Elementals((byte)2, (int)f.lambda.calc(null)));
            break;
         case EARTH_RES:
         case EARTH_POWER:
            this.setElementals(new Elementals((byte)3, (int)f.lambda.calc(null)));
            break;
         case HOLY_RES:
         case HOLY_POWER:
            this.setElementals(new Elementals((byte)4, (int)f.lambda.calc(null)));
            break;
         case DARK_RES:
         case DARK_POWER:
            this.setElementals(new Elementals((byte)5, (int)f.lambda.calc(null)));
      }

      if (this._funcTemplates == null) {
         this._funcTemplates = new FuncTemplate[]{f};
      } else {
         int len = this._funcTemplates.length;
         FuncTemplate[] tmp = new FuncTemplate[len + 1];
         System.arraycopy(this._funcTemplates, 0, tmp, 0, len);
         tmp[len] = f;
         this._funcTemplates = tmp;
      }
   }

   public void attach(EffectTemplate effect) {
      if (this._effectTemplates == null) {
         this._effectTemplates = new EffectTemplate[]{effect};
      } else {
         int len = this._effectTemplates.length;
         EffectTemplate[] tmp = new EffectTemplate[len + 1];
         System.arraycopy(this._effectTemplates, 0, tmp, 0, len);
         tmp[len] = effect;
         this._effectTemplates = tmp;
      }
   }

   public final void attach(Condition c) {
      if (this._preConditions == null) {
         this._preConditions = new ArrayList<>(1);
      }

      if (!this._preConditions.contains(c)) {
         this._preConditions.add(c);
      }
   }

   public boolean hasSkills() {
      return this._SkillsHolder != null;
   }

   public final SkillHolder[] getSkills() {
      return this._SkillsHolder;
   }

   public final Skill getUnequipSkill() {
      return this._unequipSkill == null ? null : this._unequipSkill.getSkill();
   }

   public boolean checkCondition(Creature activeChar, GameObject target, boolean sendMessage) {
      if (activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !Config.GM_ITEM_RESTRICTION) {
         return true;
      } else if ((this.isOlyRestrictedItem() || this.isHeroItem()) && activeChar.isPlayer() && activeChar.getActingPlayer().isInOlympiadMode()) {
         if (this.isEquipable()) {
            activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT);
         } else {
            activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
         }

         return false;
      } else if (!this.isConditionAttached()) {
         return true;
      } else {
         Env env = new Env();
         env.setCharacter(activeChar);
         if (target instanceof Creature) {
            env.setTarget((Creature)target);
         }

         for(Condition preCondition : this._preConditions) {
            if (preCondition != null && !preCondition.test(env)) {
               if (activeChar instanceof Summon) {
                  activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
                  return false;
               }

               if (sendMessage) {
                  String msg = preCondition.getMessage();
                  int msgId = preCondition.getMessageId();
                  if (msg != null) {
                     activeChar.sendMessage(msg);
                  } else if (msgId != 0) {
                     SystemMessage sm = SystemMessage.getSystemMessage(msgId);
                     if (preCondition.isAddName()) {
                        sm.addItemName(this._itemId);
                     }

                     activeChar.sendPacket(sm);
                  }
               }

               return false;
            }
         }

         return true;
      }
   }

   public boolean isConditionAttached() {
      return this._preConditions != null && !this._preConditions.isEmpty();
   }

   public boolean isQuestItem() {
      return this._questItem;
   }

   public boolean isFreightable() {
      return this._freightable;
   }

   public boolean isOlyRestrictedItem() {
      return this._isOlyRestricted || Config.LIST_OLY_RESTRICTED_ITEMS.contains(this._itemId);
   }

   public boolean isEventRestrictedItem() {
      return this._isEventRestricted;
   }

   public boolean isForNpc() {
      return this._for_npc;
   }

   @Override
   public String toString() {
      return this._nameEn + "(" + this._itemId + ")";
   }

   public boolean is_ex_immediate_effect() {
      return this._ex_immediate_effect;
   }

   public ActionType getDefaultAction() {
      return this._defaultAction;
   }

   public int useSkillDisTime() {
      return this._useSkillDisTime;
   }

   public int getReuseDelay() {
      return this._reuseDelay;
   }

   public boolean isReuseByCron() {
      return this._isReuseByCron;
   }

   public int getSharedReuseGroup() {
      return this._sharedReuseGroup;
   }

   public int getDisplayReuseGroup() {
      return this._sharedReuseGroup < 0 ? -1 : this._sharedReuseGroup;
   }

   public int getAgathionMaxEnergy() {
      return this._agathionMaxEnergy;
   }

   public String getIcon() {
      return this._icon;
   }

   public String getShowBoard() {
      return this._showBoard;
   }

   public void addQuestEvent(Quest q) {
      if (this._questEvents == null) {
         this._questEvents = new ArrayList<>();
      }

      this._questEvents.add(q);
   }

   public List<Quest> getQuestEvents() {
      return this._questEvents;
   }

   public int getDefaultEnchantLevel() {
      return this._defaultEnchantLevel;
   }

   public boolean isPetItem() {
      return this.getItemType() == EtcItemType.PET_COLLAR;
   }

   public Skill getEnchant4Skill() {
      return null;
   }

   public boolean isShield() {
      return this._bodyPart == 256;
   }

   public boolean isAccessory() {
      return this.getType2() == 2;
   }

   public boolean isCloak() {
      return this._bodyPart == 8192;
   }

   public boolean isUnderwear() {
      return this._bodyPart == 1;
   }

   public boolean isBelt() {
      return this._bodyPart == 268435456;
   }

   public boolean isSoulCrystal() {
      return this._itemId >= 4629 && this._itemId <= 4661
         || this._itemId >= 5577 && this._itemId <= 5582
         || this._itemId >= 5908 && this._itemId <= 5914
         || this._itemId >= 9570 && this._itemId <= 9572
         || this._itemId >= 10480 && this._itemId <= 10482
         || this._itemId >= 13071 && this._itemId <= 13073
         || this._itemId >= 15541 && this._itemId <= 15543
         || this._itemId >= 15826 && this._itemId <= 15828;
   }

   public boolean isLifeStone() {
      return this._itemId >= 8723 && this._itemId <= 8762
         || this._itemId >= 9573 && this._itemId <= 9576
         || this._itemId >= 10483 && this._itemId <= 10486
         || this._itemId >= 12754 && this._itemId <= 12763
         || this._itemId == 12821
         || this._itemId == 12822
         || this._itemId >= 12840 && this._itemId <= 12851
         || this._itemId == 14008
         || this._itemId >= 14166 && this._itemId <= 14169
         || this._itemId >= 16160 && this._itemId <= 16167
         || this._itemId == 16177
         || this._itemId == 16178;
   }

   public boolean isEnchantScroll() {
      return this._itemId >= 6569 && this._itemId <= 6578
         || this._itemId >= 17255 && this._itemId <= 17264
         || this._itemId >= 22314 && this._itemId <= 22323
         || this._itemId >= 949 && this._itemId <= 962
         || this._itemId >= 729 && this._itemId <= 732;
   }

   public boolean isForgottenScroll() {
      return this._itemId >= 10549 && this._itemId <= 10599
         || this._itemId >= 12768 && this._itemId <= 12778
         || this._itemId >= 14170 && this._itemId <= 14227
         || this._itemId == 17030
         || this._itemId >= 17034 && this._itemId <= 17039;
   }

   public boolean isCodexBook() {
      return this._itemId >= 9625 && this._itemId <= 9627 || this._itemId == 6622;
   }

   public boolean isAttributeStone() {
      return this._itemId >= 9546 && this._itemId <= 9551;
   }

   public boolean isAttributeCrystal() {
      return this._itemId >= 9552 && this._itemId <= 9557;
   }

   public boolean isAttributeJewel() {
      return this._itemId >= 9558 && this._itemId <= 9563;
   }

   public boolean isAttributeEnergy() {
      return this._itemId >= 9564 && this._itemId <= 9569;
   }

   public boolean isSealStone() {
      return this._itemId >= 6360 && this._itemId <= 6362;
   }

   public boolean isKeyMatherial() {
      return this.getItemType() == EtcItemType.MATERIAL;
   }

   public boolean isRecipe() {
      return this.getItemType() == EtcItemType.RECIPE;
   }

   public boolean isAdena() {
      return this._itemId == 57 || this._itemId == 6360 || this._itemId == 6361 || this._itemId == 6362;
   }

   public boolean isWeapon() {
      return this.getType2() == 0;
   }

   public boolean isArmor() {
      return this.getType2() == 1;
   }

   public boolean isJewel() {
      return this.getBodyPart() == 8 || this.getBodyPart() == 48 || this.getBodyPart() == 6;
   }

   public FuncTemplate[] getAttachedFuncs() {
      return this._funcTemplates != null && this._funcTemplates.length != 0 ? this._funcTemplates : new FuncTemplate[0];
   }

   public boolean isEquipment() {
      return this._type1 != 4;
   }

   public boolean isCommonItem() {
      return this._nameEn.startsWith("Common Item - ");
   }

   public boolean isHerb() {
      return this.getItemType() == EtcItemType.HERB;
   }

   public boolean isEpolets() {
      return this._itemId == 9912;
   }

   public boolean isExtractableItem() {
      return false;
   }

   public boolean isBracelet() {
      return this._bodyPart == 1048576 || this._bodyPart == 2097152;
   }

   public boolean isCostume() {
      return this._isCostume;
   }

   public boolean isSealedItem() {
      return this._nameEn.startsWith("Sealed");
   }

   public int getPremiumId() {
      return this._premiumId;
   }

   public final int getItemConsume() {
      return this._itemConsumeCount;
   }

   public boolean isNobleStone() {
      return this._itemId == 14052;
   }

   public boolean isTalisman() {
      return this._bodyPart == 4194304;
   }
}
