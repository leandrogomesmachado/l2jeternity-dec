package l2e.gameserver.handler.skillhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Disablers implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{
      SkillType.STUN,
      SkillType.ROOT,
      SkillType.SLEEP,
      SkillType.CONFUSION,
      SkillType.AGGDAMAGE,
      SkillType.AGGREDUCE,
      SkillType.AGGREDUCE_CHAR,
      SkillType.AGGREMOVE,
      SkillType.MUTE,
      SkillType.CONFUSE_MOB_ONLY,
      SkillType.PARALYZE,
      SkillType.ERASE,
      SkillType.BETRAY,
      SkillType.DISARM
   };
   protected static final Logger _log = Logger.getLogger(Skill.class.getName());

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      SkillType type = skill.getSkillType();
      byte shld = 0;
      boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
      boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
      boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

      for(GameObject obj : targets) {
         if (obj instanceof Creature) {
            Creature target = (Creature)obj;
            if (!target.isDead() && (!target.isInvul() || target.isParalyzed())) {
               shld = Formulas.calcShldUse(activeChar, target, skill);
               switch(type) {
                  case BETRAY:
                     if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                     } else {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                        sm.addCharName(target);
                        sm.addSkillName(skill);
                        activeChar.sendPacket(sm);
                     }
                     break;
                  case ROOT:
                  case DISARM:
                  case STUN:
                  case SLEEP:
                  case PARALYZE:
                     if (Formulas.calcSkillReflect(target, skill) == 1) {
                        target = activeChar;
                     }

                     if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                     } else if (activeChar.isPlayer()) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                        sm.addCharName(target);
                        sm.addSkillName(skill);
                        activeChar.sendPacket(sm);
                     }
                     break;
                  case CONFUSION:
                  case MUTE:
                     if (Formulas.calcSkillReflect(target, skill) == 1) {
                        target = activeChar;
                     }

                     if (!Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                        if (activeChar.isPlayer()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                           sm.addCharName(target);
                           sm.addSkillName(skill);
                           activeChar.sendPacket(sm);
                        }
                        break;
                     }

                     Effect[] effects = target.getAllEffects();
                     Effect[] var33 = effects;
                     int var34 = effects.length;
                     int var35 = 0;

                     for(; var35 < var34; ++var35) {
                        Effect e = var33[var35];
                        if (e != null && e.getSkill() != null && e.getSkill().getSkillType() == type) {
                           e.exit();
                        }
                     }

                     skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                     break;
                  case CONFUSE_MOB_ONLY:
                     if (!target.isAttackable()) {
                        activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                     } else {
                        if (!Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                           if (activeChar.isPlayer()) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                              sm.addCharName(target);
                              sm.addSkillName(skill);
                              activeChar.sendPacket(sm);
                           }
                           continue;
                        }

                        Effect[] effects = target.getAllEffects();

                        for(Effect e : effects) {
                           if (e.getSkill().getSkillType() == type) {
                              e.exit();
                           }
                        }

                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                     }
                     break;
                  case AGGDAMAGE:
                     if (target.isAutoAttackable(activeChar)) {
                        if (target.hasAI() && target.isNpc() && skill.getId() == 51) {
                           ((DefaultAI)target.getAI()).setNotifyFriend(false);
                        }

                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                        Formulas.calcLethalHit(activeChar, target, skill);
                     }
                     break;
                  case AGGREDUCE:
                     if (target.isAttackable()) {
                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                        double aggdiff = (double)((Attackable)target).getHating(activeChar)
                           - target.calcStat(Stats.AGGRESSION, (double)((Attackable)target).getHating(activeChar), target, skill);
                        if (skill.getPower() > 0.0) {
                           ((Attackable)target).reduceHate(null, (int)skill.getPower());
                        } else if (aggdiff > 0.0) {
                           ((Attackable)target).reduceHate(null, (int)aggdiff);
                        }
                     }
                     break;
                  case AGGREDUCE_CHAR:
                     if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                        if (target.isAttackable()) {
                           Attackable targ = (Attackable)target;
                           targ.stopHating(activeChar);
                           if (targ.getMostHated() == null && targ.hasAI() && targ.getAI() instanceof DefaultAI) {
                              ((DefaultAI)targ.getAI()).setGlobalAggro(-25);
                              targ.clearAggroList();
                              targ.getAI().setIntention(CtrlIntention.ACTIVE);
                              targ.setWalking();
                           }
                        }

                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
                     } else {
                        if (activeChar.isPlayer()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                           sm.addCharName(target);
                           sm.addSkillName(skill);
                           activeChar.sendPacket(sm);
                        }

                        target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, Integer.valueOf(0));
                     }
                     break;
                  case AGGREMOVE:
                     if (target.isAttackable() && !target.isRaid()) {
                        if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                           if (skill.getTargetType() == TargetType.UNDEAD) {
                              if (target.isUndead()) {
                                 ((Attackable)target).reduceHate(null, ((Attackable)target).getHating(((Attackable)target).getMostHated()));
                              }
                           } else {
                              ((Attackable)target).reduceHate(null, ((Attackable)target).getHating(((Attackable)target).getMostHated()));
                           }
                        } else {
                           if (activeChar.isPlayer()) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                              sm.addCharName(target);
                              sm.addSkillName(skill);
                              activeChar.sendPacket(sm);
                           }

                           target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, Integer.valueOf(0));
                        }
                        break;
                     }

                     target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, Integer.valueOf(0));
                     break;
                  case ERASE:
                     if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss) && !(target instanceof SiegeSummonInstance)) {
                        Player summonOwner = ((Summon)target).getOwner();
                        Summon summon = summonOwner.getSummon();
                        if (summon != null) {
                           if (summon.isPhoenixBlessed()) {
                              if (summon.isNoblesseBlessed()) {
                                 summon.stopEffects(EffectType.NOBLESSE_BLESSING);
                              }
                           } else if (summon.isNoblesseBlessed()) {
                              summon.stopEffects(EffectType.NOBLESSE_BLESSING);
                           } else {
                              summon.stopAllEffectsExceptThoseThatLastThroughDeath();
                           }

                           summon.abortAttack();
                           summon.abortCast();
                           summon.unSummon(summonOwner);
                           summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
                        }
                     } else if (activeChar.isPlayer()) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                        sm.addCharName(target);
                        sm.addSkillName(skill);
                        activeChar.sendPacket(sm);
                     }
               }
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
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
