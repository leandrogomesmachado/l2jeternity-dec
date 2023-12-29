package l2e.gameserver.handler.skillhandlers.impl;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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

public class Mdam implements ISkillHandler {
   protected static final Logger _log = Logger.getLogger(Mdam.class.getName());
   private static final Logger _logDamage = Logger.getLogger("damage");
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.MDAM, SkillType.DEATHLINK};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
         boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
         boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

         for(Creature target : (Creature[])targets) {
            if (target.isPlayer() && target.getActingPlayer().isFakeDeathNow()) {
               target.stopFakeDeath(true);
            } else if (target.isDead()) {
               continue;
            }

            boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
            byte shld = Formulas.calcShldUse(activeChar, target, skill);
            byte reflect = Formulas.calcSkillReflect(target, skill);
            int damage = skill.isStaticDamage() ? (int)skill.getPower() : (int)Formulas.calcMagicDam(activeChar, target, skill, shld, sps, bss, mcrit);
            if (!skill.isStaticDamage() && skill.getDependOnTargetBuff()) {
               damage = (int)((double)damage * (((double)target.getBuffCount() * 0.3 + 1.3) / 4.0));
            }

            if (!skill.isStaticDamage() && skill.getMaxSoulConsumeCount() > 0 && activeChar.isPlayer()) {
               int chargedSouls = activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()
                  ? activeChar.getActingPlayer().getChargedSouls()
                  : skill.getMaxSoulConsumeCount();
               damage = (int)((double)damage * (1.0 + (double)chargedSouls * 0.04));
            }

            Formulas.calcLethalHit(activeChar, target, skill);
            boolean allowDamage = true;
            if (skill.getId() == 1400 && (skill.getLevel() < 300 || skill.getLevel() > 330)) {
               allowDamage = false;
            }

            if (damage > 0 || skill.getId() == 1400) {
               double drainPercent = activeChar.calcStat(Stats.DRAIN_PERCENT, 0.0, null, null);
               if (drainPercent != 0.0) {
                  Formulas.calcDrainDamage(activeChar, target, damage, 0.0, drainPercent);
               }

               if (allowDamage) {
                  if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit)) {
                     target.breakAttack();
                     target.breakCast();
                  }

                  if ((reflect & 2) != 0) {
                     activeChar.reduceCurrentHp((double)damage, target, skill);
                  } else {
                     activeChar.sendDamageMessage(target, damage, skill, mcrit, false, false);
                     target.reduceCurrentHp((double)damage, activeChar, skill);
                  }
               }

               if (skill.hasEffects()) {
                  if ((reflect & 1) != 0) {
                     activeChar.stopSkillEffects(skill.getId());
                     skill.getEffects(target, activeChar, true);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                     sm.addSkillName(skill);
                     activeChar.sendPacket(sm);
                  } else {
                     skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                  }
               }

               if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD) {
                  LogRecord record = new LogRecord(Level.INFO, "");
                  record.setParameters(new Object[]{activeChar, " did damage ", damage, skill, " to ", target});
                  record.setLoggerName("mdam");
                  _logDamage.log(record);
               }
            }
         }

         if (skill.hasSelfEffects()) {
            Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect()) {
               effect.exit();
            }

            skill.getEffectsSelf(activeChar);
         }

         activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
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
