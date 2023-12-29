package l2e.gameserver.ai.character;

import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;

public class SummonAI extends PlayableAI implements Runnable {
   private static final int AVOID_RADIUS = 70;
   private volatile boolean _thinking;
   private volatile boolean _startFollow = ((Summon)this._actor).isInFollowStatus();
   private Creature _lastAttack = null;
   private volatile boolean _startAvoid;
   private Future<?> _avoidTask = null;

   public SummonAI(Summon summon) {
      super(summon);
   }

   @Override
   protected void onIntentionIdle() {
      this.stopFollow();
      this._startFollow = false;
      this.onIntentionActive();
   }

   @Override
   protected void onIntentionActive() {
      Summon summon = (Summon)this._actor;
      if (this._startFollow) {
         this.setIntention(CtrlIntention.FOLLOW, summon.getOwner());
      } else {
         super.onIntentionActive();
      }
   }

   @Override
   protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
      switch(intention) {
         case ACTIVE:
         case FOLLOW:
         case ATTACK:
            this.startAvoidTask();
            break;
         default:
            this.stopAvoidTask();
      }

      super.changeIntention(intention, arg0, arg1);
   }

   private void thinkAttack() {
      Creature target = this.getAttackTarget();
      if (target != null) {
         if (target.isDead()) {
            ((Summon)this._actor).setFollowStatus(true);
            this._actor.getAI().setIntention(CtrlIntention.ACTIVE);
         } else {
            int range = this._actor.getPhysicalAttackRange();
            boolean canSee = GeoEngine.canSeeTarget(this._actor, target, false);
            if (canSee || range <= 200 && Math.abs(this._actor.getZ() - target.getZ()) <= 200) {
               if (this._actor.isInRangeZ(this._actor, target, (long)range)) {
                  if (!canSee) {
                     this._actor.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                     this._actor.getAI().setIntention(CtrlIntention.ACTIVE);
                     return;
                  }

                  if (this.checkTargetLostOrDead(target)) {
                     this.setAttackTarget(null);
                     return;
                  }

                  if (this.maybeMoveToPawn(target, range)) {
                     return;
                  }

                  this.clientStopMoving(null);
                  this._actor.doAttack(target);
               } else {
                  if (this.checkTargetLostOrDead(target)) {
                     this.setAttackTarget(null);
                     return;
                  }

                  if (this.maybeMoveToPawn(target, range)) {
                     return;
                  }

                  this.clientStopMoving(null);
                  this._actor.doAttack(target);
               }
            } else {
               this.clientStopMoving(null);
               this._actor.getAI().setIntention(CtrlIntention.ACTIVE);
            }
         }
      }
   }

   private void thinkCast() {
      Summon summon = (Summon)this._actor;
      if (this.checkTargetLost(this.getCastTarget())) {
         this.setCastTarget(null);
      } else {
         boolean val = this._startFollow;
         if (!this.maybeMoveToPawn(this.getCastTarget(), this._actor.getMagicalAttackRange(this._skill))) {
            this.clientStopMoving(null);
            summon.setFollowStatus(false);
            this.setIntention(CtrlIntention.IDLE);
            this._startFollow = val;
            this._actor.doCast(this._skill);
         }
      }
   }

   private void thinkPickUp() {
      if (!this.checkTargetLost(this.getTarget())) {
         if (!this.maybeMoveToPawn(this.getTarget(), 36)) {
            this.setIntention(CtrlIntention.IDLE);
            this.getActor().doPickupItem(this.getTarget());
         }
      }
   }

   private void thinkInteract() {
      if (!this.checkTargetLost(this.getTarget())) {
         if (!this.maybeMoveToPawn(this.getTarget(), 36)) {
            this.setIntention(CtrlIntention.IDLE);
         }
      }
   }

   @Override
   protected void onEvtThink() {
      if (!this._thinking && !this._actor.isCastingNow() && !this._actor.isAllSkillsDisabled()) {
         this._thinking = true;

         try {
            switch(this.getIntention()) {
               case ATTACK:
                  this.thinkAttack();
                  break;
               case CAST:
                  this.thinkCast();
                  break;
               case PICK_UP:
                  this.thinkPickUp();
                  break;
               case INTERACT:
                  this.thinkInteract();
            }
         } finally {
            this._thinking = false;
         }
      }
   }

   @Override
   protected void onEvtFinishCasting() {
      if (this._lastAttack == null) {
         ((Summon)this._actor).setFollowStatus(this._startFollow);
      } else {
         this.setIntention(CtrlIntention.ATTACK, this._lastAttack);
         this._lastAttack = null;
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      super.onEvtAttacked(attacker, damage);
      this.avoidAttack(attacker);
   }

   @Override
   protected void onEvtEvaded(Creature attacker) {
      super.onEvtEvaded(attacker);
      this.avoidAttack(attacker);
   }

   private void avoidAttack(Creature attacker) {
      if (!this._actor.isCastingNow()) {
         Creature owner = this.getActor().getOwner();
         if (owner != null && owner != attacker && owner.isInsideRadius(this._actor, 140, true, false)) {
            this._startAvoid = true;
         }
      }
   }

   @Override
   public void run() {
      if (this._startAvoid) {
         this._startAvoid = false;
         if (!this._clientMoving && !this._actor.isAttackingNow() && !this._actor.isDead() && !this._actor.isMovementDisabled() && !this._actor.isCastingNow()
            )
          {
            int ownerX = ((Summon)this._actor).getOwner().getX();
            int ownerY = ((Summon)this._actor).getOwner().getY();
            double angle = Math.toRadians((double)Rnd.get(-90, 90)) + Math.atan2((double)(ownerY - this._actor.getY()), (double)(ownerX - this._actor.getX()));
            int targetX = ownerX + (int)(70.0 * Math.cos(angle));
            int targetY = ownerY + (int)(70.0 * Math.sin(angle));
            this.moveTo(targetX, targetY, this._actor.getZ(), 0);
         }
      }
   }

   public void notifyFollowStatusChange() {
      this._startFollow = !this._startFollow;
      switch(this.getIntention()) {
         case ACTIVE:
         case FOLLOW:
         case PICK_UP:
         case IDLE:
         case MOVING:
            ((Summon)this._actor).setFollowStatus(this._startFollow);
         case ATTACK:
         case CAST:
         case INTERACT:
      }
   }

   public void setStartFollowController(boolean val) {
      this._startFollow = val;
   }

   @Override
   protected void onIntentionCast(Skill skill, GameObject target) {
      if (this.getIntention() == CtrlIntention.ATTACK) {
         this._lastAttack = this.getAttackTarget();
      } else {
         this._lastAttack = null;
      }

      super.onIntentionCast(skill, target);
   }

   private void startAvoidTask() {
      if (this._avoidTask == null) {
         this._avoidTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 100L, 100L);
      }
   }

   private void stopAvoidTask() {
      if (this._avoidTask != null) {
         this._avoidTask.cancel(false);
         this._avoidTask = null;
      }
   }

   @Override
   public void stopAITask() {
      this.stopAvoidTask();
      super.stopAITask();
   }

   public Summon getActor() {
      return (Summon)super.getActor();
   }
}
