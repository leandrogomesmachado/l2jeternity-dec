package l2e.gameserver.model.actor.stat;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Calculator;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.type.WaterZone;

public class CharStat {
   private final Creature _activeChar;
   private long _exp = 0L;
   private int _sp = 0;
   private byte _level = 1;
   private double _oldMaxHp;
   private double _oldMaxMp;

   public CharStat(Creature activeChar) {
      this._activeChar = activeChar;
   }

   public final double calcStat(Stats stat, double init) {
      return this.calcStat(stat, init, null, null);
   }

   public final double calcStat(Stats stat, double init, Creature target, Skill skill) {
      double value = init;
      if (stat == null) {
         return init;
      } else {
         int id = stat.ordinal();
         Calculator c = this._activeChar.getCalculators()[id];
         if (c != null && c.size() != 0) {
            if (this.getActiveChar().isPlayer() && this.getActiveChar().isTransformed()) {
               double val = this.getActiveChar().getTransformation().getStat(this.getActiveChar().getActingPlayer(), stat);
               if (val > 0.0) {
                  value = val;
               }
            }

            Env env = new Env();
            env.setCharacter(this._activeChar);
            env.setTarget(target);
            env.setSkill(skill);
            env.setValue(value);
            c.calc(env);
            if (env.getValue() <= 0.0) {
               switch(stat) {
                  case MAX_HP:
                  case MAX_MP:
                  case MAX_CP:
                  case MAGIC_DEFENCE:
                  case POWER_DEFENCE:
                  case POWER_ATTACK:
                  case MAGIC_ATTACK:
                  case POWER_ATTACK_SPEED:
                  case MAGIC_ATTACK_SPEED:
                  case SHIELD_DEFENCE:
                  case STAT_CON:
                  case STAT_DEX:
                  case STAT_INT:
                  case STAT_MEN:
                  case STAT_STR:
                  case STAT_WIT:
                     env.setValue(1.0);
               }
            }

            return env.getValue();
         } else {
            return init;
         }
      }
   }

   public int getAccuracy() {
      return (int)Math.round(this.calcStat(Stats.ACCURACY_COMBAT, 0.0, null, null));
   }

   public Creature getActiveChar() {
      return this._activeChar;
   }

   public float getAttackSpeedMultiplier() {
      return (float)(1.1 * this.getPAtkSpd() / this._activeChar.getTemplate().getBasePAtkSpd());
   }

   public final int getCON() {
      return (int)this.calcStat(Stats.STAT_CON, (double)this._activeChar.getTemplate().getBaseCON());
   }

   public double getCriticalDmg(Creature target, double init, Skill skill) {
      return this.calcStat(Stats.CRITICAL_DAMAGE, init, target, skill);
   }

   public double getCriticalHit(Creature target, Skill skill) {
      return Math.min(this.calcStat(Stats.CRITICAL_RATE, this._activeChar.getTemplate().getBaseCritRate(), target, skill), (double)Config.MAX_PCRIT_RATE);
   }

   public final int getDEX() {
      return (int)this.calcStat(Stats.STAT_DEX, (double)this._activeChar.getTemplate().getBaseDEX());
   }

   public int getEvasionRate(Creature target) {
      int val = (int)Math.round(this.calcStat(Stats.EVASION_RATE, 0.0, target, null));
      if (val > Config.MAX_EVASION && !this._activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
         val = Config.MAX_EVASION;
      }

      return val;
   }

   public long getExp() {
      return this._exp;
   }

   public void setExp(long value) {
      this._exp = value;
   }

   public int getINT() {
      return (int)this.calcStat(Stats.STAT_INT, (double)this._activeChar.getTemplate().getBaseINT());
   }

   public byte getLevel() {
      return this._level;
   }

   public void setLevel(byte value) {
      this._level = value;
   }

   public final int getMagicalAttackRange(Skill skill) {
      return skill != null
         ? (int)this.calcStat(Stats.MAGIC_ATTACK_RANGE, (double)skill.getCastRange(), null, skill)
         : this._activeChar.getTemplate().getBaseAttackRange();
   }

   public double getMaxCp() {
      return this.calcStat(Stats.MAX_CP, this._activeChar.getTemplate().getBaseCpMax());
   }

   public int getMaxRecoverableCp() {
      return (int)this.calcStat(Stats.MAX_RECOVERABLE_CP, this.getMaxCp());
   }

