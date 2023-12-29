package l2e.gameserver.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.math.random.RndSelector;
import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.ai.npc.Priest;
import l2e.gameserver.ai.npc.Ranger;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.FestivalMonsterInstance;
import l2e.gameserver.model.actor.instance.FriendlyMobInstance;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.GuardInstance;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.QuestGuardInstance;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.instance.RiftInvaderInstance;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class DefaultAI extends CharacterAI implements Runnable {
   protected Future<?> _aiTask;
   protected int _attackTimeout;
   protected static final int _attackTimeoutLimit = 1200;
   protected int _globalAggro;
   protected boolean _thinking;
   protected boolean _canNotifyFriend;
   protected int _waitTimeOut;
   protected long _waitTimeOutTime = 0L;
   protected long _waitTimeOutLimit = 120000L;
   protected long _lastFactionNotifyTime = 0L;
   protected long _minFactionNotifyInterval = 5000L;
   private final Skill _defSkill = SkillsParser.getInstance().getInfo(5044, 3);
   private volatile int _defFlag = 0;
   private final int _defRate;
   protected ScheduledFuture<?> _madnessTask;
   private int _lastBuffTick;
   protected int MAX_PURSUE_RANGE;
   protected final Skill[] _damSkills;
   protected final Skill[] _dotSkills;
   protected final Skill[] _debuffSkills;
   protected final Skill[] _healSkills;
   protected final Skill[] _buffSkills;
   protected final Skill[] _stunSkills;
   protected final Skill[] _suicideSkills;
   protected final Skill[] _resSkills;

   public DefaultAI(Attackable actor) {
      super(actor);
      this._attackTimeout = Integer.MAX_VALUE;
      this._waitTimeOut = -1;
      this._globalAggro = -10;
      this._canNotifyFriend = true;
      this.MAX_PURSUE_RANGE = actor.getTemplate().getParameter("maxPursueRange", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : Config.MAX_PURSUE_RANGE);
      this._defRate = actor.getTemplate().getParameter("defenceChance", 0);
      Attackable npc = this.getActiveChar();
      this._damSkills = npc.getTemplate().getDamageSkills();
      this._dotSkills = npc.getTemplate().getDotSkills();
      this._debuffSkills = npc.getTemplate().getDebuffSkills();
      this._buffSkills = npc.getTemplate().getBuffSkills();
      this._stunSkills = npc.getTemplate().getStunSkills();
      this._healSkills = npc.getTemplate().getHealSkills();
      this._suicideSkills = npc.getTemplate().getSuicideSkills();
      this._resSkills = npc.getTemplate().getResSkills();
   }

   @Override
   public void run() {
      this.onEvtThink();
   }

   protected boolean checkAggression(Creature target) {
      if (target != null && this.getActiveChar() != null) {
         Attackable me = this.getActiveChar();
         if (!target.isDoor() && !target.isAlikeDead()) {
            boolean haveHideAggro = me.getHideAggroRange() > 0;
            if (haveHideAggro) {
               if (target.isPlayable()) {
                  if (!target.isInvisible() && !((Playable)target).isSilentMoving()) {
                     if (!me.isInsideRadius(target, me.getAggroRange(), true, false)) {
                        return false;
                     }
                  } else if (!me.isInsideRadius(target, me.getHideAggroRange(), true, false)) {
                     return false;
                  }
               }
            } else if (target.isInvisible() || target.isPlayable() && !me.isInsideRadius(target, me.getAggroRange(), true, false)) {
               return false;
            }

            if (target.isPlayable()
               && !haveHideAggro
               && (!(me instanceof GrandBossInstance) || !me.isRaid())
               && !me.canSeeThroughSilentMove()
               && ((Playable)target).isSilentMoving()) {
               return false;
            } else {
               Player player = target.getActingPlayer();
               if (player != null) {
                  if (player.isGM() && !player.getAccessLevel().canTakeAggro()) {
                     return false;
                  }

                  if (player.isFakeDeath()) {
                     return false;
                  }

                  if (player.isInParty() && player.getParty().isInDimensionalRift()) {
                     byte riftType = player.getParty().getDimensionalRift().getType();
                     byte riftRoom = player.getParty().getDimensionalRift().getCurrentRoom();
                     if (me instanceof RiftInvaderInstance
                        && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ())) {
                        return false;
                     }
                  }
               }

               if (me instanceof GuardInstance) {
                  if (player != null && player.getKarma() > 0 && me.getId() != 4328) {
                     return GeoEngine.canSeeTarget(me, player, false);
                  } else if (target.isMonster() && Config.GUARD_ATTACK_AGGRO_MOB) {
                     return ((MonsterInstance)target).isAggressive() && GeoEngine.canSeeTarget(me, target, false);
                  } else {
                     return false;
                  }
               } else if (me instanceof FriendlyMobInstance) {
                  if (target instanceof Npc) {
                     return false;
                  } else {
                     return target instanceof Player && ((Player)target).getKarma() > 0 ? GeoEngine.canSeeTarget(me, target, false) : false;
                  }
               } else if (target instanceof Npc) {
                  return false;
               } else if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE)) {
                  return false;
               } else if (me.getChampionTemplate() != null && me.getChampionTemplate().isPassive) {
                  return false;
               } else {
                  if (me.getSpawn() != null) {
                     Attackable.AggroInfo ai = me.getAggroList().get(target);
                     if (ai != null && ai.getHate() > 0 && !target.isInRangeZ(me.getSpawnedLoc(), (long)this.MAX_PURSUE_RANGE)) {
                        return false;
                     }
                  }

                  return me.isAggressive() && GeoEngine.canSeeTarget(me, target, false);
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void startAITask() {
      if (this._aiTask == null) {
         this._aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
      }
   }

   @Override
   public void stopAITask() {
      if (this._aiTask != null) {
         this._aiTask.cancel(false);
         this._aiTask = null;
      }

      super.stopAITask();
   }

   @Override
   protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
      if (intention == CtrlIntention.IDLE || intention == CtrlIntention.ACTIVE) {
         Attackable npc = this.getActiveChar();
         if (!npc.isAlikeDead()) {
            if (!World.getInstance().getAroundPlayers(npc).isEmpty() || this.isGlobalAI()) {
               intention = CtrlIntention.ACTIVE;
            } else if (npc.getSpawn() != null) {
               int range = Config.MAX_DRIFT_RANGE;
               if (!npc.isInsideRadius(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), range + range, true, false)) {
                  intention = CtrlIntention.ACTIVE;
               }
            }
         }

         if (intention == CtrlIntention.IDLE && !this.isGlobalAI()) {
            super.changeIntention(CtrlIntention.IDLE, null, null);
            this.stopAITask();
            this._actor.detachAI();
            return;
         }
      }

      super.changeIntention(intention, arg0, arg1);
      this.startAITask();
   }

   @Override
   protected void changeIntentionToCast(Skill skill, GameObject target) {
      this.setTarget(target);
      super.changeIntentionToCast(skill, target);
   }

   @Override
   protected void onIntentionAttack(Creature target) {
      this._attackTimeout = 1200 + GameTimeController.getInstance().getGameTicks();
      super.onIntentionAttack(target);
   }

   protected void thinkCast() {
      if (this.checkTargetLost(this.getCastTarget())) {
         this.setCastTarget(null);
      } else if (!this.maybeMoveToPawn(this.getCastTarget(), this._actor.getMagicalAttackRange(this._skill))) {
         this.clientStopMoving(null);
         this.setIntention(CtrlIntention.ACTIVE);
         this._actor.doCast(this._skill);
      }
   }

   protected boolean thinkActive() {
      Attackable npc = this.getActiveChar();
      if (npc.isActionsDisabled()) {
         return true;
      } else if (npc.getFindTargetDelay() > System.currentTimeMillis()) {
         this.clientStartAutoAttack();
         return false;
      } else {
         if (this._globalAggro != 0) {
            if (this._globalAggro < 0) {
               ++this._globalAggro;
            } else {
               --this._globalAggro;
            }
         }

         if (this._globalAggro >= 0) {
            for(GameObject obj : World.getInstance().getAroundObjects(npc)) {
               if (obj instanceof Creature && !(obj instanceof StaticObjectInstance)) {
                  Creature target = (Creature)obj;
                  if (npc instanceof FestivalMonsterInstance && obj instanceof Player) {
                     Player targetPlayer = (Player)obj;
                     if (!targetPlayer.isFestivalParticipant()) {
                        continue;
                     }
                  }

                  if (this.checkAggression(target)) {
                     int hating = npc.getHating(target);
                     if (hating == 0) {
                        npc.addDamageHate(target, 0, 0);
                     }
                  }
               }
            }

            Creature hated;
            if (npc.isConfused()) {
               hated = this.getAttackTarget();
            } else {
               hated = npc.getMostHated();
            }

            if (hated != null && !npc.isCoreAIDisabled()) {
               int aggro = npc.getHating(hated);
               if (aggro + this._globalAggro > 0) {
                  if (!npc.isRunning()) {
                     if (npc.isEkimusFood()) {
                        npc.setWalking();
                     } else {
                        npc.setRunning();
                     }
                  }

                  this.setIntention(CtrlIntention.ATTACK, hated);
               }

               return true;
            }
         }

         if (npc.getCurrentHp() == npc.getMaxHp() && npc.getCurrentMp() == npc.getMaxMp() && !npc.getAttackByList().isEmpty() && Rnd.nextInt(500) == 0) {
            npc.clearAggroList();
            npc.getAttackByList().clear();
         }

         if (npc instanceof GuardInstance
            && !npc.isWalker()
            && !npc.isRunner()
            && !npc.isSpecialCamera()
            && !npc.isEkimusFood()
            && !(npc instanceof QuestGuardInstance)) {
            ((GuardInstance)npc).returnHome();
         }

         if (npc instanceof FestivalMonsterInstance) {
            return false;
         } else if (!npc.canReturnToSpawnPoint()) {
            return false;
         } else if (this._waitTimeOut > 0) {
            --this._waitTimeOut;
            if (this._waitTimeOut == 0 && npc.getSpawn() != null && !npc.isRunner()) {
               this.returnHome();
            }

            return false;
         } else {
            Creature leader = npc.getLeader();
            if (leader != null) {
               if (leader.isDead() && !leader.isVisible()) {
                  npc.deleteMe();
                  return false;
               }

               double distance = npc.getDistance(leader);
               if (distance > 1000.0 || !GeoEngine.canSeeTarget(npc, leader, npc.isFlying())) {
                  Location loc = leader.getMinionPosition();
                  if (loc != null) {
                     npc.teleToLocation(loc, true);
                  } else {
                     this._log
                        .warning(
                           "Problem to found minion position for npcId[" + npc.getId() + "] Current loc: " + npc.getX() + " " + npc.getY() + " " + npc.getZ()
                        );
                     npc.doDie(null);
                  }

                  return true;
               }

               if (distance > 200.0) {
                  Location loc = leader.getMinionPosition();
                  if (loc != null) {
                     if (leader.isRunning()) {
                        npc.setRunning();
                     } else {
                        npc.setWalking();
                     }

                     this.moveTo(loc);
                  } else {
                     this._log
                        .warning(
                           "Problem to found minion position for npcId[" + npc.getId() + "] Current loc: " + npc.getX() + " " + npc.getY() + " " + npc.getZ()
                        );
                     npc.doDie(null);
                  }

                  return true;
               }
            } else if (npc.getSpawn() != null && !npc.isNoRndWalk() && npc.canReturnToSpawnPoint()) {
               if (this.maybeMoveToHome()) {
                  return true;
               }
            } else if (npc.getSpawn() != null && npc.isNoRndWalk() && npc.canReturnToSpawnPoint()) {
               boolean isInRange = npc.isInRangeZ(npc.getSpawnedLoc(), (long)Config.MAX_DRIFT_RANGE);
               if (isInRange) {
                  return false;
               }

               if (this.maybeMoveToHome()) {
                  return true;
               }
            }

            return true;
         }
      }
   }

   protected void thinkAttack() {
      Attackable npc = this.getActiveChar();
      if (!npc.isCastingNow() && !npc.isAttackingNow()) {
         if (!npc.isCoreAIDisabled()) {
            if (npc.getSpawn() != null && !npc.isInRange(npc.getSpawnedLoc(), (long)this.MAX_PURSUE_RANGE)) {
               this.teleportHome();
            } else {
               Creature target = npc.getMostHated();
               if (target == null) {
                  this._waitTimeOut = 10;
                  this.setIntention(CtrlIntention.ACTIVE);
               } else {
                  this.setAttackTarget(target);
                  npc.setTarget(target);
                  Creature attackTarget = this.getAttackTarget();
                  if (attackTarget != null && !attackTarget.isAlikeDead() && this.getAttackTimeout() >= (long)GameTimeController.getInstance().getGameTicks()) {
                     if (!npc.isInRangeZ(attackTarget, (long)this.MAX_PURSUE_RANGE)) {
                        npc.stopHating(attackTarget);
                        if (npc.getAggroList().isEmpty()) {
                           this._waitTimeOut = 10;
                           this.setIntention(CtrlIntention.ACTIVE);
                        }
                     } else {
                        int collision = (int)npc.getColRadius();
                        int combinedCollision = (int)((double)collision + target.getColRadius());
                        if (!npc.isMovementDisabled() && Rnd.nextInt(100) <= 3) {
                           for(Npc nearby : World.getInstance().getAroundNpc(npc)) {
                              if (nearby instanceof Attackable && npc.isInsideRadius(nearby, collision, false, false) && nearby != target) {
                                 int newX = Rnd.nextBoolean()
                                    ? target.getX() + combinedCollision + Rnd.get(40)
                                    : target.getX() - combinedCollision + Rnd.get(40);
                                 int newY = Rnd.nextBoolean()
                                    ? target.getY() + combinedCollision + Rnd.get(40)
                                    : target.getY() - combinedCollision + Rnd.get(40);
                                 if (!npc.isInsideRadius(newX, newY, 0, collision, false, false)) {
                                    int newZ = npc.getZ() + 30;
                                    if (GeoEngine.canMoveToCoord(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getGeoIndex())) {
                                       this.moveTo(newX, newY, newZ, 0);
                                    }
                                 }

                                 return;
                              }
                           }
                        }

                        if (!npc.isMovementDisabled() && npc.getAI() instanceof Ranger && Rnd.get(100) <= this.getRateDodge()) {
                           double distance2 = npc.getPlanDistanceSq(target.getX(), target.getY());
                           if (Math.sqrt(distance2) <= (double)(60 + combinedCollision)) {
                              int newX = Rnd.nextBoolean()
                                 ? target.getX() + combinedCollision + Rnd.get(150)
                                 : target.getX() - combinedCollision + Rnd.get(150);
                              int newY = Rnd.nextBoolean()
                                 ? target.getY() + combinedCollision + Rnd.get(150)
                                 : target.getY() - combinedCollision + Rnd.get(150);
                              int newZ = npc.getZ() + Rnd.get(10);
                              if (GeoEngine.canMoveToCoord(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getGeoIndex())) {
                                 this.moveTo(newX, newY, newZ, 0);
                                 return;
                              }
                           }
                        }

                        if ((npc.isRaid() || npc.isRaidMinion()) && Rnd.chance(npc.getTemplate().getParameter("isMadness", 0)) && !npc.isConfused()) {
                           if (npc instanceof RaidBossInstance) {
                              if (!((MonsterInstance)npc).hasMinions()) {
                                 if ((double)Rnd.get(100) <= 100.0 - npc.getCurrentHp() * 100.0 / npc.getMaxHp()) {
                                    this.aggroReconsider();
                                    return;
                                 }
                              } else if ((double)Rnd.get(100) <= 100.0 - npc.getCurrentHp() * 200.0 / npc.getMaxHp()) {
                                 this.aggroReconsider();
                                 return;
                              }
                           } else if (npc instanceof GrandBossInstance) {
                              double chaosRate = 100.0 - npc.getCurrentHp() * 300.0 / npc.getMaxHp();
                              if (chaosRate <= 10.0 && Rnd.get(100) <= 10 || chaosRate > 10.0 && (double)Rnd.get(100) <= chaosRate) {
                                 this.aggroReconsider();
                                 return;
                              }
                           } else if ((double)Rnd.get(100) <= 100.0 - npc.getCurrentHp() * 200.0 / npc.getMaxHp()) {
                              this.aggroReconsider();
                              return;
                           }
                        }

                        double dist = Math.sqrt(npc.getPlanDistanceSq(target.getX(), target.getY()));
                        int dist2 = (int)dist - collision;
                        int range = npc.getPhysicalAttackRange() + combinedCollision;
                        if (target.isMoving()) {
                           range += 25;
                           if (npc.isMoving()) {
                              range += 25;
                           }
                        }

                        if (npc.isMovementDisabled()) {
                           this.movementDisable();
                        } else if (!this.createNewTask()) {
                           if (dist2 <= range && GeoEngine.canSeeTarget(npc, target, target.isFlying())) {
                              this._actor.doAttack(this.getAttackTarget());
                           } else {
                              if (npc.isMovementDisabled()) {
                                 this.targetReconsider();
                              } else {
                                 Creature tgt = this.getAttackTarget();
                                 if (tgt != null) {
                                    this.moveTo(tgt.getLocation());
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     if (attackTarget != null) {
                        npc.stopHating(attackTarget);
                     }

                     this._waitTimeOut = 10;
                     this.setIntention(CtrlIntention.ACTIVE);
                  }
               }
            }
         }
      }
   }

   protected boolean cast(Skill sk) {
      if (sk == null) {
         return false;
      } else {
         Attackable caster = this.getActiveChar();
         if (caster.isCastingNow() && !sk.isSimultaneousCast()) {
            return false;
         } else if (!this.checkSkillCastConditions(sk)) {
            return false;
         } else {
            if (this.getAttackTarget() == null && caster.getMostHated() != null) {
               this.setAttackTarget(caster.getMostHated());
            }

            Creature attackTarget = this.getAttackTarget();
            if (attackTarget == null) {
               return false;
            } else {
               double dist = Math.sqrt(caster.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
               double dist2 = dist - attackTarget.getColRadius();
               double range = (double)caster.getPhysicalAttackRange() + caster.getColRadius() + attackTarget.getColRadius();
               double srange = (double)sk.getCastRange() + caster.getColRadius();
               if (attackTarget.isMoving()) {
                  dist2 -= 30.0;
               }

               switch(sk.getSkillType()) {
                  case BUFF:
                     if (caster.getFirstEffect(sk) == null) {
                        this.clientStopMoving(null);
                        caster.setTarget(caster);
                        caster.doCast(sk);
                        return true;
                     }

                     if (sk.getTargetType() == TargetType.SELF) {
                        return false;
                     }

                     if (sk.getTargetType() == TargetType.ONE) {
                        Creature target = this.effectTargetReconsider(sk, true);
                        if (target != null) {
                           this.clientStopMoving(null);
                           caster.setTarget(target);
                           caster.doCast(sk);
                           caster.setTarget(attackTarget);
                           return true;
                        }
                     }

                     if (this.canParty(sk)) {
                        this.clientStopMoving(null);
                        caster.setTarget(caster);
                        caster.doCast(sk);
                        caster.setTarget(attackTarget);
                        return true;
                     }
                     break;
                  case RESURRECT:
                     if (!this.isParty(sk)) {
                        if (caster.isMinion() && sk.getTargetType() != TargetType.SELF) {
                           Creature leader = caster.getLeader();
                           if (leader != null
                              && leader.isDead()
                              && !Util.checkIfInRange((int)((double)sk.getCastRange() + caster.getColRadius() + leader.getColRadius()), caster, leader, false)
                              && !this.isParty(sk)
                              && !caster.isMovementDisabled()) {
                              this.moveToPawn(leader, (int)((double)sk.getCastRange() + caster.getColRadius() + leader.getColRadius()));
                           }

                           if (GeoEngine.canSeeTarget(caster, leader, false)) {
                              this.clientStopMoving(null);
                              caster.setTarget(leader);
                              caster.doCast(sk);
                              return true;
                           }
                        }

                        for(Creature obj : World.getInstance().getAroundCharacters(caster, (int)((double)sk.getCastRange() + caster.getColRadius()), 200)) {
                           if (obj instanceof Attackable && obj.isDead()) {
                              Attackable targets = (Attackable)obj;
                              if ((caster.getFaction().isNone() || caster.isInFaction(targets))
                                 && Rnd.get(100) < 10
                                 && GeoEngine.canSeeTarget(caster, targets, false)) {
                                 this.clientStopMoving(null);
                                 caster.setTarget(obj);
                                 caster.doCast(sk);
                                 return true;
                              }
                           }
                        }
                     } else if (this.isParty(sk)) {
                        for(Creature obj : World.getInstance().getAroundCharacters(caster, (int)((double)sk.getAffectRange() + caster.getColRadius()), 200)) {
                           if (obj instanceof Attackable) {
                              Npc targets = (Npc)obj;
                              if (!caster.getFaction().isNone()
                                 && caster.isInFaction((Attackable)targets)
                                 && obj.getCurrentHp() < obj.getMaxHp()
                                 && Rnd.get(100) <= 20) {
                                 this.clientStopMoving(null);
                                 caster.setTarget(caster);
                                 caster.doCast(sk);
                                 return true;
                              }
                           }
                        }
                     }
                     break;
                  case DEBUFF:
                  case POISON:
                  case DOT:
                  case MDOT:
                  case BLEED:
                     if (GeoEngine.canSeeTarget(caster, attackTarget, false) && !this.canAOE(sk) && !attackTarget.isDead() && dist2 <= srange) {
                        if (attackTarget.getFirstEffect(sk) == null) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     } else if (this.canAOE(sk)) {
                        if (sk.getTargetType() == TargetType.AURA
                           || sk.getTargetType() == TargetType.BEHIND_AURA
                           || sk.getTargetType() == TargetType.FRONT_AURA
                           || sk.getTargetType() == TargetType.AURA_CORPSE_MOB) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }

                        if ((
                              sk.getTargetType() == TargetType.AREA
                                 || sk.getTargetType() == TargetType.BEHIND_AREA
                                 || sk.getTargetType() == TargetType.FRONT_AREA
                           )
                           && GeoEngine.canSeeTarget(caster, attackTarget, false)
                           && !attackTarget.isDead()
                           && dist2 <= srange) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     } else if (sk.getTargetType() == TargetType.ONE) {
                        Creature target = this.effectTargetReconsider(sk, false);
                        if (target != null) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     }
                     break;
                  case SLEEP:
                     if (sk.getTargetType() == TargetType.ONE) {
                        if (!attackTarget.isDead() && dist2 <= srange && (dist2 > range || attackTarget.isMoving()) && attackTarget.getFirstEffect(sk) == null
                           )
                         {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }

                        Creature target = this.effectTargetReconsider(sk, false);
                        if (target != null) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     } else if (this.canAOE(sk)) {
                        if (sk.getTargetType() == TargetType.AURA
                           || sk.getTargetType() == TargetType.BEHIND_AURA
                           || sk.getTargetType() == TargetType.FRONT_AURA) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }

                        if ((
                              sk.getTargetType() == TargetType.AREA
                                 || sk.getTargetType() == TargetType.BEHIND_AREA
                                 || sk.getTargetType() == TargetType.FRONT_AREA
                           )
                           && GeoEngine.canSeeTarget(caster, attackTarget, false)
                           && !attackTarget.isDead()
                           && dist2 <= srange) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     }
                     break;
                  case ROOT:
                  case STUN:
                  case PARALYZE:
                  case MUTE:
                  case FEAR:
                     if (GeoEngine.canSeeTarget(caster, attackTarget, false) && !this.canAOE(sk) && dist2 <= srange) {
                        if (attackTarget.getFirstEffect(sk) == null) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     } else if (this.canAOE(sk)) {
                        if (sk.getTargetType() == TargetType.AURA
                           || sk.getTargetType() == TargetType.BEHIND_AURA
                           || sk.getTargetType() == TargetType.FRONT_AURA) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }

                        if ((
                              sk.getTargetType() == TargetType.AREA
                                 || sk.getTargetType() == TargetType.BEHIND_AREA
                                 || sk.getTargetType() == TargetType.FRONT_AREA
                           )
                           && GeoEngine.canSeeTarget(caster, attackTarget, false)
                           && !attackTarget.isDead()
                           && dist2 <= srange) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     } else if (sk.getTargetType() == TargetType.ONE) {
                        Creature target = this.effectTargetReconsider(sk, false);
                        if (target != null) {
                           this.clientStopMoving(null);
                           caster.doCast(sk);
                           return true;
                        }
                     }
                     break;
                  case PDAM:
                  case MDAM:
                  case BLOW:
                  case DRAIN:
                  case CHARGEDAM:
                  case FATAL:
                  case DEATHLINK:
                  case MANADAM:
                  case CPDAMPERCENT:
                     if (this.canAura(sk)) {
                        this.clientStopMoving(null);
                        caster.doCast(sk);
                        return true;
                     }

                     if (GeoEngine.canSeeTarget(caster, attackTarget, false) && !attackTarget.isDead() && dist2 <= srange) {
                        this.clientStopMoving(null);
                        caster.doCast(sk);
                        return true;
                     }

                     Creature target = this.skillTargetReconsider(sk);
                     if (target != null) {
                        this.clientStopMoving(null);
                        caster.setTarget(target);
                        caster.doCast(sk);
                        caster.setTarget(attackTarget);
                        return true;
                     }
                     break;
                  default:
                     if (sk.hasEffectType(EffectType.CANCEL, EffectType.CANCEL_ALL, EffectType.CANCEL_BY_SLOT, EffectType.NEGATE)) {
                        if (Rnd.get(50) != 0) {
                           return true;
                        }

                        if (sk.getTargetType() == TargetType.ONE) {
                           if (attackTarget.getFirstEffect(EffectType.BUFF) != null
                              && GeoEngine.canSeeTarget(caster, attackTarget, false)
                              && !attackTarget.isDead()
                              && dist2 <= srange) {
                              this.clientStopMoving(null);
                              caster.doCast(sk);
                              return true;
                           }

                           Creature target = this.effectTargetReconsider(sk, false);
                           if (target != null) {
                              this.clientStopMoving(null);
                              caster.setTarget(target);
                              caster.doCast(sk);
                              caster.setTarget(attackTarget);
                              return true;
                           }
                        } else if (this.canAOE(sk)) {
                           if ((
                                 sk.getTargetType() == TargetType.AURA
                                    || sk.getTargetType() == TargetType.BEHIND_AURA
                                    || sk.getTargetType() == TargetType.FRONT_AURA
                              )
                              && GeoEngine.canSeeTarget(caster, attackTarget, false)) {
                              this.clientStopMoving(null);
                              caster.doCast(sk);
                              return true;
                           }

                           if ((
                                 sk.getTargetType() == TargetType.AREA
                                    || sk.getTargetType() == TargetType.BEHIND_AREA
                                    || sk.getTargetType() == TargetType.FRONT_AREA
                              )
                              && GeoEngine.canSeeTarget(caster, attackTarget, false)
                              && !attackTarget.isDead()
                              && dist2 <= srange) {
                              this.clientStopMoving(null);
                              caster.doCast(sk);
                              return true;
                           }
                        }
                     }

                     if (sk.hasEffectType(EffectType.HEAL, EffectType.HEAL_PERCENT)) {
                        double percentage = caster.getCurrentHp() / caster.getMaxHp() * 100.0;
                        if (caster.isMinion() && sk.getTargetType() != TargetType.SELF) {
                           Creature leader = caster.getLeader();
                           if (leader != null && !leader.isDead() && (double)Rnd.get(100) > leader.getCurrentHp() / leader.getMaxHp() * 100.0) {
                              if (!Util.checkIfInRange((int)((double)sk.getCastRange() + caster.getColRadius() + leader.getColRadius()), caster, leader, false)
                                 && !this.isParty(sk)
                                 && !caster.isMovementDisabled()) {
                                 this.moveToPawn(leader, (int)((double)sk.getCastRange() + caster.getColRadius() + leader.getColRadius()));
                              }

                              if (GeoEngine.canSeeTarget(caster, leader, false)) {
                                 this.clientStopMoving(null);
                                 caster.setTarget(leader);
                                 caster.doCast(sk);
                                 return true;
                              }
                           }
                        }

                        if ((double)Rnd.get(100) < (100.0 - percentage) / 3.0) {
                           this.clientStopMoving(null);
                           caster.setTarget(caster);
                           caster.doCast(sk);
                           return true;
                        }

                        if (sk.getTargetType() == TargetType.ONE) {
                           for(Creature obj : World.getInstance().getAroundCharacters(caster, (int)((double)sk.getCastRange() + caster.getColRadius()), 200)) {
                              if (obj instanceof Attackable && !obj.isDead()) {
                                 Attackable targets = (Attackable)obj;
                                 if (caster.getFaction().isNone() || caster.isInFaction(targets)) {
                                    percentage = targets.getCurrentHp() / targets.getMaxHp() * 100.0;
                                    if ((double)Rnd.get(100) < (100.0 - percentage) / 10.0 && GeoEngine.canSeeTarget(caster, targets, false)) {
                                       this.clientStopMoving(null);
                                       caster.setTarget(obj);
                                       caster.doCast(sk);
                                       return true;
                                    }
                                 }
                              }
                           }
                        }

                        if (this.isParty(sk)) {
                           for(Creature obj : World.getInstance().getAroundCharacters(caster, (int)((double)sk.getAffectRange() + caster.getColRadius()), 200)) {
                              if (obj instanceof Attackable) {
                                 Npc targets = (Npc)obj;
                                 if (!caster.getFaction().isNone() && targets.isInFaction(caster) && obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20
                                    )
                                  {
                                    this.clientStopMoving(null);
                                    caster.setTarget(caster);
                                    caster.doCast(sk);
                                    return true;
                                 }
                              }
                           }
                        }
                     }

                     if (this.canAura(sk)) {
                        this.clientStopMoving(null);
                        caster.doCast(sk);
                        return true;
                     }

                     if (GeoEngine.canSeeTarget(caster, attackTarget, false) && !attackTarget.isDead() && dist2 <= srange) {
                        this.clientStopMoving(null);
                        caster.doCast(sk);
                        return true;
                     }

                     Creature target = this.skillTargetReconsider(sk);
                     if (target != null) {
                        this.clientStopMoving(null);
                        caster.setTarget(target);
                        caster.doCast(sk);
                        caster.setTarget(attackTarget);
                        return true;
                     }
               }

               return false;
            }
         }
      }
   }

   private void movementDisable() {
      Attackable npc = this.getActiveChar();
      Creature target = this.getAttackTarget();
      if (target != null) {
         if (npc.getTarget() == null) {
            npc.setTarget(target);
         }

         double dist = npc.calculateDistance(target, false, false);
         int range = (int)((double)npc.getPhysicalAttackRange() + npc.getColRadius() + target.getColRadius());
         int random = Rnd.get(100);
         if (random < 5) {
            for(Skill sk : npc.getTemplate().getDotSkills()) {
               if (sk != null
                  && this.checkSkillCastConditions(sk)
                  && (!((double)sk.getCastRange() + npc.getColRadius() + target.getColRadius() <= dist) || this.canAura(sk))
                  && GeoEngine.canSeeTarget(npc, target, false)
                  && target.getFirstEffect(sk) == null) {
                  this.clientStopMoving(null);
                  npc.doCast(sk);
                  return;
               }
            }
         }

         if (random < 8) {
            for(Skill sk : npc.getTemplate().getDebuffSkills()) {
               if (sk != null
                  && this.checkSkillCastConditions(sk)
                  && (!((double)sk.getCastRange() + npc.getColRadius() + target.getColRadius() <= dist) || this.canAura(sk))
                  && GeoEngine.canSeeTarget(npc, target, false)
                  && target.getFirstEffect(sk) == null) {
                  this.clientStopMoving(null);
                  npc.doCast(sk);
                  return;
               }
            }
         }

         if (npc.isMovementDisabled() || npc.getAI() instanceof Mystic || npc.getAI() instanceof Priest) {
            for(Skill sk : npc.getTemplate().getDamageSkills()) {
               if (sk != null
                  && this.checkSkillCastConditions(sk)
                  && (!((double)sk.getCastRange() + npc.getColRadius() + target.getColRadius() <= dist) || this.canAura(sk))
                  && GeoEngine.canSeeTarget(npc, target, false)) {
                  this.clientStopMoving(null);
                  npc.doCast(sk);
                  return;
               }
            }
         }

         if (dist <= (double)range && GeoEngine.canSeeTarget(npc, this.getAttackTarget(), false)) {
            this._actor.doAttack(target);
         } else {
            this.targetReconsider();
         }
      }
   }

   private boolean checkSkillCastConditions(Skill skill) {
      if (skill == null) {
         return false;
      } else if ((double)skill.getMpConsume() >= this.getActiveChar().getCurrentMp()) {
         return false;
      } else if (!this.getActiveChar().isSkillDisabled(skill) && !this.getActiveChar().isSkillBlocked(skill)) {
         return skill.isStatic() || (!skill.isMagic() || !this.getActiveChar().isMuted()) && !this.getActiveChar().isPhysicalMuted();
      } else {
         return false;
      }
   }

   private Creature effectTargetReconsider(Skill sk, boolean positive) {
      Creature target = this.getAttackTarget();
      if (sk != null && target != null) {
         Attackable actor = this.getActiveChar();
         if (!sk.hasEffectType(EffectType.CANCEL, EffectType.CANCEL_ALL, EffectType.CANCEL_BY_SLOT, EffectType.NEGATE)) {
            if (!positive) {
               double dist = 0.0;
               double dist2 = 0.0;
               int range = 0;
               Iterator var10 = actor.getAttackByList().iterator();

               while(true) {
                  Creature obj;
                  while(true) {
                     if (!var10.hasNext()) {
                        var10 = World.getInstance().getAroundCharacters(actor, range, 200).iterator();

                        while(true) {
                           while(true) {
                              if (!var10.hasNext()) {
                                 return null;
                              }

                              obj = (Creature)var10.next();
                              if (!obj.isDead() && GeoEngine.canSeeTarget(actor, obj, false)) {
                                 try {
                                    actor.setTarget(target);
                                    dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                                    dist2 = dist;
                                    range = (int)((double)sk.getCastRange() + actor.getColRadius() + obj.getColRadius());
                                    if (obj.isMoving()) {
                                       dist2 = dist - 70.0;
                                    }
                                    break;
                                 } catch (NullPointerException var16) {
                                 }
                              }
                           }

                           if ((obj instanceof Player || obj instanceof Summon)
                              && dist2 <= (double)range
                              && target != null
                              && target.getFirstEffect(sk) == null) {
                              return obj;
                           }
                        }
                     }

                     obj = (Creature)var10.next();
                     if (obj != null && !obj.isDead() && GeoEngine.canSeeTarget(actor, obj, false) && obj != target) {
                        try {
                           actor.setTarget(target);
                           dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                           dist2 = dist - actor.getColRadius();
                           range = (int)((double)sk.getCastRange() + actor.getColRadius() + obj.getColRadius());
                           if (obj.isMoving()) {
                              dist2 -= 70.0;
                           }
                           break;
                        } catch (NullPointerException var17) {
                        }
                     }
                  }

                  if (dist2 <= (double)range && target.getFirstEffect(sk) == null) {
                     return obj;
                  }
               }
            } else if (positive) {
               double dist = 0.0;
               double dist2 = 0.0;
               int range = 0;
               Iterator var37 = World.getInstance().getAroundCharacters(actor, range, 200).iterator();

               while(true) {
                  Creature obj;
                  while(true) {
                     if (!var37.hasNext()) {
                        return null;
                     }

                     obj = (Creature)var37.next();
                     if (obj instanceof Attackable && !obj.isDead() && GeoEngine.canSeeTarget(actor, obj, false)) {
                        Attackable targets = (Attackable)obj;
                        if (actor.getFaction().isNone() || actor.isInFaction(targets)) {
                           try {
                              actor.setTarget(target);
                              dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                              dist2 = dist - actor.getColRadius();
                              range = (int)((double)sk.getCastRange() + actor.getColRadius() + obj.getColRadius());
                              if (obj.isMoving()) {
                                 dist2 -= 70.0;
                              }
                              break;
                           } catch (NullPointerException var15) {
                           }
                        }
                     }
                  }

                  if (dist2 <= (double)range && obj.getFirstEffect(sk) == null) {
                     return obj;
                  }
               }
            } else {
               return null;
            }
         } else {
            double dist = 0.0;
            double dist2 = 0.0;
            int range = 0;
            range = (int)((double)sk.getCastRange() + actor.getColRadius() + target.getColRadius());
            Iterator var38 = World.getInstance().getAroundCharacters(actor, range, 200).iterator();

            while(true) {
               Creature obj;
               while(true) {
                  if (!var38.hasNext()) {
                     return null;
                  }

                  obj = (Creature)var38.next();
                  if (obj != null && !obj.isDead() && GeoEngine.canSeeTarget(actor, obj, false)) {
                     try {
                        actor.setTarget(target);
                        dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                        dist2 = dist - actor.getColRadius();
                        range = (int)((double)sk.getCastRange() + actor.getColRadius() + obj.getColRadius());
                        if (obj.isMoving()) {
                           dist2 -= 70.0;
                        }
                        break;
                     } catch (NullPointerException var14) {
                     }
                  }
               }

               if ((obj instanceof Player || obj instanceof Summon)
                  && dist2 <= (double)range
                  && this.getAttackTarget().getFirstEffect(EffectType.BUFF) != null) {
                  return obj;
               }
            }
         }
      } else {
         return null;
      }
   }

   private Creature skillTargetReconsider(Skill sk) {
      double dist = 0.0;
      double dist2 = 0.0;
      int range = 0;
      Attackable actor = this.getActiveChar();
      List<Creature> hateList = actor.getHateList();
      if (hateList != null && !hateList.isEmpty()) {
         for(Creature obj : hateList) {
            if (obj != null && GeoEngine.canSeeTarget(actor, obj, false) && !obj.isDead()) {
               try {
                  actor.setTarget(this.getAttackTarget());
                  dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                  dist2 = dist - actor.getColRadius();
                  range = (int)((double)sk.getCastRange() + actor.getColRadius() + this.getAttackTarget().getColRadius());
               } catch (NullPointerException var13) {
                  continue;
               }

               if (dist2 <= (double)range) {
                  return obj;
               }
            }
         }
      }

      if (!(actor instanceof GuardInstance)) {
         for(GameObject target : World.getInstance().getAroundObjects(actor)) {
            try {
               actor.setTarget(this.getAttackTarget());
               dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY()));
               dist2 = dist;
               range = (int)((double)sk.getCastRange() + actor.getColRadius() + this.getAttackTarget().getColRadius());
            } catch (NullPointerException var12) {
               continue;
            }

            Creature obj = null;
            if (target instanceof Creature) {
               obj = (Creature)target;
            }

            if (obj != null && GeoEngine.canSeeTarget(actor, obj, false) && !(dist2 > (double)range)) {
               if (obj instanceof Player) {
                  return obj;
               }

               if (obj instanceof Summon) {
                  return obj;
               }
            }
         }
      }

      return null;
   }

   private void targetReconsider() {
      double dist = 0.0;
      double dist2 = 0.0;
      int range = 0;
      Attackable actor = this.getActiveChar();
      Creature MostHate = actor.getMostHated();
      List<Creature> hateList = actor.getHateList();
      if (hateList != null && !hateList.isEmpty()) {
         Iterator var9 = hateList.iterator();

         label114:
         while(true) {
            Creature obj;
            while(true) {
               if (!var9.hasNext()) {
                  break label114;
               }

               obj = (Creature)var9.next();
               if (obj != null && GeoEngine.canSeeTarget(actor, obj, false) && !obj.isDead() && obj == MostHate && obj != actor) {
                  try {
                     dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
                     dist2 = dist - actor.getColRadius();
                     range = (int)((double)actor.getPhysicalAttackRange() + actor.getColRadius() + obj.getColRadius());
                     if (obj.isMoving()) {
                        dist2 -= 70.0;
                     }
                     break;
                  } catch (NullPointerException var12) {
                  }
               }
            }

            if (dist2 <= (double)range) {
               if (MostHate != null) {
                  actor.addDamageHate(obj, 0, actor.getHating(MostHate));
               } else {
                  actor.addDamageHate(obj, 0, 2000);
               }

               actor.setTarget(obj);
               this.setAttackTarget(obj);
               return;
            }
         }
      }

      if (!(actor instanceof GuardInstance)) {
         for(Creature obj : World.getInstance().getAroundCharacters(actor)) {
            if (obj != null && GeoEngine.canSeeTarget(actor, obj, false) && !obj.isDead() && obj == MostHate && obj != actor && obj != this.getAttackTarget()) {
               if (obj instanceof Player) {
                  if (MostHate != null) {
                     actor.addDamageHate(obj, 0, actor.getHating(MostHate));
                  } else {
                     actor.addDamageHate(obj, 0, 2000);
                  }

                  actor.setTarget(obj);
                  this.setAttackTarget(obj);
               } else if (obj instanceof Attackable) {
                  if (((Attackable)obj).getFaction().isNone() || !((Attackable)obj).isInFaction(actor)) {
                     if (MostHate != null) {
                        actor.addDamageHate(obj, 0, actor.getHating(MostHate));
                     } else {
                        actor.addDamageHate(obj, 0, 2000);
                     }

                     actor.setTarget(obj);
                     this.setAttackTarget(obj);
                  }
               } else if (obj instanceof Summon) {
                  if (MostHate != null) {
                     actor.addDamageHate(obj, 0, actor.getHating(MostHate));
                  } else {
                     actor.addDamageHate(obj, 0, 2000);
                  }

                  actor.setTarget(obj);
                  this.setAttackTarget(obj);
               }
            }
         }
      }
   }

   protected void aggroReconsider() {
      Attackable actor;
      Creature MostHate;
      Creature obj;
      label141: {
         actor = this.getActiveChar();
         MostHate = actor.getMostHated();
         List<Creature> hateList = actor.getHateList();
         if (hateList != null && !hateList.isEmpty()) {
            int rand = Rnd.get(hateList.size());
            int count = 0;

            for(obj : hateList) {
               if (count < rand) {
                  ++count;
               } else if (obj != null && GeoEngine.canSeeTarget(actor, obj, false) && !obj.isDead() && obj != this.getAttackTarget() && obj != actor) {
                  try {
                     actor.setTarget(this.getAttackTarget());
                     break label141;
                  } catch (NullPointerException var9) {
                  }
               }
            }
         }

         if (!(actor instanceof GuardInstance)) {
            for(Creature obj : World.getInstance().getAroundCharacters(actor)) {
               if (GeoEngine.canSeeTarget(actor, obj, false) && !obj.isDead() && obj == MostHate && obj != actor) {
                  if (obj instanceof Player) {
                     if (MostHate != null && !MostHate.isDead()) {
                        actor.addDamageHate(obj, 0, actor.getHating(MostHate));
                     } else {
                        actor.addDamageHate(obj, 0, 2000);
                     }

                     actor.setTarget(obj);
                     this.setAttackTarget(obj);
                  } else if (obj instanceof Attackable) {
                     if (!((Attackable)obj).getFaction().isNone() && ((Attackable)obj).isInFaction(actor)) {
                        continue;
                     }

                     if (MostHate != null) {
                        actor.addDamageHate(obj, 0, actor.getHating(MostHate));
                     } else {
                        actor.addDamageHate(obj, 0, 2000);
                     }

                     actor.setTarget(obj);
                     this.setAttackTarget(obj);
                  } else if (obj instanceof Summon) {
                     if (MostHate != null) {
                        actor.addDamageHate(obj, 0, actor.getHating(MostHate));
                     } else {
                        actor.addDamageHate(obj, 0, 2000);
                     }

                     actor.setTarget(obj);
                     this.setAttackTarget(obj);
                  }

                  if (this._madnessTask == null && !actor.isConfused()) {
                     actor.startConfused();
                     this._madnessTask = ThreadPoolManager.getInstance().schedule(new DefaultAI.madnessTask(), 10000L);
                  }
               }
            }
         }

         return;
      }

      if (MostHate != null) {
         actor.addDamageHate(obj, 0, actor.getHating(MostHate));
      } else {
         actor.addDamageHate(obj, 0, 2000);
      }

      actor.setTarget(obj);
      this.setAttackTarget(obj);
      if (this._madnessTask == null && !actor.isConfused()) {
         actor.startConfused();
         this._madnessTask = ThreadPoolManager.getInstance().schedule(new DefaultAI.madnessTask(), 10000L);
      }
   }

   @Override
   protected void onEvtThink() {
      if (!this._thinking && !this.getActiveChar().isActionsDisabled()) {
         this._thinking = true;

         try {
            switch(this.getIntention()) {
               case ACTIVE:
               case MOVING:
                  this.thinkActive();
                  break;
               case ATTACK:
                  this.thinkAttack();
                  break;
               case CAST:
                  this.thinkCast();
            }
         } finally {
            this._thinking = false;
         }
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable me = this.getActiveChar();
      if (attacker != null && !me.isDead()) {
         if (me.hasAI() && attacker.isPlayer() && !me.getAI().getTargetList().contains(attacker)) {
            me.getAI().addToTargetList(attacker.getActingPlayer());
         }

         this._attackTimeout = 1200 + GameTimeController.getInstance().getGameTicks();
         if (this._globalAggro < 0) {
            this._globalAggro = 0;
         }

         int transformer = me.getTemplate().getParameter("transformOnUnderAttack", 0);
         if (transformer > 0) {
            int chance = me.getTemplate().getParameter("transformChance", 5);
            if (chance == 100 || me.getCurrentHpPercents() > 50.0 && Rnd.chance(chance)) {
               me.decayMe();
               MonsterInstance npc = NpcUtils.spawnSingle(transformer, me.getLocation(), (long)me.getReflectionId());
               npc.setRunning();
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
               attacker.setTarget(npc);
               return;
            }
         }

         if (!Config.ALLOW_UNLIM_ENTER_CATACOMBS && me.isSevenSignsMonster()) {
            Player player = attacker.getActingPlayer();
            if (player != null && (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())) {
               int pcabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
               int wcabal = SevenSigns.getInstance().getCabalHighestScore();
               if (pcabal != wcabal && wcabal != 0) {
                  player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
                  player.teleToClosestTown();
                  return;
               }
            }
         }

         me.addDamageHate(attacker, damage, !attacker.isSummon() && !attacker.isPet() ? 0 : (int)((double)damage * Config.PET_HATE_MOD));
         if (!me.isRunning()) {
            if (me.isEkimusFood()) {
               me.setWalking();
            } else {
               me.setRunning();
            }
         }

         if (me.getHating(attacker) > 0 && (attacker.isSummon() || attacker.isPet())) {
            me.addDamageHate(attacker.getActingPlayer(), 0, me.getTemplate().getParameter("searchingMaster", false) ? me.getHating(attacker) : 1);
         }

         if (this.getIntention() != CtrlIntention.ATTACK) {
            this.setIntention(CtrlIntention.ATTACK, attacker);
         } else if (me.getMostHated() != this.getAttackTarget()) {
            this.setIntention(CtrlIntention.ATTACK, attacker);
         }

         this.notifyFriends(attacker, damage);
         this.checkUD(attacker);
         super.onEvtAttacked(attacker, damage);
      }
   }

   protected void notifyFriends(Creature attacker, int damage) {
      if (!attacker.isInvisible()) {
         if (!this._canNotifyFriend) {
            this._lastFactionNotifyTime = System.currentTimeMillis();
            this._canNotifyFriend = true;
         } else {
            Attackable actor = this.getActiveChar();
            if (System.currentTimeMillis() - this._lastFactionNotifyTime > this._minFactionNotifyInterval) {
               this._lastFactionNotifyTime = System.currentTimeMillis();
               if (actor.isMinion()) {
                  MonsterInstance master = ((MinionInstance)actor).getLeader();
                  if (master != null) {
                     if (!master.isDead() && master.isVisible()) {
                        master.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(damage));
                     }

                     MinionList minionList = master.getMinionList();
                     if (minionList != null) {
                        for(MinionInstance minion : minionList.getAliveMinions()) {
                           if (minion != actor) {
                              minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(damage));
                           }
                        }
                     }
                  }
               }

               MinionList minionList = actor.getMinionList();
               if (minionList != null && minionList.hasAliveMinions()) {
                  for(MinionInstance minion : minionList.getAliveMinions()) {
                     minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(damage));
                  }
               }

               for(Npc npc : this.activeFactionTargets(actor)) {
                  if (attacker.isPlayable()) {
                     npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(1));
                     List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL);
                     if (quests != null && !quests.isEmpty()) {
                        Player player = attacker.getActingPlayer();
                        boolean isSummon = attacker.isSummon();

                        for(Quest quest : quests) {
                           quest.notifyFactionCall(npc, this.getActiveChar(), player, isSummon);
                        }
                     }
                  } else if (npc.getAI()._intention != CtrlIntention.ATTACK) {
                     ((Attackable)npc).addDamageHate(attacker, 0, actor.getHating(attacker));
                     npc.getAI().setIntention(CtrlIntention.ATTACK, attacker);
                  }
               }
            }
         }
      }
   }

   protected List<Npc> activeFactionTargets(Attackable actor) {
      if (actor.getFaction().isNone()) {
         return Collections.emptyList();
      } else {
         List<Npc> npcFriends = new ArrayList<>();

         for(Npc npc : World.getInstance().getAroundNpc(actor, (int)((double)actor.getFaction().getRange() + actor.getColRadius()), 200)) {
            if (npc instanceof MonsterInstance
               && !npc.isDead()
               && !npc.isAttackingNow()
               && npc.isInFaction(actor)
               && GeoEngine.canSeeTarget(npc, actor, false)) {
               npcFriends.add(npc);
            }
         }

         return npcFriends;
      }
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
      Attackable me = this.getActiveChar();
      if (target != null) {
         me.addDamageHate(target, 0, aggro);
         if (aggro > 0 && (target.isSummon() || target.isPet())) {
            me.addDamageHate(target.getActingPlayer(), 0, me.getTemplate().getParameter("searchingMaster", false) ? aggro : 1);
         }

         if (this.getIntention() != CtrlIntention.ATTACK) {
            if (!me.isRunning()) {
               if (me.isEkimusFood()) {
                  me.setWalking();
               } else {
                  me.setRunning();
               }
            }

            this.setIntention(CtrlIntention.ATTACK, target);
         }
      }
   }

   @Override
   protected void onIntentionActive() {
      this._attackTimeout = Integer.MAX_VALUE;
      super.onIntentionActive();
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      int transformer = actor.getTemplate().getParameter("transformOnDead", 0);
      int chance = actor.getTemplate().getParameter("transformChance", 100);
      if (transformer > 0 && Rnd.chance(chance)) {
         MonsterInstance npc = NpcUtils.spawnSingle(transformer, actor.getLocation(), (long)actor.getReflectionId());
         if (killer != null && killer.isPlayable()) {
            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(100));
            killer.setTarget(npc);
         }
      }

      super.onEvtDead(killer);
   }

   @Override
   protected void onEvtSpawn() {
      this._defFlag = 0;
   }

   protected boolean createNewTask() {
      return false;
   }

   protected boolean defaultFightTask() {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead() && !actor.isMuted()) {
         Creature target = actor.getMostHated();
         if (target == null) {
            return false;
         } else if (this.applyUD()) {
            return true;
         } else {
            double distance = actor.getDistance(target);
            double targetHp = target.getCurrentHpPercents();
            double actorHp = actor.getCurrentHpPercents();
            Skill[] dam = Rnd.chance(this.getRateDAM()) ? this.selectUsableSkills(target, distance, this._damSkills) : null;
            Skill[] dot = Rnd.chance(this.getRateDOT()) ? this.selectUsableSkills(target, distance, this._dotSkills) : null;
            Skill[] debuff = targetHp > 10.0
               ? (Rnd.chance(this.getRateDEBUFF()) ? this.selectUsableSkills(target, distance, this._debuffSkills) : null)
               : null;
            Skill[] stun = Rnd.chance(this.getRateSTUN()) ? this.selectUsableSkills(target, distance, this._stunSkills) : null;
            Skill[] heal = actorHp < 50.0 ? (Rnd.chance(this.getRateHEAL()) ? this.selectUsableSkills(actor, 0.0, this._healSkills) : null) : null;
            Skill[] buff = Rnd.chance(this.getRateBUFF()) ? this.selectUsableSkills(actor, 0.0, this._buffSkills) : null;
            Skill[] suicide = actorHp < 30.0 ? (Rnd.chance(this.getRateSuicide()) ? this.selectUsableSkills(actor, 0.0, this._suicideSkills) : null) : null;
            Skill[] res = Rnd.chance(this.getRateHEAL()) ? this.selectUsableSkills(target, distance, this._resSkills) : null;
            RndSelector<Skill[]> rnd = new RndSelector<>();
            if (!actor.isMuted()) {
               rnd.add(null, this.getRatePHYS());
            }

            rnd.add(dam, this.getRateDAM());
            rnd.add(dot, this.getRateDOT());
            rnd.add(debuff, this.getRateDEBUFF());
            rnd.add(heal, this.getRateHEAL());
            rnd.add(buff, this.getRateBUFF());
            rnd.add(stun, this.getRateSTUN());
            rnd.add(suicide, this.getRateSuicide());
            rnd.add(res, this.getRateRes());
            Skill[] selected = (Skill[])rnd.select();
            if (selected != null) {
               if (selected == dam || selected == dot) {
                  return this.checkOtherSkills(actor, target, selectTopSkillByDamage(actor, target, distance, selected));
               }

               if (selected == debuff || selected == stun) {
                  return this.checkOtherSkills(actor, target, selectTopSkillByDebuff(actor, target, distance, selected));
               }

               if (selected == buff) {
                  return this.checkBuffSkills(actor, target, selectTopSkillByBuff(actor, selected));
               }

               if (selected == heal) {
                  return this.checkHealSkills(actor, target, selectTopSkillByHeal(actor, selected));
               }

               if (selected == suicide) {
                  return this.checkSuisideSkills(actor, target, selectTopSkillByDamage(actor, target, distance, selected));
               }

               if (selected == res) {
                  return this.checkResSkills(actor, target, selectTopSkillByDamage(actor, target, distance, selected));
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   protected Skill[] selectUsableSkills(Creature target, double distance, Skill[] skills) {
      if (skills != null && skills.length != 0 && target != null) {
         Skill[] ret = null;
         int usable = 0;

         for(Skill skill : skills) {
            if (this.canUseSkill(skill, target, distance)) {
               if (ret == null) {
                  ret = new Skill[skills.length];
               }

               ret[usable++] = skill;
            }
         }

         if (ret == null || usable == skills.length) {
            return ret;
         } else {
            return usable == 0 ? null : Arrays.copyOf(ret, usable);
         }
      } else {
         return null;
      }
   }

   protected boolean checkResSkills(Attackable npc, Creature target, Skill skill) {
      if (npc.isMinion()) {
         Creature leader = npc.getLeader();
         if (leader != null && leader.isDead()) {
            if (skill.getTargetType() == TargetType.SELF) {
               return false;
            }

            if (!this.checkSkillCastConditions(skill)) {
               return false;
            }

            if (!Util.checkIfInRange((int)((double)skill.getCastRange() + leader.getColRadius()), npc, leader, false)
               && !this.isParty(skill)
               && !npc.isMovementDisabled()) {
               this.moveToPawn(leader, (int)((double)skill.getCastRange() + leader.getColRadius()));
               return true;
            }

            if (GeoEngine.canSeeTarget(npc, leader, false)) {
               this.clientStopMoving(null);
               npc.setTarget(leader);
               npc.doCast(skill);
               return true;
            }
         }
      }

      if (!this.checkSkillCastConditions(skill)) {
         return false;
      } else {
         if (skill.getTargetType() == TargetType.ONE) {
            for(Creature obj : World.getInstance().getAroundCharacters(npc, (int)((double)skill.getCastRange() + npc.getColRadius()), 200)) {
               if (obj instanceof Attackable && obj.isDead()) {
                  Attackable targets = (Attackable)obj;
                  if ((npc.getFaction().isNone() || npc.isInFaction(targets)) && Rnd.get(100) < 10 && GeoEngine.canSeeTarget(npc, targets, false)) {
                     this.clientStopMoving(null);
                     npc.setTarget(obj);
                     npc.doCast(skill);
                     return true;
                  }
               }
            }
         }

         if (this.isParty(skill)) {
            this.clientStopMoving(null);
            GameObject newTarget = this.getAttackTarget();
            npc.setTarget(npc);
            npc.doCast(skill);
            npc.setTarget(newTarget);
            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean checkOtherSkills(Attackable npc, Creature target, Skill skill) {
      return this.cast(skill);
   }

   protected boolean checkSuisideSkills(Attackable npc, Creature target, Skill skill) {
      return Util.checkIfInRange(skill.getAffectRange(), this.getActiveChar(), target, false) ? this.cast(skill) : false;
   }

   protected boolean checkBuffSkills(Attackable npc, Creature target, Skill skill) {
      if (this._lastBuffTick + 30 < GameTimeController.getInstance().getGameTicks()) {
         this.setBuffTicks(GameTimeController.getInstance().getGameTicks());
         return this.cast(skill);
      } else {
         return false;
      }
   }

   protected boolean checkHealSkills(Attackable npc, Creature target, Skill skill) {
      double percentage = npc.getCurrentHp() / npc.getMaxHp() * 100.0;
      if (npc.isMinion()) {
         Creature leader = npc.getLeader();
         if (leader != null && !leader.isDead() && (double)Rnd.get(100) > leader.getCurrentHp() / leader.getMaxHp() * 100.0) {
            if (skill.getTargetType() == TargetType.SELF) {
               return false;
            }

            if (!this.checkSkillCastConditions(skill)) {
               return false;
            }

            if (!Util.checkIfInRange((int)((double)skill.getCastRange() + leader.getColRadius()), npc, leader, false)
               && !this.isParty(skill)
               && !npc.isMovementDisabled()) {
               this.moveToPawn(leader, (int)((double)skill.getCastRange() + leader.getColRadius()));
               return true;
            }

            if (GeoEngine.canSeeTarget(npc, leader, false)) {
               this.clientStopMoving(null);
               npc.setTarget(leader);
               this.clientStopMoving(null);
               npc.doCast(skill);
               return true;
            }
         }
      }

      if ((double)Rnd.get(100) < (100.0 - percentage) / 3.0) {
         if (!this.checkSkillCastConditions(skill)) {
            return false;
         } else {
            this.clientStopMoving(null);
            npc.setTarget(npc);
            npc.doCast(skill);
            return true;
         }
      } else if (!this.checkSkillCastConditions(skill)) {
         return false;
      } else {
         if (skill.getTargetType() == TargetType.ONE) {
            for(Creature obj : World.getInstance().getAroundCharacters(npc, (int)((double)skill.getCastRange() + npc.getColRadius()), 200)) {
               if (obj instanceof Attackable && !obj.isDead()) {
                  Attackable targets = (Attackable)obj;
                  if (npc.getFaction().isNone() || npc.isInFaction(targets)) {
                     percentage = targets.getCurrentHp() / targets.getMaxHp() * 100.0;
                     if ((double)Rnd.get(100) < (100.0 - percentage) / 10.0 && GeoEngine.canSeeTarget(npc, targets, false)) {
                        this.clientStopMoving(null);
                        npc.setTarget(obj);
                        npc.doCast(skill);
                        return true;
                     }
                  }
               }
            }
         }

         if (this.isParty(skill)) {
            this.clientStopMoving(null);
            npc.doCast(skill);
            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean defaultThinkBuff(int rateSelf) {
      return this.defaultThinkBuff(rateSelf, 0);
   }

   protected boolean defaultThinkBuff(int rateSelf, int rateFriends) {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return false;
      } else {
         if (Rnd.chance(rateSelf) && this._lastBuffTick + 30 < GameTimeController.getInstance().getGameTicks()) {
            double actorHp = actor.getCurrentHpPercents();
            Skill[] skills = actorHp < 50.0 ? this.selectUsableSkills(actor, 0.0, this._healSkills) : this.selectUsableSkills(actor, 0.0, this._buffSkills);
            if (skills == null || skills.length == 0) {
               return false;
            }

            Skill skill = skills[Rnd.get(skills.length)];
            if (this.cast(skill)) {
               return true;
            }

            this.setBuffTicks(GameTimeController.getInstance().getGameTicks());
         }

         if (Rnd.chance(rateFriends) && this._lastBuffTick + 30 < GameTimeController.getInstance().getGameTicks()) {
            for(Npc npc : this.activeFactionTargets(actor)) {
               double targetHp = npc.getCurrentHpPercents();
               Skill[] skills = targetHp < 50.0
                  ? this.selectUsableSkills(actor, 0.0, this._healSkills)
                  : this.selectUsableSkills(actor, 0.0, this._buffSkills);
               if (skills != null && skills.length != 0) {
                  Skill skill = skills[Rnd.get(skills.length)];
                  if (this.cast(skill)) {
                     return true;
                  }
               }
            }

            this.setBuffTicks(GameTimeController.getInstance().getGameTicks());
         }

         return false;
      }
   }

   public void setBuffTicks(int value) {
      this._lastBuffTick = value;
   }

   public Attackable getActiveChar() {
      return (Attackable)this._actor;
   }

   protected int getRatePHYS() {
      return 100;
   }

   protected int getRateDOT() {
      return 0;
   }

   protected int getRateDEBUFF() {
      return 0;
   }

   protected int getRateDAM() {
      return 0;
   }

   protected int getRateSTUN() {
      return 0;
   }

   protected int getRateBUFF() {
      return 0;
   }

   protected int getRateHEAL() {
      return 0;
   }

   protected int getRateSuicide() {
      return 0;
   }

   protected int getRateRes() {
      return 0;
   }

   protected int getRateDodge() {
      return 0;
   }

   protected void returnHome() {
      this.returnHome(true, Config.ALWAYS_TELEPORT_HOME);
   }

   protected void teleportHome() {
      this.returnHome(true, true);
   }

   protected void returnHome(boolean clearAggro, boolean teleport) {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead() && !actor.isMovementDisabled()) {
         if (actor.isMinion()) {
            MonsterInstance leader = ((MinionInstance)actor).getLeader();
            if (leader != null) {
               if (leader.getSpawnedLoc() == null) {
                  actor.deleteMe();
               } else {
                  actor.decayMe();
               }

               return;
            }
         }

         Location sloc = actor.getSpawnedLoc();
         actor.stopMove(null);
         if (clearAggro) {
            actor.clearAggroList();
         }

         this._attackTimeout = Integer.MAX_VALUE;
         this.setAttackTarget(null);
         this.changeIntention(CtrlIntention.ACTIVE, null, null);
         if (teleport) {
            actor.broadcastPacket(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
            actor.teleToLocation(sloc.getX(), sloc.getY(), GeoEngine.getHeight(sloc, actor.getGeoIndex()), true);
         } else {
            if (!clearAggro) {
               actor.setRunning();
            } else {
               actor.setWalking();
            }

            actor.getAI().setIntention(CtrlIntention.MOVING, sloc);
         }

         if (actor.getTemplate().getParameter("isDestructionBoss", false)) {
            actor.setCurrentHp(actor.getMaxHp());
            actor.setCurrentMp(actor.getMaxMp());
         }
      }
   }

   protected long getAttackTimeout() {
      return (long)this._attackTimeout;
   }

   protected static Skill selectTopSkillByDamage(Creature actor, Creature target, double distance, Skill[] skills) {
      if (skills != null && skills.length != 0) {
         if (skills.length == 1) {
            return skills[0];
         } else {
            Skill oneTargetSkill = null;

            for(Skill skill : skills) {
               if (skill.oneTarget()
                  && (
                     oneTargetSkill == null
                        || (double)skill.getCastRange() >= distance
                           && distance / (double)oneTargetSkill.getCastRange() < distance / (double)skill.getCastRange()
                  )) {
                  oneTargetSkill = skill;
               }
            }

            RndSelector<Skill> rnd = new RndSelector<>(skills.length);

            for(Skill skill : skills) {
               if (!skill.oneTarget()) {
                  double weight = skill.getSimpleDamage(actor, target) / 10.0 + distance / (double)skill.getCastRange() * 100.0;
                  if (weight < 1.0) {
                     weight = 1.0;
                  }

                  rnd.add(skill, (int)weight);
               }
            }

            Skill aoeSkill = rnd.select();
            if (aoeSkill == null) {
               return oneTargetSkill;
            } else if (oneTargetSkill == null) {
               return aoeSkill;
            } else {
               return Rnd.chance(90) ? oneTargetSkill : aoeSkill;
            }
         }
      } else {
         return null;
      }
   }

   protected static Skill selectTopSkillByDebuff(Creature actor, Creature target, double distance, Skill[] skills) {
      if (skills != null && skills.length != 0) {
         if (skills.length == 1) {
            return skills[0];
         } else {
            RndSelector<Skill> rnd = new RndSelector<>(skills.length);

            for(Skill skill : skills) {
               if (target.getFirstEffect(skill.getId()) == null) {
                  double weight;
                  if ((weight = 100.0 * (double)skill.getAOECastRange() / distance) <= 0.0) {
                     weight = 1.0;
                  }

                  rnd.add(skill, (int)weight);
               }
            }

            return rnd.select();
         }
      } else {
         return null;
      }
   }

   protected static Skill selectTopSkillByBuff(Creature target, Skill[] skills) {
      if (skills != null && skills.length != 0) {
         if (skills.length == 1) {
            return skills[0];
         } else {
            RndSelector<Skill> rnd = new RndSelector<>(skills.length);

            for(Skill skill : skills) {
               if (target.getFirstEffect(skill.getId()) == null) {
                  double weight;
                  if ((weight = skill.getPower()) <= 0.0) {
                     weight = 1.0;
                  }

                  rnd.add(skill, (int)weight);
               }
            }

            return rnd.select();
         }
      } else {
         return null;
      }
   }

   protected static Skill selectTopSkillByHeal(Creature target, Skill[] skills) {
      if (skills != null && skills.length != 0) {
         double hpReduced = target.getMaxHp() - target.getCurrentHp();
         if (hpReduced < 1.0) {
            return null;
         } else if (skills.length == 1) {
            return skills[0];
         } else {
            RndSelector<Skill> rnd = new RndSelector<>(skills.length);

            for(Skill skill : skills) {
               double weight;
               if ((weight = Math.abs(skill.getPower() - hpReduced)) <= 0.0) {
                  weight = 1.0;
               }

               rnd.add(skill, (int)weight);
            }

            return rnd.select();
         }
      } else {
         return null;
      }
   }

   protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill[] skills) {
      if (skills != null && skills.length != 0 && target != null) {
         for(Skill sk : skills) {
            this.addDesiredSkill(skillMap, target, distance, sk);
         }
      }
   }

   protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill skill) {
      if (skill != null && target != null && this.canUseSkill(skill, target)) {
         int weight = (int)(-Math.abs((double)skill.getCastRange() - distance));
         if ((double)skill.getCastRange() >= distance) {
            weight += 1000000;
         } else if (skill.isNotTargetAoE() && skill.getTargetList(this.getActor(), false, target).length == 0) {
            return;
         }

         skillMap.put(skill, weight);
      }
   }

   protected void addDesiredHeal(Map<Skill, Integer> skillMap, Skill[] skills) {
      if (skills != null && skills.length != 0) {
         Attackable actor = this.getActiveChar();
         if (actor != null) {
            double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
            double hpPercent = actor.getCurrentHpPercents();
            if (hpReduced < 1.0) {
               return;
            }

            for(Skill sk : skills) {
               if (this.canUseSkill(sk, actor) && sk.getPower() <= hpReduced) {
                  int weight = (int)sk.getPower();
                  if (hpPercent < 50.0) {
                     weight += 1000000;
                  }

                  skillMap.put(sk, weight);
               }
            }
         }
      }
   }

   protected void addDesiredBuff(Map<Skill, Integer> skillMap, Skill[] skills) {
      if (skills != null && skills.length != 0) {
         Attackable actor = this.getActiveChar();
         if (actor != null) {
            for(Skill sk : skills) {
               if (this.canUseSkill(sk, actor)) {
                  skillMap.put(sk, 1000000);
               }
            }
         }
      }
   }

   protected Skill selectTopSkill(Map<Skill, Integer> skillMap) {
      if (skillMap != null && !skillMap.isEmpty()) {
         int topWeight = Integer.MIN_VALUE;

         for(Skill next : skillMap.keySet()) {
            int nWeight;
            if ((nWeight = skillMap.get(next)) > topWeight) {
               topWeight = nWeight;
            }
         }

         if (topWeight == Integer.MIN_VALUE) {
            return null;
         } else {
            Skill[] skills = new Skill[skillMap.size()];
            int nWeight = 0;

            for(Entry<Skill, Integer> e : skillMap.entrySet()) {
               if (e.getValue() >= topWeight) {
                  skills[nWeight++] = e.getKey();
               }
            }

            return skills[Rnd.get(nWeight)];
         }
      } else {
         return null;
      }
   }

   protected boolean canUseSkill(Skill sk, Creature target) {
      return this.canUseSkill(sk, target, 0.0);
   }

   protected boolean canUseSkill(Skill skill, Creature target, double distance) {
      Attackable actor = this.getActiveChar();
      if (skill == null) {
         return false;
      } else if (skill.getTargetType() == TargetType.SELF && target != actor) {
         return false;
      } else {
         int castRange = skill.getCastRange();
         if (castRange <= 200 && distance > 200.0) {
            return false;
         } else if (actor.isSkillDisabled(skill)) {
            return false;
         } else {
            return target.getFirstEffect(skill.getId()) == null;
         }
      }
   }

   protected boolean maybeMoveToHome() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return false;
      } else {
         if (actor.isMinion()) {
            MonsterInstance master = ((MinionInstance)actor).getLeader();
            if (master != null && master.isAlikeDead()) {
               if (master.getSpawnedLoc() == null) {
                  actor.deleteMe();
               } else {
                  actor.decayMe();
               }

               return true;
            }
         }

         boolean isTimeOut = this._waitTimeOut == 0;
         if (!Rnd.chance(isTimeOut ? 100 : (this._waitTimeOutTime > 0L ? 50 : 1))) {
            return false;
         } else {
            Location sloc = actor.getSpawnedLoc();
            if (sloc == null) {
               return false;
            } else {
               boolean isInRange = actor.isInRangeZ(sloc, (long)Config.MAX_DRIFT_RANGE);
               if (this._waitTimeOutTime > 0L) {
                  if (this._waitTimeOutTime < System.currentTimeMillis()
                     && !actor.isInsideRadius(sloc.getX(), sloc.getY(), sloc.getZ(), Config.MAX_DRIFT_RANGE * 2, true, false)) {
                     this._waitTimeOutTime = 0L;
                     this.teleportHome();
                     return true;
                  } else {
                     if (actor.isInsideRadius(sloc.getX(), sloc.getY(), sloc.getZ(), Config.MAX_DRIFT_RANGE, true, false)) {
                        this._waitTimeOutTime = 0L;
                     } else {
                        Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE / 2, false);
                        if (pos != null) {
                           actor.setWalking();
                           this.moveTo(pos);
                        }
                     }

                     return true;
                  }
               } else {
                  if (actor.getDistance(sloc.getX(), sloc.getY()) > 2000.0 && !isInRange && !isTimeOut) {
                     this.teleportHome();
                  } else {
                     Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE, false);
                     if (pos != null) {
                        actor.setWalking();
                        this.moveTo(pos);
                        if (isTimeOut) {
                           this._waitTimeOut = -1;
                           this._waitTimeOutTime = System.currentTimeMillis() + this._waitTimeOutLimit;
                        }
                     } else {
                        this.teleportHome();
                     }
                  }

                  return true;
               }
            }
         }
      }
   }

   public void setGlobalAggro(int value) {
      this._globalAggro = value;
   }

   public void setNotifyFriend(boolean value) {
      this._canNotifyFriend = value;
   }

   public boolean isGlobalAI() {
      return this.getActiveChar().isGlobalAI();
   }

   @Override
   public void enableAI() {
      if (this.getActiveChar().getAI().getIntention() == CtrlIntention.IDLE) {
         this.getActiveChar().getAI().changeIntention(CtrlIntention.ACTIVE, null, null);
      }
   }

   public void checkUD(Creature attacker) {
      if (this._defRate != 0 && this.getActiveChar() != null && attacker != null) {
         if (this.getActiveChar().getDistance(attacker) > 150.0) {
            if ((this._defFlag == 0 || this._defFlag == 2) && Rnd.chance(this._defRate) && this.canUseSkill(this._defSkill, this.getActiveChar(), 0.0)) {
               this._defFlag = 1;
            }
         } else if (this._defFlag == 2 || this._defFlag == 1) {
            this.getActiveChar().stopSkillEffects(this._defSkill.getId());
            this._defFlag = 0;
         }
      }
   }

   private boolean applyUD() {
      if (this._defRate == 0 || this._defFlag == 0 || this.getActiveChar() == null) {
         return false;
      } else if (this._defFlag == 1) {
         this.getActiveChar().stopMove(null);
         this._defSkill.getEffects(this.getActiveChar(), this.getActiveChar(), false);
         this._defFlag = 2;
         return true;
      } else {
         return false;
      }
   }

   protected class madnessTask implements Runnable {
      @Override
      public void run() {
         Attackable actor = DefaultAI.this.getActiveChar();
         if (actor != null) {
            actor.stopConfused();
         }

         DefaultAI.this._madnessTask = null;
      }
   }
}
