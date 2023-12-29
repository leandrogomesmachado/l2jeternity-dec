package l2e.gameserver.ai.guard;

import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.DefenderInstance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;

public class GuardAI extends CharacterAI implements Runnable {
   private static final int MAX_ATTACK_TIMEOUT = 300;
   private Future<?> _aiTask;
   private int _attackTimeout = Integer.MAX_VALUE;
   private int _globalAggro = -10;
   private boolean _thinking;
   private final int _attackRange = ((Attackable)this._actor).getPhysicalAttackRange();
   protected final Skill[] _damSkills = ((Npc)this._actor).getTemplate().getDamageSkills();
   protected final Skill[] _debuffSkills = ((Npc)this._actor).getTemplate().getDebuffSkills();
   protected final Skill[] _healSkills;
   protected final Skill[] _buffSkills = ((Npc)this._actor).getTemplate().getBuffSkills();
   protected final Skill[] _stunSkills = ((Npc)this._actor).getTemplate().getStunSkills();

   public GuardAI(Creature character) {
      super(character);
      this._healSkills = ((Npc)this._actor).getTemplate().getHealSkills();
   }

   @Override
   public void run() {
      this.onEvtThink();
   }

   protected boolean checkAggression(Creature target) {
      if (target != null
         && !(target instanceof DefenderInstance)
         && !(target instanceof NpcInstance)
         && !(target instanceof DoorInstance)
         && !target.isAlikeDead()) {
         if (target.isSummon()) {
            Player owner = ((Summon)target).getOwner();
            if (this._actor.isInsideRadius(owner, 1000, true, false)) {
               target = owner;
            }
         }

         if (target instanceof Playable && ((Playable)target).isSilentMoving() && Rnd.chance(90)) {
            return false;
         } else {
            return this._actor.isAutoAttackable(target) && GeoEngine.canSeeTarget(this._actor, target, false);
         }
      } else {
         return false;
      }
   }

   @Override
   protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
      if (intention == CtrlIntention.IDLE) {
         if (!this._actor.isAlikeDead()) {
            Attackable npc = (Attackable)this._actor;
            if (!World.getInstance().getAroundPlayers(npc).isEmpty()) {
               intention = CtrlIntention.ACTIVE;
            } else {
               intention = CtrlIntention.IDLE;
            }
         }

         if (intention == CtrlIntention.IDLE) {
            super.changeIntention(CtrlIntention.IDLE, null, null);
            if (this._aiTask != null) {
               this._aiTask.cancel(true);
               this._aiTask = null;
            }

            this._actor.detachAI();
            return;
         }
      }

