package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class CpDamPercent implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.CPDAMPERCENT};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
         boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
         boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

         for(Creature target : (Creature[])targets) {
            if (target.isPlayer() && target.getActingPlayer().isFakeDeathNow()) {
               target.stopFakeDeath(true);
            } else if (target.isDead() || target.isInvul()) {
               continue;
            }

            byte shld = Formulas.calcShldUse(activeChar, target, skill);
            int damage = (int)(target.getCurrentCp() * (skill.getPower() / 100.0));
            if (!target.isRaid() && Formulas.calcAtkBreak(target, false)) {
               target.breakAttack();
               target.breakCast();
            }

            skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
            activeChar.sendDamageMessage(target, damage, skill, false, false, false);
            target.setCurrentCp(target.getCurrentCp() - (double)damage);
         }

         activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
