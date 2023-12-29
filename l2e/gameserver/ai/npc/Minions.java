package l2e.gameserver.ai.npc;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.MobGroup;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControllableMobInstance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;

public class Minions extends DefaultAI {
   public static final int AI_IDLE = 1;
   public static final int AI_NORMAL = 2;
   public static final int AI_FORCEATTACK = 3;
   public static final int AI_FOLLOW = 4;
   public static final int AI_CAST = 5;
   public static final int AI_ATTACK_GROUP = 6;
   private int _alternateAI;
   private boolean _isThinking;
   private boolean _isNotMoving;
   private Creature _forcedTarget;
   private MobGroup _targetGroup;

   public Minions(ControllableMobInstance controllableMob) {
      super(controllableMob);
      this.setAlternateAI(1);
   }

   protected void thinkFollow() {
      Attackable me = (Attackable)this._actor;
      if (!Util.checkIfInRange(300, me, this.getForcedTarget(), true)) {
         int signX = Rnd.nextInt(2) == 0 ? -1 : 1;
         int signY = Rnd.nextInt(2) == 0 ? -1 : 1;
         int randX = Rnd.nextInt(300);
         int randY = Rnd.nextInt(300);
         this.moveTo(this.getForcedTarget().getX() + signX * randX, this.getForcedTarget().getY() + signY * randY, this.getForcedTarget().getZ(), 0);
      }
   }

   @Override
   protected void onEvtThink() {
      if (!this.isThinking()) {
         this.setThinking(true);

         try {
            switch(this.getAlternateAI()) {
               case 1:
                  if (this.getIntention() != CtrlIntention.ACTIVE) {
                     this.setIntention(CtrlIntention.ACTIVE);
                  }
                  break;
               case 2:
               default:
                  if (this.getIntention() == CtrlIntention.ACTIVE) {
                     this.thinkActive();
                  } else if (this.getIntention() == CtrlIntention.ATTACK) {
                     this.thinkAttack();
                  }
                  break;
               case 3:
                  this.thinkForceAttack();
                  break;
               case 4:
                  this.thinkFollow();
                  break;
               case 5:
                  this.thinkCast();
                  break;
               case 6:
                  this.thinkAttackGroup();
            }
         } finally {
            this.setThinking(false);
         }
      }
   }

   @Override
   protected void thinkCast() {
      Attackable npc = (Attackable)this._actor;
      if (this.getAttackTarget() == null || this.getAttackTarget().isAlikeDead()) {
         this.setAttackTarget(this.findNextRndTarget());
         this.clientStopMoving(null);
      }

      if (this.getAttackTarget() != null) {
         npc.setTarget(this.getAttackTarget());
         if (!this._actor.isMuted()) {
            int max_range = 0;

            for(Skill sk : this._actor.getAllSkills()) {
               if (Util.checkIfInRange(sk.getCastRange(), this._actor, this.getAttackTarget(), true)
                  && !this._actor.isSkillDisabled(sk)
                  && this._actor.getCurrentMp() > (double)this._actor.getStat().getMpConsume(sk)) {
                  this._actor.doCast(sk);
                  return;
               }

               max_range = Math.max(max_range, sk.getCastRange());
            }

            if (!this.isNotMoving()) {
               this.moveToPawn(this.getAttackTarget(), max_range);
            }
         }
      }
   }

   protected void thinkAttackGroup() {
      Creature target = this.getForcedTarget();
      if (target == null || target.isAlikeDead()) {
         this.setForcedTarget(this.findNextGroupTarget());
         this.clientStopMoving(null);
      }

      if (target != null) {
         this._actor.setTarget(target);
         ControllableMobInstance theTarget = (ControllableMobInstance)target;
         Minions ctrlAi = (Minions)theTarget.getAI();
         ctrlAi.forceAttack(this._actor);
         double dist2 = this._actor.getPlanDistanceSq(target.getX(), target.getY());
         int range = (int)((double)this._actor.getPhysicalAttackRange() + this._actor.getColRadius() + target.getColRadius());
         int max_range = range;
         if (!this._actor.isMuted() && dist2 > (double)((range + 20) * (range + 20))) {
            for(Skill sk : this._actor.getAllSkills()) {
               int castRange = sk.getCastRange();
               if ((double)(castRange * castRange) >= dist2
                  && !this._actor.isSkillDisabled(sk)
                  && this._actor.getCurrentMp() > (double)this._actor.getStat().getMpConsume(sk)) {
                  this._actor.doCast(sk);
                  return;
               }

               max_range = Math.max(max_range, castRange);
            }

            if (!this.isNotMoving()) {
               this.moveToPawn(target, range);
            }
         } else {
            this._actor.doAttack(target);
         }
      }
   }