   public double getMaxHp() {
      double val = 1.0;
      if (this._activeChar.isServitor()) {
         Player owner = ((Summon)this._activeChar).getOwner();
         double addHp = owner != null ? owner.getMaxHp() * (owner.getServitorShareBonus(Stats.MAX_HP) - 1.0) : 0.0;
         val = this.calcStat(Stats.MAX_HP, this._activeChar.getTemplate().getBaseHpMax() + addHp);
      } else if (this._activeChar.getChampionTemplate() != null) {
         val = this.calcStat(Stats.MAX_HP, this._activeChar.getTemplate().getBaseHpMax() * this._activeChar.getChampionTemplate().hpMultiplier);
      } else {
         val = this.calcStat(Stats.MAX_HP, this._activeChar.getTemplate().getBaseHpMax());
      }

      if (val != this._oldMaxHp && this._activeChar.isSummon()) {
         this._oldMaxHp = val;
         if (this._activeChar.getStatus().getCurrentHp() != val) {
            this._activeChar.getStatus().setCurrentHp(this._activeChar.getStatus().getCurrentHp());
         }
      }

      return val;
   }

   public int getMaxRecoverableHp() {
      return (int)this.calcStat(Stats.MAX_RECOVERABLE_HP, this.getMaxHp());
   }

   public double getMaxMp() {
      double val = 1.0;
      if (this._activeChar.isServitor()) {
         Player owner = ((Summon)this._activeChar).getOwner();
         double addMp = owner != null ? owner.getMaxMp() * (owner.getServitorShareBonus(Stats.MAX_MP) - 1.0) : 0.0;
         val = this.calcStat(Stats.MAX_MP, this._activeChar.getTemplate().getBaseMpMax() + addMp);
      } else {
         val = this.calcStat(Stats.MAX_MP, this._activeChar.getTemplate().getBaseMpMax());
      }

      if (val != this._oldMaxMp && this._activeChar.isSummon()) {
         this._oldMaxMp = val;
         if (this._activeChar.getStatus().getCurrentMp() != val) {
            this._activeChar.getStatus().setCurrentMp(this._activeChar.getStatus().getCurrentMp());
         }
      }

      return val;
   }

   public int getMaxRecoverableMp() {
      return (int)this.calcStat(Stats.MAX_RECOVERABLE_MP, this.getMaxMp());
   }

   public double getMAtk(Creature target, Skill skill) {
      float bonusAtk = 1.0F;
      if (this._activeChar.getChampionTemplate() != null) {
         bonusAtk = this._activeChar.getChampionTemplate().matkMultiplier;
      }

      if (this._activeChar.isRaid()) {
         bonusAtk = (float)((double)bonusAtk * Config.RAID_MATTACK_MULTIPLIER);
      }

      return this.calcStat(Stats.MAGIC_ATTACK, this._activeChar.getTemplate().getBaseMAtk() * (double)bonusAtk, target, skill);
   }

   public double getMAtkSpd() {
      double bonusSpdAtk = 1.0;
      if (this._activeChar.getChampionTemplate() != null) {
         bonusSpdAtk = (double)this._activeChar.getChampionTemplate().matkSpdMultiplier;
      }

      double val = this.calcStat(Stats.MAGIC_ATTACK_SPEED, this._activeChar.getTemplate().getBaseMAtkSpd() * bonusSpdAtk) * (double)Config.MATK_SPEED_MULTI;
      if (val > (double)Config.MAX_MATK_SPEED && !this._activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
         val = (double)Config.MAX_MATK_SPEED;
      }

      return val;
   }

   public double getMCriticalHit(Creature target, Skill skill) {
      double mrate = this.calcStat(Stats.MCRITICAL_RATE, 1.0, target, skill) * 10.0;
      return Math.min(mrate, (double)Config.MAX_MCRIT_RATE);
   }

   public double getMDef(Creature target, Skill skill) {
      double defence = this._activeChar.getTemplate().getBaseMDef();
      if (this._activeChar.getChampionTemplate() != null) {
         defence *= (double)this._activeChar.getChampionTemplate().mdefMultiplier;
      }

      if (this._activeChar.isRaid()) {
         defence *= Config.RAID_MDEFENCE_MULTIPLIER;
      }

      return this.calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
   }

   public final int getMEN() {
      return (int)this.calcStat(Stats.STAT_MEN, (double)this._activeChar.getTemplate().getBaseMEN());
   }

