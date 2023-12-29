package l2e.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.handler.skillhandlers.SkillHandler;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.l2skills.SkillDrain;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class CubicInstance implements IIdentifiable {
   protected static final Logger _log = Logger.getLogger(CubicInstance.class.getName());
   public static final int STORM_CUBIC = 1;
   public static final int VAMPIRIC_CUBIC = 2;
   public static final int LIFE_CUBIC = 3;
   public static final int VIPER_CUBIC = 4;
   public static final int POLTERGEIST_CUBIC = 5;
   public static final int BINDING_CUBIC = 6;
   public static final int AQUA_CUBIC = 7;
   public static final int SPARK_CUBIC = 8;
   public static final int ATTRACT_CUBIC = 9;
   public static final int SMART_CUBIC_EVATEMPLAR = 10;
   public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
   public static final int SMART_CUBIC_ARCANALORD = 12;
   public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
   public static final int SMART_CUBIC_SPECTRALMASTER = 14;
   public static final int MAX_MAGIC_RANGE = 900;
   public static final int SKILL_CUBIC_HEAL = 4051;
   public static final int SKILL_CUBIC_CURE = 5579;
   protected Player _owner;
   protected Creature _target;
   protected int _id;
   protected int _cubicPower;
   protected int _cubicDuration;
   protected int _cubicDelay;
   protected int _cubicSkillChance;
   protected int _cubicMaxCount;
   protected int _currentcount;
   protected boolean _active;
   private final boolean _givenByOther;
   protected List<Skill> _skills = new ArrayList<>();
   private Future<?> _disappearTask;
   private Future<?> _actionTask;

   public CubicInstance(
      Player owner, int id, int level, int cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther
   ) {
      this._owner = owner;
      this._id = id;
      this._cubicPower = cubicPower;
      this._cubicDuration = cubicDuration * 1000;
      this._cubicDelay = cubicDelay * 1000;
      this._cubicSkillChance = cubicSkillChance;
      this._cubicMaxCount = cubicMaxCount;
      this._currentcount = 0;
      this._active = false;
      this._givenByOther = givenByOther;
      switch(this._id) {
         case 1:
            this._skills.add(SkillsParser.getInstance().getInfo(4049, level));
            break;
         case 2:
            this._skills.add(SkillsParser.getInstance().getInfo(4050, level));
            break;
         case 3:
            this._skills.add(SkillsParser.getInstance().getInfo(4051, level));
            this.doAction();
            break;
         case 4:
            this._skills.add(SkillsParser.getInstance().getInfo(4052, level));
            break;
         case 5:
            this._skills.add(SkillsParser.getInstance().getInfo(4053, level));
            this._skills.add(SkillsParser.getInstance().getInfo(4054, level));
            this._skills.add(SkillsParser.getInstance().getInfo(4055, level));
            break;
         case 6:
            this._skills.add(SkillsParser.getInstance().getInfo(4164, level));
            break;
         case 7:
            this._skills.add(SkillsParser.getInstance().getInfo(4165, level));
            break;
         case 8:
            this._skills.add(SkillsParser.getInstance().getInfo(4166, level));
            break;
         case 9:
            this._skills.add(SkillsParser.getInstance().getInfo(5115, level));
            this._skills.add(SkillsParser.getInstance().getInfo(5116, level));
            break;
         case 10:
            this._skills.add(SkillsParser.getInstance().getInfo(4053, 8));
            this._skills.add(SkillsParser.getInstance().getInfo(4165, 9));
            break;
         case 11:
            this._skills.add(SkillsParser.getInstance().getInfo(4049, 8));
            this._skills.add(SkillsParser.getInstance().getInfo(5115, 4));
            break;
         case 12:
            this._skills.add(SkillsParser.getInstance().getInfo(4051, 7));
            this._skills.add(SkillsParser.getInstance().getInfo(4165, 9));
            break;
         case 13:
            this._skills.add(SkillsParser.getInstance().getInfo(4049, 8));
            this._skills.add(SkillsParser.getInstance().getInfo(4166, 9));
            break;
         case 14:
            this._skills.add(SkillsParser.getInstance().getInfo(4049, 8));
            this._skills.add(SkillsParser.getInstance().getInfo(4052, 6));
      }

      this._disappearTask = ThreadPoolManager.getInstance().schedule(new CubicInstance.Disappear(this), (long)this._cubicDuration);
   }

   public synchronized void doAction() {
      if (!this._active) {
         this._active = true;
         switch(this._id) {
            case 1:
            case 2:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
               this._actionTask = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(new CubicInstance.Action(this, this._cubicSkillChance), 0L, (long)this._cubicDelay);
               break;
            case 3:
               this._actionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CubicInstance.Heal(this), 0L, (long)this._cubicDelay);
         }
      }
   }

   @Override
   public int getId() {
      return this._id;
   }

   public Player getOwner() {
      return this._owner;
   }

   public final int getMCriticalHit(Creature target, Skill skill) {
      return (int)(BaseStats.WIT.calcBonus(this._owner) * 10.0);
   }

   public int getCubicPower() {
      return this._cubicPower;
   }

   public void stopAction() {
      this._target = null;
      if (this._actionTask != null) {
         this._actionTask.cancel(true);
         this._actionTask = null;
      }

      this._active = false;
   }

   public void cancelDisappear() {
      if (this._disappearTask != null) {
         this._disappearTask.cancel(true);
         this._disappearTask = null;
      }
   }

   public void getCubicTarget() {
      try {
         this._target = null;
         GameObject ownerTarget = this._owner.getTarget();
         if (ownerTarget == null) {
            return;
         }

         if (this._owner.isInFightEvent()) {
            if (ownerTarget.getActingPlayer() != null) {
               for(AbstractFightEvent e : this._owner.getFightEvents()) {
                  if (e != null && e.canAttack((Creature)ownerTarget, this._owner) && !ownerTarget.getActingPlayer().isDead()) {
                     this._target = (Creature)ownerTarget;
                  }
               }
            }

            return;
         }

         if (this._owner.isInDuel()) {
            Player PlayerA = DuelManager.getInstance().getDuel(this._owner.getDuelId()).getPlayerA();
            Player PlayerB = DuelManager.getInstance().getDuel(this._owner.getDuelId()).getPlayerB();
            if (DuelManager.getInstance().getDuel(this._owner.getDuelId()).isPartyDuel()) {
               Party partyA = PlayerA.getParty();
               Party partyB = PlayerB.getParty();
               Party partyEnemy = null;
               if (partyA != null) {
                  if (partyA.getMembers().contains(this._owner)) {
                     if (partyB != null) {
                        partyEnemy = partyB;
                     } else {
                        this._target = PlayerB;
                     }
                  } else {
                     partyEnemy = partyA;
                  }
               } else if (PlayerA == this._owner) {
                  if (partyB != null) {
                     partyEnemy = partyB;
                  } else {
                     this._target = PlayerB;
                  }
               } else {
                  this._target = PlayerA;
               }

               if ((this._target == PlayerA || this._target == PlayerB) && this._target == ownerTarget) {
                  return;
               }

               if (partyEnemy != null) {
                  if (partyEnemy.getMembers().contains(ownerTarget)) {
                     this._target = (Creature)ownerTarget;
                  }

                  return;
               }
            }

            if (PlayerA != this._owner && ownerTarget == PlayerA) {
               this._target = PlayerA;
               return;
            }

            if (PlayerB != this._owner && ownerTarget == PlayerB) {
               this._target = PlayerB;
               return;
            }

            this._target = null;
            return;
         }

         if (this._owner.isInOlympiadMode()) {
            if (this._owner.isOlympiadStart() && ownerTarget instanceof Playable) {
               Player targetPlayer = ownerTarget.getActingPlayer();
               if (targetPlayer != null
                  && targetPlayer.getOlympiadGameId() == this._owner.getOlympiadGameId()
                  && targetPlayer.getOlympiadSide() != this._owner.getOlympiadSide()) {
                  this._target = (Creature)ownerTarget;
               }
            }

            return;
         }

         if (ownerTarget instanceof Creature && ownerTarget != this._owner.getSummon() && ownerTarget != this._owner) {
            if (ownerTarget instanceof Attackable) {
               if (((Attackable)ownerTarget).getAggroList().get(this._owner) != null && !((Attackable)ownerTarget).isDead()) {
                  this._target = (Creature)ownerTarget;
                  return;
               }

               if (this._owner.hasSummon()
                  && ((Attackable)ownerTarget).getAggroList().get(this._owner.getSummon()) != null
                  && !((Attackable)ownerTarget).isDead()) {
                  this._target = (Creature)ownerTarget;
                  return;
               }
            }

            Player enemy = null;
            if (this._owner.getPvpFlag() > 0 && !this._owner.isInsideZone(ZoneId.PEACE) || this._owner.isInsideZone(ZoneId.PVP)) {
               if (!((Creature)ownerTarget).isDead()) {
                  enemy = ownerTarget.getActingPlayer();
               }

               if (enemy != null) {
                  boolean targetIt = true;
                  if (this._owner.getParty() != null) {
                     if (this._owner.getParty().getMembers().contains(enemy)) {
                        targetIt = false;
                     } else if (this._owner.getParty().getCommandChannel() != null && this._owner.getParty().getCommandChannel().getMembers().contains(enemy)) {
                        targetIt = false;
                     }
                  }

                  if (this._owner.getClan() != null && !this._owner.isInsideZone(ZoneId.PVP)) {
                     if (this._owner.getClan().isMember(enemy.getObjectId())) {
                        targetIt = false;
                     }

                     if (this._owner.getAllyId() > 0 && enemy.getAllyId() > 0 && this._owner.getAllyId() == enemy.getAllyId()) {
                        targetIt = false;
                     }
                  }

                  if (enemy.getPvpFlag() == 0 && !enemy.isInsideZone(ZoneId.PVP)) {
                     targetIt = false;
                  }

                  if (enemy.isInsideZone(ZoneId.PEACE)) {
                     targetIt = false;
                  }

                  if (this._owner.getSiegeState() > 0 && this._owner.getSiegeState() == enemy.getSiegeState()) {
                     targetIt = false;
                  }

                  if (!enemy.isVisible()) {
                     targetIt = false;
                  }

                  if (targetIt) {
                     this._target = enemy;
                     return;
                  }
               }
            }
         }
      } catch (Exception var7) {
         _log.log(Level.SEVERE, "", (Throwable)var7);
      }
   }

   public void useCubicContinuous(CubicInstance activeCubic, Skill skill, GameObject[] targets) {
      for(Creature target : (Creature[])targets) {
         if (target != null && !target.isDead()) {
            if (skill.isOffensive()) {
               byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
               boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld);
               if (!acted) {
                  activeCubic.getOwner().sendPacket(SystemMessageId.ATTACK_FAILED);
                  continue;
               }
            }

            skill.getEffects(activeCubic, target, null);
         }
      }
   }

   public void useCubicMdam(CubicInstance activeCubic, Skill skill, GameObject[] targets) {
      for(Creature target : (Creature[])targets) {
         if (target != null) {
            if (target.isAlikeDead()) {
               if (!target.isPlayer()) {
                  continue;
               }

               target.stopFakeDeath(true);
            }

            boolean mcrit = Formulas.calcMCrit(activeCubic.getOwner().getMCriticalHit(target, skill));
            byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
            int damage = (int)Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);
            if (Config.DEBUG) {
               _log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);
            }

            if (damage > 0) {
               if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit)) {
                  target.breakAttack();
                  target.breakCast();
               }

               if (target.getStat().calcStat(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0.0, target, skill) > (double)Rnd.get(100)) {
                  int var11 = false;
               } else {
                  activeCubic.getOwner().sendDamageMessage(target, damage, skill, mcrit, false, false);
                  target.reduceCurrentHp((double)damage, activeCubic.getOwner(), skill);
               }
            }
         }
      }
   }

   public void useCubicDisabler(SkillType type, CubicInstance activeCubic, Skill skill, GameObject[] targets) {
      if (Config.DEBUG) {
         _log.info("Disablers: useCubicSkill()");
      }

      for(Creature target : (Creature[])targets) {
         if (target != null && !target.isDead()) {
            byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
            switch(type) {
               case STUN:
               case PARALYZE:
               case ROOT:
                  if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld)) {
                     skill.getEffects(activeCubic, target, null);
                     if (Config.DEBUG) {
                        _log.info("Disablers: useCubicSkill() -> success");
                     }
                  } else if (Config.DEBUG) {
                     _log.info("Disablers: useCubicSkill() -> failed");
                  }
                  break;
               case AGGDAMAGE:
                  if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld)) {
                     if (target instanceof Attackable) {
                        target.getAI()
                           .notifyEvent(
                              CtrlEvent.EVT_AGGRESSION,
                              activeCubic.getOwner(),
                              Integer.valueOf((int)(150.0 * skill.getPower() / (double)(target.getLevel() + 7)))
                           );
                     }

                     skill.getEffects(activeCubic, target, null);
                     if (Config.DEBUG) {
                        _log.info("Disablers: useCubicSkill() -> success");
                     }
                  } else if (Config.DEBUG) {
                     _log.info("Disablers: useCubicSkill() -> failed");
                  }
            }
         }
      }
   }

   public boolean isInCubicRange(Creature owner, Creature target) {
      if (owner != null && target != null) {
         int range = 900;
         int x = owner.getX() - target.getX();
         int y = owner.getY() - target.getY();
         int z = owner.getZ() - target.getZ();
         return x * x + y * y + z * z <= 810000;
      } else {
         return false;
      }
   }

   public void cubicTargetForHeal() {
      Creature target = null;
      double percentleft = 100.0;
      Party party = this._owner.getParty();
      if (this._owner.isInDuel() && !DuelManager.getInstance().getDuel(this._owner.getDuelId()).isPartyDuel()) {
         party = null;
      }

      if (party != null && !this._owner.isInOlympiadMode()) {
         for(Creature partyMember : party.getMembers()) {
            if (!partyMember.isDead()
               && this.isInCubicRange(this._owner, partyMember)
               && partyMember.getCurrentHp() < partyMember.getMaxHp()
               && percentleft > partyMember.getCurrentHp() / partyMember.getMaxHp()) {
               percentleft = partyMember.getCurrentHp() / partyMember.getMaxHp();
               target = partyMember;
            }

            if (partyMember.getSummon() != null
               && !partyMember.getSummon().isDead()
               && this.isInCubicRange(this._owner, partyMember.getSummon())
               && partyMember.getSummon().getCurrentHp() < partyMember.getSummon().getMaxHp()
               && percentleft > partyMember.getSummon().getCurrentHp() / partyMember.getSummon().getMaxHp()) {
               percentleft = partyMember.getSummon().getCurrentHp() / partyMember.getSummon().getMaxHp();
               target = partyMember.getSummon();
            }
         }
      } else {
         if (this._owner.getCurrentHp() < this._owner.getMaxHp()) {
            percentleft = this._owner.getCurrentHp() / this._owner.getMaxHp();
            target = this._owner;
         }

         if (this._owner.hasSummon()
            && !this._owner.getSummon().isDead()
            && this._owner.getSummon().getCurrentHp() < this._owner.getSummon().getMaxHp()
            && percentleft > this._owner.getSummon().getCurrentHp() / this._owner.getSummon().getMaxHp()
            && this.isInCubicRange(this._owner, this._owner.getSummon())) {
            target = this._owner.getSummon();
         }
      }

      this._target = target;
   }

   public boolean givenByOther() {
      return this._givenByOther;
   }

   private class Action implements Runnable {
      private final int _chance;
      private final CubicInstance _cubic;

      protected Action(CubicInstance cubic, int chance) {
         this._cubic = cubic;
         this._chance = chance;
      }

      @Override
      public void run() {
         try {
            if (CubicInstance.this._owner.isDead() || !CubicInstance.this._owner.isOnline()) {
               CubicInstance.this.stopAction();
               CubicInstance.this._owner.getCubics().remove(this._cubic.getId());
               CubicInstance.this._owner.broadcastUserInfo(true);
               CubicInstance.this.cancelDisappear();
               return;
            }

            if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(CubicInstance.this._owner)) {
               if (!CubicInstance.this._owner.hasSummon()) {
                  CubicInstance.this.stopAction();
                  return;
               }

               if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(CubicInstance.this._owner.getSummon())) {
                  CubicInstance.this.stopAction();
                  return;
               }
            }

            if (CubicInstance.this._cubicMaxCount > -1 && CubicInstance.this._currentcount >= CubicInstance.this._cubicMaxCount) {
               CubicInstance.this.stopAction();
               return;
            }

            boolean UseCubicCure = false;
            Skill skill = null;
            if (CubicInstance.this._id >= 10 && CubicInstance.this._id <= 14) {
               Effect[] effects = CubicInstance.this._owner.getAllEffects();

               for(Effect e : effects) {
                  if (e != null && e.getSkill().hasDebuffEffects() && e.getSkill().canBeDispeled()) {
                     UseCubicCure = true;
                     e.exit();
                  }
               }
            }

            if (UseCubicCure) {
               MagicSkillUse msu = new MagicSkillUse(CubicInstance.this._owner, CubicInstance.this._owner, 5579, 1, 0, 0);
               CubicInstance.this._owner.broadcastPacket(msu);
               ++CubicInstance.this._currentcount;
            } else if (Rnd.get(1, 100) < this._chance) {
               skill = CubicInstance.this._skills.get(Rnd.get(CubicInstance.this._skills.size()));
               if (skill != null) {
                  if (skill.getId() == 4051) {
                     CubicInstance.this.cubicTargetForHeal();
                  } else {
                     CubicInstance.this.getCubicTarget();
                     if (!CubicInstance.this.isInCubicRange(CubicInstance.this._owner, CubicInstance.this._target)) {
                        CubicInstance.this._target = null;
                     }
                  }

                  Creature target = CubicInstance.this._target;
                  if (target != null && !target.isDead()) {
                     if (Config.DEBUG) {
                        CubicInstance._log.info("CubicInstance: Action.run();");
                        CubicInstance._log
                           .info(
                              "Cubic Id: "
                                 + CubicInstance.this._id
                                 + " Target: "
                                 + target.getName()
                                 + " distance: "
                                 + Math.sqrt(
                                    target.getDistanceSq(CubicInstance.this._owner.getX(), CubicInstance.this._owner.getY(), CubicInstance.this._owner.getZ())
                                 )
                           );
                     }

                     CubicInstance.this._owner.broadcastPacket(new MagicSkillUse(CubicInstance.this._owner, target, skill.getId(), skill.getLevel(), 0, 0));
                     SkillType type = skill.getSkillType();
                     ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
                     Creature[] targets = new Creature[]{target};
                     if (type != SkillType.PARALYZE && type != SkillType.STUN && type != SkillType.ROOT && type != SkillType.AGGDAMAGE) {
                        if (type == SkillType.MDAM) {
                           if (Config.DEBUG) {
                              CubicInstance._log.info("CubicInstance: Action.run() handler " + type);
                           }

                           CubicInstance.this.useCubicMdam(CubicInstance.this, skill, targets);
                        } else if (type != SkillType.POISON && type != SkillType.DEBUFF && type != SkillType.DOT) {
                           if (type == SkillType.DRAIN) {
                              if (Config.DEBUG) {
                                 CubicInstance._log.info("CubicInstance: Action.run() skill " + type);
                              }

                              ((SkillDrain)skill).useCubicSkill(CubicInstance.this, targets);
                           } else {
                              handler.useSkill(CubicInstance.this._owner, skill, targets);
                              if (Config.DEBUG) {
                                 CubicInstance._log.info("CubicInstance: Action.run(); other handler");
                              }
                           }
                        } else {
                           if (Config.DEBUG) {
                              CubicInstance._log.info("CubicInstance: Action.run() handler " + type);
                           }

                           CubicInstance.this.useCubicContinuous(CubicInstance.this, skill, targets);
                        }
                     } else {
                        if (Config.DEBUG) {
                           CubicInstance._log.info("CubicInstance: Action.run() handler " + type);
                        }

                        CubicInstance.this.useCubicDisabler(type, CubicInstance.this, skill, targets);
                     }

                     if (skill.hasEffectType(EffectType.DMG_OVER_TIME, EffectType.DMG_OVER_TIME_PERCENT)) {
                        if (Config.DEBUG) {
                           CubicInstance._log.info("CubicInstance: Action.run() handler " + type);
                        }

                        CubicInstance.this.useCubicContinuous(CubicInstance.this, skill, targets);
                     }

                     ++CubicInstance.this._currentcount;
                  }
               }
            }
         } catch (Exception var8) {
            CubicInstance._log.log(Level.SEVERE, "", (Throwable)var8);
         }
      }
   }

   private class Disappear implements Runnable {
      private final CubicInstance _cubic;

      public Disappear(CubicInstance cubic) {
         this._cubic = cubic;
      }

      @Override
      public void run() {
         CubicInstance.this.stopAction();
         CubicInstance.this._owner.getCubics().remove(this._cubic.getId());
         CubicInstance.this._owner.broadcastUserInfo(true);
      }
   }

   protected class Heal implements Runnable {
      private final CubicInstance _cubic;

      public Heal(CubicInstance cubic) {
         this._cubic = cubic;
      }

      @Override
      public void run() {
         if (!CubicInstance.this._owner.isDead() && CubicInstance.this._owner.isOnline()) {
            try {
               Skill skill = null;

               for(Skill sk : CubicInstance.this._skills) {
                  if (sk.getId() == 4051) {
                     skill = sk;
                     break;
                  }
               }

               if (skill != null) {
                  CubicInstance.this.cubicTargetForHeal();
                  Creature target = CubicInstance.this._target;
                  if (target != null && !target.isDead() && target.getMaxHp() - target.getCurrentHp() > skill.getPower()) {
                     Creature[] targets = new Creature[]{target};
                     ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
                     if (handler != null) {
                        handler.useSkill(CubicInstance.this._owner, skill, targets);
                     } else {
                        skill.useSkill(CubicInstance.this._owner, targets);
                     }

                     MagicSkillUse msu = new MagicSkillUse(CubicInstance.this._owner, target, skill.getId(), skill.getLevel(), 0, 0);
                     CubicInstance.this._owner.broadcastPacket(msu);
                  }
               }
            } catch (Exception var6) {
               CubicInstance._log.log(Level.SEVERE, "", (Throwable)var6);
            }
         } else {
            CubicInstance.this.stopAction();
            CubicInstance.this._owner.getCubics().remove(this._cubic.getId());
            CubicInstance.this._owner.broadcastUserInfo(true);
            CubicInstance.this.cancelDisappear();
         }
      }
   }
}