   protected void thinkForceAttack() {
      if (this.getForcedTarget() == null || this.getForcedTarget().isAlikeDead()) {
         this.clientStopMoving(null);
         this.setIntention(CtrlIntention.ACTIVE);
         this.setAlternateAI(1);
      }

      this._actor.setTarget(this.getForcedTarget());
      double dist2 = this._actor.getPlanDistanceSq(this.getForcedTarget().getX(), this.getForcedTarget().getY());
      int range = (int)((double)this._actor.getPhysicalAttackRange() + this._actor.getColRadius() + this.getForcedTarget().getColRadius());
      int max_range = range;
      if (!this._actor.isMuted() && dist2 > (double)((range + 20) * (range + 20))) {
         for(Skill sk : this._actor.getAllSkills()) {
            int castRange = sk.getCastRange();
            if ((double)(castRange * castRange) >= dist2
               && !this._actor.isSkillDisabled(sk)
               && this._actor.getCurrentMp() > (double)this._actor.getStat().getMpConsume(sk)) {
               this._actor.doCast(sk);
               return;
            }

            max_range = Math.max(max_range, castRange);
         }

         if (!this.isNotMoving()) {
            this.moveToPawn(this.getForcedTarget(), this._actor.getPhysicalAttackRange());
         }
      } else {
         this._actor.doAttack(this.getForcedTarget());
      }
   }