      super.changeIntention(intention, arg0, arg1);
      if (this._aiTask == null) {
         this._aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
      }
   }

   @Override
   protected void onIntentionAttack(Creature target) {
      this._attackTimeout = 300 + GameTimeController.getInstance().getGameTicks();
      super.onIntentionAttack(target);
   }

   protected void thinkActive() {
      Attackable npc = (Attackable)this._actor;
      int aggroRange = 0;
      if (npc.getFaction().isNone()) {
         aggroRange = this._attackRange;
      } else {
         aggroRange = npc.getFaction().getRange();
      }

      if (this._globalAggro != 0) {
         if (this._globalAggro < 0) {
            ++this._globalAggro;
         } else {
            --this._globalAggro;
         }
      }

      if (this._globalAggro >= 0) {
         for(Creature target : World.getInstance().getAroundCharacters(npc, aggroRange, 200)) {
            if (target != null && this.checkAggression(target)) {
               int hating = npc.getHating(target);
               if (hating == 0) {
                  npc.addDamageHate(target, 0, 1);
               }
            }
         }

         Creature hated;
         if (this._actor.isConfused()) {
            hated = this.getAttackTarget();
         } else {
            hated = npc.getMostHated();
         }

         if (hated != null) {
            int aggro = npc.getHating(hated);
            if (aggro + this._globalAggro > 0) {
               if (!this._actor.isRunning()) {
                  this._actor.setRunning();
               }

               this.setIntention(CtrlIntention.ATTACK, hated, null);
            }

            return;
         }
      }

      ((DefenderInstance)this._actor).returnHome();
   }

   protected void thinkAttack() {
      if (this._attackTimeout < GameTimeController.getInstance().getGameTicks() && this._actor.isRunning()) {
         this._actor.setWalking();
         this._attackTimeout = 300 + GameTimeController.getInstance().getGameTicks();
      }

      Creature attackTarget = this.getAttackTarget();
      if (attackTarget != null && !attackTarget.isAlikeDead() && this._attackTimeout >= GameTimeController.getInstance().getGameTicks()) {
         this.factionNotifyAndSupport();
         this.attackPrepare();
      } else {
         if (attackTarget != null) {
            Attackable npc = (Attackable)this._actor;
            npc.stopHating(attackTarget);
         }

         this._attackTimeout = Integer.MAX_VALUE;
         this.setAttackTarget(null);
         this.setIntention(CtrlIntention.ACTIVE, null, null);
         this._actor.setWalking();
      }
   }

   private final void factionNotifyAndSupport() {
      Creature target = this.getAttackTarget();
      if (!((Npc)this._actor).getFaction().isNone() && target != null) {
         if (!target.isInvul()) {
            String faction_id = ((Npc)this._actor).getFaction().getName();

            for(Creature cha : World.getInstance().getAroundCharacters(this._actor, 1000, 200)) {
               if (cha != null) {
                  if (cha instanceof Npc) {
                     Npc npc = (Npc)cha;
                     if (faction_id.equals(npc.getFaction().getName()) && npc.getAI() != null) {
                        if (!npc.isDead()
                           && Math.abs(target.getZ() - npc.getZ()) < 600
                           && (npc.getAI().getIntention() == CtrlIntention.IDLE || npc.getAI().getIntention() == CtrlIntention.ACTIVE)
                           && target.isInsideRadius(npc, 1500, true, false)
                           && GeoEngine.canSeeTarget(npc, target, false)) {
                           npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(1));
                           return;
                        }

                        if (this._healSkills.length != 0
                           && !this._actor.isAttackingDisabled()
                           && npc.getCurrentHp() < npc.getMaxHp() * 0.6
                           && this._actor.getCurrentHp() > this._actor.getMaxHp() / 2.0
                           && this._actor.getCurrentMp() > this._actor.getMaxMp() / 2.0
                           && npc.isInCombat()) {
                           for(Skill sk : this._healSkills) {
                              if (!(this._actor.getCurrentMp() < (double)sk.getMpConsume())
                                 && !this._actor.isSkillDisabled(sk)
                                 && Util.checkIfInRange(sk.getCastRange(), this._actor, npc, true)) {
                                 int chance = 4;
                                 if (4 < Rnd.get(100)) {
                                    if (GeoEngine.canSeeTarget(this._actor, npc, false)) {
                                       GameObject OldTarget = this._actor.getTarget();
                                       this._actor.setTarget(npc);
                                       this.clientStopMoving(null);
                                       this._actor.doCast(sk);
                                       this._actor.setTarget(OldTarget);
                                       return;
                                    }
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  } else if (this._healSkills.length != 0
                     && cha instanceof Player
                     && ((Npc)this._actor).getCastle().getSiege().checkIsDefender(((Player)cha).getClan())
                     && !this._actor.isAttackingDisabled()
                     && cha.getCurrentHp() < cha.getMaxHp() * 0.6
                     && this._actor.getCurrentHp() > this._actor.getMaxHp() / 2.0
                     && this._actor.getCurrentMp() > this._actor.getMaxMp() / 2.0
                     && cha.isInCombat()) {
                     for(Skill sk : this._healSkills) {
                        if (!(this._actor.getCurrentMp() < (double)sk.getMpConsume())
                           && !this._actor.isSkillDisabled(sk)
                           && Util.checkIfInRange(sk.getCastRange(), this._actor, cha, true)) {
                           int chance = 5;
                           if (5 < Rnd.get(100)) {
                              if (GeoEngine.canSeeTarget(this._actor, cha, false)) {
                                 GameObject OldTarget = this._actor.getTarget();
                                 this._actor.setTarget(cha);
                                 this.clientStopMoving(null);
                                 this._actor.doCast(sk);
                                 this._actor.setTarget(OldTarget);
                                 return;
                              }
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void attackPrepare() {
      double dist2 = 0.0;
      int range = 0;
      DefenderInstance sGuard = (DefenderInstance)this._actor;
      Creature attackTarget = this.getAttackTarget();

      try {
         this._actor.setTarget(attackTarget);
         int combinedCollision = (int)(this._actor.getColRadius() + attackTarget.getColRadius());
         double dist = Math.sqrt(this._actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
         dist2 = (double)((int)dist) - this._actor.getColRadius();
         range = this._attackRange + combinedCollision;
         if (attackTarget.isMoving()) {
            range += 25;
            if (this._actor.isMoving()) {
               range += 25;
            }
         }
      } catch (NullPointerException var17) {
         this._actor.setTarget(null);
         this.setIntention(CtrlIntention.IDLE, null, null);
         return;
      }

      if (attackTarget instanceof Player && sGuard.getCastle().getSiege().checkIsDefender(((Player)attackTarget).getClan())) {
         sGuard.stopHating(attackTarget);
         this._actor.setTarget(null);
         this.setIntention(CtrlIntention.IDLE, null, null);
      } else if (!GeoEngine.canSeeTarget(this._actor, attackTarget, false)) {
         sGuard.stopHating(attackTarget);
         this._actor.setTarget(null);
         this.setIntention(CtrlIntention.IDLE, null, null);
      } else if (dist2 > (double)range) {
         if (!this.checkSkills(dist2)) {
            if (!this._actor.isAttackingNow()
               && this._actor.getRunSpeed() == 0.0
               && World.getInstance().getAroundCharacters(this._actor).contains(attackTarget)) {
               this._actor.setTarget(null);
               this.setIntention(CtrlIntention.IDLE, null, null);
            } else {
               double dx = (double)(this._actor.getX() - attackTarget.getX());
               double dy = (double)(this._actor.getY() - attackTarget.getY());
               double dz = (double)(this._actor.getZ() - attackTarget.getZ());
               double homeX = (double)(attackTarget.getX() - sGuard.getSpawn().getX());
               double homeY = (double)(attackTarget.getY() - sGuard.getSpawn().getY());
               if (dx * dx + dy * dy > 10000.0
                  && homeX * homeX + homeY * homeY > 3240000.0
                  && World.getInstance().getAroundCharacters(this._actor).contains(attackTarget)) {
                  this._actor.setTarget(null);
                  this.setIntention(CtrlIntention.IDLE, null, null);
               } else {
                  Creature hated = null;
                  if (this._actor.isConfused()) {
                     hated = attackTarget;
                  } else {
                     hated = ((Attackable)this._actor).getMostHated();
                  }

                  if (hated == null) {
                     this.setIntention(CtrlIntention.ACTIVE, null, null);
                     return;
                  }

                  if (hated != attackTarget) {
                     attackTarget = hated;
                  }

                  if (dz * dz < 28900.0 && attackTarget != null) {
                     this.moveTo(attackTarget.getLocation());
                  }
               }
            }
         }
      } else {
         this._attackTimeout = 300 + GameTimeController.getInstance().getGameTicks();
         if (Rnd.nextInt(100) > 5 || !this.checkSkills(dist2)) {
            if (((NpcTemplate)this._actor.getTemplate()).getAI() != "Priest") {
               this._actor.doAttack(attackTarget);
            }
         }
      }
   }

   protected boolean checkSkills(double distance) {
      if (this._actor.isMuted()) {
         return false;
      } else {
         if (this._debuffSkills.length != 0 && Rnd.chance(20)) {
            Skill skill = this._debuffSkills[Rnd.get(this._debuffSkills.length)];
            int castRange = skill.getCastRange();
            if (distance <= (double)castRange
               && !this._actor.isSkillDisabled(skill)
               && this._actor.getCurrentMp() >= (double)this._actor.getStat().getMpConsume(skill)
               && !skill.isPassive()
               && this.getAttackTarget().getFirstEffect(skill) == null) {
               this.clientStopMoving(null);
               this._actor.setTarget(this._actor.getTarget());
               this._actor.doCast(skill);
               return true;
            }
         }

         if (this._damSkills.length != 0) {
            Skill skill = this._damSkills[Rnd.get(this._damSkills.length)];
            int castRange = skill.getCastRange();
            if (distance <= (double)castRange
               && !this._actor.isSkillDisabled(skill)
               && this._actor.getCurrentMp() >= (double)this._actor.getStat().getMpConsume(skill)
               && !skill.isPassive()) {
               this.clientStopMoving(null);
               this._actor.setTarget(this._actor.getTarget());
               this._actor.doCast(skill);
               return true;
            }
         }

         if (this._stunSkills.length != 0 && Rnd.chance(20)) {
            Skill skill = this._stunSkills[Rnd.get(this._stunSkills.length)];
            int castRange = (int)(this.getAttackTarget().getColRadius() + this._actor.getColRadius());
            if (distance <= (double)castRange
               && !this._actor.isSkillDisabled(skill)
               && this._actor.getCurrentMp() >= (double)this._actor.getStat().getMpConsume(skill)
               && !skill.isPassive()) {
               this.clientStopMoving(null);
               this._actor.setTarget(this._actor.getTarget());
               this._actor.doCast(skill);
               return true;
            }
         }

         if (this._buffSkills.length != 0) {
            GameObject OldTarget = this._actor.getTarget();

            for(Skill skill : this._buffSkills) {
               boolean useSkillSelf = true;
               Effect[] effects = this._actor.getAllEffects();

               for(int i = 0; effects != null && i < effects.length; ++i) {
                  Effect effect = effects[i];
                  if (effect.getSkill() == skill) {
                     useSkillSelf = false;
                  }
               }

               if (useSkillSelf) {
                  this._actor.setTarget(this._actor);
                  this.clientStopMoving(null);
                  this._actor.doCast(skill);
                  this._actor.setTarget(OldTarget);
                  return true;
               }
            }
         }

         if (this._healSkills.length != 0 && this._actor.getCurrentHp() < this._actor.getMaxHp() / 2.0) {
            Skill skill = this._healSkills[Rnd.get(this._healSkills.length)];
            if (!this._actor.isSkillDisabled(skill) && this._actor.getCurrentMp() >= (double)this._actor.getStat().getMpConsume(skill) && !skill.isPassive()) {
               GameObject OldTarget = this._actor.getTarget();
               this._actor.setTarget(this._actor);
               this.clientStopMoving(null);
               this._actor.doCast(skill);
               this._actor.setTarget(OldTarget);
               return true;
            }
         }

         return false;
      }
   }

   @Override
   protected void onEvtThink() {
      if (!this._thinking && !this._actor.isCastingNow() && !this._actor.isAllSkillsDisabled()) {
         this._thinking = true;

         try {
            if (this.getIntention() == CtrlIntention.ACTIVE) {
               this.thinkActive();
            } else if (this.getIntention() == CtrlIntention.ATTACK) {
               this.thinkAttack();
            }
         } finally {
            this._thinking = false;
         }
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      this._attackTimeout = 300 + GameTimeController.getInstance().getGameTicks();
      if (this._globalAggro < 0) {
         this._globalAggro = 0;
      }

      ((Attackable)this._actor).addDamageHate(attacker, 0, 1);
      if (!this._actor.isRunning()) {
         this._actor.setRunning();
      }

      if (this.getIntention() != CtrlIntention.ATTACK) {
         this.setIntention(CtrlIntention.ATTACK, attacker, null);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
      if (this._actor != null) {
         Attackable me = (Attackable)this._actor;
         if (target != null) {
            me.addDamageHate(target, 0, aggro);
            aggro = me.getHating(target);
            if (aggro <= 0) {
               if (me.getMostHated() == null) {
                  this._globalAggro = -25;
                  me.clearAggroList();
                  this.setIntention(CtrlIntention.IDLE, null, null);
               }

               return;
            }

            if (this.getIntention() != CtrlIntention.ATTACK) {
               if (!this._actor.isRunning()) {
                  this._actor.setRunning();
               }

               DefenderInstance sGuard = (DefenderInstance)this._actor;
               double homeX = (double)(target.getX() - sGuard.getSpawn().getX());
               double homeY = (double)(target.getY() - sGuard.getSpawn().getY());
               if (homeX * homeX + homeY * homeY < 3240000.0) {
                  this.setIntention(CtrlIntention.ATTACK, target, null);
               }
            }
         } else {
            if (aggro >= 0) {
               return;
            }

            Creature mostHated = me.getMostHated();
            if (mostHated == null) {
               this._globalAggro = -25;
               return;
            }

            for(Creature aggroed : me.getAggroList().keySet()) {
               me.addDamageHate(aggroed, 0, aggro);
            }

            aggro = me.getHating(mostHated);
            if (aggro <= 0) {
               this._globalAggro = -25;
               me.clearAggroList();
               this.setIntention(CtrlIntention.IDLE, null, null);
            }
         }
      }
   }

   @Override
   public void stopAITask() {
      if (this._aiTask != null) {
         this._aiTask.cancel(false);
         this._aiTask = null;
      }

      this._actor.detachAI();
      super.stopAITask();
   }
}
