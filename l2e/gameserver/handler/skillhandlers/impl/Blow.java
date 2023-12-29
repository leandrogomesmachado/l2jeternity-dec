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
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Blow implements ISkillHandler {
   private static final Logger _logDamage = Logger.getLogger("damage");
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.BLOW};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
         boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
         boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

         for(Creature target : (Creature[])targets) {
            if (!target.isAlikeDead()) {
               boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(activeChar, target, skill);
               if (!skillIsEvaded && Formulas.calcBlowSuccess(activeChar, target, skill)) {
                  byte reflect = Formulas.calcSkillReflect(target, skill);
                  if (skill.hasEffects()) {
                     if (reflect == 1) {
                        activeChar.stopSkillEffects(skill.getId());
                        skill.getEffects(target, activeChar, true);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                        sm.addSkillName(skill);
                        activeChar.sendPacket(sm);
                     } else {
                        skill.getEffects(activeChar, target, new Env(Formulas.calcShldUse(activeChar, target, skill), ss, sps, bss), true);
                     }
                  }

                  byte shld = Formulas.calcShldUse(activeChar, target, skill);
                  double damage = skill.isStaticDamage() ? skill.getPower() : (double)((int)Formulas.calcBlowDamage(activeChar, target, skill, shld, ss));
                  if (!skill.isStaticDamage() && skill.getMaxSoulConsumeCount() > 0 && activeChar.isPlayer()) {
                     int chargedSouls = activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()
                        ? activeChar.getActingPlayer().getChargedSouls()
                        : skill.getMaxSoulConsumeCount();
                     damage *= 1.0 + (double)chargedSouls * 0.04;
                  }

                  boolean crit = Rnd.chance(Formulas.calcCrit(activeChar, target, skill, true));
                  if (!skill.isStaticDamage() && crit) {
                     damage *= 2.0;
                  }

                  if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > (double)Config.LOG_GAME_DAMAGE_THRESHOLD) {
                     LogRecord record = new LogRecord(Level.INFO, "");
                     record.setParameters(new Object[]{activeChar, " did damage ", (int)damage, skill, " to ", target});
                     record.setLoggerName("pdam");
                     _logDamage.log(record);
                  }

                  double drainPercent = activeChar.calcStat(Stats.DRAIN_PERCENT, 0.0, null, null);
                  if (drainPercent != 0.0) {
                     Formulas.calcDrainDamage(activeChar, target, (int)damage, 0.0, drainPercent);
                  }

                  target.reduceCurrentHp(damage, activeChar, skill);
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

                  if (!target.isRaid() && Formulas.calcAtkBreak(target, crit)) {
                     target.breakAttack();
                     target.breakCast();
                  }

                  if (activeChar.isPlayer()) {
                     Player activePlayer = activeChar.getActingPlayer();
                     activePlayer.sendDamageMessage(target, (int)damage, skill, false, true, false);
                  }
               }

               Formulas.calcLethalHit(activeChar, target, skill);
               if (skill.hasSelfEffects()) {
                  Effect effect = activeChar.getFirstEffect(skill.getId());
                  if (effect != null && effect.isSelfEffect()) {
                     effect.exit();
                  }

                  skill.getEffectsSelf(activeChar);
               }

               activeChar.setChargedShot(ShotType.SOULSHOTS, false);
            }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
