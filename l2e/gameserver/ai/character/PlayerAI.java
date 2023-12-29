package l2e.gameserver.ai.character;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public class PlayerAI extends PlayableAI {
   private boolean _thinking;
   CharacterAI.IntentionCommand _nextIntention = null;

   public PlayerAI(Player accessor) {
      super(accessor);
   }

   void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1) {
      this._nextIntention = new CharacterAI.IntentionCommand(intention, arg0, arg1);
   }

   @Override
   public CharacterAI.IntentionCommand getNextIntention() {
      return this._nextIntention;
   }

   @Override
   protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
      if (intention == CtrlIntention.CAST && (arg0 == null || ((Skill)arg0).isToggle())) {
         if (intention == this._intention && arg0 == this._intentionArg0 && arg1 == this._intentionArg1) {
            super.changeIntention(intention, arg0, arg1);
         } else {
            this.saveNextIntention(this._intention, this._intentionArg0, this._intentionArg1);
            super.changeIntention(intention, arg0, arg1);
         }
      } else {
         this._nextIntention = null;
         super.changeIntention(intention, arg0, arg1);
      }
   }

   @Override
   protected void onEvtReadyToAct() {
      if (this._nextIntention != null) {
         this.setIntention(this._nextIntention._crtlIntention, this._nextIntention._arg0, this._nextIntention._arg1);
         this._nextIntention = null;
      }

      super.onEvtReadyToAct();
   }

   @Override
   protected void onEvtCancel() {
      this._nextIntention = null;
      super.onEvtCancel();
   }

   @Override
   protected void onEvtFinishCasting() {
      if (this.getIntention() == CtrlIntention.CAST) {
         CharacterAI.IntentionCommand nextIntention = this._nextIntention;
         if (nextIntention != null) {
            if (nextIntention._crtlIntention != CtrlIntention.CAST) {
               this.setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
            } else {
               this.setIntention(CtrlIntention.IDLE);
            }
         } else {
            this.setIntention(CtrlIntention.IDLE);
         }
      }
   }

   @Override
   protected void onIntentionRest() {
      if (this.getIntention() != CtrlIntention.REST) {
         this.changeIntention(CtrlIntention.REST, null, null);
         this.setTarget(null);
         if (this.getAttackTarget() != null) {
            this.setAttackTarget(null);
         }

         this.clientStopMoving(null);
      }
   }

   @Override
   protected void onIntentionActive() {
      this.setIntention(CtrlIntention.IDLE);
   }

   @Override
   protected void onIntentionMoveTo(Location loc) {
      if (this.getIntention() == CtrlIntention.REST) {
         this.clientActionFailed();
      } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         this.changeIntention(CtrlIntention.MOVING, loc, null);
         this.clientStopAutoAttack();
         this._actor.abortAttack();
         this.moveTo(loc);
      } else {
         this.clientActionFailed();
         this.saveNextIntention(CtrlIntention.MOVING, loc, null);
      }
   }

   @Override
   protected void clientNotifyDead() {
      this._clientMovingToPawnOffset = 0;
      this._clientMoving = false;
      super.clientNotifyDead();
   }

   private void thinkAttack() {
      Creature target = this.getAttackTarget();
      if (target != null) {
         if (this._actor.isActionsDisabled()) {
            this._actor.sendActionFailed();
         } else {
            int range = this._actor.getPhysicalAttackRange();
            boolean canSee = GeoEngine.canSeeTarget(this._actor, target, false);
            if (canSee || range <= 200 && Math.abs(this._actor.getZ() - target.getZ()) <= 200) {
               if (this._actor.isInRangeZ(this._actor, target, (long)range)) {
                  if (!canSee) {
                     this._actor.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                     this._actor.getAI().setIntention(CtrlIntention.ACTIVE);
                     this._actor.sendActionFailed();
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
               this._actor.sendActionFailed();
            }
         }
      }
   }

   private void thinkCast() {
      Creature target = this.getCastTarget();
      int range = this._actor.getMagicalAttackRange(this._skill);
      boolean canSee = this._skill.isDisableGeoCheck() || GeoEngine.canSeeTarget(this._actor, target, this._actor.isFlying());
      if ((this._skill.getCastRange() > 0 || this._skill.getEffectRange() > 0) && (this._actor.isInRangeZ(this._actor, target, (long)range) || !canSee)) {
         if (!canSee) {
            this._actor.setIsCastingNow(false);
            this._actor.getAI().setIntention(CtrlIntention.ACTIVE);
            this._actor.sendActionFailed();
            return;
         }

         if (this._skill.getTargetType() == TargetType.GROUND && this._actor instanceof Player) {
            if (this.maybeMoveToPosition(((Player)this._actor).getCurrentSkillWorldPosition(), range)) {
               this._actor.setIsCastingNow(false);
               return;
            }
         } else {
            if (this.checkTargetLost(target)) {
               if (this._skill.isOffensive() && this.getAttackTarget() != null) {
                  this.setCastTarget(null);
               }

               this._actor.setIsCastingNow(false);
               return;
            }

            if (target != null && this.maybeMoveToPawn(target, range)) {
               this._actor.setIsCastingNow(false);
               return;
            }
         }

         GameObject oldTarget = this._actor.getTarget();
         if (oldTarget != null && target != null && oldTarget != target) {
            this._actor.getActingPlayer().setFastTarget(this.getCastTarget());
            this._actor.doCast(this._skill);
            this._actor.setTarget(oldTarget);
         } else {
            this._actor.doCast(this._skill);
         }
      } else {
         if (this._skill.getTargetType() == TargetType.GROUND && this._actor instanceof Player) {
            if (this.maybeMoveToPosition(((Player)this._actor).getCurrentSkillWorldPosition(), range)) {
               this._actor.setIsCastingNow(false);
               return;
            }
         } else {
            if (this.checkTargetLost(target)) {
               if (this._skill.isOffensive() && this.getAttackTarget() != null) {
                  this.setCastTarget(null);
               }

               this._actor.setIsCastingNow(false);
               return;
            }

            if (target != null && this.maybeMoveToPawn(target, range)) {
               this._actor.setIsCastingNow(false);
               return;
            }
         }

         GameObject oldTarget = this._actor.getTarget();
         if (oldTarget != null && target != null && oldTarget != target) {
            this._actor.getActingPlayer().setFastTarget(this.getCastTarget());
            this._actor.doCast(this._skill);
            this._actor.setTarget(oldTarget);
         } else {
            this._actor.doCast(this._skill);
         }
      }
   }

   private void thinkPickUp() {
      if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         GameObject target = this.getTarget();
         if (!this.checkTargetLost(target)) {
            if (!this.maybeMoveToPawn(target, 36)) {
               this.setIntention(CtrlIntention.IDLE);
               this._actor.getActingPlayer().doPickupItem(target);
            }
         }
      }
   }

   private void thinkInteract() {
      if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         GameObject target = this.getTarget();
         if (!this.checkTargetLost(target)) {
            if (!this.maybeMoveToPawn(target, 36)) {
               if (!(target instanceof StaticObjectInstance)) {
                  ((Player)this._actor).doInteract((Creature)target);
               }

               this.setIntention(CtrlIntention.IDLE);
            }
         }
      }
   }

   private void thinkMoveAndInteract() {
      if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         GameObject target = this.getTarget();
         if (!this.checkTargetLost(target)) {
            if (!this.checkDistanceAndMove(target)) {
               if (!(target instanceof StaticObjectInstance)) {
                  this._actor.getActingPlayer().doInteract((Creature)target);
               }

               this.setIntention(CtrlIntention.IDLE);
            }
         }
      }
   }

   @Override
   protected void onEvtThink() {
      if (!this._thinking || this.getIntention() == CtrlIntention.CAST) {
         this._thinking = true;

         try {
            if (this.getIntention() == CtrlIntention.ATTACK) {
               this.thinkAttack();
            } else if (this.getIntention() == CtrlIntention.CAST) {
               this.thinkCast();
            } else if (this.getIntention() == CtrlIntention.PICK_UP) {
               this.thinkPickUp();
            } else if (this.getIntention() == CtrlIntention.INTERACT) {
               this.thinkInteract();
            } else if (this.getIntention() == CtrlIntention.MOVE_AND_INTERACT) {
               this.thinkMoveAndInteract();
            }
         } finally {
            this._thinking = false;
         }
      }
   }

   public Player getActor() {
      return (Player)super.getActor();
   }
}
