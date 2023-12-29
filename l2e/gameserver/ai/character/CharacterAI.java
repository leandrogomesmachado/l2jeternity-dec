package l2e.gameserver.ai.character;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.AbstractAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AutoAttackStop;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public class CharacterAI extends AbstractAI {
   private final List<ScheduledFuture<?>> _timers = new ArrayList<>();

   public CharacterAI(Creature character) {
      super(character);
   }

   public CharacterAI.IntentionCommand getNextIntention() {
      return null;
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker instanceof Attackable && !((Attackable)attacker).isCoreAIDisabled()) {
         if (this._actor.isPlayer() && this._actor.isInvul()) {
            return;
         }

         this.clientStartAutoAttack();
      }
   }

   @Override
   protected void onIntentionIdle() {
      this.changeIntention(CtrlIntention.IDLE, null, null);
      this.setCastTarget(null);
      this.setAttackTarget(null);
      this.clientStopMoving(null);
      this.clientStopAutoAttack();
   }

   @Override
   protected void onIntentionActive() {
      if (this.getIntention() != CtrlIntention.ACTIVE) {
         this.changeIntention(CtrlIntention.ACTIVE, null, null);
         this.setCastTarget(null);
         this.setAttackTarget(null);
         this.clientStopMoving(null);
         this.clientStopAutoAttack();
         if (this._actor instanceof Attackable) {
            ((Npc)this._actor).startRandomAnimationTimer();
         }

         this.onEvtThink();
      }
   }

   @Override
   protected void onIntentionRest() {
      this.setIntention(CtrlIntention.IDLE);
   }

   @Override
   protected void onIntentionAttack(Creature target) {
      if (target != null && target.isTargetable()) {
         if (this.getIntention() == CtrlIntention.REST) {
            this.clientActionFailed();
         } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAfraid()) {
            if (this.getIntention() == CtrlIntention.ATTACK) {
               if (this.getAttackTarget() != target) {
                  this.setAttackTarget(target);
                  this.stopFollow();
                  this.notifyEvent(CtrlEvent.EVT_THINK);
               } else {
                  this.clientActionFailed();
               }
            } else {
               this.changeIntention(CtrlIntention.ATTACK, target, null);
               this.setAttackTarget(target);
               this.stopFollow();
               this.notifyEvent(CtrlEvent.EVT_THINK);
            }
         } else {
            this.clientActionFailed();
         }
      } else {
         this.clientActionFailed();
      }
   }

   @Override
   protected void onIntentionCast(Skill skill, GameObject target) {
      if ((this.getIntention() != CtrlIntention.REST || !skill.isMagic()) && !this._actor.isAfraid()) {
         long bowAttackDelay = this._actor.getBowAttackEndTime() - System.currentTimeMillis();
         long normalAttackDelay = TimeUnit.MILLISECONDS.convert(this._actor.getAttackEndTime() - System.nanoTime(), TimeUnit.NANOSECONDS);
         if (bowAttackDelay <= 0L && normalAttackDelay <= 0L) {
            this.changeIntentionToCast(skill, target);
         } else {
            ThreadPoolManager.getInstance()
               .schedule(
                  new CharacterAI.CastTask(this._actor, skill, target), bowAttackDelay > 0L ? (long)((int)((double)bowAttackDelay * 0.5)) : normalAttackDelay
               );
         }
      } else {
         this.clientActionFailed();
         this._actor.setIsCastingNow(false);
      }
   }

   protected void changeIntentionToCast(Skill skill, GameObject target) {
      this.setCastTarget((Creature)target);
      if (skill.getHitTime() > 50) {
         this._actor.abortAttack();
      }

      this._skill = skill;
      this.changeIntention(CtrlIntention.CAST, skill, target);
      this.notifyEvent(CtrlEvent.EVT_THINK);
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
      }
   }

   @Override
   protected void onIntentionFollow(Creature target) {
      if (this.getIntention() == CtrlIntention.REST) {
         this.clientActionFailed();
      } else if (this._actor.isAllSkillsDisabled() || this._actor.isActionsDisabled()) {
         this.clientActionFailed();
      } else if (this._actor.isMovementDisabled()) {
         this.clientActionFailed();
      } else if (this._actor.isDead()) {
         this.clientActionFailed();
      } else if (this._actor == target) {
         this.clientActionFailed();
      } else {
         this.clientStopAutoAttack();
         this.changeIntention(CtrlIntention.FOLLOW, target, null);
         this.startFollow(target);
      }
   }

   @Override
   protected void onIntentionPickUp(GameObject object) {
      if (this.getIntention() == CtrlIntention.REST) {
         this.clientActionFailed();
      } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         this.clientStopAutoAttack();
         if (!(object instanceof ItemInstance) || ((ItemInstance)object).getItemLocation() == ItemInstance.ItemLocation.VOID) {
            this.changeIntention(CtrlIntention.PICK_UP, object, null);
            this.setTarget(object);
            if (object.getX() == 0 && object.getY() == 0) {
               this._log.warning("Object in coords 0,0 - using a temporary fix");
               object.setXYZ(this.getActor().getX(), this.getActor().getY(), this.getActor().getZ() + 5);
            }

            this.moveToPawn(object, 20);
         }
      } else {
         this.clientActionFailed();
      }
   }

   @Override
   protected void onIntentionInteract(GameObject object) {
      if (this.getIntention() == CtrlIntention.REST) {
         this.clientActionFailed();
      } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         this.clientStopAutoAttack();
         if (this.getIntention() != CtrlIntention.INTERACT) {
            this.changeIntention(CtrlIntention.INTERACT, object, null);
            this.setTarget(object);
            this.moveTo(object.getLocation(), 40);
         }
      } else {
         this.clientActionFailed();
      }
   }

   @Override
   protected void onEvtThink() {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }

   @Override
   protected void onEvtStunned(Creature attacker) {
      this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
      if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor)) {
         AttackStanceTaskManager.getInstance().removeAttackStanceTask(this._actor);
      }

      this.setAutoAttacking(false);
      this.clientStopMoving(null);
      this.onEvtAttacked(attacker, 0);
   }

   @Override
   protected void onEvtParalyzed(Creature attacker) {
      this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
      if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor)) {
         AttackStanceTaskManager.getInstance().removeAttackStanceTask(this._actor);
      }

      this.setAutoAttacking(false);
      this.clientStopMoving(null);
      this.onEvtAttacked(attacker, 0);
   }

   @Override
   protected void onEvtSleeping(Creature attacker) {
      this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
      if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor)) {
         AttackStanceTaskManager.getInstance().removeAttackStanceTask(this._actor);
      }

      this.setAutoAttacking(false);
      this.clientStopMoving(null);
   }

   @Override
   protected void onEvtRooted(Creature attacker) {
      this.clientStopMoving(null);
      this.onEvtAttacked(attacker, 0);
   }

   @Override
   protected void onEvtConfused(Creature attacker) {
      this.clientStopMoving(null);
      this.onEvtAttacked(attacker, 0);
   }

   @Override
   protected void onEvtMuted(Creature attacker) {
      this.onEvtAttacked(attacker, 0);
   }

   @Override
   protected void onEvtEvaded(Creature attacker) {
   }

   @Override
   protected void onEvtReadyToAct() {
      this.onEvtThink();
   }

   @Override
   protected void onEvtUserCmd(Object arg0, Object arg1) {
   }

   @Override
   protected void onEvtArrived() {
      this._actor.revalidateZone(true);
      if (this._actor.moveToNextRoutePoint()) {
         if (this._actor.isSummon() && this._actor.getAI().getIntention() == CtrlIntention.IDLE) {
            ((Summon)this._actor).setFollowStatus(true);
         }
      } else {
         if (this._actor.isPlayer()
            && this._actor.getAI().getIntention() == CtrlIntention.CAST
            && this.getCastTarget() != null
            && this.getCastTarget().isDoor()) {
            int x = 0;
            int y = 0;
            int z = 0;
            if (this.getCastTarget().isDoor()) {
               DoorInstance dor = (DoorInstance)this.getCastTarget();
               x = dor.getTemplate().posX;
               y = dor.getTemplate().posY;
               z = dor.getTemplate().posZ + 32;
            }

            if (!GeoEngine.canMoveToCoord(this._actor.getX(), this._actor.getY(), this._actor.getZ(), x, y, z, this._actor.getGeoIndex())) {
               this.setIntention(CtrlIntention.IDLE);
            }
         }

         if (this._actor instanceof Attackable) {
            ((Attackable)this._actor).setisReturningToSpawnPoint(false);
         }

         this.clientStoppedMoving();
         if (this._actor instanceof Npc) {
            Npc npc = (Npc)this._actor;
            WalkingManager.getInstance().onArrived(npc);
            if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_MOVE_FINISHED) != null) {
               for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_MOVE_FINISHED)) {
                  quest.notifyMoveFinished(npc);
               }
            }
         }

         if (this.getIntention() == CtrlIntention.MOVING) {
            this.setIntention(CtrlIntention.ACTIVE);
         }

         this.onEvtThink();
      }
   }

   @Override
   protected void onEvtArrivedRevalidate() {
      this.onEvtThink();
   }

   @Override
   protected void onEvtArrivedBlocked(Location blocked_at_loc) {
      if (this.getIntention() == CtrlIntention.MOVING || this.getIntention() == CtrlIntention.CAST) {
         this.setIntention(CtrlIntention.ACTIVE);
      }

      this.clientStopMoving(blocked_at_loc);
      this.onEvtThink();
   }

   @Override
   protected void onEvtForgetObject(GameObject object) {
      if (this.getTarget() == object) {
         this.setTarget(null);
         if (this.getIntention() == CtrlIntention.INTERACT || this.getIntention() == CtrlIntention.PICK_UP) {
            this.setIntention(CtrlIntention.ACTIVE);
         }
      }

      if (this.getAttackTarget() == object) {
         this.setAttackTarget(null);
         this.setIntention(CtrlIntention.ACTIVE);
      }

      if (this.getCastTarget() == object) {
         this.setCastTarget(null);
         this.setIntention(CtrlIntention.ACTIVE);
      }

      if (this.getFollowTarget() == object) {
         this.clientStopMoving(null);
         this.stopFollow();
         this.setIntention(CtrlIntention.ACTIVE);
      }

      if (this._actor == object) {
         this.setTarget(null);
         this.setAttackTarget(null);
         this.setCastTarget(null);
         this.stopFollow();
         this.clientStopMoving(null);
         this.changeIntention(CtrlIntention.IDLE, null, null);
      }
   }

   @Override
   protected void onEvtCancel() {
      this._actor.abortCast();
      this.stopFollow();
      if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(this._actor)) {
         this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
      }

      this.onEvtThink();
   }

   @Override
   protected void onEvtDead(Creature killer) {
      this.stopAITask();
      this.clientNotifyDead();
      if (!(this._actor instanceof Playable)) {
         this._actor.setWalking();
      }
   }

   @Override
   protected void onEvtFakeDeath() {
      this.stopFollow();
      this.clientStopMoving(null);
      this._intention = CtrlIntention.IDLE;
      this.setTarget(null);
      this.setCastTarget(null);
      this.setAttackTarget(null);
   }

   @Override
   protected void onEvtTimer(int timerId, Object arg1) {
      Creature actor = this.getActor();
      if (actor != null) {
         actor.onEvtTimer(timerId, arg1);
      }
   }

   @Override
   protected void onEvtFinishCasting() {
   }

   @Override
   protected void onEvtSeeSpell(Skill skill, Creature caster) {
   }

   @Override
   protected void onEvtSpawn() {
   }

   protected boolean maybeMoveToPosition(Location worldPosition, int offset) {
      if (worldPosition == null) {
         return false;
      } else if (offset < 0) {
         return false;
      } else if (!this._actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), (int)((double)offset + this._actor.getColRadius()), false)) {
         if (this._actor.isMovementDisabled()) {
            return true;
         } else {
            if (!this._actor.isRunning() && !(this instanceof PlayableAI) && !(this instanceof SummonAI)) {
               this._actor.setRunning();
            }

            this.stopFollow();
            int x = this._actor.getX();
            int y = this._actor.getY();
            double dx = (double)(worldPosition.getX() - x);
            double dy = (double)(worldPosition.getY() - y);
            double dist = Math.sqrt(dx * dx + dy * dy);
            double sin = dy / dist;
            double cos = dx / dist;
            dist -= (double)(offset - 5);
            x += (int)(dist * cos);
            y += (int)(dist * sin);
            this.moveTo(x, y, worldPosition.getZ(), 0);
            return true;
         }
      } else {
         if (this.getFollowTarget() != null) {
            this.stopFollow();
         }

         return false;
      }
   }

   protected boolean maybeMoveToPawn(GameObject target, int offset) {
      if (target == null) {
         this._log.warning("maybeMoveToPawn: target == NULL!");
         return false;
      } else if (offset < 0) {
         return false;
      } else {
         offset = (int)((double)offset + this._actor.getColRadius() / 2.0);
         if (target instanceof Creature) {
            offset = (int)((double)offset + (target.isDoor() ? 60.0 : target.getColRadius() / 2.0));
         }

         int xPoint = 0;
         int yPoint = 0;
         int zPoint = 0;
         boolean needToMove;
         if (target.isDoor()) {
            DoorInstance dor = (DoorInstance)target;
            xPoint = dor.getTemplate().posX;
            yPoint = dor.getTemplate().posY;
            zPoint = dor.getTemplate().posZ + 32;
            needToMove = !this._actor.isInsideRadius(xPoint, yPoint, zPoint, offset, false, false);
         } else if (target instanceof ItemInstance) {
            needToMove = !this._actor.isInRange(target, (long)offset);
         } else {
            needToMove = !this._actor.isInsideRadius(target, offset, false, false);
         }

         if (needToMove) {
            if (this.getFollowTarget() != null) {
               if (!this._actor.isInsideRadius(target, offset + 50, false, false)) {
                  if (target instanceof Creature && !(target instanceof DoorInstance)) {
                     if (((Creature)target).isMoving()) {
                        offset -= 50;
                     }

                     this.startFollow((Creature)target, Math.max(offset, 5));
                  }

                  return true;
               } else {
                  this.stopFollow();
                  return false;
               }
            } else if (!this._actor.isMovementDisabled() && !(this._actor.getMoveSpeed() <= 0.0)) {
               if (this._actor.isFlying()
                  && this._actor.getAI().getIntention() == CtrlIntention.CAST
                  && this._actor.isPlayer()
                  && this._actor.getActingPlayer().isTransformed()
                  && !this._actor.getActingPlayer().getTransformation().isCombat()) {
                  this._actor.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
                  this._actor.sendActionFailed();
                  return true;
               } else {
                  if (!this._actor.isRunning() && !(this instanceof PlayerAI) && !(this instanceof SummonAI)) {
                     this._actor.setRunning();
                  }

                  this.stopFollow();
                  if (target instanceof Creature && !(target instanceof DoorInstance)) {
                     if (((Creature)target).isMoving()) {
                        offset -= 50;
                     }

                     this.startFollow((Creature)target, Math.max(offset, 5));
                  } else if (target instanceof DoorInstance) {
                     this.moveTo(xPoint, yPoint, zPoint, Math.max(offset, 5));
                  } else {
                     this.moveTo(target.getLocation(), Math.max(offset, 5));
                  }

                  return true;
               }
            } else {
               if (this._actor.getAI().getIntention() == CtrlIntention.ATTACK) {
                  this._actor.getAI().setIntention(CtrlIntention.IDLE);
               }

               return true;
            }
         } else {
            if (this.getFollowTarget() != null) {
               this.stopFollow();
            }

            return false;
         }
      }
   }

   protected boolean checkTargetLostOrDead(Creature target) {
      if (target != null && !target.isAlikeDead()) {
         return false;
      } else if (target instanceof Player && ((Player)target).isFakeDeathNow()) {
         target.stopFakeDeath(true);
         return false;
      } else {
         this.setIntention(CtrlIntention.ACTIVE);
         return true;
      }
   }

   protected boolean checkTargetLost(GameObject target) {
      if (target == null) {
         this.setIntention(CtrlIntention.ACTIVE);
         return true;
      } else if (this._actor != null
         && this._skill != null
         && this._skill.isOffensive()
         && this._skill.getAffectRange() > 0
         && Config.GEODATA
         && !GeoEngine.canSeeTarget(this._actor, target, false)) {
         this.setIntention(CtrlIntention.ACTIVE);
         return true;
      } else {
         return false;
      }
   }

   public boolean canAura(Skill sk) {
      if (sk.getTargetType() == TargetType.AURA
         || sk.getTargetType() == TargetType.BEHIND_AURA
         || sk.getTargetType() == TargetType.FRONT_AURA
         || sk.getTargetType() == TargetType.AURA_CORPSE_MOB) {
         for(Creature target : World.getInstance().getAroundCharacters(this._actor, sk.getAffectRange(), 200)) {
            if (target == this.getAttackTarget()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean canAOE(Skill sk) {
      if (sk.hasEffectType(EffectType.CANCEL, EffectType.CANCEL_ALL, EffectType.CANCEL_BY_SLOT, EffectType.NEGATE)) {
         if (sk.getTargetType() == TargetType.AURA
            || sk.getTargetType() == TargetType.BEHIND_AURA
            || sk.getTargetType() == TargetType.FRONT_AURA
            || sk.getTargetType() == TargetType.AURA_CORPSE_MOB) {
            boolean cancast = true;
            Iterator var11 = World.getInstance().getAroundCharacters(this._actor, sk.getAffectRange(), 200).iterator();

            while(true) {
               Creature target;
               while(true) {
                  if (!var11.hasNext()) {
                     if (cancast) {
                        return true;
                     }

                     return false;
                  }

                  target = (Creature)var11.next();
                  if (target != null && GeoEngine.canSeeTarget(this._actor, target, false)) {
                     if (!(target instanceof Attackable)) {
                        break;
                     }

                     Npc actors = (Npc)this._actor;
                     if (!actors.getFaction().isNone()) {
                        break;
                     }
                  }
               }

               Effect[] effects = target.getAllEffects();

               for(int i = 0; effects != null && i < effects.length; ++i) {
                  Effect effect = effects[i];
                  if (effect.getSkill() == sk) {
                     cancast = false;
                     break;
                  }
               }
            }
         } else if (sk.getTargetType() == TargetType.AREA || sk.getTargetType() == TargetType.BEHIND_AREA || sk.getTargetType() == TargetType.FRONT_AREA) {
            boolean cancast = true;

            for(Creature target : World.getInstance().getAroundCharacters(this._actor, sk.getAffectRange(), 200)) {
               if (target != null && GeoEngine.canSeeTarget(this._actor, target, false) && target != null) {
                  if (target instanceof Attackable) {
                     Npc actors = (Npc)this._actor;
                     if (actors.getFaction().isNone()) {
                        continue;
                     }
                  }

                  Effect[] effects = target.getAllEffects();
                  if (effects.length > 0) {
                     cancast = true;
                  }
               }
            }

            if (cancast) {
               return true;
            }
         }
      } else if (sk.getTargetType() == TargetType.AURA
         || sk.getTargetType() == TargetType.BEHIND_AURA
         || sk.getTargetType() == TargetType.FRONT_AURA
         || sk.getTargetType() == TargetType.AURA_CORPSE_MOB) {
         boolean cancast = false;

         for(Creature target : World.getInstance().getAroundCharacters(this._actor, sk.getAffectRange(), 200)) {
            if (target != null && GeoEngine.canSeeTarget(this._actor, target, false)) {
               if (target instanceof Attackable) {
                  Npc actors = (Npc)this._actor;
                  if (actors.getFaction().isNone()) {
                     continue;
                  }
               }

               Effect[] effects = target.getAllEffects();
               if (effects.length > 0) {
                  cancast = true;
               }
            }
         }

         if (cancast) {
            return true;
         }
      } else if (sk.getTargetType() == TargetType.AREA || sk.getTargetType() == TargetType.BEHIND_AREA || sk.getTargetType() == TargetType.FRONT_AREA) {
         boolean cancast = true;
         Iterator var12 = World.getInstance().getAroundCharacters(this._actor, sk.getAffectRange(), 200).iterator();

         while(true) {
            Creature target;
            while(true) {
               if (!var12.hasNext()) {
                  if (cancast) {
                     return true;
                  }

                  return false;
               }

               target = (Creature)var12.next();
               if (target != null && GeoEngine.canSeeTarget(this._actor, target, false)) {
                  if (!(target instanceof Attackable)) {
                     break;
                  }

                  Npc actors = (Npc)this._actor;
                  if (!actors.getFaction().isNone()) {
                     break;
                  }
               }
            }

            Effect[] effects = target.getAllEffects();

            for(int i = 0; effects != null && i < effects.length; ++i) {
               Effect effect = effects[i];
               if (effect.getSkill() == sk) {
                  cancast = false;
                  break;
               }
            }
         }
      }

      return false;
   }

   public boolean canParty(Skill sk) {
      if (sk.getTargetType() == TargetType.PARTY) {
         int count = 0;
         int ccount = 0;

         for(Creature target : World.getInstance().getAroundCharacters(this._actor, sk.getAffectRange(), 200)) {
            if (target instanceof Attackable && GeoEngine.canSeeTarget(this._actor, target, false)) {
               Npc targets = (Npc)target;
               Npc actors = (Npc)this._actor;
               if (!actors.getFaction().isNone() && targets.isInFaction((Attackable)actors)) {
                  ++count;
                  Effect[] effects = target.getAllEffects();

                  for(int i = 0; effects != null && i < effects.length; ++i) {
                     Effect effect = effects[i];
                     if (effect.getSkill() == sk) {
                        ++ccount;
                        break;
                     }
                  }
               }
            }
         }

         if (ccount < count) {
            return true;
         }
      }

      return false;
   }

   public boolean isParty(Skill sk) {
      return sk.getTargetType() == TargetType.PARTY;
   }

   protected boolean checkDistanceAndMove(GameObject target) {
      if ((int)this._actor.calculateDistance(target, false, false) > 150) {
         Location destination = GeoEngine.moveCheck(
            this._actor.getX(), this._actor.getY(), this._actor.getZ(), target.getX(), target.getY(), this._actor.getGeoIndex()
         );
         this.changeIntention(CtrlIntention.MOVE_AND_INTERACT, target, destination);
         return true;
      } else {
         return false;
      }
   }

   @Override
   protected void onIntentionMoveAndInteract(GameObject object, Location loc) {
      if (this.getIntention() == CtrlIntention.REST) {
         this.clientActionFailed();
      } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isActionsDisabled()) {
         this.changeIntention(CtrlIntention.MOVE_AND_INTERACT, object, loc);
         this.clientStopAutoAttack();
         this._actor.abortAttack();
         this.setTarget(object);
         this.moveTo(loc, 40);
      } else {
         this.clientActionFailed();
      }
   }

   public void stopAllTaskAndTimers() {
      for(ScheduledFuture<?> timer : this._timers) {
         timer.cancel(false);
      }

      this._timers.clear();
   }

   public void addTimer(int timerId, long delay) {
      this.addTimer(timerId, null, delay);
   }

   public void addTimer(int timerId, Object arg1, long delay) {
      ScheduledFuture<?> timer = ThreadPoolManager.getInstance().schedule(new CharacterAI.Timer(timerId, arg1), delay);
      if (timer != null) {
         this._timers.add(timer);
      }
   }

   public static class CastTask implements Runnable {
      private final Creature _activeChar;
      private final GameObject _target;
      private final Skill _skill;

      public CastTask(Creature actor, Skill skill, GameObject target) {
         this._activeChar = actor;
         this._target = target;
         this._skill = skill;
      }

      @Override
      public void run() {
         if (this._activeChar.isAttackingNow()) {
            this._activeChar.abortAttack();
         }

         this._activeChar.getAI().changeIntentionToCast(this._skill, this._target);
      }
   }

   public static class IntentionCommand {
      protected final CtrlIntention _crtlIntention;
      protected final Object _arg0;
      protected final Object _arg1;

      protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1) {
         this._crtlIntention = pIntention;
         this._arg0 = pArg0;
         this._arg1 = pArg1;
      }

      public CtrlIntention getCtrlIntention() {
         return this._crtlIntention;
      }
   }

   protected class Timer extends RunnableImpl {
      private final int _timerId;
      private final Object _arg1;

      public Timer(int timerId, Object arg1) {
         this._timerId = timerId;
         this._arg1 = arg1;
      }

      @Override
      public void runImpl() {
         CharacterAI.this.notifyEvent(CtrlEvent.EVT_TIMER, Integer.valueOf(this._timerId), this._arg1);
      }
   }
}
