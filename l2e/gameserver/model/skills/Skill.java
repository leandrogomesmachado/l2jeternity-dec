package l2e.gameserver.model.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillBalanceParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.handler.targethandlers.TargetHandler;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.BaseToCaptureInstance;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.GuardInstance;
import l2e.gameserver.model.actor.templates.ExtractableProductItemTemplate;
import l2e.gameserver.model.actor.templates.ExtractableSkillTemplate;
import l2e.gameserver.model.base.SkillChangeType;
import l2e.gameserver.model.entity.Duel;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.FlyToLocation;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class Skill implements IChanceSkillTrigger, IIdentifiable {
   protected static final Logger _log = Logger.getLogger(Skill.class.getName());
   private static final GameObject[] EMPTY_TARGET_LIST = new GameObject[0];
   private static final Effect[] _emptyEffectSet = new Effect[0];
   private static final Func[] _emptyFunctionSet = new Func[0];
   public static final int SKILL_CREATE_DWARVEN = 172;
   public static final int SKILL_EXPERTISE = 239;
   public static final int SKILL_CRYSTALLIZE = 248;
   public static final int SKILL_CLAN_LUCK = 390;
   public static final int SKILL_ONYX_BEAST_TRANSFORMATION = 617;
   public static final int SKILL_CREATE_COMMON = 1320;
   public static final int SKILL_DIVINE_INSPIRATION = 1405;
   public static final int SKILL_NPC_RACE = 4416;
   public static final int SKILL_FISHING_MASTERY = 1315;
   public static final int COND_BEHIND = 8;
   public static final int COND_CRIT = 16;
   private final int _id;
   private final int _level;
   private final int _displayId;
   private final int _displayLevel;
   private final String _nameEn;
   private final String _nameRu;
   private final SkillOpType _operateType;
   private final int _magic;
   private final TraitType _traitType;
   private final boolean _staticReuse;
   private final boolean _staticDamage;
   private final int _mpConsume;
   private final int _mpInitialConsume;
   private final int _hpConsume;
   private final int _skillInterruptTime;
   private final int _activateRate;
   private final int _levelModifier;
   private final int _itemConsumeCount;
   private final int _itemConsumeId;
   private final int _castRange;
   private final int _effectRange;
   private final int _abnormalLvl;
   private final boolean _isAbnormalInstant;
   private final int _abnormalTime;
   private final boolean _stayAfterDeath;
   private final boolean _stayOnSubclassChange;
   private final int[] _negateCasterId;
   private final int _refId;
   private final int _hitTime;
   private final int[] _hitTimings;
   private final int _coolTime;
   private final int _reuseHashCode;
   private final int _reuseDelay;
   private final TargetType _targetType;
   private final int _feed;
   private final double _power;
   private final double _pvpPower;
   private final double _pvePower;
   private final int _magicLevel;
   private final int _lvlBonusRate;
   private final int _minChance;
   private final int _maxChance;
   private final int _blowChance;
   private final boolean _isNeutral;
   private final int _affectRange;
   private final int[] _affectLimit = new int[2];
   private final SkillType _skillType;
   private final int _effectId;
   private final int _effectLvl;
   private final boolean _nextActionIsAttack;
   private final boolean _removedOnAnyActionExceptMove;
   private final boolean _removedOnDamage;
   private final byte _element;
   private final int _elementPower;
   private final BaseStats _saveVs;
   private final int _condition;
   private final int _conditionValue;
   private final boolean _overhit;
   private final int _minPledgeClass;
   private final boolean _isOffensive;
   private final boolean _isPVP;
   private final int _chargeConsume;
   private final int _triggeredId;
   private final int _triggeredLevel;
   private final String _chanceType;
   private final int _soulMaxConsume;
   private final boolean _dependOnTargetBuff;
   private final int _afterEffectId;
   private final int _afterEffectLvl;
   private final boolean _isHeroSkill;
   private final boolean _isGMSkill;
   private final boolean _isSevenSigns;
   private final int _baseCritRate;
   private final int _halfKillRate;
   private final int _lethalStrikeRate;
   private final boolean _directHpDmg;
   private final boolean _isTriggeredSkill;
   private final float _sSBoost;
   private final int _aggroPoints;
   private List<Condition> _preCondition;
   private List<Condition> _itemPreCondition;
   private FuncTemplate[] _funcTemplates;
   public EffectTemplate[] _effectTemplates;
   private EffectTemplate[] _effectTemplatesSelf;
   private EffectTemplate[] _effectTemplatesPassive;
   protected ChanceCondition _chanceCondition = null;
   protected FlyToLocation.FlyType _flyType;
   private final int _flyRadius;
   private final float _flyCourse;
   protected boolean _flyToBack;
   private final boolean _isDebuff;
   private final String _attribute;
   private final boolean _ignoreShield;
   private final boolean _isSuicideAttack;
   private final boolean _canBeReflected;
   private final boolean _canBeDispeled;
   private final boolean _isClanSkill;
   private final boolean _excludedFromCheck;
   private final boolean _simultaneousCast;
   private final boolean _isCustom;
   private final boolean _disableGeoCheck;
   private final boolean _ignoreInvincible;
   private ExtractableSkillTemplate _extractableItems = null;
   private int _npcId = 0;
   private final String _icon;
   private byte[] _effectTypes;
   private final int _energyConsume;
   private final boolean _blockSkillMastery;
   private Map<String, Short> _negateAbnormalType;
   private final int _negateRate;
   private final boolean _petMajorHeal;

   protected Skill(StatsSet set) {
      this._isAbnormalInstant = set.getBool("abnormalInstant", false);
      this._id = set.getInteger("skill_id");
      this._level = set.getInteger("level");
      this._refId = set.getInteger("referenceId", 0);
      this._displayId = set.getInteger("displayId", this._id);
      this._displayLevel = set.getInteger("displayLevel", this._level);
      this._nameEn = set.getString("nameEn", "");
      this._nameRu = set.getString("nameRu", "");
      this._operateType = set.getEnum("operateType", SkillOpType.class);
      this._magic = set.getInteger("isMagic", 0);
      this._traitType = set.getEnum("trait", TraitType.class, TraitType.NONE);
      this._staticReuse = set.getBool("staticReuse", false);
      this._staticDamage = set.getBool("staticDamage", false);
      this._mpConsume = set.getInteger("mpConsume", 0);
      this._energyConsume = set.getInteger("energyConsume", 0);
      this._mpInitialConsume = set.getInteger("mpInitialConsume", 0);
      this._hpConsume = set.getInteger("hpConsume", 0);
      this._itemConsumeCount = set.getInteger("itemConsumeCount", 0);
      this._itemConsumeId = set.getInteger("itemConsumeId", 0);
      this._afterEffectId = set.getInteger("afterEffectId", 0);
      this._afterEffectLvl = set.getInteger("afterEffectLvl", 1);
      this._castRange = set.getInteger("castRange", -1);
      this._effectRange = set.getInteger("effectRange", -1);
      this._abnormalLvl = set.getInteger("abnormalLvl", -1);
      String negateCasterId = set.getString("negateCasterId", null);
      if (negateCasterId != null) {
         String[] valuesSplit = negateCasterId.split(",");
         this._negateCasterId = new int[valuesSplit.length];

         for(int i = 0; i < valuesSplit.length; ++i) {
            this._negateCasterId[i] = Integer.parseInt(valuesSplit[i]);
         }
      } else {
         this._negateCasterId = new int[0];
      }

      this._abnormalTime = set.getInteger("abnormalTime", 1);
      this._attribute = set.getString("attribute", "");
      this._stayAfterDeath = set.getBool("stayAfterDeath", false);
      this._stayOnSubclassChange = set.getBool("stayOnSubclassChange", true);
      this._isNeutral = set.getBool("neutral", false);
      this._hitTime = set.getInteger("hitTime", 0);
      String hitTimings = set.getString("hitTimings", null);
      if (hitTimings != null) {
         try {
            String[] valuesSplit = hitTimings.split(",");
            this._hitTimings = new int[valuesSplit.length];

            for(int i = 0; i < valuesSplit.length; ++i) {
               this._hitTimings[i] = Integer.parseInt(valuesSplit[i]);
            }
         } catch (Exception var10) {
            throw new IllegalArgumentException(
               "SkillId: " + this._id + " invalid hitTimings value: " + hitTimings + ", \"percent,percent,...percent\" required"
            );
         }
      } else {
         this._hitTimings = new int[0];
      }

      this._coolTime = set.getInteger("coolTime", 0);
      this._skillInterruptTime = set.getInteger("hitCancelTime", 500);
      this._isDebuff = set.getBool("isDebuff", false);
      this._feed = set.getInteger("feed", 0);
      this._reuseHashCode = SkillsParser.getSkillHashCode(this._id, this._level);
      if (Config.ENABLE_MODIFY_SKILL_REUSE && Config.SKILL_REUSE_LIST.containsKey(this._id)) {
         if (Config.DEBUG) {
            _log.info(
               "*** Skill "
                  + this._nameEn
                  + " ("
                  + this._level
                  + ") changed reuse from "
                  + set.getInteger("reuseDelay", 0)
                  + " to "
                  + Config.SKILL_REUSE_LIST.get(this._id)
                  + " seconds."
            );
         }

         this._reuseDelay = Config.SKILL_REUSE_LIST.get(this._id);
      } else {
         this._reuseDelay = set.getInteger("reuseDelay", 0);
      }

      this._affectRange = set.getInteger("affectRange", 0);
      String affectLimit = set.getString("affectLimit", null);
      if (affectLimit != null) {
         try {
            String[] valuesSplit = affectLimit.split("-");
            this._affectLimit[0] = Integer.parseInt(valuesSplit[0]);
            this._affectLimit[1] = Integer.parseInt(valuesSplit[1]);
         } catch (Exception var9) {
            throw new IllegalArgumentException("SkillId: " + this._id + " invalid affectLimit value: " + affectLimit + ", \"percent-percent\" required");
         }
      }

      this._targetType = set.getEnum("targetType", TargetType.class);
      this._power = (double)set.getFloat("power", 0.0F);
      this._pvpPower = (double)set.getFloat("pvpPower", (float)this.getPower());
      this._pvePower = (double)set.getFloat("pvePower", (float)this.getPower());
      this._magicLevel = set.getInteger("magicLvl", 0);
      this._lvlBonusRate = set.getInteger("lvlBonusRate", 0);
      this._minChance = set.getInteger("minChance", Config.MIN_ABNORMAL_STATE_SUCCESS_RATE);
      this._maxChance = set.getInteger("maxChance", Config.MAX_ABNORMAL_STATE_SUCCESS_RATE);
      this._ignoreShield = set.getBool("ignoreShld", false);
      this._skillType = set.getEnum("skillType", SkillType.class, SkillType.DUMMY);
      this._effectId = set.getInteger("effectId", 0);
      this._effectLvl = set.getInteger("effectLevel", 0);
      this._nextActionIsAttack = set.getBool("nextActionAttack", false);
      this._removedOnAnyActionExceptMove = set.getBool("removedOnAnyActionExceptMove", false);
      this._removedOnDamage = set.getBool("removedOnDamage", false);
      this._element = set.getByte("element", (byte)-1);
      this._elementPower = set.getInteger("elementPower", 0);
      this._activateRate = set.getInteger("activateRate", -1);
      this._levelModifier = set.getInteger("levelModifier", 1);
      this._saveVs = set.getEnum("saveVs", BaseStats.class, BaseStats.NULL);
      this._condition = set.getInteger("condition", 0);
      this._conditionValue = set.getInteger("conditionValue", 0);
      this._overhit = set.getBool("overHit", false);
      this._isSuicideAttack = set.getBool("isSuicideAttack", false);
      this._minPledgeClass = set.getInteger("minPledgeClass", 0);
      this._isOffensive = set.getBool("offensive", false);
      this._isPVP = set.getBool("pvp", false);
      this._chargeConsume = set.getInteger("chargeConsume", 0);
      this._triggeredId = set.getInteger("triggeredId", 0);
      this._triggeredLevel = set.getInteger("triggeredLevel", 1);
      this._chanceType = set.getString("chanceType", "");
      if (!this._chanceType.isEmpty()) {
         this._chanceCondition = ChanceCondition.parse(set);
      }

      this._soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
      this._blowChance = set.getInteger("blowChance", 0);
      this._isHeroSkill = SkillTreesParser.getInstance().isHeroSkill(this._id, this._level);
      this._isGMSkill = SkillTreesParser.getInstance().isGMSkill(this._id, this._level);
      this._isSevenSigns = this._id > 4360 && this._id < 4367;
      this._isClanSkill = SkillTreesParser.getInstance().isClanSkill(this._id, this._level);
      this._baseCritRate = set.getInteger("baseCritRate", 0);
      this._halfKillRate = set.getInteger("halfKillRate", 0);
      this._lethalStrikeRate = set.getInteger("lethalStrikeRate", 0);
      this._directHpDmg = set.getBool("dmgDirectlyToHp", false);
      this._isTriggeredSkill = set.getBool("isTriggeredSkill", false);
      this._sSBoost = set.getFloat("SSBoost", 0.0F);
      this._aggroPoints = set.getInteger("aggroPoints", 0);
      this._flyType = FlyToLocation.FlyType.valueOf(set.getString("flyType", "NONE").toUpperCase());
      this._flyToBack = set.getBool("flyToBack", false);
      this._flyRadius = set.getInteger("flyRadius", 0);
      this._flyCourse = set.getFloat("flyCourse", 0.0F);
      this._canBeReflected = set.getBool("canBeReflected", true);
      this._canBeDispeled = set.getBool("canBeDispeled", true);
      this._excludedFromCheck = set.getBool("excludedFromCheck", false);
      this._dependOnTargetBuff = set.getBool("dependOnTargetBuff", false);
      this._simultaneousCast = set.getBool("simultaneousCast", false);
      this._isCustom = set.getBool("isCustom", false);
      this._disableGeoCheck = set.getBool("disableGeoCheck", false);
      this._ignoreInvincible = set.getBool("ignoreInvincible", false);
      String capsuled_items = set.getString("capsuled_items_skill", null);
      if (capsuled_items != null) {
         if (capsuled_items.isEmpty()) {
            _log.warning("Empty Extractable Item Skill data in Skill Id: " + this._id);
         }

         this._extractableItems = this.parseExtractableSkill(this._id, this._level, capsuled_items);
      }

      this._npcId = set.getInteger("npcId", 0);
      this._icon = set.getString("icon", "icon.skill0000");
      this._blockSkillMastery = set.getBool("blockSkillMastery", false);
      String[] negateStackTypeString = set.getString("negateAbnormalType", "").split(";");
      if (negateStackTypeString.length > 0) {
         this._negateAbnormalType = new ConcurrentHashMap<>();

         for(int i = 0; i < negateStackTypeString.length; ++i) {
            if (!negateStackTypeString[i].isEmpty()) {
               String[] entry = negateStackTypeString[i].split(",");
               this._negateAbnormalType.put(entry[0], entry.length > 1 ? Short.parseShort(entry[1]) : 32767);
            }
         }
      }

      this._negateRate = set.getInteger("negateRate", 0);
      this._petMajorHeal = set.getBool("petMajorHeal", false);
   }

   public abstract void useSkill(Creature var1, GameObject[] var2);

   public final int getConditionValue() {
      return this._conditionValue;
   }

   public final int getCondition() {
      return this._condition;
   }

   public final SkillType getSkillType() {
      return this._skillType;
   }

   public final TraitType getTraitType() {
      return this._traitType;
   }

   public final byte getElement() {
      return this._element;
   }

   public final int getElementPower() {
      return this._elementPower;
   }

   public final TargetType getTargetType() {
      return this._targetType;
   }

   public final boolean isOverhit() {
      return this._overhit;
   }

   public final boolean isSuicideAttack() {
      return this._isSuicideAttack;
   }

   public final boolean allowOnTransform() {
      return this.isPassive();
   }

   public final double getPower(Creature activeChar, Creature target, boolean isPvP, boolean isPvE) {
      double power = this.getPower(isPvP, isPvE);
      if (activeChar == null) {
         return power;
      } else {
         int targetClassId = activeChar.getTarget() instanceof Player ? activeChar.getTarget().getActingPlayer().getClassId().getId() : -1;
         return power
            * SkillBalanceParser.getInstance()
               .getSkillValue(
                  this.getId() + ";" + targetClassId,
                  SkillChangeType.Power,
                  activeChar.getTarget() instanceof Player ? activeChar.getTarget().getActingPlayer() : null
               );
      }
   }

   public final double getPower() {
      return this._power;
   }

   public final int getAbnormalLvl() {
      return this._abnormalLvl;
   }

   public final double getPower(boolean isPvP, boolean isPvE) {
      return isPvE ? this._pvePower : (isPvP ? this._pvpPower : this._power);
   }

   public final boolean isAbnormalInstant() {
      return this._isAbnormalInstant;
   }

   public final int getAbnormalTime() {
      return this._abnormalTime;
   }

   public final int[] getNegateCasterId() {
      return this._negateCasterId;
   }

   public final int getMagicLevel() {
      return this._magicLevel;
   }

   public final int getLvlBonusRate() {
      return this._lvlBonusRate;
   }

   public final int getMinChance() {
      return this._minChance;
   }

   public final int getMaxChance() {
      return this._maxChance;
   }

   public final boolean isRemovedOnAnyActionExceptMove() {
      return this._removedOnAnyActionExceptMove;
   }

   public final boolean isRemovedOnDamage() {
      return this._removedOnDamage;
   }

   public final int getEffectId() {
      return this._effectId;
   }

   public final int getEffectLvl() {
      return this._effectLvl;
   }

   public final boolean nextActionIsAttack() {
      return this._nextActionIsAttack;
   }

   public final int getCastRange() {
      return this._castRange;
   }

   public final int getEffectRange() {
      return this._effectRange;
   }

   public final int getHpConsume() {
      return this._hpConsume;
   }

   @Override
   public final int getId() {
      return this._id;
   }

   public final boolean isDebuff() {
      return this._isDebuff;
   }

   public final boolean hasDebuffEffects() {
      return this._isDebuff || this.hasEffectType(EffectType.STUN) || this.hasEffectType(EffectType.DEBUFF) || this.isOffensive();
   }

   public int getDisplayId() {
      return this._displayId;
   }

   public int getDisplayLevel() {
      return this._displayLevel;
   }

   public int getTriggeredId() {
      return this._triggeredId;
   }

   public int getTriggeredLevel() {
      return this._triggeredLevel;
   }

   public boolean triggerAnotherSkill() {
      return this._triggeredId > 1;
   }

   public final BaseStats getSaveVs() {
      return this._saveVs;
   }

   public final int getItemConsume() {
      return this._itemConsumeCount;
   }

   public final int getItemConsumeId() {
      return this._itemConsumeId;
   }

   public final int getLevel() {
      return this._level;
   }

   public final boolean isPhysical() {
      return this._magic == 0;
   }

   public final boolean isMagic() {
      return this._magic == 1;
   }

   public final boolean isDance() {
      return this._magic == 3;
   }

   public final boolean isStatic() {
      return this._magic == 2;
   }

   public final boolean isStaticReuse() {
      return this._staticReuse;
   }

   public final boolean isStaticDamage() {
      return this._staticDamage;
   }

   public final int getMpConsume() {
      return this._mpConsume;
   }

   public final int getMpInitialConsume() {
      return this._mpInitialConsume;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }

   public final int getReuseDelay() {
      return (int)((double)this._reuseDelay * SkillBalanceParser.getInstance().getSkillValue(this.getId() + ";-2", SkillChangeType.Reuse, null));
   }

   public final int getReuseHashCode() {
      return this._reuseHashCode;
   }

   public final int getHitTime() {
      return (int)((double)this._hitTime * SkillBalanceParser.getInstance().getSkillValue(this.getId() + ";-2", SkillChangeType.CastTime, null));
   }

   public final int getHitCounts() {
      return this._hitTimings.length;
   }

   public final int[] getHitTimings() {
      return this._hitTimings;
   }

   public final int getCoolTime() {
      return this._coolTime;
   }

   public final int getAffectRange() {
      return this._affectRange;
   }

   public final int getAffectLimit() {
      return this._affectLimit[0] + Rnd.get(this._affectLimit[1]);
   }

   public final boolean isActive() {
      return this._operateType != null && this._operateType.isActive();
   }

   public final boolean isPassive() {
      return this._operateType != null && this._operateType.isPassive();
   }

   public final boolean isToggle() {
      return this._operateType != null && this._operateType.isToggle();
   }

   public boolean isContinuous() {
      return this._operateType != null && this._operateType.isContinuous() || this.isSelfContinuous();
   }

   public boolean isSelfContinuous() {
      return this._operateType != null && this._operateType.isSelfContinuous();
   }

   public final boolean isChance() {
      return this._chanceCondition != null && this.isPassive();
   }

   public final boolean isTriggeredSkill() {
      return this._isTriggeredSkill;
   }

   public final float getSSBoost() {
      return this._sSBoost;
   }

   public final int getAggroPoints() {
      return this._aggroPoints;
   }

   public final boolean useSoulShot() {
      switch(this.getSkillType()) {
         case PDAM:
         case CHARGEDAM:
         case BLOW:
            return true;
         default:
            return false;
      }
   }

   public final boolean useSpiritShot() {
      return this._magic == 1 || this.getSkillType() == SkillType.PUMPING || this.getSkillType() == SkillType.REELING;
   }

   public int getMinPledgeClass() {
      return this._minPledgeClass;
   }

   public final boolean isOffensive() {
      return this._isOffensive || this.isPVP();
   }

   public final boolean isNeutral() {
      return this._isNeutral;
   }

   public final boolean isPVP() {
      return this._isPVP;
   }

   public final boolean isHeroSkill() {
      return this._isHeroSkill;
   }

   public final boolean isGMSkill() {
      return this._isGMSkill;
   }

   public final boolean is7Signs() {
      return this._isSevenSigns;
   }

   public final int getChargeConsume() {
      return this._chargeConsume;
   }

   public final boolean isChargeBoost() {
      return this._chargeConsume > 0;
   }

   public final int getMaxSoulConsumeCount() {
      return this._soulMaxConsume;
   }

   public final int getBaseCritRate() {
      return this._baseCritRate;
   }

   public final int getHalfKillRate() {
      return this._halfKillRate;
   }

   public final int getLethalStrikeRate() {
      return this._lethalStrikeRate;
   }

   public final boolean getDmgDirectlyToHP() {
      return this._directHpDmg;
   }

   public FlyToLocation.FlyType getFlyType() {
      return this._flyType;
   }

   public final int getFlyRadius() {
      return this._flyRadius;
   }

   public boolean isFlyToBack() {
      return this._flyToBack;
   }

   public final float getFlyCourse() {
      return this._flyCourse;
   }

   public final boolean isEffectTypeBattle() {
      switch(this.getSkillType()) {
         case PDAM:
         case CHARGEDAM:
         case BLOW:
         case MDAM:
         case DEATHLINK:
            return true;
         default:
            return false;
      }
   }

   public final boolean isStayAfterDeath() {
      return this._stayAfterDeath;
   }

   public final boolean isStayOnSubclassChange() {
      return this._stayOnSubclassChange;
   }

   public boolean checkCondition(Creature activeChar, GameObject object, boolean itemOrWeapon, boolean printMsg) {
      if (activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && !Config.GM_SKILL_RESTRICTION) {
         return true;
      } else {
         List<Condition> preCondition = itemOrWeapon ? this._itemPreCondition : this._preCondition;
         if (preCondition != null && !preCondition.isEmpty()) {
            Creature target = object instanceof Creature ? (Creature)object : null;

            for(Condition cond : preCondition) {
               Env env = new Env();
               env.setCharacter(activeChar);
               env.setTarget(target);
               env.setSkill(this);
               if (cond != null && !cond.test(env)) {
                  if (printMsg) {
                     String msg = cond.getMessage();
                     int msgId = cond.getMessageId();
                     if (msgId != 0) {
                        SystemMessage sm = SystemMessage.getSystemMessage(msgId);
                        if (cond.isAddName()) {
                           sm.addSkillName(this._id);
                        }

                        activeChar.sendPacket(sm);
                     } else if (msg != null) {
                        activeChar.sendMessage(msg);
                     }
                  }

                  return false;
               }
            }

            return true;
         } else {
            return true;
         }
      }
   }

   public final GameObject[] getTargetList(Creature activeChar, boolean onlyFirst) {
      Creature target = activeChar.getTarget() != null
         ? (Creature)activeChar.getTarget()
         : (activeChar.isPlayable() ? activeChar.getAI().getCastTarget() : null);
      return this.getTargetList(activeChar, onlyFirst, target);
   }

   public final GameObject[] getTargetList(Creature activeChar, boolean onlyFirst, Creature target) {
      ITargetTypeHandler handler = TargetHandler.getInstance().getHandler(this.getTargetType());
      if (handler != null) {
         try {
            return handler.getTargetList(this, activeChar, onlyFirst, target);
         } catch (Exception var6) {
            _log.log(Level.WARNING, "Exception in Skill.getTargetList(): " + var6.getMessage(), (Throwable)var6);
         }
      }

      activeChar.sendMessage("Target type of skill is not currently handled.");
      return EMPTY_TARGET_LIST;
   }

   public final GameObject[] getTargetList(Creature activeChar) {
      return this.getTargetList(activeChar, false);
   }

   public final GameObject getFirstOfTargetList(Creature activeChar) {
      GameObject[] targets = this.getTargetList(activeChar, true);
      return targets.length == 0 ? null : targets[0];
   }

   public static final boolean checkForAreaOffensiveSkills(Creature caster, Creature target, Skill skill, boolean sourceInArena) {
      if (target != null && !target.isDead() && target != caster) {
         Player player = caster.getActingPlayer();
         Player targetPlayer = target.getActingPlayer();
         if (player != null) {
            if (targetPlayer != null) {
               if (targetPlayer == caster || targetPlayer == player) {
                  return false;
               }

               if (!targetPlayer.isVisibleFor(player) && targetPlayer.isGM()) {
                  return false;
               }

               if (targetPlayer.inObserverMode()) {
                  return false;
               }

               if (skill.isOffensive()
                  && player.getSiegeState() > 0
                  && player.isInsideZone(ZoneId.SIEGE)
                  && player.getSiegeState() == targetPlayer.getSiegeState()
                  && player.getSiegeSide() == targetPlayer.getSiegeSide()) {
                  return false;
               }

               if (skill.isOffensive() && target.isInsideZone(ZoneId.PEACE)) {
                  return false;
               }

               if (player.getDuelState() == 1 && player.getDuelId() == targetPlayer.getDuelId()) {
                  Duel duel = DuelManager.getInstance().getDuel(player.getDuelId());
                  if (duel.isPartyDuel()) {
                     Party partyA = player.getParty();
                     Party partyB = targetPlayer.getParty();
                     if (partyA != null && partyA.getMembers().contains(targetPlayer)) {
                        return false;
                     }

                     if (partyB != null && partyB.getMembers().contains(player)) {
                        return false;
                     }

                     return true;
                  }

                  return true;
               }

               if (player.isInSameParty(targetPlayer) || player.isInSameChannel(targetPlayer)) {
                  return false;
               }

               for(AbstractFightEvent e : player.getFightEvents()) {
                  if (e != null && !e.canUseMagic(targetPlayer, player, skill)) {
                     return false;
                  }
               }

               if (!sourceInArena && (!targetPlayer.isInsideZone(ZoneId.PVP) || targetPlayer.isInsideZone(ZoneId.SIEGE))) {
                  if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId()) {
                     return false;
                  }

                  if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId()) {
                     return false;
                  }

                  if (!player.checkPvpSkill(targetPlayer, skill, caster instanceof Summon)) {
                     return false;
                  }
               }
            } else if (target instanceof GuardInstance) {
               boolean isCtrlPressed = player.getCurrentSkill() != null && player.getCurrentSkill().isCtrlPressed();
               if (!isCtrlPressed && !sourceInArena) {
                  return false;
               }
            }
         } else if (targetPlayer == null && target.isAttackable() && caster.isAttackable()) {
            return false;
         }

         return GeoEngine.canSeeTarget(caster, target, false);
      } else {
         return false;
      }
   }

   public static final boolean addSummon(Creature caster, Player owner, int radius, boolean isDead) {
      return !owner.hasSummon() ? false : addCharacter(caster, owner.getSummon(), radius, isDead);
   }

   public static final boolean addCharacter(Creature caster, Creature target, int radius, boolean isDead) {
      if (isDead != target.isDead()) {
         return false;
      } else if (caster.isPlayer()
         && target.isPlayer()
         && caster.getActingPlayer().isInFightEvent()
         && target.getActingPlayer().isInFightEvent()
         && caster.getActingPlayer().getFightEvent().getFightEventPlayer(caster).getTeam()
            != target.getActingPlayer().getFightEvent().getFightEventPlayer(target).getTeam()) {
         return false;
      } else {
         return radius <= 0 || Util.checkIfInRange(radius, caster, target, true);
      }
   }

   public final Func[] getStatFuncs(Effect effect, Creature player) {
      if (this._funcTemplates == null) {
         return _emptyFunctionSet;
      } else if (!(player instanceof Playable) && !(player instanceof Attackable)) {
         return _emptyFunctionSet;
      } else {
         List<Func> funcs = new ArrayList<>(this._funcTemplates.length);
         Env env = new Env();
         env.setCharacter(player);
         env.setSkill(this);

         for(FuncTemplate t : this._funcTemplates) {
            Func f = t.getFunc(env, this);
            if (f != null) {
               funcs.add(f);
            }
         }

         return funcs.isEmpty() ? _emptyFunctionSet : funcs.toArray(new Func[funcs.size()]);
      }
   }

   public boolean hasEffects() {
      return this._effectTemplates != null && this._effectTemplates.length > 0;
   }

   public EffectTemplate[] getEffectTemplates() {
      return this._effectTemplates;
   }

   public EffectTemplate[] getEffectTemplatesPassive() {
      return this._effectTemplatesPassive;
   }

   public boolean hasSelfEffects() {
      return this._effectTemplatesSelf != null && this._effectTemplatesSelf.length > 0;
   }

   public boolean hasPassiveEffects() {
      return this._effectTemplatesPassive != null && this._effectTemplatesPassive.length > 0;
   }

   public final Effect[] getEffects(Creature effector, Creature effected, Env env, boolean allowSkillMastery) {
      if (this.hasEffects() && !this.isPassive()) {
         if (!(effected instanceof DoorInstance) && !(effected instanceof BaseToCaptureInstance)) {
            if (effector != effected && (this.isOffensive() || this.isDebuff())) {
               if (effected.isInvul() && !this.IsIgnoreInvincible()) {
                  return _emptyEffectSet;
               }

               if (effector instanceof Player && ((Player)effector).isGM() && !((Player)effector).getAccessLevel().canGiveDamage()) {
                  return _emptyEffectSet;
               }
            }

            if (effected.isInvulAgainst(this.getId(), this.getLevel())) {
               return _emptyEffectSet;
            } else {
               List<Effect> effects = new ArrayList<>(this._effectTemplates.length);
               if (env == null) {
                  env = new Env();
               }

               env.setSkillMastery(allowSkillMastery ? Formulas.calcSkillMastery(effector, this) : false);
               env.setCharacter(effector);
               env.setTarget(effected);
               env.setSkill(this);

               for(EffectTemplate et : this._effectTemplates) {
                  if (Formulas.calcEffectSuccess(
                     effector, effected, et, this, env.getShield(), env.isSoulShot(), env.isSpiritShot(), env.isBlessedSpiritShot()
                  )) {
                     Effect e = et.getEffect(env);
                     if (e != null && (e.isReflectable() || effected != effector || effector.isTrap())) {
                        if (Config.DEBUFF_REOVERLAY
                           && !e.getSkill().hasEffectType(EffectType.CANCEL)
                           && e.getAbnormalType() != null
                           && !e.getAbnormalType().equalsIgnoreCase("none")
                           && e.getSkill().getSkillType() != SkillType.BUFF
                           && !e.getClass().getSimpleName().equalsIgnoreCase("buff")) {
                           effected.getEffectList().stopSkillEffect(e);
                        }

                        e.scheduleEffect(true);
                        effects.add(e);
                     }
                  } else if (effector.isPlayer()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                     sm.addCharName(effected);
                     sm.addSkillName(this);
                     ((Player)effector).sendPacket(sm);
                  }
               }

               return effects.isEmpty() ? _emptyEffectSet : effects.toArray(new Effect[effects.size()]);
            }
         } else {
            return _emptyEffectSet;
         }
      } else {
         return _emptyEffectSet;
      }
   }

   public final Effect[] getEffects(Creature effector, Creature effected, boolean allowSkillMastery) {
      return this.getEffects(effector, effected, null, allowSkillMastery);
   }

   public final Effect[] getEffects(CubicInstance effector, Creature effected, Env env) {
      if (this.hasEffects() && !this.isPassive()) {
         if (effector.getOwner() != effected && (this.isDebuff() || this.isOffensive())) {
            if (effected.isInvul() && !this.IsIgnoreInvincible()) {
               return _emptyEffectSet;
            }

            if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage()) {
               return _emptyEffectSet;
            }
         }

         List<Effect> effects = new ArrayList<>(this._effectTemplates.length);
         if (env == null) {
            env = new Env();
         }

         env.setCharacter(effector.getOwner());
         env.setCubic(effector);
         env.setTarget(effected);
         env.setSkill(this);

         for(EffectTemplate et : this._effectTemplates) {
            if (Formulas.calcEffectSuccess(
               effector.getOwner(), effected, et, this, env.getShield(), env.isSoulShot(), env.isSpiritShot(), env.isBlessedSpiritShot()
            )) {
               Effect e = et.getEffect(env);
               if (e != null) {
                  e.scheduleEffect(true);
                  effects.add(e);
               }
            }
         }

         return effects.isEmpty() ? _emptyEffectSet : effects.toArray(new Effect[effects.size()]);
      } else {
         return _emptyEffectSet;
      }
   }

   public final Effect[] getEffectsSelf(Creature effector) {
      if (this.hasSelfEffects() && !this.isPassive()) {
         List<Effect> effects = new ArrayList<>(this._effectTemplatesSelf.length);

         for(EffectTemplate et : this._effectTemplatesSelf) {
            Env env = new Env();
            env.setCharacter(effector);
            env.setTarget(effector);
            env.setSkill(this);
            Effect e = et.getEffect(env);
            if (e != null) {
               e.setSelfEffect();
               e.scheduleEffect(true);
               effects.add(e);
            }
         }

         return effects.isEmpty() ? _emptyEffectSet : effects.toArray(new Effect[effects.size()]);
      } else {
         return _emptyEffectSet;
      }
   }

   public final Effect[] getEffectsPassive(Creature effector) {
      if (!this.hasPassiveEffects()) {
         return _emptyEffectSet;
      } else {
         List<Effect> effects = new ArrayList<>(this._effectTemplatesPassive.length);

         for(EffectTemplate et : this._effectTemplatesPassive) {
            Env env = new Env();
            env.setCharacter(effector);
            env.setTarget(effector);
            env.setSkill(this);
            Effect e = et.getEffect(env);
            if (e != null) {
               e.setPassiveEffect();
               e.scheduleEffect(true);
               effects.add(e);
            }
         }

         return effects.isEmpty() ? _emptyEffectSet : effects.toArray(new Effect[effects.size()]);
      }
   }

   public final void attach(FuncTemplate f) {
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

   public final void attach(EffectTemplate effect) {
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

   public final void attachSelf(EffectTemplate effect) {
      if (this._effectTemplatesSelf == null) {
         this._effectTemplatesSelf = new EffectTemplate[]{effect};
      } else {
         int len = this._effectTemplatesSelf.length;
         EffectTemplate[] tmp = new EffectTemplate[len + 1];
         System.arraycopy(this._effectTemplatesSelf, 0, tmp, 0, len);
         tmp[len] = effect;
         this._effectTemplatesSelf = tmp;
      }
   }

   public final void attachPassive(EffectTemplate effect) {
      if (this._effectTemplatesPassive == null) {
         this._effectTemplatesPassive = new EffectTemplate[]{effect};
      } else {
         int len = this._effectTemplatesPassive.length;
         EffectTemplate[] tmp = new EffectTemplate[len + 1];
         System.arraycopy(this._effectTemplatesPassive, 0, tmp, 0, len);
         tmp[len] = effect;
         this._effectTemplatesPassive = tmp;
      }
   }

   public final void attach(Condition c, boolean itemOrWeapon) {
      if (itemOrWeapon) {
         if (this._itemPreCondition == null) {
            this._itemPreCondition = new ArrayList<>();
         }

         this._itemPreCondition.add(c);
      } else {
         if (this._preCondition == null) {
            this._preCondition = new ArrayList<>();
         }

         this._preCondition.add(c);
      }
   }

   @Override
   public String toString() {
      return "Skill " + this._nameEn + "(" + this._id + "," + this._level + ")";
   }

   public int getFeed() {
      return this._feed;
   }

   public int getReferenceItemId() {
      return this._refId;
   }

   public int getAfterEffectId() {
      return this._afterEffectId;
   }

   public int getAfterEffectLvl() {
      return this._afterEffectLvl;
   }

   @Override
   public boolean triggersChanceSkill() {
      return this._triggeredId > 0 && this.isChance();
   }

   @Override
   public int getTriggeredChanceId() {
      return this._triggeredId;
   }

   @Override
   public int getTriggeredChanceLevel() {
      return this._triggeredLevel;
   }

   @Override
   public ChanceCondition getTriggeredChanceCondition() {
      return this._chanceCondition;
   }

   public String getAttributeName() {
      return this._attribute;
   }

   public int getBlowChance() {
      return this._blowChance;
   }

   public boolean ignoreShield() {
      return this._ignoreShield;
   }

   public boolean canBeReflected() {
      return this._canBeReflected;
   }

   public boolean canBeDispeled() {
      return this._canBeDispeled;
   }

   public boolean isClanSkill() {
      return this._isClanSkill;
   }

   public boolean isExcludedFromCheck() {
      return this._excludedFromCheck;
   }

   public boolean getDependOnTargetBuff() {
      return this._dependOnTargetBuff;
   }

   public boolean isSimultaneousCast() {
      return this._simultaneousCast;
   }

   public boolean isCustom() {
      return this._isCustom;
   }

   public boolean isDisableGeoCheck() {
      return this._disableGeoCheck;
   }

   private ExtractableSkillTemplate parseExtractableSkill(int skillId, int skillLvl, String values) {
      String[] prodLists = values.split(";");
      List<ExtractableProductItemTemplate> products = new ArrayList<>();

      for(String prodList : prodLists) {
         String[] prodData = prodList.split(",");
         if (prodData.length < 3) {
            _log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
         }

         List<ItemHolder> items = null;
         double chance = 0.0;
         int prodId = 0;
         int quantity = 0;
         int lenght = prodData.length - 1;

         try {
            items = new ArrayList<>(lenght / 2);

            for(int j = 0; j < lenght; ++j) {
               prodId = Integer.parseInt(prodData[j]);
               quantity = Integer.parseInt(prodData[++j]);
               if (prodId <= 0 || quantity <= 0) {
                  _log.warning(
                     "Extractable skills data: Error in Skill Id: "
                        + skillId
                        + " Level: "
                        + skillLvl
                        + " wrong production Id: "
                        + prodId
                        + " or wrond quantity: "
                        + quantity
                        + "!"
                  );
               }

               items.add(new ItemHolder(prodId, (long)quantity));
            }

            chance = Double.parseDouble(prodData[lenght]);
         } catch (Exception var18) {
            _log.warning(
               "Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!"
            );
         }

         products.add(new ExtractableProductItemTemplate(items, chance));
      }

      if (products.isEmpty()) {
         _log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
      }

      return new ExtractableSkillTemplate(SkillsParser.getSkillHashCode(skillId, skillLvl), products);
   }

   public ExtractableSkillTemplate getExtractableSkill() {
      return this._extractableItems;
   }

   public final int getActivateRate() {
      return this._activateRate;
   }

   public final int getLevelModifier() {
      return this._levelModifier;
   }

   public int getNpcId() {
      return this._npcId;
   }

   public boolean hasEffectType(EffectType... types) {
      if (this.hasEffects() && types != null && types.length > 0) {
         if (this._effectTypes == null) {
            this._effectTypes = new byte[this._effectTemplates.length];
            Env env = new Env();
            env.setSkill(this);
            int i = 0;

            for(EffectTemplate et : this._effectTemplates) {
               Effect e = et.getEffect(env, true);
               if (e != null) {
                  this._effectTypes[i++] = (byte)e.getEffectType().ordinal();
               }
            }

            Arrays.sort(this._effectTypes);
         }

         for(EffectType type : types) {
            if (Arrays.binarySearch(this._effectTypes, (byte)type.ordinal()) >= 0) {
               return true;
            }
         }
      }

      return false;
   }

   public String getIcon() {
      return this._icon;
   }

   public int getEnergyConsume() {
      return this._energyConsume;
   }

   public static boolean getBlockBuffConditions(Creature activeChar, Creature aimingTarget) {
      if (activeChar.isPlayable() && activeChar.getActingPlayer() != null) {
         if (aimingTarget.isMonster()) {
            return false;
         }

         Player player = activeChar.getActingPlayer();
         if (aimingTarget.isPlayable() && aimingTarget.getActingPlayer() != null) {
            Player target = aimingTarget.getActingPlayer();
            if (player.getParty() != null && target.getParty() != null && player.getParty() == target.getParty()) {
               return true;
            }

            if (player.getClan() != null && target.getClan() != null) {
               if (player.getClan().getId() == target.getClan().getId() && !player.isInOlympiadMode()) {
                  return true;
               }

               if (player.getAllyId() > 0 && target.getAllyId() > 0 && player.getAllyId() == target.getAllyId() && !player.isInOlympiadMode()) {
                  return true;
               }
            }

            if (player.isInOlympiadMode() && player.getOlympiadSide() == target.getOlympiadSide()) {
               return true;
            }

            if (player.getTeam() != 0 && target.getTeam() != 0 && player.getTeam() == target.getTeam()) {
               return true;
            }

            if (player.isInSiege()) {
               return false;
            }

            if (player.isInsideZone(ZoneId.PVP)) {
               return false;
            }

            if (player.isInZonePeace()) {
               return false;
            }

            if (target.getKarma() > 0) {
               return false;
            }
         }
      }

      return false;
   }

   public boolean isNotTargetAoE() {
      switch(this._targetType) {
         case AURA:
         case FRONT_AURA:
         case BEHIND_AURA:
         case PARTY_CLAN:
         case CLAN:
         case PARTY_MEMBER:
         case PARTY:
            return true;
         default:
            return false;
      }
   }

   public boolean oneTarget() {
      switch(this._targetType) {
         case PARTY_MEMBER:
         case CORPSE:
         case CORPSE_PLAYER:
         case HOLY:
         case FLAGPOLE:
         case NONE:
         case ONE:
         case SERVITOR:
         case SUMMON:
         case PET:
         case OWNER_PET:
         case ENEMY_SUMMON:
         case SELF:
         case UNLOCKABLE:
            return true;
         case PARTY:
         default:
            return false;
      }
   }

   public boolean isAura() {
      switch(this._targetType) {
         case AURA:
         case FRONT_AURA:
         case BEHIND_AURA:
         case AURA_CORPSE_MOB:
         case AURA_UNDEAD_ENEMY:
            return true;
         default:
            return false;
      }
   }

   public boolean isArea() {
      switch(this._targetType) {
         case AREA:
         case AREA_FRIENDLY:
         case AREA_CORPSE_MOB:
         case AREA_SUMMON:
         case AREA_UNDEAD:
         case BEHIND_AREA:
         case FRONT_AREA:
            return true;
         default:
            return false;
      }
   }

   public double getSimpleDamage(Creature attacker, Creature target) {
      if (this.isMagic()) {
         double mAtk = attacker.getMAtk(target, this);
         double mdef = target.getMDef(null, this);
         double power = this.getPower();
         int sps = (attacker.isChargedShot(ShotType.SPIRITSHOTS) || attacker.isChargedShot(ShotType.BLESSED_SPIRITSHOTS)) && this.useSpiritShot() ? 2 : 1;
         return 91.0 * power * Math.sqrt((double)sps * mAtk) / mdef;
      } else {
         double pAtk = attacker.getPAtk(target);
         double pdef = target.getPDef(attacker);
         double power = this.getPower();
         int ss = attacker.isChargedShot(ShotType.SOULSHOTS) && this.useSpiritShot() ? 2 : 1;
         return (double)ss * (pAtk + power) * 70.0 / pdef;
      }
   }

   public final int getAOECastRange() {
      return Math.max(this._castRange, this._effectRange);
   }

   public boolean isHealingPotionSkill() {
      switch(this.getId()) {
         case 2031:
         case 2032:
         case 2037:
         case 26025:
         case 26026:
            return true;
         default:
            return false;
      }
   }

   public boolean isDeathlink() {
      return this._skillType == SkillType.DEATHLINK || this._id == 314;
   }

   public final int getSkillInterruptTime() {
      return this._skillInterruptTime;
   }

   public boolean isBlockSkillMastery() {
      return this._blockSkillMastery;
   }

   public boolean IsIgnoreInvincible() {
      return this._ignoreInvincible;
   }

   public boolean isSpoilSkill() {
      switch(this.getId()) {
         case 254:
         case 302:
         case 348:
         case 537:
         case 947:
            return true;
         default:
            return false;
      }
   }

   public boolean isSweepSkill() {
      switch(this.getId()) {
         case 42:
         case 444:
            return true;
         default:
            return false;
      }
   }

   public boolean isHandler() {
      return this._id >= 2000 && this._id <= 9100 && this._itemConsumeCount > 0;
   }

   public Map<String, Short> getNegateAbnormalTypes() {
      return this._negateAbnormalType;
   }

   public int getNegateRate() {
      return this._negateRate;
   }

   public boolean isItemSkill() {
      return this._nameEn.contains("Item Skill") || this._nameEn.contains("Talisman");
   }

   public boolean isPetMajorHeal() {
      return this._petMajorHeal;
   }
}
