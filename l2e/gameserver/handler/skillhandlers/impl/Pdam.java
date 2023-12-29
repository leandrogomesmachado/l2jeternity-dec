package l2e.gameserver.handler.skillhandlers.impl;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Pdam implements ISkillHandler {
   private static final Logger _logDamage = Logger.getLogger("damage");
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.PDAM, SkillType.FATAL};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         int damage = 0;
         boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);

         for(Creature target : (Creature[])targets) {
            if (target.isPlayer() && target.getActingPlayer().isFakeDeathNow()) {
               target.stopFakeDeath(true);
            } else if (target.isDead()) {
               continue;
            }

            byte shld = Formulas.calcShldUse(activeChar, target, skill);
            boolean crit = false;
            if (!skill.isStaticDamage() && skill.getBaseCritRate() > 0) {
               crit = Rnd.chance(Formulas.calcCrit(activeChar, target, skill, false));
            }

            if (!crit && (skill.getCondition() & 16) != 0) {
               damage = 0;
            } else {
               damage = skill.isStaticDamage() ? (int)skill.getPower() : (int)Formulas.calcPhysDam(activeChar, target, skill, shld, false, ss);
            }

            if (!skill.isStaticDamage() && skill.getMaxSoulConsumeCount() > 0 && activeChar.isPlayer()) {
               int chargedSouls = activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()
                  ? activeChar.getActingPlayer().getChargedSouls()
                  : skill.getMaxSoulConsumeCount();
               damage = (int)((double)damage * (1.0 + (double)chargedSouls * 0.04));
            }

            if (crit) {
               damage *= 2;
            }

            byte reflect = Formulas.calcSkillReflect(target, skill);
            if (!Formulas.calcPhysicalSkillEvasion(activeChar, target, skill)) {
               if (skill.hasEffects()) {
                  if ((reflect & 1) != 0) {
                     activeChar.stopSkillEffects(skill.getId());
                     skill.getEffects(activeChar, target, true);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                     sm.addSkillName(skill);
                     activeChar.sendPacket(sm);
                  } else {
                     skill.getEffects(activeChar, target, new Env(shld, false, false, false), true);
                  }
               }

               boolean allowDamage = true;
               if (skill.getId() == 450 && (skill.getLevel() < 300 || skill.getLevel() > 330)) {
                  allowDamage = false;
               }

               if (damage > 0 && allowDamage) {
                  double drainPercent = activeChar.calcStat(Stats.DRAIN_PERCENT, 0.0, null, null);
                  if (drainPercent != 0.0) {
                     Formulas.calcDrainDamage(activeChar, target, damage, 0.0, drainPercent);
                  }

                  activeChar.sendDamageMessage(target, damage, skill, false, crit, false);
                  target.reduceCurrentHp((double)damage, activeChar, skill);
                  Formulas.calcStunBreak(target, crit);
                  if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD) {
                     LogRecord record = new LogRecord(Level.INFO, "");
                     record.setParameters(new Object[]{activeChar, " did damage ", damage, skill, " to ", target});
                     record.setLoggerName("pdam");
                     _logDamage.log(record);
                  }

                  if ((reflect & 2) != 0) {
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
                  }
               } else if (allowDamage) {
                  activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
               }
            }

            Formulas.calcLethalHit(activeChar, target, skill);
         }

         if (skill.hasSelfEffects()) {
            Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect()) {
               effect.exit();
            }

            skill.getEffectsSelf(activeChar);
         }

         activeChar.setChargedShot(ShotType.SOULSHOTS, false);
         if (skill.isSuicideAttack()) {
            activeChar.doDie(activeChar);
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
