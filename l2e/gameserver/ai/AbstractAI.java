package l2e.gameserver.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.Ctrl;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.model.NextAction;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.AutoAttackStart;
import l2e.gameserver.network.serverpackets.AutoAttackStop;
import l2e.gameserver.network.serverpackets.Die;
import l2e.gameserver.network.serverpackets.FinishRotatings;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.MoveToPawn;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.ValidateLocation;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public abstract class AbstractAI implements Ctrl {
   protected final Logger _log = Logger.getLogger(this.getClass().getName());
   private NextAction _nextAction;
   protected final Creature _actor;
   protected CtrlIntention _intention = CtrlIntention.IDLE;
   protected Object _intentionArg0 = null;
   protected Object _intentionArg1 = null;
   protected volatile boolean _clientMoving;
   protected volatile boolean _clientAutoAttacking;
   protected int _clientMovingToPawnOffset;
   private GameObject _target;
   private Creature _castTarget;
   protected Creature _attackTarget;
   protected Creature _followTarget;
   protected List<Player> _targetList = new ArrayList<>();
   protected Skill _skill;
   private int _moveToPawnTimeout;
   protected Future<?> _followTask = null;
   private static final int FOLLOW_INTERVAL = 1000;
   private static final int ATTACK_FOLLOW_INTERVAL = 500;

   public NextAction getNextAction() {
      return this._nextAction;
   }

   public void setNextAction(NextAction nextAction) {
      this._nextAction = nextAction;
   }

   protected AbstractAI(Creature character) {
      this._actor = character;
   }

   @Override
   public Creature getActor() {
      return this._actor;
   }

   @Override
   public CtrlIntention getIntention() {
      return this._intention;
   }

   protected void setCastTarget(Creature target) {
      this._castTarget = target;
   }

   public Creature getCastTarget() {
      return this._castTarget;
   }

   protected void setAttackTarget(Creature target) {
      this._attackTarget = target;
   }

   @Override
   public Creature getAttackTarget() {
      return this._attackTarget;
   }

   protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
      this._intention = intention;
      this._intentionArg0 = arg0;
      this._intentionArg1 = arg1;
   }

   @Override
   public final void setIntention(CtrlIntention intention) {
      this.setIntention(intention, null, null);
   }

   @Override
   public final void setIntention(CtrlIntention intention, Object arg0) {
      this.setIntention(intention, arg0, null);
   }

   @Override
   public final void setIntention(CtrlIntention intention, Object arg0, Object arg1) {
      if (intention != CtrlIntention.FOLLOW && intention != CtrlIntention.ATTACK) {
         this.stopFollow();
      }

      switch(intention) {
         case IDLE:
            this.onIntentionIdle();
            break;
         case ACTIVE:
            this.onIntentionActive();
            break;
         case REST:
            this.onIntentionRest();
            break;
         case ATTACK:
            this.onIntentionAttack((Creature)arg0);
            break;
         case CAST:
            this.onIntentionCast((Skill)arg0, (GameObject)arg1);
            break;
         case MOVING:
            this.onIntentionMoveTo((Location)arg0);
            break;
         case FOLLOW:
            this.onIntentionFollow((Creature)arg0);
            break;
         case PICK_UP:
            this.onIntentionPickUp((GameObject)arg0);
            break;
         case INTERACT:
            this.onIntentionInteract((GameObject)arg0);
            break;
         case MOVE_AND_INTERACT:
            this.onIntentionMoveAndInteract((GameObject)arg0, (Location)arg1);
      }

      if (this._nextAction != null && this._nextAction.getIntentions().contains(intention)) {
         this._nextAction = null;
      }
   }

   @Override
   public final void notifyEvent(CtrlEvent evt) {
      this.notifyEvent(evt, null, null);
   }

   @Override
   public final void notifyEvent(CtrlEvent evt, Object arg0) {
      this.notifyEvent(evt, arg0, null);
   }

   @Override
   public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1) {
      if ((this._actor.isVisible() || this._actor.isTeleporting()) && this._actor.hasAI()) {
         switch(evt) {
            case EVT_THINK:
               this.onEvtThink();
               break;
            case EVT_ATTACKED:
               this.onEvtAttacked((Creature)arg0, ((Number)arg1).intValue());
               break;
            case EVT_SPAWN:
               this.onEvtSpawn();
               break;
            case EVT_AGGRESSION:
               this.onEvtAggression((Creature)arg0, ((Number)arg1).intValue());
               break;
            case EVT_STUNNED:
               this.onEvtStunned((Creature)arg0);
               break;
            case EVT_PARALYZED:
               this.onEvtParalyzed((Creature)arg0);
               break;
            case EVT_SLEEPING:
               this.onEvtSleeping((Creature)arg0);
               break;
            case EVT_ROOTED:
               this.onEvtRooted((Creature)arg0);
               break;
            case EVT_CONFUSED:
               this.onEvtConfused((Creature)arg0);
               break;
            case EVT_MUTED:
               this.onEvtMuted((Creature)arg0);
               break;
            case EVT_EVADED:
               this.onEvtEvaded((Creature)arg0);
               break;
            case EVT_READY_TO_ACT:
               if (!this._actor.isCastingNow() && !this._actor.isCastingSimultaneouslyNow()) {
                  this.onEvtReadyToAct();
               }
               break;
            case EVT_USER_CMD:
               this.onEvtUserCmd(arg0, arg1);
               break;
            case EVT_ARRIVED:
               if (!this._actor.isCastingNow() && !this._actor.isCastingSimultaneouslyNow()) {
                  this.onEvtArrived();
               }
               break;
            case EVT_ARRIVED_REVALIDATE:
               if (this._actor.isMoving()) {
                  this.onEvtArrivedRevalidate();
               }
               break;
            case EVT_ARRIVED_BLOCKED:
               this.onEvtArrivedBlocked((Location)arg0);
               break;
            case EVT_FORGET_OBJECT:
               this.onEvtForgetObject((GameObject)arg0);
               break;
            case EVT_CANCEL:
               this.onEvtCancel();
               break;
            case EVT_DEAD:
               this.onEvtDead((Creature)arg0);
               break;
            case EVT_FAKE_DEATH:
               this.onEvtFakeDeath();
               break;
            case EVT_FINISH_CASTING:
               this.onEvtFinishCasting();
               break;
            case EVT_SEE_SPELL:
               this.onEvtSeeSpell((Skill)arg0, (Creature)arg1);
               break;
            case EVT_TIMER:
               this.onEvtTimer(((Number)arg0).intValue(), arg1);
         }

         if (this._nextAction != null && this._nextAction.getEvents().contains(evt)) {
            this._nextAction.doAction();
         }
      }
   }

   protected abstract void onIntentionIdle();

   protected abstract void onIntentionActive();

   protected abstract void onIntentionRest();

   protected abstract void onIntentionAttack(Creature var1);

   protected abstract void onIntentionCast(Skill var1, GameObject var2);

   protected abstract void onIntentionMoveTo(Location var1);

   protected abstract void onIntentionFollow(Creature var1);

   protected abstract void onIntentionPickUp(GameObject var1);

   protected abstract void onIntentionInteract(GameObject var1);

   protected abstract void onEvtThink();

   protected abstract void onEvtAttacked(Creature var1, int var2);

   protected abstract void onEvtAggression(Creature var1, int var2);

   protected abstract void onEvtStunned(Creature var1);

   protected abstract void onEvtParalyzed(Creature var1);

   protected abstract void onEvtSleeping(Creature var1);

   protected abstract void onEvtRooted(Creature var1);

   protected abstract void onEvtConfused(Creature var1);

   protected abstract void onEvtMuted(Creature var1);

   protected abstract void onEvtEvaded(Creature var1);

   protected abstract void onEvtReadyToAct();

   protected abstract void onEvtUserCmd(Object var1, Object var2);

   protected abstract void onEvtArrived();

   protected abstract void onEvtArrivedRevalidate();

   protected abstract void onEvtArrivedBlocked(Location var1);

   protected abstract void onEvtForgetObject(GameObject var1);

   protected abstract void onEvtCancel();

   protected abstract void onEvtDead(Creature var1);

   protected abstract void onEvtSpawn();

   protected abstract void onEvtFakeDeath();

   protected abstract void onEvtFinishCasting();

   protected abstract void onEvtSeeSpell(Skill var1, Creature var2);

   protected abstract void onIntentionMoveAndInteract(GameObject var1, Location var2);

   protected abstract void onEvtTimer(int var1, Object var2);

   protected void clientActionFailed() {
      if (this._actor.isPlayer()) {
         this._actor.sendActionFailed();
      }
   }

   protected void moveToPawn(GameObject pawn, int offset) {
      if (!this._actor.isMovementDisabled()) {
         if (offset < 10) {
            offset = 10;
         }

         if (this._clientMoving && this._target == pawn) {
            if (this._clientMovingToPawnOffset == offset) {
               if (GameTimeController.getInstance().getGameTicks() < this._moveToPawnTimeout) {
                  return;
               }
            } else if (this._actor.isOnGeodataPath() && GameTimeController.getInstance().getGameTicks() < this._moveToPawnTimeout + 10) {
               return;
            }
         }

         this._clientMoving = true;
         this._clientMovingToPawnOffset = offset;
         this._target = pawn;
         this._moveToPawnTimeout = GameTimeController.getInstance().getGameTicks();
         this._moveToPawnTimeout += 10;
         if (pawn == null) {
            return;
         }

         this._actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
         if (!this._actor.isMoving()) {
            this.clientActionFailed();
            return;
         }

         if (pawn.isCreature()) {
            if (this._actor.isOnGeodataPath()) {
               this._actor.broadcastPacket(new MoveToLocation(this._actor));
               this._clientMovingToPawnOffset = 0;
            } else {
               this._actor.broadcastPacket(new MoveToPawn(this._actor, (Creature)pawn, offset));
            }
         } else {
            this._actor.broadcastPacket(new MoveToLocation(this._actor));
         }
      } else {
         this.clientActionFailed();
      }
   }

   protected void moveTo(Location loc) {
      this.moveTo(loc, 0);
   }

   protected void moveTo(int x, int y, int z, int offset) {
      this.moveTo(new Location(x, y, z), 0);
   }

   protected void moveTo(Location loc, int offset) {
      if (!this._actor.isMovementDisabled()) {
         this._clientMoving = true;
         this._clientMovingToPawnOffset = 0;
         this._actor.moveToLocation(loc.getX(), loc.getY(), loc.getZ(), offset);
         if (!this._actor.isMoving()) {
            this.clientActionFailed();
            return;
         }

         this._actor.broadcastPacket(new MoveToLocation(this._actor));
         if (this._actor.isNpc()) {
            this._actor.broadcastPacket(new ValidateLocation(this._actor));
         }
      } else {
         this.clientActionFailed();
      }
   }

   public void clientStopMoving(Location loc) {
      if (this._actor.isMoving()) {
         this._actor.stopMove(loc);
      }

      this._clientMovingToPawnOffset = 0;
      if (this._clientMoving || loc != null) {
         this._clientMoving = false;
         this._actor.broadcastPacket(new StopMove(this._actor));
         if (loc != null) {
            this._actor.broadcastPacket(new FinishRotatings(this._actor.getObjectId(), loc.getHeading(), 0));
         }
      }
   }

   protected void clientStoppedMoving() {
      if (this._clientMovingToPawnOffset > 0) {
         this._clientMovingToPawnOffset = 0;
         this._actor.broadcastPacket(new StopMove(this._actor));
      }

      this._clientMoving = false;
   }

   public boolean isAutoAttacking() {
      return this._clientAutoAttacking;
   }

   public void setAutoAttacking(boolean isAutoAttacking) {
      if (this._actor.isSummon()) {
         Summon summon = (Summon)this._actor;
         if (summon.getOwner() != null) {
            summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
         }
      } else {
         this._clientAutoAttacking = isAutoAttacking;
      }
   }

   public void clientStartAutoAttack() {
      if (this._actor.isSummon()) {
         Summon summon = (Summon)this._actor;
         if (summon.getOwner() != null) {
            summon.getOwner().getAI().clientStartAutoAttack();
         }
      } else {
         if (!this.isAutoAttacking()) {
            if (this._actor.isPlayer() && this._actor.hasSummon()) {
               this._actor.getSummon().broadcastPacket(new AutoAttackStart(this._actor.getSummon().getObjectId()));
            }

            this._actor.broadcastPacket(new AutoAttackStart(this._actor.getObjectId()));
            this.setAutoAttacking(true);
         }

         AttackStanceTaskManager.getInstance().addAttackStanceTask(this._actor);
      }
   }

   public void clientStopAutoAttack() {
      if (this._actor.isSummon()) {
         Summon summon = (Summon)this._actor;
         if (summon.getOwner() != null) {
            summon.getOwner().getAI().clientStopAutoAttack();
         }
      } else {
         if (this._actor.isPlayer()) {
            if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor) && this.isAutoAttacking()) {
               AttackStanceTaskManager.getInstance().addAttackStanceTask(this._actor);
            }
         } else if (this.isAutoAttacking()) {
            this.stopAutoAttack();
         }
      }
   }

   protected void clientNotifyDead() {
      Die msg = new Die(this._actor);
      this._actor.broadcastPacket(msg);
      this._intention = CtrlIntention.IDLE;
      this._target = null;
      this._castTarget = null;
      this._attackTarget = null;
      this.stopFollow();
   }

   public void describeStateToPlayer(Player player) {
      if (this.getActor().isVisibleFor(player) && this._clientMoving) {
         if (this._clientMovingToPawnOffset != 0 && this._followTarget != null) {
            player.sendPacket(new MoveToPawn(this._actor, this._followTarget, this._clientMovingToPawnOffset));
         } else {
            player.sendPacket(new MoveToLocation(this._actor));
            player.sendPacket(new ValidateLocation(this._actor));
         }
      }
   }

   public void stopAutoAttack() {
      this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
      this.setAutoAttacking(false);
   }

   public synchronized void startFollow(Creature target) {
      if (this._followTask != null) {
         this._followTask.cancel(false);
         this._followTask = null;
      }

      this._followTarget = target;
      this._followTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AbstractAI.FollowTask(), 5L, 1000L);
   }

   public synchronized void startFollow(Creature target, int range) {
      if (this._followTask != null) {
         this._followTask.cancel(false);
         this._followTask = null;
      }

      this._followTarget = target;
      this._followTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AbstractAI.FollowTask(range), 5L, 500L);
   }

   public synchronized void stopFollow() {
      if (this._followTask != null) {
         this._followTask.cancel(false);
         this._followTask = null;
      }

      this._followTarget = null;
   }

   protected Creature getFollowTarget() {
      return this._followTarget;
   }

   protected GameObject getTarget() {
      return this._target;
   }

   protected void setTarget(GameObject target) {
      this._target = target;
   }

   public void stopAITask() {
      this.stopFollow();
   }

   public void startAITask() {
   }

   public void enableAI() {
   }

   public void addToTargetList(Player player) {
      if (this._targetList != null && !this._targetList.contains(player)) {
         this._targetList.add(player);
      }
   }

   public List<Player> getTargetList() {
      return this._targetList;
   }

   @Override
   public String toString() {
      return this._actor == null ? "Actor: null" : "Actor: " + this._actor;
   }

   private class FollowTask implements Runnable {
      protected int _range = 70;

      public FollowTask() {
      }

      public FollowTask(int range) {
         this._range = range;
      }

      @Override
      public void run() {
         try {
            if (AbstractAI.this._followTask == null) {
               return;
            }

            Creature followTarget = AbstractAI.this._followTarget;
            if (followTarget == null) {
               if (AbstractAI.this._actor.isSummon()) {
                  ((Summon)AbstractAI.this._actor).setFollowStatus(false);
               }

               AbstractAI.this.setIntention(CtrlIntention.IDLE);
               return;
            }

            if (!AbstractAI.this._actor.isInsideRadius(followTarget, this._range, true, false)) {
               if (!AbstractAI.this._actor.isInsideRadius(followTarget, 3000, true, false)) {
                  if (AbstractAI.this._actor.isSummon()) {
                     ((Summon)AbstractAI.this._actor).setFollowStatus(false);
                  }

                  AbstractAI.this.setIntention(CtrlIntention.IDLE);
                  return;
               }

               if (!AbstractAI.this._actor.isOnGeodataPath()) {
                  AbstractAI.this.moveTo(followTarget.getLocation(), this._range);
               }
            }
         } catch (Exception var2) {
            AbstractAI.this._log.warning(this.getClass().getSimpleName() + ": Error: " + var2.getMessage());
         }
      }
   }
}
