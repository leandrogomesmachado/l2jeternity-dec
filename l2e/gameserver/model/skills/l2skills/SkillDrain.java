package l2e.gameserver.model.skills.l2skills;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SkillDrain extends Skill {
   private static final Logger _logDamage = Logger.getLogger("damage");
   private final float _absorbPart;
   private final int _absorbAbs;

   public SkillDrain(StatsSet set) {
      super(set);
      this._absorbPart = set.getFloat("absorbPart", 0.0F);
      this._absorbAbs = set.getInteger("absorbAbs", 0);
   }

   @Override
   public void useSkill(Creature activeChar, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         boolean ss = this.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
         boolean sps = this.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
         boolean bss = this.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

         for(Creature target : (Creature[])targets) {
            if ((!target.isAlikeDead() || this.getTargetType() == TargetType.CORPSE_MOB) && (activeChar == target || !target.isInvul())) {
               boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
               byte shld = Formulas.calcShldUse(activeChar, target, this);
               int damage = this.isStaticDamage() ? (int)this.getPower() : (int)Formulas.calcMagicDam(activeChar, target, this, shld, sps, bss, mcrit);
               Formulas.calcDrainDamage(activeChar, target, damage, (double)this._absorbAbs, (double)this._absorbPart);
               if (damage > 0 && (!target.isDead() || this.getTargetType() != TargetType.CORPSE_MOB)) {
                  if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit)) {
                     target.breakAttack();
                     target.breakCast();
                  }

                  activeChar.sendDamageMessage(target, damage, this, mcrit, false, false);
                  if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD) {
                     LogRecord record = new LogRecord(Level.INFO, "");
                     record.setParameters(new Object[]{activeChar, " did damage ", damage, this, " to ", target});
                     record.setLoggerName("mdam");
                     _logDamage.log(record);
                  }

                  if (this.hasEffects() && this.getTargetType() != TargetType.CORPSE_MOB) {
                     if ((Formulas.calcSkillReflect(target, this) & 1) > 0) {
                        activeChar.stopSkillEffects(this.getId());
                        this.getEffects(target, activeChar, true);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                        sm.addSkillName(this.getId());
                        activeChar.sendPacket(sm);
                     } else {
                        target.stopSkillEffects(this.getId());
                        if (Formulas.calcSkillSuccess(activeChar, target, this, shld, ss, sps, bss)) {
                           this.getEffects(activeChar, target, true);
                        } else {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                           sm.addCharName(target);
                           sm.addSkillName(this);
                           activeChar.sendPacket(sm);
                        }
                     }
                  }

                  target.reduceCurrentHp((double)damage, activeChar, this);
               }

               if (target.isDead() && this.getTargetType() == TargetType.CORPSE_MOB && target.isNpc()) {
                  ((Npc)target).endDecayTask();
               }
            }
         }

         Effect effect = activeChar.getFirstEffect(this.getId());
         if (effect != null && effect.isSelfEffect()) {
            effect.exit();
         }

         this.getEffectsSelf(activeChar);
         activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
      }
   }

   public void useCubicSkill(CubicInstance activeCubic, GameObject[] targets) {
      if (Config.DEBUG) {
         _log.info("SkillDrain: useCubicSkill()");
      }

      for(Creature target : (Creature[])targets) {
         if (!target.isAlikeDead() || this.getTargetType() == TargetType.CORPSE_MOB) {
            boolean mcrit = Formulas.calcMCrit((double)activeCubic.getMCriticalHit(target, this));
            byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, this);
            int damage = (int)Formulas.calcMagicDam(activeCubic, target, this, mcrit, shld);
            if (Config.DEBUG) {
               _log.info("SkillDrain: useCubicSkill() -> damage = " + damage);
            }

            double hpAdd = (double)((float)this._absorbAbs + this._absorbPart * (float)damage);
            Player owner = activeCubic.getOwner();
            double hp = owner.getCurrentHp() + hpAdd > owner.getMaxHp() ? owner.getMaxHp() : owner.getCurrentHp() + hpAdd;
            if (!owner.isHealBlocked()) {
               owner.setCurrentHp(hp);
               StatusUpdate suhp = new StatusUpdate(owner);
               suhp.addAttribute(9, (int)hp);
               owner.sendPacket(suhp);
            }

            if (damage > 0 && (!target.isDead() || this.getTargetType() != TargetType.CORPSE_MOB)) {
               target.reduceCurrentHp((double)damage, activeCubic.getOwner(), this);
               if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit)) {
                  target.breakAttack();
                  target.breakCast();
               }

               owner.sendDamageMessage(target, damage, this, mcrit, false, false);
            }
         }
      }
   }
}