   @Override
   protected void thinkAttack() {
      if (this.getAttackTarget() != null && !this.getAttackTarget().isAlikeDead()) {
         if (!((Npc)this._actor).getFaction().isNone()) {
            for(Npc npc : World.getInstance().getAroundNpc(this._actor)) {
               if ((npc.getFaction().isNone() || ((Npc)this._actor).isInFaction((Attackable)npc))
                  && this._actor.isInsideRadius(npc, npc.getFaction().getRange(), false, true)
                  && Math.abs(this.getAttackTarget().getZ() - npc.getZ()) < 200) {
                  npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(1));
               }
            }
         }

         this._actor.setTarget(this.getAttackTarget());
         double dist2 = this._actor.getPlanDistanceSq(this.getAttackTarget().getX(), this.getAttackTarget().getY());
         int range = (int)((double)this._actor.getPhysicalAttackRange() + this._actor.getColRadius() + this.getAttackTarget().getColRadius());
         int max_range = range;
         if (!this._actor.isMuted() && dist2 > (double)((range + 20) * (range + 20))) {
            for(Skill sk : this._actor.getAllSkills()) {
               int castRange = sk.getCastRange();
               if ((double)(castRange * castRange) >= dist2
                  && !this._actor.isSkillDisabled(sk)
                  && this._actor.getCurrentMp() > (double)this._actor.getStat().getMpConsume(sk)) {
                  this._actor.doCast(sk);
                  return;
               }

               max_range = Math.max(max_range, castRange);
            }

            this.moveToPawn(this.getAttackTarget(), range);
            return;
         }

         Creature hated;
         if (this._actor.isConfused()) {
            hated = this.findNextRndTarget();
         } else {
            hated = this.getAttackTarget();
         }

         if (hated == null) {
            this.setIntention(CtrlIntention.ACTIVE);
            return;
         }

         if (hated != this.getAttackTarget()) {
            this.setAttackTarget(hated);
         }

         if (!this._actor.isMuted() && Rnd.nextInt(5) == 3) {
            for(Skill sk : this._actor.getAllSkills()) {
               int castRange = sk.getCastRange();
               if ((double)(castRange * castRange) >= dist2
                  && !this._actor.isSkillDisabled(sk)
                  && this._actor.getCurrentMp() < (double)this._actor.getStat().getMpConsume(sk)) {
                  this._actor.doCast(sk);
                  return;
               }
            }
         }

         this._actor.doAttack(this.getAttackTarget());
      } else {
         if (this.getAttackTarget() != null) {
            Attackable npc = (Attackable)this._actor;
            npc.stopHating(this.getAttackTarget());
         }

         this.setIntention(CtrlIntention.ACTIVE);
      }
   }

   @Override
   protected boolean thinkActive() {
      this.setAttackTarget(this.findNextRndTarget());
      Creature hated;
      if (this._actor.isConfused()) {
         hated = this.findNextRndTarget();
      } else {
         hated = this.getAttackTarget();
      }

      if (hated != null) {
         this._actor.setRunning();
         this.setIntention(CtrlIntention.ATTACK, hated);
      }

      return true;
   }

   @Override
   protected boolean checkAggression(Creature target) {
      if (target != null && this._actor instanceof Attackable) {
         Attackable me = (Attackable)this._actor;
         if (!(target instanceof NpcInstance) && !(target instanceof DoorInstance)) {
            if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || Math.abs(this._actor.getZ() - target.getZ()) > 100) {
               return false;
            } else if (target.isInvul()) {
               return false;
            } else if (target instanceof Player && ((Player)target).isSpawnProtected()) {
               return false;
            } else if (target.isPlayable() && ((Playable)target).isSilentMoving()) {
               return false;
            } else {
               return target instanceof Npc ? false : me.isAggressive();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private Creature findNextRndTarget() {
      int aggroRange = ((Attackable)this._actor).getAggroRange();
      Attackable npc = (Attackable)this._actor;
      double dblAggroRange = (double)(aggroRange * aggroRange);
      List<Creature> potentialTarget = new ArrayList<>();

      for(Creature obj : World.getInstance().getAroundCharacters(npc)) {
         int npcX = npc.getX();
         int npcY = npc.getY();
         int targetX = obj.getX();
         int targetY = obj.getY();
         double dx = (double)(npcX - targetX);
         double dy = (double)(npcY - targetY);
         if (!(dx * dx + dy * dy > dblAggroRange) && this.checkAggression(obj)) {
            potentialTarget.add(obj);
         }
      }

      if (potentialTarget.isEmpty()) {
         return null;
      } else {
         int choice = Rnd.nextInt(potentialTarget.size());
         Creature target = potentialTarget.get(choice);
         return target;
      }
   }

   private ControllableMobInstance findNextGroupTarget() {
      return this.getGroupTarget().getRandomMob();
   }

   public int getAlternateAI() {
      return this._alternateAI;
   }

   public void setAlternateAI(int _alternateai) {
      this._alternateAI = _alternateai;
   }

   public void forceAttack(Creature target) {
      this.setAlternateAI(3);
      this.setForcedTarget(target);
   }

   public void forceAttackGroup(MobGroup group) {
      this.setForcedTarget(null);
      this.setGroupTarget(group);
      this.setAlternateAI(6);
   }

   public void stop() {
      this.setAlternateAI(1);
      this.clientStopMoving(null);
   }

   public void move(int x, int y, int z) {
      this.moveTo(x, y, z, 0);
   }

   public void follow(Creature target) {
      this.setAlternateAI(4);
      this.setForcedTarget(target);
   }

   public boolean isThinking() {
      return this._isThinking;
   }

   public boolean isNotMoving() {
      return this._isNotMoving;
   }

   public void setNotMoving(boolean isNotMoving) {
      this._isNotMoving = isNotMoving;
   }

   public void setThinking(boolean isThinking) {
      this._isThinking = isThinking;
   }

   private Creature getForcedTarget() {
      return this._forcedTarget;
   }

   private MobGroup getGroupTarget() {
      return this._targetGroup;
   }

   private void setForcedTarget(Creature forcedTarget) {
      this._forcedTarget = forcedTarget;
   }

   private void setGroupTarget(MobGroup targetGroup) {
      this._targetGroup = targetGroup;
   }

   @Override
   protected boolean createNewTask() {
      return this.defaultFightTask();
   }

   @Override
   protected int getRatePHYS() {
      return 10;
   }

   @Override
   protected int getRateDOT() {
      return 8;
   }

   @Override
   protected int getRateDEBUFF() {
      return 5;
   }

   @Override
   protected int getRateDAM() {
      return 5;
   }

   @Override
   protected int getRateSTUN() {
      return 8;
   }

   @Override
   protected int getRateBUFF() {
      return 5;
   }

   @Override
   protected int getRateHEAL() {
      return 5;
   }

   @Override
   protected int getRateSuicide() {
      return 3;
   }

   @Override
   protected int getRateRes() {
      return 2;
   }

   @Override
   protected int getRateDodge() {
      Weapon weaponItem = this.getActiveChar().getActiveWeaponItem();
      return weaponItem.getItemType() == WeaponType.BOW ? 15 : 0;
   }
}