   public double getMovementSpeedMultiplier() {
      double baseSpeed;
      if (this._activeChar.isInWater(this._activeChar)) {
         WaterZone waterZone = ZoneManager.getInstance().getZone(this._activeChar, WaterZone.class);
         if (waterZone != null && waterZone.canUseWaterTask()) {
            baseSpeed = this.getBaseMoveSpeed(this._activeChar.isRunning() ? MoveType.FAST_SWIM : MoveType.SLOW_SWIM);
         } else {
            baseSpeed = this.getBaseMoveSpeed(this._activeChar.isRunning() ? MoveType.RUN : MoveType.WALK);
         }
      } else {
         baseSpeed = this.getBaseMoveSpeed(this._activeChar.isRunning() ? MoveType.RUN : MoveType.WALK);
      }

      return this.getMoveSpeed() * (1.0 / baseSpeed);
   }

   public double getBaseMoveSpeed(MoveType type) {
      return this._activeChar.getTemplate().getBaseMoveSpeed(type);
   }

   public double getMoveSpeed() {
      if (this._activeChar.isInWater(this._activeChar)) {
         return this._activeChar.isRunning() ? this.getSwimRunSpeed() : this.getSwimWalkSpeed();
      } else {
         return this._activeChar.isRunning() ? this.getRunSpeed() : this.getWalkSpeed();
      }
   }

   public final double getMReuseRate(Skill skill) {
      return this.calcStat(Stats.MAGIC_REUSE_RATE, (double)this._activeChar.getTemplate().getBaseMReuseRate(), null, skill);
   }

   public double getPAtk(Creature target) {
      double bonusAtk = 1.0;
      if (this._activeChar.getChampionTemplate() != null) {
         bonusAtk = (double)this._activeChar.getChampionTemplate().patkMultiplier;
      }

      if (this._activeChar.isRaid()) {
         bonusAtk *= Config.RAID_PATTACK_MULTIPLIER;
      }

      return this.calcStat(Stats.POWER_ATTACK, this._activeChar.getTemplate().getBasePAtk() * bonusAtk, target, null);
   }

   public final double getPAtkAnimals(Creature target) {
      return this.calcStat(Stats.PATK_ANIMALS, 1.0, target, null);
   }

   public final double getPAtkDragons(Creature target) {
      return this.calcStat(Stats.PATK_DRAGONS, 1.0, target, null);
   }

   public final double getPAtkInsects(Creature target) {
      return this.calcStat(Stats.PATK_INSECTS, 1.0, target, null);
   }

   public final double getPAtkMonsters(Creature target) {
      return this.calcStat(Stats.PATK_MONSTERS, 1.0, target, null);
   }

   public final double getPAtkPlants(Creature target) {
      return this.calcStat(Stats.PATK_PLANTS, 1.0, target, null);
   }

   public final double getPAtkGiants(Creature target) {
      return this.calcStat(Stats.PATK_GIANTS, 1.0, target, null);
   }

   public final double getPAtkMagicCreatures(Creature target) {
      return this.calcStat(Stats.PATK_MCREATURES, 1.0, target, null);
   }

   public double getPAtkSpd() {
      double bonusAtk = 1.0;
      if (this._activeChar.getChampionTemplate() != null) {
         bonusAtk = (double)this._activeChar.getChampionTemplate().atkSpdMultiplier;
      }

      return (double)Math.round(
         this.calcStat(Stats.POWER_ATTACK_SPEED, this._activeChar.getTemplate().getBasePAtkSpd() * bonusAtk, null, null) * (double)Config.PATK_SPEED_MULTI
      );
   }

   public final double getPDefAnimals(Creature target) {
      return this.calcStat(Stats.PDEF_ANIMALS, 1.0, target, null);
   }

   public final double getPDefDragons(Creature target) {
      return this.calcStat(Stats.PDEF_DRAGONS, 1.0, target, null);
   }

   public final double getPDefInsects(Creature target) {
      return this.calcStat(Stats.PDEF_INSECTS, 1.0, target, null);
   }

   public final double getPDefMonsters(Creature target) {
      return this.calcStat(Stats.PDEF_MONSTERS, 1.0, target, null);
   }

   public final double getPDefPlants(Creature target) {
      return this.calcStat(Stats.PDEF_PLANTS, 1.0, target, null);
   }

   public final double getPDefGiants(Creature target) {
      return this.calcStat(Stats.PDEF_GIANTS, 1.0, target, null);
   }

   public final double getPDefMagicCreatures(Creature target) {
      return this.calcStat(Stats.PDEF_MCREATURES, 1.0, target, null);
   }

   public double getPDef(Creature target) {
      float bonusPdef = 1.0F;
      if (this._activeChar.getChampionTemplate() != null) {
         bonusPdef = this._activeChar.getChampionTemplate().pdefMultiplier;
      }

      return this.calcStat(
         Stats.POWER_DEFENCE,
         this._activeChar.isRaid()
            ? this._activeChar.getTemplate().getBasePDef() * Config.RAID_PDEFENCE_MULTIPLIER
            : this._activeChar.getTemplate().getBasePDef() * (double)bonusPdef,
         target,
         null
      );
   }

