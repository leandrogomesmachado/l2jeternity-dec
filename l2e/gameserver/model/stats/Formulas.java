package l2e.gameserver.model.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.util.PositionUtils;
import l2e.commons.util.Rnd;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.data.parser.ClassBalanceParser;
import l2e.gameserver.data.parser.HitConditionBonusParser;
import l2e.gameserver.data.parser.SkillBalanceParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.actor.instance.FortCommanderInstance;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.base.AttackType;
import l2e.gameserver.model.base.SkillChangeType;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.TraitType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.funcs.formulas.FuncArmorSet;
import l2e.gameserver.model.skills.funcs.formulas.FuncAtkAccuracy;
import l2e.gameserver.model.skills.funcs.formulas.FuncAtkCritical;
import l2e.gameserver.model.skills.funcs.formulas.FuncAtkEvasion;
import l2e.gameserver.model.skills.funcs.formulas.FuncGatesMDefMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncGatesPDefMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncHenna;
import l2e.gameserver.model.skills.funcs.formulas.FuncMAtkCritical;
import l2e.gameserver.model.skills.funcs.formulas.FuncMAtkMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncMAtkSpeed;
import l2e.gameserver.model.skills.funcs.formulas.FuncMDefMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncMaxCpMul;
import l2e.gameserver.model.skills.funcs.formulas.FuncMaxHpMul;
import l2e.gameserver.model.skills.funcs.formulas.FuncMaxMpMul;
import l2e.gameserver.model.skills.funcs.formulas.FuncMoveSpeed;
import l2e.gameserver.model.skills.funcs.formulas.FuncPAtkMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncPAtkSpeed;
import l2e.gameserver.model.skills.funcs.formulas.FuncPDefMod;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.CastleZone;
import l2e.gameserver.model.zone.type.ClanHallZone;
import l2e.gameserver.model.zone.type.FortZone;
import l2e.gameserver.model.zone.type.MotherTreeZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class Formulas {
   private static final Logger _log = Logger.getLogger(Formulas.class.getName());
   private static final int HP_REGENERATE_PERIOD = 3000;
   public static final byte SHIELD_DEFENSE_FAILED = 0;
   public static final byte SHIELD_DEFENSE_SUCCEED = 1;
   public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2;
   public static final byte SKILL_REFLECT_FAILED = 0;
   public static final byte SKILL_REFLECT_SUCCEED = 1;
   public static final byte SKILL_REFLECT_VENGEANCE = 2;
   private static final byte MELEE_ATTACK_RANGE = 40;

   public static int getRegeneratePeriod(Creature cha) {
      return cha.isDoor() ? 300000 : 3000;
   }

   public static Calculator[] getStdNPCCalculators() {
      Calculator[] std = new Calculator[Stats.NUM_STATS];
      std[Stats.MAX_HP.ordinal()] = new Calculator();
      std[Stats.MAX_HP.ordinal()].addFunc(FuncMaxHpMul.getInstance());
      std[Stats.MAX_MP.ordinal()] = new Calculator();
      std[Stats.MAX_MP.ordinal()].addFunc(FuncMaxMpMul.getInstance());
      std[Stats.POWER_ATTACK.ordinal()] = new Calculator();
      std[Stats.POWER_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());
      std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
      std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());
      std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
      std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncPDefMod.getInstance());
      std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
      std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncMDefMod.getInstance());
      std[Stats.CRITICAL_RATE.ordinal()] = new Calculator();
      std[Stats.CRITICAL_RATE.ordinal()].addFunc(FuncAtkCritical.getInstance());
      std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
      std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());
      std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
      std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
      std[Stats.EVASION_RATE.ordinal()] = new Calculator();
      std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
      std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
      std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());
      std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
      std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());
      std[Stats.MOVE_SPEED.ordinal()] = new Calculator();
      std[Stats.MOVE_SPEED.ordinal()].addFunc(FuncMoveSpeed.getInstance());
      return std;
   }

   public static Calculator[] getStdDoorCalculators() {
      Calculator[] std = new Calculator[Stats.NUM_STATS];
      std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
      std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
      std[Stats.EVASION_RATE.ordinal()] = new Calculator();
      std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
      std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
      std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncGatesPDefMod.getInstance());
      std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
      std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncGatesMDefMod.getInstance());
      return std;
   }

   public static void addFuncsToNewCharacter(Creature cha) {
      if (cha.isPlayer()) {
         cha.addStatFunc(FuncMaxHpMul.getInstance());
         cha.addStatFunc(FuncMaxCpMul.getInstance());
         cha.addStatFunc(FuncMaxMpMul.getInstance());
         cha.addStatFunc(FuncPAtkMod.getInstance());
         cha.addStatFunc(FuncMAtkMod.getInstance());
         cha.addStatFunc(FuncPDefMod.getInstance());
         cha.addStatFunc(FuncMDefMod.getInstance());
         cha.addStatFunc(FuncAtkCritical.getInstance());
         cha.addStatFunc(FuncMAtkCritical.getInstance());
         cha.addStatFunc(FuncAtkAccuracy.getInstance());
         cha.addStatFunc(FuncAtkEvasion.getInstance());
         cha.addStatFunc(FuncPAtkSpeed.getInstance());
         cha.addStatFunc(FuncMAtkSpeed.getInstance());
         cha.addStatFunc(FuncMoveSpeed.getInstance());
         cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_STR));
         cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_DEX));
         cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_INT));
         cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_MEN));
         cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_CON));
         cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_WIT));
         cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_STR));
         cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_DEX));
         cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_INT));
         cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_MEN));
         cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_CON));
         cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_WIT));
      } else if (cha.isSummon()) {
         cha.addStatFunc(FuncMaxHpMul.getInstance());
         cha.addStatFunc(FuncMaxMpMul.getInstance());
         cha.addStatFunc(FuncPAtkMod.getInstance());
         cha.addStatFunc(FuncMAtkMod.getInstance());
         cha.addStatFunc(FuncPDefMod.getInstance());
         cha.addStatFunc(FuncMDefMod.getInstance());
         cha.addStatFunc(FuncAtkCritical.getInstance());
         cha.addStatFunc(FuncMAtkCritical.getInstance());
         cha.addStatFunc(FuncAtkAccuracy.getInstance());
         cha.addStatFunc(FuncAtkEvasion.getInstance());
         cha.addStatFunc(FuncMoveSpeed.getInstance());
         cha.addStatFunc(FuncPAtkSpeed.getInstance());
         cha.addStatFunc(FuncMAtkSpeed.getInstance());
      }
   }

   public static final double calcHpRegen(Creature cha) {
      double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseHpRegen(cha.getLevel()) : cha.getTemplate().getBaseHpReg();
      double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
      double hpRegenBonus = 0.0;
      if (cha.getChampionTemplate() != null) {
         hpRegenMultiplier *= cha.getChampionTemplate().hpRegenMultiplier;
      }

      if (cha.isPlayer()) {
         Player player = cha.getActingPlayer();
         if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant()) {
            hpRegenMultiplier *= calcFestivalRegenModifier(player);
         } else {
            double siegeModifier = calcSiegeRegenModifier(player);
            if (siegeModifier > 0.0) {
               hpRegenMultiplier *= siegeModifier;
            }
         }

         if (player.isInsideZone(ZoneId.CLAN_HALL) && player.getClan() != null && player.getClan().getHideoutId() > 0) {
            ClanHallZone zone = ZoneManager.getInstance().getZone(player, ClanHallZone.class);
            int posChIndex = zone == null ? -1 : zone.getClanHallId();
            int clanHallIndex = player.getClan().getHideoutId();
            if (clanHallIndex > 0 && clanHallIndex == posChIndex) {
               ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
               if (clansHall != null && clansHall.getFunction(3) != null) {
                  hpRegenMultiplier *= 1.0 + (double)clansHall.getFunction(3).getLvl() / 100.0;
               }
            }
         }

         if (player.isInsideZone(ZoneId.CASTLE) && player.getClan() != null && player.getClan().getCastleId() > 0) {
            CastleZone zone = ZoneManager.getInstance().getZone(player, CastleZone.class);
            int posCastleIndex = zone == null ? -1 : zone.getCastleId();
            int castleIndex = player.getClan().getCastleId();
            if (castleIndex > 0 && castleIndex == posCastleIndex) {
               Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
               if (castle != null && castle.getFunction(2) != null) {
                  hpRegenMultiplier *= 1.0 + (double)castle.getFunction(2).getLvl() / 100.0;
               }
            }
         }

         if (player.isInsideZone(ZoneId.FORT) && player.getClan() != null && player.getClan().getFortId() > 0) {
            FortZone zone = ZoneManager.getInstance().getZone(player, FortZone.class);
            int posFortIndex = zone == null ? -1 : zone.getFortId();
            int fortIndex = player.getClan().getFortId();
            if (fortIndex > 0 && fortIndex == posFortIndex) {
               Fort fort = FortManager.getInstance().getFortById(fortIndex);
               if (fort != null && fort.getFunction(2) != null) {
                  hpRegenMultiplier *= 1.0 + (double)fort.getFunction(2).getLvl() / 100.0;
               }
            }
         }

         if (player.isInsideZone(ZoneId.MOTHER_TREE)) {
            MotherTreeZone zone = ZoneManager.getInstance().getZone(player, MotherTreeZone.class);
            int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
            hpRegenBonus += (double)hpBonus;
         }

         if (player.isSitting()) {
            hpRegenMultiplier *= 1.5;
         } else if (!player.isMoving()) {
            hpRegenMultiplier *= 1.1;
         } else if (player.isRunning()) {
            hpRegenMultiplier *= 0.7;
         }

         init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
      } else if (cha.isPet()) {
         init = (double)((PetInstance)cha).getPetLevelData().getPetRegenHP() * Config.PET_HP_REGEN_MULTIPLIER;
      }

      return cha.calcStat(Stats.REGENERATE_HP_RATE, Math.max(1.0, init), null, null) * hpRegenMultiplier + hpRegenBonus;
   }

   public static final double calcMpRegen(Creature cha) {
      double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseMpRegen(cha.getLevel()) : cha.getTemplate().getBaseMpReg();
      double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
      double mpRegenBonus = 0.0;
      if (cha.isPlayer()) {
         Player player = cha.getActingPlayer();
         if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant()) {
            mpRegenMultiplier *= calcFestivalRegenModifier(player);
         }

         if (player.isInsideZone(ZoneId.MOTHER_TREE)) {
            MotherTreeZone zone = ZoneManager.getInstance().getZone(player, MotherTreeZone.class);
            int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
            mpRegenBonus += (double)mpBonus;
         }

         if (player.isInsideZone(ZoneId.CLAN_HALL) && player.getClan() != null && player.getClan().getHideoutId() > 0) {
            ClanHallZone zone = ZoneManager.getInstance().getZone(player, ClanHallZone.class);
            int posChIndex = zone == null ? -1 : zone.getClanHallId();
            int clanHallIndex = player.getClan().getHideoutId();
            if (clanHallIndex > 0 && clanHallIndex == posChIndex) {
               ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
               if (clansHall != null && clansHall.getFunction(4) != null) {
                  mpRegenMultiplier *= 1.0 + (double)clansHall.getFunction(4).getLvl() / 100.0;
               }
            }
         }

         if (player.isInsideZone(ZoneId.CASTLE) && player.getClan() != null && player.getClan().getCastleId() > 0) {
            CastleZone zone = ZoneManager.getInstance().getZone(player, CastleZone.class);
            int posCastleIndex = zone == null ? -1 : zone.getCastleId();
            int castleIndex = player.getClan().getCastleId();
            if (castleIndex > 0 && castleIndex == posCastleIndex) {
               Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
               if (castle != null && castle.getFunction(3) != null) {
                  mpRegenMultiplier *= 1.0 + (double)castle.getFunction(3).getLvl() / 100.0;
               }
            }
         }

         if (player.isInsideZone(ZoneId.FORT) && player.getClan() != null && player.getClan().getFortId() > 0) {
            FortZone zone = ZoneManager.getInstance().getZone(player, FortZone.class);
            int posFortIndex = zone == null ? -1 : zone.getFortId();
            int fortIndex = player.getClan().getFortId();
            if (fortIndex > 0 && fortIndex == posFortIndex) {
               Fort fort = FortManager.getInstance().getFortById(fortIndex);
               if (fort != null && fort.getFunction(3) != null) {
                  mpRegenMultiplier *= 1.0 + (double)fort.getFunction(3).getLvl() / 100.0;
               }
            }
         }

         if (player.isSitting()) {
            mpRegenMultiplier *= 1.5;
         } else if (!player.isMoving()) {
            mpRegenMultiplier *= 1.1;
         } else if (player.isRunning()) {
            mpRegenMultiplier *= 0.7;
         }

         init *= cha.getLevelMod() * BaseStats.MEN.calcBonus(cha);
      } else if (cha.isPet()) {
         init = (double)((PetInstance)cha).getPetLevelData().getPetRegenMP() * Config.PET_MP_REGEN_MULTIPLIER;
      }

      return cha.calcStat(Stats.REGENERATE_MP_RATE, Math.max(1.0, init), null, null) * mpRegenMultiplier + mpRegenBonus;
   }

   public static final double calcCpRegen(Creature cha) {
      double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseCpRegen(cha.getLevel()) : cha.getTemplate().getBaseHpReg();
      double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
      double cpRegenBonus = 0.0;
      if (cha.isPlayer()) {
         Player player = cha.getActingPlayer();
         if (player.isSitting()) {
            cpRegenMultiplier *= 1.5;
         } else if (!player.isMoving()) {
            cpRegenMultiplier *= 1.1;
         } else if (player.isRunning()) {
            cpRegenMultiplier *= 0.7;
         }
      } else if (!cha.isMoving()) {
         cpRegenMultiplier *= 1.1;
      } else if (cha.isRunning()) {
         cpRegenMultiplier *= 0.7;
      }

      init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
      return cha.calcStat(Stats.REGENERATE_CP_RATE, Math.max(1.0, init), null, null) * cpRegenMultiplier + 0.0;
   }

   public static final double calcFestivalRegenModifier(Player activeChar) {
      int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
      int oracle = festivalInfo[0];
      int festivalId = festivalInfo[1];
      if (festivalId < 0) {
         return 0.0;
      } else {
         int[] festivalCenter;
         if (oracle == 2) {
            festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
         } else {
            festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
         }

         double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
         if (Config.DEBUG) {
            _log.info("Distance: " + distToCenter + ", RegenMulti: " + distToCenter * 2.5 / 50.0);
         }

         return 1.0 - distToCenter * 5.0E-4;
      }
   }

   public static final double calcSiegeRegenModifier(Player activeChar) {
      if (activeChar != null && activeChar.getClan() != null) {
         Siege siege = SiegeManager.getInstance().getSiege(activeChar.getX(), activeChar.getY(), activeChar.getZ());
         if (siege != null && siege.getIsInProgress()) {
            SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getId());
            return siegeClan != null && !siegeClan.getFlag().isEmpty() && Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true) ? 1.5 : 0.0;
         } else {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   public static double calcBlowDamage(Creature attacker, Creature target, Skill skill, byte shld, boolean ss) {
      double defence = target.getPDef(attacker);
      switch(shld) {
         case 1:
            defence += (double)target.getShldDef();
            break;
         case 2:
            return 1.0;
      }

      boolean isPvP = attacker.isPlayable() && target.isPlayer();
      boolean isPvE = attacker.isPlayable() && target.isAttackable();
      double power = skill.getPower(isPvP, isPvE);
      double damage = 0.0;
      double proximityBonus = 1.0;
      double graciaPhysSkillBonus = skill.isMagic() ? 1.0 : 1.10113;
      double ssboost = ss ? (double)(skill.getSSBoost() > 0.0F ? skill.getSSBoost() : 1.0F) : 1.0;
      double pvpBonus = 1.0;
      if (attacker.isPlayable() && target.isPlayable()) {
         pvpBonus *= attacker.getPvpPhysSkillDmg();
         defence *= target.getPvpPhysSkillDef();
      }

      proximityBonus = attacker.isBehindTarget() ? 1.2 : (attacker.isInFrontOfTarget() ? 1.1 : 1.0);
      damage *= calcValakasTrait(attacker, target, skill);
      double element = calcElemental(attacker, target, skill);
      double critDamage = attacker.getCriticalDmg(target, 1.0, skill);
      if (skill.getSSBoost() > 0.0F) {
         damage += 70.0
               * graciaPhysSkillBonus
               * (attacker.getPAtk(target) + power)
               / defence
               * critDamage
               * target.calcStat(Stats.CRIT_VULN, 1.0, target, skill)
               * ssboost
               * proximityBonus
               * element
               * pvpBonus
            + attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, target, skill) * 6.1 * 70.0 / defence * graciaPhysSkillBonus;
      } else {
         damage += 70.0
               * graciaPhysSkillBonus
               * (power + attacker.getPAtk(target) * ssboost)
               / defence
               * critDamage
               * target.calcStat(Stats.CRIT_VULN, 1.0, target, skill)
               * proximityBonus
               * element
               * pvpBonus
            + attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, target, skill) * 6.1 * 70.0 / defence * graciaPhysSkillBonus;
      }

      damage += target.calcStat(Stats.CRIT_ADD_VULN, 0.0, target, skill) * 6.1;
      damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
      damage *= attacker.getRandomDamageMultiplier();
      damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.Blow, attacker, target);
      int keyId = target.isPlayer() ? target.getActingPlayer().getClassId().getId() : (target.isPlayer() ? -1 : -2);
      damage *= SkillBalanceParser.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.SkillBlow, target);
      if (target.isAttackable()
         && !target.isRaid()
         && !target.isRaidMinion()
         && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY
         && attacker.getActingPlayer() != null
         && target.getLevel() - attacker.getActingPlayer().getLevel() >= 2) {
         int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
         if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size()) {
            damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1).floatValue();
         } else {
            damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff).floatValue();
         }
      }

      return damage < 1.0 ? 1.0 : damage;
   }

   public static void calcStunBreak(Creature target, boolean crit) {
      if (target != null && target.isStunned() && Rnd.chance(crit ? Config.STUN_CHANCE_CRIT_MOD : Config.STUN_CHANCE_MOD)) {
         target.stopEffects(EffectType.STUN);
         target.setIsStuned(false);
      }
   }

   public static final double calcPhysDam(Creature attacker, Creature target, Skill skill, byte shld, boolean crit, boolean ss) {
      boolean isPvP = attacker.isPlayable() && target.isPlayable();
      boolean isPvE = attacker.isPlayable() && target.isAttackable();
      double proximityBonus = skill == null ? (attacker.isBehindTarget() ? 1.2 : (attacker.isInFrontOfTarget() ? 1.0 : 1.1)) : 1.0;
      double damage = attacker.getPAtk(target);
      double defence = target.getPDef(attacker);
      damage *= calcValakasTrait(attacker, target, skill);
      if (isPvP) {
         defence *= skill != null ? target.getPvpPhysSkillDef() : target.getPvpPhysDef();
      }

      switch(shld) {
         case 1:
            if (!Config.ALT_GAME_SHIELD_BLOCKS) {
               defence += (double)target.getShldDef();
            }
         default:
            if (ss) {
               damage *= 2.0;
            }

            if (skill != null) {
               double skillpower = skill.getPower(attacker, target, isPvP, isPvE);
               if (skill.getSSBoost() > 0.0F && ss) {
                  skillpower *= (double)skill.getSSBoost();
                  damage += skillpower;
               } else {
                  damage += skillpower;
               }
            }

            Weapon weapon = attacker.getActiveWeaponItem();
            Stats stat = null;
            boolean isBow = false;
            if (weapon != null && !attacker.isTransformed()) {
               switch(weapon.getItemType()) {
                  case BOW:
                     isBow = true;
                     stat = Stats.BOW_WPN_VULN;
                     break;
                  case CROSSBOW:
                     isBow = true;
                     stat = Stats.CROSSBOW_WPN_VULN;
                     break;
                  case BLUNT:
                     stat = Stats.BLUNT_WPN_VULN;
                     break;
                  case DAGGER:
                     stat = Stats.DAGGER_WPN_VULN;
                     break;
                  case DUAL:
                     stat = Stats.DUAL_WPN_VULN;
                     break;
                  case DUALFIST:
                     stat = Stats.DUALFIST_WPN_VULN;
                     break;
                  case ETC:
                     stat = Stats.ETC_WPN_VULN;
                     break;
                  case FIST:
                     stat = Stats.FIST_WPN_VULN;
                     break;
                  case POLE:
                     stat = Stats.POLE_WPN_VULN;
                     break;
                  case SWORD:
                     stat = Stats.SWORD_WPN_VULN;
                     break;
                  case BIGSWORD:
                     stat = Stats.BIGSWORD_WPN_VULN;
                     break;
                  case BIGBLUNT:
                     stat = Stats.BIGBLUNT_WPN_VULN;
                     break;
                  case DUALDAGGER:
                     stat = Stats.DUALDAGGER_WPN_VULN;
                     break;
                  case RAPIER:
                     stat = Stats.RAPIER_WPN_VULN;
                     break;
                  case ANCIENTSWORD:
                     stat = Stats.ANCIENT_WPN_VULN;
               }
            }

            if (attacker.isServitor()) {
               stat = Stats.PET_WPN_VULN;
            }

            if (crit) {
               damage = 2.0
                  * attacker.getCriticalDmg(target, 1.0, skill)
                  * target.calcStat(Stats.CRIT_VULN, target.getTemplate().getBaseCritVuln(), target, null)
                  * (76.0 * damage * proximityBonus / defence);
               damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, target, skill) * 77.0 / defence;
               if (skill != null) {
                  damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.PSkillCritical, attacker, target);
                  int keyId = target.isPlayer() ? target.getActingPlayer().getClassId().getId() : (target.isMonster() ? -1 : -2);
                  damage *= SkillBalanceParser.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.PCrit, target);
               } else {
                  damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.Crit, attacker, target);
               }
            } else {
               damage = 76.0 * damage * proximityBonus / defence;
               if (skill != null) {
                  damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.PSkillDamage, attacker, target);
               } else {
                  damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.Normal, attacker, target);
               }
            }

            if (skill != null && damage > 1.0 && skill.isDeathlink()) {
               damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
            }

            if (stat != null) {
               damage = target.calcStat(stat, damage, target, null);
            }

            if (skill == null) {
               damage *= attacker.getRandomDamageMultiplier();
            }

            if (shld > 0 && Config.ALT_GAME_SHIELD_BLOCKS) {
               damage -= (double)target.getShldDef();
               if (damage < 0.0) {
                  damage = 0.0;
               }
            }

            if (target.isNpc()) {
               switch(((Npc)target).getTemplate().getRace()) {
                  case BEAST:
                     damage *= attacker.getPAtkMonsters(target);
                     break;
                  case ANIMAL:
                     damage *= attacker.getPAtkAnimals(target);
                     break;
                  case PLANT:
                     damage *= attacker.getPAtkPlants(target);
                     break;
                  case DRAGON:
                     damage *= attacker.getPAtkDragons(target);
                     break;
                  case BUG:
                     damage *= attacker.getPAtkInsects(target);
                     break;
                  case GIANT:
                     damage *= attacker.getPAtkGiants(target);
                     break;
                  case MAGICCREATURE:
                     damage *= attacker.getPAtkMagicCreatures(target);
               }
            }

            if (damage > 0.0 && damage < 1.0) {
               damage = 1.0;
            } else if (damage < 0.0) {
               damage = 0.0;
            }

            if (isPvP) {
               damage *= skill != null ? attacker.getPvpPhysSkillDmg() : attacker.getPvpPhysDmg();
            }

            if (skill != null) {
               damage = attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, damage, null, null);
            }

            damage *= calcElemental(attacker, target, skill);
            if (target.isAttackable()) {
               if (isBow) {
                  if (skill != null) {
                     damage *= attacker.calcStat(Stats.PVE_BOW_SKILL_DMG, 1.0, null, null);
                  } else {
                     damage *= attacker.calcStat(Stats.PVE_BOW_DMG, 1.0, null, null);
                  }
               } else {
                  damage *= attacker.calcStat(Stats.PVE_PHYSICAL_DMG, 1.0, null, null);
               }

               if (!target.isRaid()
                  && !target.isRaidMinion()
                  && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY
                  && attacker.getActingPlayer() != null
                  && target.getLevel() - attacker.getActingPlayer().getLevel() >= 2) {
                  int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
                  if (skill != null) {
                     if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size()) {
                        damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1).floatValue();
                     } else {
                        damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff).floatValue();
                     }
                  } else if (crit) {
                     if (lvlDiff >= Config.NPC_CRIT_DMG_PENALTY.size()) {
                        damage *= (double)Config.NPC_CRIT_DMG_PENALTY.get(Config.NPC_CRIT_DMG_PENALTY.size() - 1).floatValue();
                     } else {
                        damage *= (double)Config.NPC_CRIT_DMG_PENALTY.get(lvlDiff).floatValue();
                     }
                  } else if (lvlDiff >= Config.NPC_DMG_PENALTY.size()) {
                     damage *= (double)Config.NPC_DMG_PENALTY.get(Config.NPC_DMG_PENALTY.size() - 1).floatValue();
                  } else {
                     damage *= (double)Config.NPC_DMG_PENALTY.get(lvlDiff).floatValue();
                  }
               }
            }

            if (target.isPlayer() && weapon != null && weapon.getItemType() == WeaponType.DAGGER && skill != null) {
               Armor armor = ((Player)target).getActiveChestArmorItem();
               if (armor != null) {
                  if (((Player)target).isWearingHeavyArmor()) {
                     damage /= (double)Config.ALT_DAGGER_DMG_VS_HEAVY;
                  }

                  if (((Player)target).isWearingLightArmor()) {
                     damage /= (double)Config.ALT_DAGGER_DMG_VS_LIGHT;
                  }

                  if (((Player)target).isWearingMagicArmor()) {
                     damage /= (double)Config.ALT_DAGGER_DMG_VS_ROBE;
                  }
               }
            }

            if (target.isPlayer() && weapon != null && weapon.getItemType() == WeaponType.BOW && skill != null) {
               Armor armor = ((Player)target).getActiveChestArmorItem();
               if (armor != null) {
                  if (((Player)target).isWearingHeavyArmor()) {
                     damage /= (double)Config.ALT_BOW_DMG_VS_HEAVY;
                  }

                  if (((Player)target).isWearingLightArmor()) {
                     damage /= (double)Config.ALT_BOW_DMG_VS_LIGHT;
                  }

                  if (((Player)target).isWearingMagicArmor()) {
                     damage /= (double)Config.ALT_BOW_DMG_VS_ROBE;
                  }
               }
            }

            if (attacker.isPlayer()) {
               if (((Player)attacker).getClassId().isMage()) {
                  damage *= (double)Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
               } else {
                  damage *= (double)Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
               }
            } else if (attacker.isSummon()) {
               damage *= (double)Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
            } else if (attacker.isNpc()) {
               damage *= (double)Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
            }

            return damage;
         case 2:
            return 1.0;
      }
   }

   public static final double calcMagicDam(Creature attacker, Creature target, Skill skill, byte shld, boolean sps, boolean bss, boolean mcrit) {
      double mAtk = attacker.getMAtk(target, skill);
      double mDef = target.getMDef(attacker, skill);
      boolean isPvP = attacker.isPlayable() && target.isPlayable();
      boolean isPvE = attacker.isPlayable() && target.isAttackable();
      if (isPvP) {
         mDef *= skill.isMagic() ? target.getPvpMagicDef() : target.getPvpMagicDef();
      }

      switch(shld) {
         case 1:
            mDef += (double)target.getShldDef();
         default:
            mAtk *= bss ? 4.0 : (sps ? 2.0 : 1.0);
            double damage = 91.0 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker, target, isPvP, isPvE);
            if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill)) {
               if (attacker.isPlayer()) {
                  if (calcMagicSuccess(attacker, target, skill) && target.getLevel() - attacker.getLevel() <= 9) {
                     if (skill.getSkillType() == SkillType.DRAIN) {
                        attacker.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
                     } else {
                        attacker.sendPacket(SystemMessageId.ATTACK_FAILED);
                     }

                     damage /= 2.0;
                  } else {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                     sm.addCharName(target);
                     sm.addSkillName(skill);
                     attacker.sendPacket(sm);
                     damage = 1.0;
                  }
               }

               if (target.isPlayer()) {
                  SystemMessage sm = skill.getSkillType() == SkillType.DRAIN
                     ? SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN)
                     : SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
                  sm.addCharName(attacker);
                  target.sendPacket(sm);
               }
            } else if (mcrit) {
               damage *= attacker.isPlayer() && target.isPlayer() ? 2.5 : 3.0;
               damage *= attacker.calcStat(Stats.MAGIC_CRIT_DMG, 1.0, null, null);
            }

            if (mcrit) {
               damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.MCrit, attacker, target);
               int keyId = target.isPlayer() ? target.getActingPlayer().getClassId().getId() : (target.isMonster() ? -1 : -2);
               damage *= SkillBalanceParser.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.MCrit, target);
            } else {
               damage *= ClassBalanceParser.getInstance().getBalancedClass(AttackType.Magic, attacker, target);
            }

            damage *= attacker.getRandomDamageMultiplier();
            if (damage > 1.0 && skill.isDeathlink()) {
               damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
            }

            if (isPvP) {
               damage *= skill.isMagic() ? attacker.getPvpMagicDmg() : attacker.getPvpPhysSkillDmg();
            }

            damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1.0, null, null);
            damage = attacker.calcStat(Stats.MAGIC_SKILL_POWER, damage, null, null);
            damage *= calcElemental(attacker, target, skill);
            if (target.isAttackable()) {
               damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1.0, null, null);
               if (!target.isRaid()
                  && !target.isRaidMinion()
                  && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY
                  && attacker.getActingPlayer() != null
                  && target.getLevel() - attacker.getActingPlayer().getLevel() >= 2) {
                  int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
                  if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size()) {
                     damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1).floatValue();
                  } else {
                     damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff).floatValue();
                  }
               }
            }

            if (attacker.isPlayer()) {
               if (((Player)attacker).getClassId().isMage()) {
                  damage *= (double)Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
               } else {
                  damage *= (double)Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
               }
            } else if (attacker.isSummon()) {
               damage *= (double)Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
            } else if (attacker.isNpc()) {
               damage *= (double)Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
            }

            return damage;
         case 2:
            return 1.0;
      }
   }

   public static final double calcMagicDam(CubicInstance attacker, Creature target, Skill skill, boolean mcrit, byte shld) {
      int mAtk = attacker.getCubicPower();
      double mDef = target.getMDef(attacker.getOwner(), skill);
      boolean isPvP = target.isPlayable();
      boolean isPvE = target.isAttackable();
      switch(shld) {
         case 1:
            mDef += (double)target.getShldDef();
            break;
         case 2:
            return 1.0;
      }

      double damage = 91.0 * (((double)mAtk + skill.getPower(isPvP, isPvE)) / mDef);
      Player owner = attacker.getOwner();
      if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill)) {
         if (calcMagicSuccess(owner, target, skill) && target.getLevel() - skill.getMagicLevel() <= 9) {
            if (skill.getSkillType() == SkillType.DRAIN) {
               owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
            } else {
               owner.sendPacket(SystemMessageId.ATTACK_FAILED);
            }

            damage /= 2.0;
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
            sm.addCharName(target);
            sm.addSkillName(skill);
            owner.sendPacket(sm);
            damage = 1.0;
         }

         if (target.isPlayer()) {
            if (skill.getSkillType() == SkillType.DRAIN) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN);
               sm.addCharName(owner);
               target.sendPacket(sm);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
               sm.addCharName(owner);
               target.sendPacket(sm);
            }
         }
      } else if (mcrit) {
         damage *= 3.0;
      }

      damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1.0, null, null);
      damage *= calcElemental(owner, target, skill);
      if (target.isAttackable()) {
         damage *= attacker.getOwner().calcStat(Stats.PVE_MAGICAL_DMG, 1.0, null, null);
         if (!target.isRaid()
            && !target.isRaidMinion()
            && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY
            && attacker.getOwner() != null
            && target.getLevel() - attacker.getOwner().getLevel() >= 2) {
            int lvlDiff = target.getLevel() - attacker.getOwner().getLevel() - 1;
            if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size()) {
               damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1).floatValue();
            } else {
               damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff).floatValue();
            }
         }
      }

      return damage;
   }

   public static double calcCrit(Creature attacker, Creature target, Skill skill, boolean blow) {
      if (attacker.isPlayer() && attacker.getActiveWeaponItem() == null) {
         return 0.0;
      } else if (skill != null) {
         return (double)skill.getBaseCritRate()
            * (blow ? BaseStats.DEX.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker))
            * target.calcStat(Stats.CRIT_VULN, 1.0, attacker, skill);
      } else {
         double rate = attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_DAMAGE_EVASION, 100.0, attacker, skill);
         switch(PositionUtils.getDirectionTo(target, attacker)) {
            case BEHIND:
               rate *= 1.4;
               break;
            case SIDE:
               rate *= 1.2;
         }

         return rate / 10.0;
      }
   }

   public static final boolean calcLethalHit(Creature activeChar, Creature target, Skill skill) {
      double lethal2chance = 0.0;
      double lethal1chance = 0.0;
      if ((!activeChar.isPlayer() || activeChar.getAccessLevel().canGiveDamage()) && ((skill.getCondition() & 8) == 0 || activeChar.isBehind(target))) {
         if (target.isRaid() || target.isRaidMinion() || target.isLethalImmune() || target.isDoor()) {
            return false;
         } else if (!target.isInvul() && target.getLevel() - activeChar.getLevel() <= 5) {
            double lethalStrikeRate = (double)skill.getLethalStrikeRate() * calcLvlBonusMod(activeChar, target, skill);
            double halfKillRate = (double)skill.getHalfKillRate() * calcLvlBonusMod(activeChar, target, skill);
            if ((double)Rnd.get(100) < activeChar.calcStat(Stats.LETHAL_RATE, lethalStrikeRate, target, null)) {
               lethal2chance = activeChar.calcStat(Stats.LETHAL_RATE, lethalStrikeRate, target, null);
               if (activeChar.isPlayer() && Config.SKILL_CHANCE_SHOW) {
                  Player attacker = activeChar.getActingPlayer();
                  attacker.sendMessage(
                     new ServerMessage("Formulas.Lethal_Shot", attacker.getLang()).toString() + ": " + String.format("%1.2f", lethal2chance / 10.0) + "%"
                  );
               }

               if (target.isPlayer()) {
                  target.setCurrentCp(1.0);
                  target.setCurrentHp(1.0);
                  target.sendPacket(SystemMessageId.LETHAL_STRIKE);
               } else if (!target.isMonster()) {
                  if (target.isSummon()) {
                     target.setCurrentHp(1.0);
                  }
               } else {
                  double damage = skill.getId() == 1400 && skill.getLevel() > 100 && skill.getLevel() <= 130
                     ? target.getCurrentHp()
                     : target.getCurrentHp() - 1.0;
                  target.reduceCurrentHp(damage, activeChar, skill);
               }

               activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL);
            } else if ((double)Rnd.get(100) < activeChar.calcStat(Stats.LETHAL_RATE, halfKillRate, target, null)) {
               lethal1chance = activeChar.calcStat(Stats.LETHAL_RATE, halfKillRate, target, null);
               if (activeChar.isPlayer() && Config.SKILL_CHANCE_SHOW) {
                  Player attacker = activeChar.getActingPlayer();
                  attacker.sendMessage(
                     new ServerMessage("Formulas.Lethal_Shot", attacker.getLang()).toString() + ": " + String.format("%1.2f", lethal1chance / 10.0) + "%"
                  );
               }

               if (target.isPlayer()) {
                  target.setCurrentCp(1.0);
                  target.sendPacket(SystemMessageId.HALF_KILL);
                  target.sendPacket(SystemMessageId.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
               } else if (target.isMonster()) {
                  double damage = target.getCurrentHp() * 0.5;
                  target.reduceCurrentHp(damage, activeChar, skill);
               } else if (target.isSummon()) {
                  target.setCurrentHp(target.getCurrentHp() * 0.5);
               }

               activeChar.sendPacket(SystemMessageId.HALF_KILL);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static final boolean calcMCrit(double mRate) {
      return mRate > (double)Rnd.get(1000);
   }

   public static final boolean calcAtkBreak(Creature target, boolean crit) {
      if (target != null && !target.isInvul() && !target.isRaid() && target.isCastingNow() && Config.ALT_GAME_CANCEL_CAST) {
         Skill skill = target.getCastingSkill();
         if (skill == null) {
            return false;
         } else {
            return !skill.isPhysical() && !skill.isDance()
               ? Rnd.chance(target.calcStat(Stats.ATTACK_CANCEL, crit ? Config.SKILL_BREAK_CRIT_MOD : Config.SKILL_BREAK_MOD, null, skill))
               : false;
         }
      } else {
         return false;
      }
   }

   public static final int calcPAtkSpd(Creature attacker, Creature target, double rate) {
      return rate < 2.0 ? 2700 : (int)(470000.0 / rate);
   }

   public static final int calcAtkSpd(Creature attacker, Skill skill, double skillTime) {
      return skill.isMagic() ? (int)(skillTime / attacker.getMAtkSpd() * 333.0) : (int)(skillTime / attacker.getPAtkSpd() * 300.0);
   }

   public static boolean calcHitMiss(Creature attacker, Creature target) {
      int chance = (80 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker))) * 10;
      chance = (int)((double)chance * HitConditionBonusParser.getInstance().getConditionBonus(attacker, target));
      chance = Math.max(chance, 200);
      chance = Math.min(chance, 980);
      return chance < Rnd.get(1000);
   }

   public static byte calcShldUse(Creature attacker, Creature target, Skill skill, boolean sendSysMsg) {
      if (skill != null && skill.ignoreShield()) {
         return 0;
      } else {
         Item item = target.getSecondaryWeaponItem();
         if (item != null && item instanceof Armor && ((Armor)item).getItemType() != ArmorType.SIGIL) {
            double shldRate = target.calcStat(Stats.SHIELD_RATE, 0.0, attacker, null) * BaseStats.DEX.calcBonus(target);
            if (shldRate <= 1.0E-6) {
               return 0;
            } else {
               int degreeside = (int)target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 0.0, null, null) + 120;
               if (degreeside < 360 && !target.isFacing(attacker, degreeside)) {
                  return 0;
               } else {
                  byte shldSuccess = 0;
                  Weapon at_weapon = attacker.getActiveWeaponItem();
                  if (at_weapon != null && at_weapon.getItemType() == WeaponType.BOW) {
                     shldRate *= 1.3;
                  }

                  shldRate *= Config.ALT_SHLD_BLOCK_MODIFIER;
                  if (shldRate > 0.0 && Rnd.chance(Config.ALT_PERFECT_SHLD_BLOCK)) {
                     shldSuccess = 2;
                  } else if (Rnd.chance(shldRate)) {
                     shldSuccess = 1;
                  }

                  if (sendSysMsg && target.isPlayer()) {
                     Player enemy = target.getActingPlayer();
                     switch(shldSuccess) {
                        case 1:
                           enemy.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
                           break;
                        case 2:
                           enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
                     }
                  }

                  return shldSuccess;
               }
            }
         } else {
            return 0;
         }
      }
   }

   public static byte calcShldUse(Creature attacker, Creature target, Skill skill) {
      return calcShldUse(attacker, target, skill, true);
   }

   public static byte calcShldUse(Creature attacker, Creature target) {
      return calcShldUse(attacker, target, null, true);
   }

   public static boolean calcMagicAffected(Creature actor, Creature target, Skill skill) {
      double defence = 0.0;
      if (skill.isActive() && skill.isOffensive() && !skill.isNeutral()) {
         defence = target.getMDef(actor, skill);
      }

      double attack = 2.0 * actor.getMAtk(target, skill) * (1.0 + calcSkillVulnerability(actor, target, skill) / 100.0);
      double d = (attack - defence) / (attack + defence);
      if (skill.hasDebuffEffects() && target.calcStat(Stats.DEBUFF_IMMUNITY, 0.0, null, skill) > 0.0 && skill.canBeReflected()) {
         return false;
      } else {
         d += 0.5 * Rnd.nextGaussian();
         return d > 0.0;
      }
   }

   public static double calcSkillVulnerability(Creature attacker, Creature target, Skill skill) {
      double multiplier = 0.0;
      if (skill != null) {
         multiplier = calcSkillTraitVulnerability(multiplier, target, skill);
      }

      return multiplier;
   }

   public static double calcSkillTraitVulnerability(double multiplier, Creature target, Skill skill) {
      if (skill == null) {
         return multiplier;
      } else {
         TraitType trait = skill.getTraitType();
         if (trait != null && trait != TraitType.NONE) {
            switch(trait) {
               case BLEED:
                  multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
                  break;
               case BOSS:
                  multiplier = target.calcStat(Stats.BOSS_VULN, multiplier, target, null);
                  break;
               case DEATH:
               case DERANGEMENT:
                  multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
                  break;
               case GUST:
                  multiplier = target.calcStat(Stats.GUST_VULN, multiplier, target, null);
                  break;
               case HOLD:
                  multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
                  break;
               case PARALYZE:
                  multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
                  break;
               case PHYSICAL_BLOCKADE:
                  multiplier = target.calcStat(Stats.PHYSICALBLOCKADE_VULN, multiplier, target, null);
                  break;
               case POISON:
                  multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
                  break;
               case SHOCK:
                  multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
                  break;
               case SLEEP:
                  multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
                  break;
               case VALAKAS:
                  multiplier = target.calcStat(Stats.VALAKAS_VULN, multiplier, target, null);
            }
         } else {
            SkillType type = skill.getSkillType();
            if (type == SkillType.BUFF) {
               multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
            } else if (type == SkillType.DEBUFF || skill.isDebuff()) {
               multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
            }
         }

         return multiplier;
      }
   }

   public static double calcSkillProficiency(Skill skill, Creature attacker, Creature target) {
      double multiplier = 0.0;
      if (skill != null) {
         multiplier = calcSkillTraitProficiency(multiplier, attacker, target, skill);
      }

      return multiplier;
   }

   public static double calcSkillTraitProficiency(double multiplier, Creature attacker, Creature target, Skill skill) {
      if (skill == null) {
         return multiplier;
      } else {
         TraitType trait = skill.getTraitType();
         if (trait != null && trait != TraitType.NONE) {
            switch(trait) {
               case BLEED:
                  multiplier = attacker.calcStat(Stats.BLEED_PROF, multiplier, target, null);
               case BOSS:
               case GUST:
               case PHYSICAL_BLOCKADE:
               default:
                  break;
               case DEATH:
               case DERANGEMENT:
                  multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
                  break;
               case HOLD:
                  multiplier = attacker.calcStat(Stats.ROOT_PROF, multiplier, target, null);
                  break;
               case PARALYZE:
                  multiplier = attacker.calcStat(Stats.PARALYZE_PROF, multiplier, target, null);
                  break;
               case POISON:
                  multiplier = attacker.calcStat(Stats.POISON_PROF, multiplier, target, null);
                  break;
               case SHOCK:
                  multiplier = attacker.calcStat(Stats.STUN_PROF, multiplier, target, null);
                  break;
               case SLEEP:
                  multiplier = attacker.calcStat(Stats.SLEEP_PROF, multiplier, target, null);
                  break;
               case VALAKAS:
                  multiplier = attacker.calcStat(Stats.VALAKAS_PROF, multiplier, target, null);
            }
         } else {
            SkillType type = skill.getSkillType();
            if (type == SkillType.DEBUFF || skill.isDebuff()) {
               multiplier = target.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
            }
         }

         return multiplier;
      }
   }

   public static double calcSkillStatMod(Skill skill, Creature target) {
      return skill.getSaveVs() != null ? skill.getSaveVs().calcBonus(target) : 1.0;
   }

   public static double calcEffectStatMod(Skill skill, Creature target) {
      return skill.getSaveVs() != null ? Math.min(Math.max(2.0 - skill.getSaveVs().calcChanceMod(target), 0.1), 1.0) : 1.0;
   }

   public static double calcResMod(Creature attacker, Creature target, Skill skill) {
      double vuln = calcSkillVulnerability(attacker, target, skill);
      double checkVuln = Math.abs(vuln);
      if (checkVuln >= 100.0 && target.isNpc()) {
         return 0.0;
      } else {
         double prof = calcSkillProficiency(skill, attacker, target);
         double resMod = 1.0 + (vuln + prof) / 100.0;
         return Math.min(Math.max(resMod, 0.1), 1.9);
      }
   }

   public static double calcLvlBonusMod(Creature attacker, Creature target, Skill skill) {
      int attackerLvl = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();
      double skillLvlBonusRateMod = 1.0 + (double)skill.getLvlBonusRate() / 100.0;
      double lvlMod = 1.0 + (double)(attackerLvl - target.getLevel()) / 100.0;
      return skillLvlBonusRateMod * lvlMod;
   }

   public static double calcElementMod(Creature attacker, Creature target, Skill skill) {
      byte skillElement = skill.getElement();
      if (skillElement == -1) {
         return 1.0;
      } else {
         int attackerElement = attacker.getAttackElement() == skillElement
            ? attacker.getAttackElementValue(skillElement) + skill.getElementPower()
            : attacker.getAttackElementValue(skillElement);
         int targetElement = target.getDefenseElementValue(skillElement);
         return 1.0 + (double)(attackerElement - targetElement) / 1000.0;
      }
   }

   public static double calcMAtkMod(Creature attacker, Creature target, Skill skill) {
      double mdef = Math.max(1.0, target.getMDef(target, skill));
      double matk = attacker.getMAtk(target, skill);
      double val = 0.0;
      if (attacker.isChargedShot(ShotType.BLESSED_SPIRITSHOTS)) {
         val = 2.0;
         matk *= val;
      } else if (attacker.isChargedShot(ShotType.SPIRITSHOTS)) {
         val = 1.5;
         matk *= val;
      }

      return Config.SKILLS_CHANCE_MOD * Math.pow(matk, Config.SKILLS_CHANCE_POW) / mdef;
   }

   public static boolean calcEffectSuccess(
      Creature attacker, Creature target, EffectTemplate effect, Skill skill, byte shld, boolean ss, boolean sps, boolean bss
   ) {
      double baseRate = effect.getEffectPower();
      if (!(baseRate < 0.0) && !skill.hasEffectType(EffectType.CANCEL_DEBUFF, EffectType.CANCEL)) {
         if (skill.hasDebuffEffects()) {
            if (target.isNpc() && !(target instanceof Attackable)) {
               attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            if (target.isEkimusFood()
               || (target.isRaid() || target instanceof FortCommanderInstance) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF
               || target.isEpicRaid() && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF
               || target.isDoor()) {
               attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            if (skill.getPower() == -1.0) {
               if (attacker.isDebug()) {
                  attacker.sendDebugMessage(attacker.getActingPlayer().getSkillName(skill) + " effect ignoring resists");
               }

               return true;
            }

            if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0.0, null, skill) > 0.0 && skill.canBeReflected()) {
               return false;
            }
         }

         if (shld == 2) {
            if (attacker.isDebug()) {
               attacker.sendDebugMessage(attacker.getActingPlayer().getSkillName(skill) + " effect blocked by shield");
            }

            return false;
         } else {
            double statMod = calcEffectStatMod(skill, target);
            double rate = baseRate * statMod;
            double mAtkMod = 1.0;
            if (skill.isMagic()) {
               mAtkMod = calcMAtkMod(attacker, target, skill);
            }

            if (attacker.isNpc()) {
               rate *= Config.SKILLS_MOB_CHANCE;
            } else {
               rate *= mAtkMod;
            }

            double lvlBonusMod = calcLvlBonusMod(attacker, target, skill);
            rate *= lvlBonusMod;
            double resMod = calcResMod(attacker, target, skill);
            if (resMod <= 0.0) {
               return false;
            } else {
               rate *= resMod;
               double elementMod = calcElementMod(attacker, target, skill);
               rate *= elementMod;
               int keyId = target.isPlayer() ? target.getActingPlayer().getClassId().getId() : (target.isMonster() ? -1 : -2);
               double multiplier = SkillBalanceParser.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.Chance, target);
               rate *= multiplier;
               if ((attacker.isDebug() || Config.DEVELOPER) && attacker.isPlayer()) {
                  StringBuilder stat = new StringBuilder(100);
                  StringUtil.append(
                     stat,
                     attacker.getActingPlayer().getSkillName(skill),
                     " power:",
                     String.valueOf(baseRate),
                     " stat:",
                     String.format("%1.2f", statMod),
                     " res:",
                     String.format("%1.2f", resMod),
                     " elem:",
                     String.format("%1.2f", elementMod),
                     " lvl:",
                     String.format("%1.2f", lvlBonusMod),
                     " mAtkMod:",
                     String.format("%1.2f", mAtkMod),
                     " total:",
                     String.valueOf(rate)
                  );
                  String result = stat.toString();
                  if (attacker.isDebug()) {
                     attacker.sendDebugMessage(result);
                  }

                  if (Config.DEVELOPER) {
                     _log.info(result);
                  }
               }

               if (target.isRaid()) {
                  if (target.isEpicRaid()) {
                     if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((Npc)target).getId()) > 0) {
                        rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
                     } else {
                        rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
                     }
                  } else if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((Npc)target).getId()) > 0) {
                     rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
                  } else {
                     rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
                  }
               }

               double finalRate = Math.min(Math.max(rate, (double)skill.getMinChance()), (double)skill.getMaxChance());
               if (Config.SKILL_CHANCE_SHOW) {
                  if (attacker.isPlayer()) {
                     attacker.sendMessage(attacker.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", finalRate) + "%");
                  }

                  if (target.isPlayer()) {
                     target.sendMessage(
                        attacker.getName() + " - " + target.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", finalRate) + "%"
                     );
                  }
               }

               return Rnd.chance(finalRate);
            }
         }
      } else {
         return true;
      }
   }

   public static boolean calcEffectSuccess(Env env) {
      double baseRate = env.getEffect().getEffectTemplate().getEffectPower();
      if (baseRate < 0.0) {
         return true;
      } else {
         if (env.getSkill().isDebuff() || env.getSkill().hasEffectType(EffectType.STUN)) {
            if (env.getTarget().isNpc() && !(env.getTarget() instanceof Attackable)) {
               env.getCharacter().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            if (env.getTarget().isEkimusFood()
               || (env.getTarget().isRaid() || env.getTarget() instanceof FortCommanderInstance) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF
               || env.getTarget().isEpicRaid() && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF
               || env.getTarget().isDoor()) {
               env.getCharacter().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            if (env.getSkill().getPower() == -1.0) {
               if (env.getCharacter().isDebug()) {
                  env.getCharacter().sendDebugMessage(env.getPlayer().getSkillName(env.getSkill()) + " effect ignoring resists");
               }

               return true;
            }

            if (env.getTarget().calcStat(Stats.DEBUFF_IMMUNITY, 0.0, null, env.getSkill()) > 0.0) {
               return false;
            }
         }

         if (env.getShield() == 2) {
            if (env.getCharacter().isDebug()) {
               env.getCharacter().sendDebugMessage(env.getPlayer().getSkillName(env.getSkill()) + " effect blocked by shield");
            }

            return false;
         } else {
            double statMod = calcEffectStatMod(env.getSkill(), env.getTarget());
            double rate = baseRate * statMod;
            double mAtkMod = 1.0;
            if (env.getSkill().isMagic()) {
               mAtkMod = calcMAtkMod(env.getCharacter(), env.getTarget(), env.getSkill());
            }

            if (env.getCharacter().isNpc()) {
               rate *= Config.SKILLS_MOB_CHANCE;
            } else {
               rate *= mAtkMod;
            }

            double lvlBonusMod = calcLvlBonusMod(env.getCharacter(), env.getTarget(), env.getSkill());
            rate *= lvlBonusMod;
            double resMod = calcResMod(env.getCharacter(), env.getTarget(), env.getSkill());
            if (resMod <= 0.0) {
               return false;
            } else {
               rate *= resMod;
               double elementMod = calcElementMod(env.getCharacter(), env.getTarget(), env.getSkill());
               rate *= elementMod;
               if ((env.getCharacter().isDebug() || Config.DEVELOPER) && env.getCharacter().isPlayer()) {
                  StringBuilder stat = new StringBuilder(100);
                  StringUtil.append(
                     stat,
                     "Effect Name: ",
                     String.valueOf(env.getEffect().getEffectTemplate().getName()),
                     " Base Rate: ",
                     String.valueOf(baseRate),
                     " Stat Type: ",
                     String.valueOf(env.getSkill().getSaveVs()),
                     " Stat Mod: ",
                     String.format("%1.2f", statMod),
                     " Res Mod: ",
                     String.format("%1.2f", resMod),
                     " Elem Mod: ",
                     String.format("%1.2f", elementMod),
                     " Lvl Mod: ",
                     String.format("%1.2f", lvlBonusMod),
                     " Final Rate: ",
                     String.valueOf(rate)
                  );
                  String result = stat.toString();
                  if (env.getCharacter().isDebug()) {
                     env.getCharacter().sendDebugMessage(result);
                  }

                  if (Config.DEVELOPER) {
                     _log.info(result);
                  }
               }

               if (env.getTarget().isRaid()) {
                  if (env.getTarget().isEpicRaid()) {
                     if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((Npc)env.getTarget()).getId()) > 0) {
                        rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
                     } else {
                        rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
                     }
                  } else if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((Npc)env.getTarget()).getId()) > 0) {
                     rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
                  } else {
                     rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
                  }
               }

               double finalRate = Math.min(Math.max(rate, (double)env.getSkill().getMinChance()), (double)env.getSkill().getMaxChance());
               if (Config.SKILL_CHANCE_SHOW) {
                  if (env.getCharacter().isPlayer()) {
                     env.getCharacter().sendMessage(env.getPlayer().getSkillName(env.getSkill()) + ": " + String.format("%1.2f", finalRate) + "%");
                  }

                  if (env.getTarget().isPlayer()) {
                     env.getTarget()
                        .sendMessage(
                           env.getCharacter().getName()
                              + " - "
                              + env.getTarget().getActingPlayer().getSkillName(env.getSkill())
                              + ": "
                              + String.format("%1.2f", finalRate)
                              + "%"
                        );
                  }
               }

               if (!Rnd.chance(finalRate)) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                  sm.addCharName(env.getTarget());
                  sm.addSkillName(env.getSkill());
                  env.getCharacter().sendPacket(sm);
                  return false;
               } else {
                  return true;
               }
            }
         }
      }
   }

   public static boolean calcSkillSuccess(Creature attacker, Creature target, Skill skill, byte shld, boolean ss, boolean sps, boolean bss) {
      if (skill.hasDebuffEffects()) {
         if (target.isNpc() && !(target instanceof Attackable)) {
            attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return false;
         }

         if (target.isEkimusFood()
            || (target.isRaid() || target instanceof FortCommanderInstance) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF
            || target.isEpicRaid() && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF
            || target.isDoor()) {
            attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return false;
         }

         if (skill.getPower() == -1.0) {
            if (attacker.isDebug()) {
               attacker.sendDebugMessage(attacker.getActingPlayer().getSkillName(skill) + " ignoring resists");
            }

            return true;
         }

         if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0.0, null, skill) > 0.0 && skill.canBeReflected()) {
            return false;
         }
      }

      if (shld == 2) {
         if (attacker.isDebug()) {
            attacker.sendDebugMessage(attacker.getActingPlayer().getSkillName(skill) + " blocked by shield");
         }

         return false;
      } else {
         double baseRate = skill.getPower();
         double statMod = calcEffectStatMod(skill, target);
         double rate = baseRate * statMod;
         double mAtkMod = 1.0;
         if (skill.isMagic()) {
            mAtkMod = calcMAtkMod(attacker, target, skill);
         }

         if (attacker.isNpc()) {
            rate *= Config.SKILLS_MOB_CHANCE;
         } else {
            rate *= mAtkMod;
         }

         double lvlBonusMod = calcLvlBonusMod(attacker, target, skill);
         rate *= lvlBonusMod;
         double resMod = calcResMod(attacker, target, skill);
         if (resMod <= 0.0) {
            return false;
         } else {
            rate *= resMod;
            double elementMod = calcElementMod(attacker, target, skill);
            rate *= elementMod;
            if ((attacker.isDebug() || Config.DEVELOPER) && attacker.isPlayer()) {
               StringBuilder stat = new StringBuilder(100);
               StringUtil.append(
                  stat,
                  attacker.getActingPlayer().getSkillName(skill),
                  " type:",
                  skill.getSkillType().toString(),
                  " power:",
                  String.valueOf(baseRate),
                  " stat:",
                  String.format("%1.2f", statMod),
                  " res:",
                  String.format("%1.2f", resMod),
                  " elem:",
                  String.format("%1.2f", elementMod),
                  " lvl:",
                  String.format("%1.2f", lvlBonusMod),
                  " mAtkMod:",
                  String.format("%1.2f", mAtkMod),
                  " total:",
                  String.valueOf(rate)
               );
               String result = stat.toString();
               if (attacker.isDebug()) {
                  attacker.sendDebugMessage(result);
               }

               if (Config.DEVELOPER) {
                  _log.info(result);
               }
            }

            if (target.isRaid()) {
               if (target.isEpicRaid()) {
                  if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((Npc)target).getId()) > 0) {
                     rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
                  } else {
                     rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
                  }
               } else if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((Npc)target).getId()) > 0) {
                  rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
               } else {
                  rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
               }
            }

            int keyId = target.isPlayer() ? target.getActingPlayer().getClassId().getId() : (target.isMonster() ? -1 : -2);
            double multiplier = SkillBalanceParser.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.Chance, target);
            rate *= multiplier;
            double finalRate = Math.min(Math.max(rate, (double)skill.getMinChance()), (double)skill.getMaxChance());
            if (Config.SKILL_CHANCE_SHOW) {
               if (attacker.isPlayer()) {
                  attacker.sendMessage(attacker.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", finalRate) + "%");
               }

               if (target.isPlayer()) {
                  target.sendMessage(
                     attacker.getName() + " - " + target.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", finalRate) + "%"
                  );
               }
            }

            return Rnd.chance(finalRate);
         }
      }
   }

   public static boolean calcSkillSuccess(Creature attacker, Creature target, Skill skill, byte shld, boolean ss, boolean sps, boolean bss, int activateRate) {
      Env env = new Env();
      env._character = attacker;
      env._target = target;
      env._skill = skill;
      env._shield = shld;
      env._soulShot = ss;
      env._spiritShot = sps;
      env._blessedSpiritShot = bss;
      env._value = (double)activateRate;
      return calcSkillSuccess(attacker, target, skill, shld, ss, sps, bss);
   }

   public static boolean calcCubicSkillSuccess(CubicInstance attacker, Creature target, Skill skill, byte shld) {
      if (skill.hasDebuffEffects()) {
         if (target.isNpc() && !(target instanceof Attackable)) {
            attacker.getOwner().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return false;
         }

         if (target.isEkimusFood()
            || (target.isRaid() || target instanceof FortCommanderInstance) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF
            || target.isEpicRaid() && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF
            || target.isDoor()) {
            attacker.getOwner().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return false;
         }

         if (skill.getPower() == -1.0) {
            return true;
         }

         if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0.0, null, skill) > 0.0 && skill.canBeReflected()) {
            return false;
         }
      }

      if (shld == 2) {
         return false;
      } else if (calcSkillReflect(target, skill) != 0) {
         return false;
      } else {
         double baseRate = skill.getPower();
         double statMod = calcEffectStatMod(skill, target);
         double rate = baseRate * statMod;
         double mAtkMod = 1.0;
         if (skill.isMagic()) {
            mAtkMod = calcMAtkMod(attacker.getOwner(), target, skill);
         }

         rate *= mAtkMod;
         double lvlBonusMod = calcLvlBonusMod(attacker.getOwner(), target, skill);
         rate *= lvlBonusMod;
         double resMod = calcResMod(attacker.getOwner(), target, skill);
         if (resMod <= 0.0) {
            return false;
         } else {
            rate *= resMod;
            double elementMod = calcElementMod(attacker.getOwner(), target, skill);
            rate *= elementMod;
            if ((attacker.getOwner().isDebug() || Config.DEVELOPER) && attacker.getOwner().isPlayer()) {
               StringBuilder stat = new StringBuilder(100);
               StringUtil.append(
                  stat,
                  attacker.getOwner().getActingPlayer().getSkillName(skill),
                  " type:",
                  skill.getSkillType().toString(),
                  " power:",
                  String.valueOf(baseRate),
                  " stat:",
                  String.format("%1.2f", statMod),
                  " res:",
                  String.format("%1.2f", resMod),
                  " elem:",
                  String.format("%1.2f", elementMod),
                  " lvl:",
                  String.format("%1.2f", lvlBonusMod),
                  " mAtkMod:",
                  String.format("%1.2f", mAtkMod),
                  " total:",
                  String.valueOf(rate)
               );
               String result = stat.toString();
               if (attacker.getOwner().isDebug()) {
                  attacker.getOwner().sendDebugMessage(result);
               }

               if (Config.DEVELOPER) {
                  _log.info(result);
               }
            }

            if (target.isRaid()) {
               if (target.isEpicRaid()) {
                  if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((Npc)target).getId()) > 0) {
                     rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
                  } else {
                     rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
                  }
               } else if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((Npc)target).getId()) > 0) {
                  rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
               } else {
                  rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
               }
            }

            int keyId = target.isPlayer() ? target.getActingPlayer().getClassId().getId() : (target.isMonster() ? -1 : -2);
            double multiplier = SkillBalanceParser.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.Chance, target);
            rate *= multiplier;
            double finalRate = Math.min(Math.max(rate, (double)skill.getMinChance()), (double)skill.getMaxChance());
            return Rnd.chance(finalRate);
         }
      }
   }

   public static boolean calcMagicSuccess(Creature attacker, Creature target, Skill skill) {
      if (skill.getPower() == -1.0) {
         return true;
      } else {
         int lvlDifference = target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
         double lvlModifier = Math.pow(1.3, (double)lvlDifference);
         float targetModifier = 1.0F;
         if (target.isAttackable()
            && !target.isRaid()
            && !target.isRaidMinion()
            && target.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY
            && attacker.getActingPlayer() != null
            && target.getLevel() - attacker.getActingPlayer().getLevel() >= 3) {
            int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 2;
            if (lvlDiff >= Config.NPC_SKILL_CHANCE_PENALTY.size()) {
               targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size() - 1);
            } else {
               targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
            }
         }

         double resModifier = target.calcStat(Stats.MAGIC_SUCCESS_RES, 1.0, null, skill);
         double failureModifier = attacker.calcStat(Stats.MAGIC_FAILURE_RATE, 1.0, target, skill);
         double rate = (double)(100 - Math.round((float)(lvlModifier * (double)targetModifier * resModifier * failureModifier)));
         if (attacker.isPlayer()) {
            if (target.getLevel() - attacker.getActingPlayer().getLevel() > 6) {
               if (rate > (double)skill.getMaxChance()) {
                  rate = (double)skill.getMaxChance();
               } else if (rate < (double)skill.getMinChance()) {
                  rate = (double)skill.getMinChance();
               }
            } else {
               rate = (double)skill.getMaxChance();
            }
         }

         if ((attacker.isDebug() || Config.DEVELOPER) && attacker.isPlayer()) {
            StringBuilder stat = new StringBuilder(100);
            StringUtil.append(
               stat,
               attacker.getActingPlayer().getSkillName(skill),
               " lvlDiff:",
               String.valueOf(lvlDifference),
               " lvlMod:",
               String.format("%1.2f", lvlModifier),
               " res:",
               String.format("%1.2f", resModifier),
               " fail:",
               String.format("%1.2f", failureModifier),
               " tgt:",
               String.valueOf(targetModifier),
               " total:",
               String.valueOf(rate)
            );
            String result = stat.toString();
            if (attacker.isDebug()) {
               attacker.sendDebugMessage(result);
            }

            if (Config.DEVELOPER) {
               _log.info(result);
            }
         }

         if (Config.SKILL_CHANCE_SHOW && skill.getId() != 1400) {
            if (attacker.isPlayer()) {
               attacker.sendMessage(attacker.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", rate) + "%");
            }

            if (target.isPlayer()) {
               target.sendMessage(attacker.getName() + " - " + target.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", rate) + "%");
            }
         }

         return (double)Rnd.get(100) < rate;
      }
   }

   public static double calcManaDam(Creature attacker, Creature target, Skill skill, boolean ss, boolean bss) {
      double mAtk = attacker.getMAtk(target, skill);
      double mDef = target.getMDef(attacker, skill);
      boolean isPvP = attacker.isPlayable() && target.isPlayable();
      boolean isPvE = attacker.isPlayable() && target.isAttackable();
      double mp = target.getMaxMp();
      if (bss) {
         mAtk *= 4.0;
      } else if (ss) {
         mAtk *= 2.0;
      }

      double damage = Math.sqrt(mAtk) * skill.getPower(attacker, target, isPvP, isPvE) * (mp / 97.0) / mDef;
      damage *= 1.0 + calcSkillVulnerability(attacker, target, skill) / 100.0;
      if (target.isAttackable()) {
         damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1.0, null, null);
         if (!target.isRaid()
            && !target.isRaidMinion()
            && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY
            && attacker.getActingPlayer() != null
            && target.getLevel() - attacker.getActingPlayer().getLevel() >= 2) {
            int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
            if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size()) {
               damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1).floatValue();
            } else {
               damage *= (double)Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff).floatValue();
            }
         }
      }

      return damage;
   }

   public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, Creature caster) {
      if (baseRestorePercent != 0.0 && baseRestorePercent != 100.0) {
         double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
         if (restorePercent - baseRestorePercent > 20.0) {
            restorePercent += 20.0;
         }

         restorePercent = Math.max(restorePercent, baseRestorePercent);
         return Math.min(restorePercent, 90.0);
      } else {
         return baseRestorePercent;
      }
   }

   public static boolean calcPhysicalSkillEvasion(Creature activeChar, Creature target, Skill skill) {
      if ((
            !skill.isMagic()
               || skill.getSkillType() == SkillType.BLOW
               || skill.getSkillType() == SkillType.PDAM
               || skill.getSkillType() == SkillType.FATAL
               || skill.getSkillType() == SkillType.CHARGEDAM
         )
         && !skill.isDebuff()) {
         if ((double)Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0.0, null, skill)) {
            if (activeChar.isPlayer()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
               sm.addString(target.getName());
               activeChar.getActingPlayer().sendPacket(sm);
            }

            if (target.isPlayer()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK2);
               sm.addString(activeChar.getName());
               target.getActingPlayer().sendPacket(sm);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean calcSkillMastery(Creature actor, Skill sk) {
      if (!sk.isStatic() && !sk.isBlockSkillMastery()) {
         return actor.getSkillLevel(331) > 0 && actor.calcStat(Stats.SKILL_MASTERY, (double)actor.getINT(), null, sk) >= (double)Rnd.get(3000)
            || actor.getSkillLevel(330) > 0 && actor.calcStat(Stats.SKILL_MASTERY, (double)actor.getSTR(), null, sk) >= (double)Rnd.get(3000);
      } else {
         return false;
      }
   }

   public static double calcValakasTrait(Creature attacker, Creature target, Skill skill) {
      double calcPower = 0.0;
      double calcDefen = 0.0;
      if (skill != null && skill.getTraitType() == TraitType.VALAKAS) {
         calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
         calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
      } else {
         calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
         if (calcPower > 0.0) {
            calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
            calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
         }
      }

      return 1.0 + (calcDefen + calcPower) / 100.0;
   }

   public static double calcElemental(Creature attacker, Creature target, Skill skill) {
      int calcPower = 0;
      int calcDefen = 0;
      int calcTotal = 0;
      double result = 1.0;
      if (skill != null) {
         byte element = skill.getElement();
         if (element >= 0) {
            calcPower = skill.getElementPower();
            calcDefen = target.getDefenseElementValue(element);
            if (attacker.getAttackElement() == element) {
               calcPower += attacker.getAttackElementValue(element);
            }

            calcTotal = calcPower - calcDefen;
            if (calcTotal > 0) {
               if (calcTotal < 50) {
                  result += (double)calcTotal * 0.003948;
               } else if (calcTotal < 150) {
                  result = 1.1974;
               } else if (calcTotal < 300) {
                  result = 1.3973;
               } else {
                  result = 1.6963;
               }
            }

            if (Config.DEVELOPER) {
               _log.info(skill.getNameEn() + ": " + calcPower + ", " + calcDefen + ", " + result);
            }
         }
      } else {
         byte element = attacker.getAttackElement();
         if (element >= 0) {
            calcTotal = Math.max(attacker.getAttackElementValue(element) - target.getDefenseElementValue(element), 0);
            if (calcTotal < 50) {
               result += (double)calcTotal * 0.003948;
            } else if (calcTotal < 150) {
               result = 1.1974;
            } else if (calcTotal < 300) {
               result = 1.3973;
            } else {
               result = 1.6963;
            }

            if (Config.DEVELOPER) {
               _log.info("Hit: " + calcPower + ", " + calcDefen + ", " + result);
            }
         }
      }

      return result;
   }

   public static void calcDamageReflected(Creature activeChar, Creature target, Skill skill, boolean crit) {
      boolean reflect = true;
      if (skill.getCastRange() == -1 || skill.getCastRange() > 40) {
         reflect = false;
      }

      if (reflect) {
         double vengeanceChance = target.getStat().calcStat(Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0.0, target, skill);
         if (vengeanceChance > (double)Rnd.get(100)) {
            if (target.isPlayer()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
               sm.addCharName(activeChar);
               target.sendPacket(sm);
            }

            if (activeChar.isPlayer()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
               sm.addCharName(target);
               activeChar.sendPacket(sm);
            }

            double vegdamage = 1189.0 * target.getPAtk(activeChar) / activeChar.getPDef(target);
            activeChar.reduceCurrentHp(vegdamage, target, skill);
            if (crit) {
               activeChar.reduceCurrentHp(vegdamage, target, skill);
            }
         }
      }
   }

   public static byte calcSkillReflect(Creature target, Skill skill) {
      if (skill.canBeReflected() && skill.getPower() != -1.0) {
         if (skill.isMagic() || skill.getCastRange() != -1 && skill.getCastRange() <= 40) {
            byte reflect = 0;
            switch(skill.getSkillType()) {
               case FEAR:
               case ROOT:
               case STUN:
               case MUTE:
               case BLEED:
               case PARALYZE:
               case SLEEP:
               case DEBUFF:
               case PDAM:
               case MDAM:
               case BLOW:
               case DRAIN:
               case CHARGEDAM:
               case FATAL:
               case DEATHLINK:
               case MANADAM:
               case CPDAMPERCENT:
                  Stats stat = skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE;
                  double venganceChance = target.getStat().calcStat(stat, 0.0, target, skill);
                  if (venganceChance > (double)Rnd.get(100)) {
                     reflect = (byte)(reflect | 2);
                  }
               default:
                  double reflectChance = target.calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0.0, null, skill);
                  if ((double)Rnd.get(100) < reflectChance) {
                     reflect = (byte)(reflect | 1);
                  }

                  return reflect;
            }
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static boolean calcBlowSuccess(Creature activeChar, Creature target, Skill skill) {
      if ((skill.getCondition() & 8) != 0 && !activeChar.isBehindTarget()) {
         return false;
      } else {
         double dexMod = BaseStats.DEX.calcBonus(activeChar);
         double blowChance = (double)skill.getBlowChance();
         double sideMod = activeChar.isInFrontOfTarget() ? 1.0 : (activeChar.isBehindTarget() ? 2.0 : 1.5);
         double baseRate = blowChance * dexMod * sideMod;
         double rate = activeChar.calcStat(Stats.BLOW_RATE, baseRate, target, null);
         double finalRate = Math.min(rate, 80.0);
         boolean isBackstab = skill.getDmgDirectlyToHP();
         boolean isOnAngle = Util.isOnAngle(target, activeChar, 180, 120);
         boolean result = isBackstab && !isOnAngle ? false : (double)Rnd.get(1000) < finalRate * 10.0;
         if (Config.SKILL_CHANCE_SHOW && activeChar.isPlayer()) {
            activeChar.sendMessage(activeChar.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", blowChance) + "%");
         }

         return result;
      }
   }

   public static List<Effect> calcCancelStealEffects(
      Creature activeChar, Creature target, Skill skill, String slot, int rate, int min, int max, boolean randomEffects, boolean checkResistAmount
   ) {
      int total = 0;
      if (min > 0 && min != max) {
         total = Rnd.get(min, max);
      } else {
         total = max;
      }

      if (total <= 0) {
         return Collections.emptyList();
      } else {
         List<Effect> canceled = new ArrayList<>(total);
         switch(slot) {
            case "buff":
               int cancelMagicLvl = skill.getMagicLevel();
               double vuln = target.calcStat(Stats.CANCEL_VULN, 0.0, target, null);
               double prof = activeChar.calcStat(Stats.CANCEL_PROF, 0.0, target, null);
               double resMod = 1.0 + (vuln + prof) * -1.0 / 100.0;
               double finalRate = (double)rate / resMod;
               if (Config.SKILL_CHANCE_SHOW && activeChar.isPlayer()) {
                  activeChar.sendMessage(activeChar.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", finalRate) + "%");
               }

               if ((activeChar.isDebug() || Config.DEVELOPER) && activeChar.isPlayer()) {
                  StringBuilder stat = new StringBuilder(100);
                  StringUtil.append(
                     stat,
                     activeChar.getActingPlayer().getSkillName(skill),
                     " Base Rate:",
                     String.valueOf(rate),
                     " Magiclvl:",
                     String.valueOf(cancelMagicLvl),
                     " resMod:",
                     String.format("%1.2f", resMod),
                     " Rate:",
                     String.format("%1.2f", finalRate)
                  );
                  String result = stat.toString();
                  if (activeChar.isDebug()) {
                     activeChar.sendDebugMessage(result);
                  }

                  if (Config.DEVELOPER) {
                     _log.info(result);
                  }
               }

               Effect[] effects = target.getAllEffects();
               List<Effect> musicList = new LinkedList<>();
               List<Effect> buffList = new LinkedList<>();

               for(Effect eff : effects) {
                  if (eff != null && !eff.getSkill().isOffensive() && eff.canBeStolen() && !eff.getSkill().isTriggeredSkill() && !eff.getSkill().isToggle()) {
                     if (eff.getSkill().isDance()) {
                        musicList.add(eff);
                     } else {
                        buffList.add(eff);
                     }
                  }
               }

               List<Effect> effectList = new LinkedList<>();
               Collections.reverse(musicList);
               Collections.reverse(buffList);
               effectList.addAll(musicList);
               effectList.addAll(buffList);
               if (randomEffects) {
                  Collections.shuffle(effectList);
               }

               if (!effectList.isEmpty()) {
                  int i = 0;

                  for(Effect e : effectList) {
                     if (e.canBeStolen()) {
                        if (!calcStealSuccess(activeChar, target, skill, (double)rate)) {
                           if (checkResistAmount) {
                              if (++i >= total) {
                                 return canceled;
                              }
                           }
                        } else {
                           ++i;
                           canceled.add(e);
                           if (i >= total) {
                              return canceled;
                           }
                        }
                     }
                  }
               }
               break;
            case "debuff":
               for(Effect info : target.getAllEffects()) {
                  if (info != null && info.getSkill().isOffensive() && info.getSkill().canBeDispeled() && Rnd.get(100) <= rate) {
                     canceled.add(info);
                     if (canceled.size() >= total) {
                        break;
                     }
                  }
               }
         }

         return canceled;
      }
   }

   public static boolean calcStealSuccess(Creature activeChar, Creature target, Skill skill, double power) {
      double vuln = target.calcStat(Stats.CANCEL_VULN, 0.0, target, null);
      double prof = activeChar.calcStat(Stats.CANCEL_PROF, 0.0, target, null);
      double resMod = 1.0 + (vuln + prof) * -1.0 / 100.0;
      double rate = power / resMod;
      double finalRate = Math.min(Math.max(rate, (double)skill.getMinChance()), (double)skill.getMaxChance());
      return Rnd.chance(finalRate);
   }

   private static int calcSimpleTime(Creature target, Skill skill, EffectTemplate template) {
      if (target != null && skill != null && Config.SKILL_DURATION_LIST_SIMPLE.containsKey(skill.getId())) {
         return Config.SKILL_DURATION_LIST_SIMPLE.get(skill.getId());
      } else {
         return template.getAbnormalTime() == 0 && skill != null
            ? (!skill.isPassive() && !skill.isToggle() ? skill.getAbnormalTime() : -1)
            : template.getAbnormalTime();
      }
   }

   private static int calcPremiumTime(Creature target, Skill skill, EffectTemplate template) {
      if (Config.SKILL_DURATION_LIST_PREMIUM.containsKey(skill.getId())) {
         return Config.SKILL_DURATION_LIST_PREMIUM.get(skill.getId());
      } else {
         return template.getAbnormalTime() == 0 && skill != null
            ? (!skill.isPassive() && !skill.isToggle() ? skill.getAbnormalTime() : -1)
            : template.getAbnormalTime();
      }
   }

   public static int calcEffectAbnormalTime(Env env, EffectTemplate template) {
      Creature caster = env.getCharacter();
      Creature target = env.getTarget();
      Skill skill = env.getSkill();
      if (skill != null && !skill.isToggle() && template.getTotalTickCount() > 0) {
         return 1;
      } else {
         int time;
         if (Config.ENABLE_MODIFY_SKILL_DURATION && target != null && skill != null) {
            time = target.hasPremiumBonus() ? calcPremiumTime(target, skill, template) : calcSimpleTime(target, skill, template);
         } else {
            time = template.getAbnormalTime() != 0 || skill == null
               ? template.getAbnormalTime()
               : (!skill.isPassive() && !skill.isToggle() ? skill.getAbnormalTime() : -1);
         }

         if (target != null && target.isServitor() && skill != null && skill.isAbnormalInstant()) {
            time /= 2;
         }

         if (env.isSkillMastery()) {
            time *= 2;
         }

         if (caster != null && target != null && skill != null && skill.isDebuff()) {
            double res = 0.0;
            res += calcSkillTraitVulnerability(0.0, target, skill);
            res += calcSkillTraitProficiency(0.0, caster, target, skill);
            res -= calcElementMod(caster, target, skill);
            if (res != 0.0) {
               double mod = 1.0 + Math.abs(0.005 * res);
               if (res > 0.0) {
                  mod = 1.0 / mod;
               }

               int diff = (int)Math.round(Math.max((double)time * mod, 1.0)) - time;
               if (diff > 0) {
                  time -= diff;
               }
            }
         }

         if (caster != null && (caster.isDebug() || Config.DEVELOPER) && caster.isPlayer() && time > 1) {
            StringBuilder stat = new StringBuilder(100);
            StringUtil.append(stat, "Effect Name: ", String.valueOf(skill.getNameEn()), " Time: ", String.valueOf(time));
            String result = stat.toString();
            if (env.getCharacter().isDebug()) {
               env.getCharacter().sendDebugMessage(result);
            }

            if (Config.DEVELOPER) {
               _log.info(result);
            }
         }

         return time;
      }
   }

   public static int calcEffectTickCount(Env env, EffectTemplate template) {
      Creature caster = env.getCharacter();
      Creature target = env.getTarget();
      Skill skill = env.getSkill();
      int tickCount = template.getTotalTickCount();
      if (tickCount <= 0) {
         return 0;
      } else {
         if (caster != null && target != null && skill != null && skill.isDebuff()) {
            double res = 0.0;
            res += calcSkillTraitVulnerability(0.0, target, skill);
            res += calcSkillTraitProficiency(0.0, caster, target, skill);
            res -= calcElementMod(caster, target, skill);
            if (res != 0.0) {
               double mod = 1.0 + Math.abs(0.005 * res);
               if (res > 0.0) {
                  mod = 1.0 / mod;
               }

               int diff = (int)Math.round(Math.max((double)tickCount * mod, 1.0)) - tickCount;
               if (diff > 0) {
                  tickCount -= diff;
               }
            }
         }

         if (caster != null && (caster.isDebug() || Config.DEVELOPER) && caster.isPlayer() && tickCount > 1) {
            StringBuilder stat = new StringBuilder(100);
            StringUtil.append(stat, "Effect Name: ", String.valueOf(skill.getNameEn()), " Ticks: ", String.valueOf(tickCount));
            String result = stat.toString();
            if (env.getCharacter().isDebug()) {
               env.getCharacter().sendDebugMessage(result);
            }

            if (Config.DEVELOPER) {
               _log.info(result);
            }
         }

         return tickCount;
      }
   }

   public static boolean calcProbability(double baseChance, Creature attacker, Creature target, Skill skill, boolean printChance) {
      if (baseChance == -1.0) {
         return true;
      } else {
         double chance = ((double)skill.getMagicLevel() + baseChance - (double)target.getLevel() + 30.0 - (double)target.getINT())
            * calcElemental(attacker, target, skill)
            * calcValakasTrait(attacker, target, skill)
            * 10.0;
         if (attacker.isPlayer() && Config.SKILL_CHANCE_SHOW && printChance) {
            attacker.sendMessage(attacker.getActingPlayer().getSkillName(skill) + ": " + String.format("%1.2f", chance / 10.0) + "%");
         }

         return (double)Rnd.get(1000) < chance;
      }
   }

   public static final void calcDrainDamage(Creature activeChar, Creature target, int damage, double absorbAbs, double percent) {
      int drain = 0;
      int cp = (int)target.getCurrentCp();
      int hp = (int)target.getCurrentHp();
      if (cp > 0) {
         if (damage < cp) {
            drain = 0;
         } else {
            drain = damage - cp;
         }
      } else if (damage > hp) {
         drain = hp;
      } else {
         drain = damage;
      }

      double hpAdd = absorbAbs + percent * (double)drain;
      double finalHp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;
      if (!activeChar.isHealBlocked()) {
         activeChar.setCurrentHp(finalHp);
         StatusUpdate suhp = new StatusUpdate(activeChar);
         suhp.addAttribute(9, (int)finalHp);
         activeChar.sendPacket(suhp);
      }
   }
}
