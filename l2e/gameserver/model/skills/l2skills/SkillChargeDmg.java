package l2e.gameserver.model.skills.l2skills;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SkillChargeDmg extends Skill {
   private static final Logger _logDamage = Logger.getLogger("damage");

   public SkillChargeDmg(StatsSet set) {
      super(set);
   }

   @Override
   public void useSkill(Creature caster, GameObject[] targets) {
      if (!caster.isAlikeDead()) {
         double modifier = 0.0;
         if (caster.isPlayer()) {
            modifier = (double)caster.getActingPlayer().getCharges() * 0.25 + 1.0;
         }

         boolean ss = this.useSoulShot() && caster.isChargedShot(ShotType.SOULSHOTS);

         for(Creature target : (Creature[])targets) {
            if (!target.isAlikeDead()) {
               boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(caster, target, this);
               if (!skillIsEvaded) {
                  byte shld = Formulas.calcShldUse(caster, target, this);
                  boolean crit = false;
                  if (this.getBaseCritRate() > 0 && !this.isStaticDamage()) {
                     crit = Rnd.chance(Formulas.calcCrit(caster, target, this, false));
                  }

                  double damage = this.isStaticDamage() ? this.getPower() : Formulas.calcPhysDam(caster, target, this, shld, false, ss);
                  if (crit) {
                     damage *= 2.0;
                  }

                  if (damage > 0.0) {
                     byte reflect = Formulas.calcSkillReflect(target, this);
                     if (this.hasEffects()) {
                        if ((reflect & 1) != 0) {
                           caster.stopSkillEffects(this.getId());
                           this.getEffects(target, caster, true);
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                           sm.addSkillName(this);
                           caster.sendPacket(sm);
                        } else {
                           this.getEffects(caster, target, new Env(shld, false, false, false), true);
                        }
                     }

                     double finalDamage = this.isStaticDamage() ? damage : damage * modifier;
                     if (Config.LOG_GAME_DAMAGE && caster.isPlayable() && damage > (double)Config.LOG_GAME_DAMAGE_THRESHOLD) {
                        LogRecord record = new LogRecord(Level.INFO, "");
                        record.setParameters(new Object[]{caster, " did damage ", (int)damage, this, " to ", target});
                        record.setLoggerName("pdam");
                        _logDamage.log(record);
                     }

                     target.reduceCurrentHp(finalDamage, caster, this);
                     if ((reflect & 2) != 0) {
                        if (target.isPlayer()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
                           sm.addCharName(caster);
                           target.sendPacket(sm);
                        }

                        if (caster.isPlayer()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
                           sm.addCharName(target);
                           caster.sendPacket(sm);
                        }

                        double vegdamage = 1189.0 * target.getPAtk(caster) / caster.getPDef(target);
                        caster.reduceCurrentHp(vegdamage, target, this);
                     }

                     caster.sendDamageMessage(target, (int)finalDamage, this, false, crit, false);
                     Formulas.calcStunBreak(target, crit);
                  } else {
                     caster.sendDamageMessage(target, 0, this, false, false, true);
                  }
               }
            }
         }

         if (this.hasSelfEffects()) {
            Effect effect = caster.getFirstEffect(this.getId());
            if (effect != null && effect.isSelfEffect()) {
               effect.exit();
            }

            this.getEffectsSelf(caster);
         }

         caster.setChargedShot(ShotType.SOULSHOTS, false);
      }
   }
}