   public final int getPhysicalAttackRange() {
      Weapon weapon = this._activeChar.getActiveWeaponItem();
      if (this._activeChar.isTransformed() && this._activeChar.isPlayer()) {
         return this._activeChar.getTransformation().getBaseAttackRange(this._activeChar.getActingPlayer());
      } else {
         int baseAttackRange;
         if (weapon != null) {
            baseAttackRange = weapon.getBaseAttackRange();
         } else {
            baseAttackRange = this._activeChar.getTemplate().getBaseAttackRange();
         }

         return (int)this.calcStat(Stats.POWER_ATTACK_RANGE, (double)baseAttackRange, null, null);
      }
   }

   public final double getWeaponReuseModifier(Creature target) {
      return this.calcStat(Stats.ATK_REUSE, 1.0, target, null);
   }

   public double getRunSpeed() {
      double baseRunSpd = this._activeChar.isInWater(this._activeChar) ? this.getSwimRunSpeed() : this.getBaseMoveSpeed(MoveType.RUN);
      if (baseRunSpd <= 0.0) {
         return 0.0;
      } else {
         double bonus = 1.0;
         if (this._activeChar.isPet()) {
            if (((PetInstance)this._activeChar).isUncontrollable()) {
               bonus = 0.5;
            } else if (((PetInstance)this._activeChar).isHungry()) {
               bonus = 0.7;
            }
         }

         return this.calcStat(Stats.MOVE_SPEED, baseRunSpd * bonus, null, null);
      }
   }

   public double getWalkSpeed() {
      double baseWalkSpd = this._activeChar.isInWater(this._activeChar) ? this.getSwimWalkSpeed() : this.getBaseMoveSpeed(MoveType.WALK);
      if (baseWalkSpd <= 0.0) {
         return 0.0;
      } else {
         double bonus = 1.0;
         if (this._activeChar.isPet()) {
            if (((PetInstance)this._activeChar).isUncontrollable()) {
               bonus = 0.5;
            } else if (((PetInstance)this._activeChar).isHungry()) {
               bonus = 0.7;
            }
         }

         return this.calcStat(Stats.MOVE_SPEED, baseWalkSpd * bonus);
      }
   }

   public double getSwimRunSpeed() {
      double baseRunSpd = this.getBaseMoveSpeed(MoveType.FAST_SWIM);
      return baseRunSpd <= 0.0 ? 0.0 : this.calcStat(Stats.MOVE_SPEED, baseRunSpd, null, null);
   }

   public double getSwimWalkSpeed() {
      double baseWalkSpd = this.getBaseMoveSpeed(MoveType.SLOW_SWIM);
      return baseWalkSpd <= 0.0 ? 0.0 : this.calcStat(Stats.MOVE_SPEED, baseWalkSpd);
   }

   public final int getShldDef() {
      return (int)this.calcStat(Stats.SHIELD_DEFENCE, 0.0);
   }

   public int getSp() {
      return this._sp;
   }

   public void setSp(int value) {
      this._sp = value;
   }

   public final int getSTR() {
      return (int)this.calcStat(Stats.STAT_STR, (double)this._activeChar.getTemplate().getBaseSTR());
   }

   public final int getWIT() {
      return (int)this.calcStat(Stats.STAT_WIT, (double)this._activeChar.getTemplate().getBaseWIT());
   }

