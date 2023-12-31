package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.ClanHallManagerInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;

public class Continuous implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{
      SkillType.BUFF,
      SkillType.DEBUFF,
      SkillType.DOT,
      SkillType.MDOT,
      SkillType.POISON,
      SkillType.BLEED,
      SkillType.FEAR,
      SkillType.CONT,
      SkillType.AGGDEBUFF,
      SkillType.FUSION
   };

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      boolean acted = true;
      Player player = null;
      if (activeChar.isPlayer()) {
         player = activeChar.getActingPlayer();
      }

      if (skill.getEffectId() != 0) {
         Skill sk = SkillsParser.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());
         if (sk != null) {
            skill = sk;
         }
      }

      boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
      boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
      boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

      for(Creature target : (Creature[])targets) {
         byte shld = 0;
         if (Formulas.calcSkillReflect(target, skill) == 1) {
            target = activeChar;
         }

         if (skill.getSkillType() == SkillType.BUFF && !(activeChar instanceof ClanHallManagerInstance)) {
            if (target != activeChar) {
               if (target.isPlayer()) {
                  Player trg = target.getActingPlayer();
                  if (trg.isCursedWeaponEquipped()
                     || trg.getBlockCheckerArena() != -1
                     || !target.equals(activeChar)
                        && activeChar.isPlayer()
                        && activeChar.getActingPlayer().isInFightEvent()
                        && !activeChar.getActingPlayer().getFightEvent().canUsePositiveMagic(activeChar, target)) {
                     continue;
                  }
               } else if (player != null && player.isCursedWeaponEquipped()) {
                  continue;
               }
            }

            if (Config.ALLOW_BLOCKBUFFS_COMMAND && target.isPlayable() && activeChar.isPlayable() && !target.getActingPlayer().equals(activeChar)) {
               Player pTarget = target.getActingPlayer();
               if (pTarget.getBlockBuffs() && !pTarget.isInOlympiadMode() && !Skill.getBlockBuffConditions(activeChar, pTarget)) {
                  continue;
               }
            }
         }

         if (skill.isOffensive() || skill.isDebuff()) {
            shld = Formulas.calcShldUse(activeChar, target, skill);
            acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss);
         }

         if (acted) {
            if (skill.isToggle()) {
               Effect[] effects = target.getAllEffects();
               if (effects != null) {
                  for(Effect e : effects) {
                     if (e != null && e.getSkill().getId() == skill.getId()) {
                        e.exit();
                        return;
                     }
                  }
               }
            }

            Effect[] effects = skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
            Summon summon = target.getSummon();
            if (summon != null
               && summon != activeChar
               && summon.isServitor()
               && effects.length > 0
               && (effects[0].canBeStolen() || skill.isHeroSkill() || skill.isStatic())) {
               skill.getEffects(activeChar, target.getSummon(), new Env(shld, ss, sps, bss), true);
            }

            if (skill.getSkillType() == SkillType.AGGDEBUFF) {
               if (target.isAttackable()) {
                  target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf((int)skill.getPower()));
               } else if (target.isPlayable()) {
                  if (target.getTarget() == activeChar) {
                     target.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
                  } else {
                     target.setTarget(activeChar);
                  }
               }
            }
         } else {
            activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
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

      activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
