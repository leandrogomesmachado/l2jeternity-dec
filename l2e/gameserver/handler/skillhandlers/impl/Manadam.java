package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Manadam implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.MANADAM};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
         boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
         boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

         for(Creature target : (Creature[])targets) {
            if (Formulas.calcSkillReflect(target, skill) == 1) {
               target = activeChar;
            }

            boolean acted = Formulas.calcMagicAffected(activeChar, target, skill);
            if (!target.isInvul() && acted) {
               if (skill.hasEffects()) {
                  skill.getEffects(activeChar, target, new Env(Formulas.calcShldUse(activeChar, target, skill), ss, sps, bss), true);
               }

               double damage = skill.isStaticDamage() ? skill.getPower() : Formulas.calcManaDam(activeChar, target, skill, ss, bss);
               if (!skill.isStaticDamage() && Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill))) {
                  damage *= 3.0;
                  activeChar.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
               }

               double mp = damage > target.getCurrentMp() ? target.getCurrentMp() : damage;
               target.reduceCurrentMp(mp);
               if (damage > 0.0) {
                  target.stopEffectsOnDamage(true);
               }

               if (target.isPlayer()) {
                  StatusUpdate sump = new StatusUpdate(target);
                  sump.addAttribute(11, (int)target.getCurrentMp());
                  target.sendPacket(sump);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_C1);
                  sm.addCharName(activeChar);
                  sm.addNumber((int)mp);
                  target.sendPacket(sm);
               }

               if (activeChar.isPlayer()) {
                  SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
                  sm2.addNumber((int)mp);
                  activeChar.sendPacket(sm2);
               }
            } else {
               activeChar.sendPacket(SystemMessageId.MISSED_TARGET);
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
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