   public final int getMpConsume(Skill skill) {
      if (skill == null) {
         return 1;
      } else {
         double mpConsume = (double)skill.getMpConsume();
         double nextDanceMpCost = Math.ceil((double)skill.getMpConsume() / 2.0);
         if (skill.isDance() && Config.DANCE_CONSUME_ADDITIONAL_MP && this._activeChar != null && this._activeChar.getDanceCount() > 0) {
            mpConsume += (double)this._activeChar.getDanceCount() * nextDanceMpCost;
         }

         mpConsume = this.calcStat(Stats.MP_CONSUME, mpConsume, null, skill);
         if (skill.isDance()) {
            return (int)this.calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume);
         } else {
            return skill.isMagic()
               ? (int)this.calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume)
               : (int)this.calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume);
         }
      }
   }

   public final int getMpInitialConsume(Skill skill) {
      if (skill == null) {
         return 1;
      } else {
         double mpConsume = this.calcStat(Stats.MP_CONSUME, (double)skill.getMpInitialConsume(), null, skill);
         if (skill.isDance()) {
            return (int)this.calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume);
         } else {
            return skill.isMagic()
               ? (int)this.calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume)
               : (int)this.calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume);
         }
      }
   }

   public byte getAttackElement() {
      ItemInstance weaponInstance = this._activeChar.getActiveWeaponInstance();
      if (weaponInstance != null && weaponInstance.getAttackElementType() >= 0) {
         return weaponInstance.getAttackElementType();
      } else {
         int tempVal = 0;
         int[] stats = new int[]{0, 0, 0, 0, 0, 0};
         byte returnVal = -2;
         stats[0] = (int)this.calcStat(Stats.FIRE_POWER, (double)this._activeChar.getTemplate().getBaseFire());
         stats[1] = (int)this.calcStat(Stats.WATER_POWER, (double)this._activeChar.getTemplate().getBaseWater());
         stats[2] = (int)this.calcStat(Stats.WIND_POWER, (double)this._activeChar.getTemplate().getBaseWind());
         stats[3] = (int)this.calcStat(Stats.EARTH_POWER, (double)this._activeChar.getTemplate().getBaseEarth());
         stats[4] = (int)this.calcStat(Stats.HOLY_POWER, (double)this._activeChar.getTemplate().getBaseHoly());
         stats[5] = (int)this.calcStat(Stats.DARK_POWER, (double)this._activeChar.getTemplate().getBaseDark());

         for(byte x = 0; x < 6; ++x) {
            if (stats[x] > tempVal) {
               returnVal = x;
               tempVal = stats[x];
            }
         }

         return returnVal;
      }
   }

   public int getAttackElementValue(byte attackAttribute) {
      switch(attackAttribute) {
         case 0:
            return (int)this.calcStat(Stats.FIRE_POWER, (double)this._activeChar.getTemplate().getBaseFire());
         case 1:
            return (int)this.calcStat(Stats.WATER_POWER, (double)this._activeChar.getTemplate().getBaseWater());
         case 2:
            return (int)this.calcStat(Stats.WIND_POWER, (double)this._activeChar.getTemplate().getBaseWind());
         case 3:
            return (int)this.calcStat(Stats.EARTH_POWER, (double)this._activeChar.getTemplate().getBaseEarth());
         case 4:
            return (int)this.calcStat(Stats.HOLY_POWER, (double)this._activeChar.getTemplate().getBaseHoly());
         case 5:
            return (int)this.calcStat(Stats.DARK_POWER, (double)this._activeChar.getTemplate().getBaseDark());
         default:
            return 0;
      }
   }

   public int getDefenseElementValue(byte defenseAttribute) {
      switch(defenseAttribute) {
         case 0:
            return (int)this.calcStat(Stats.FIRE_RES, this._activeChar.getTemplate().getBaseFireRes());
         case 1:
            return (int)this.calcStat(Stats.WATER_RES, this._activeChar.getTemplate().getBaseWaterRes());
         case 2:
            return (int)this.calcStat(Stats.WIND_RES, this._activeChar.getTemplate().getBaseWindRes());
         case 3:
            return (int)this.calcStat(Stats.EARTH_RES, this._activeChar.getTemplate().getBaseEarthRes());
         case 4:
            return (int)this.calcStat(Stats.HOLY_RES, this._activeChar.getTemplate().getBaseHolyRes());
         case 5:
            return (int)this.calcStat(Stats.DARK_RES, this._activeChar.getTemplate().getBaseDarkRes());
         default:
            return 0;
      }
   }

   public final double getRExp() {
      double val = this.calcStat(Stats.RUNE_OF_EXP, 1.0);
      if (val > 1.5) {
         val = 1.5;
      }

      return val;
   }

   public final double getRSp() {
      double val = this.calcStat(Stats.RUNE_OF_SP, 1.0);
      if (val > 1.5) {
         val = 1.5;
      }

      return val;
   }

   public double getPvpPhysSkillDmg() {
      return this.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0, null, null);
   }

   public double getPvpPhysSkillDef() {
      return this.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1.0, null, null);
   }

   public double getPvpPhysDef() {
      return this.calcStat(Stats.PVP_PHYSICAL_DEF, 1.0, null, null);
   }

   public double getPvpPhysDmg() {
      return this.calcStat(Stats.PVP_PHYSICAL_DMG, 1.0, null, null);
   }

   public double getPvpMagicDmg() {
      return this.calcStat(Stats.PVP_MAGICAL_DMG, 1.0, null, null);
   }

   public double getPvpMagicDef() {
      return this.calcStat(Stats.PVP_MAGICAL_DEF, 1.0, null, null);
   }
}
