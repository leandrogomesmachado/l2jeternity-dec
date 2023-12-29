package l2e.gameserver.model.actor.tasks.player;

import java.util.List;
import l2e.commons.util.PositionUtils;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import l2e.gameserver.model.skills.Skill;

public class AutoMagicFarmTask implements Runnable {
   private final Player _player;
   private Attackable _committedTarget = null;
   private Player _committedOwner = null;

   public AutoMagicFarmTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      this.tryUseSpell();
   }

   private void tryUseSpell() {
      if (!this._player.isCastingNow()) {
         boolean canAttack = this.selectRandomTarget();
         if (canAttack) {
            Skill chanceSkill = this._player.getFarmSystem().nextChanceSkill(this._committedTarget, 0L);
            if (chanceSkill != null) {
               this.useMagicSkill(chanceSkill, false);
               return;
            }
         }

         Skill lowLifeSkill = this._player.getFarmSystem().nextHealSkill(this._committedTarget, null);
         if (lowLifeSkill != null) {
            this.useMagicSkill(lowLifeSkill, !lowLifeSkill.isOffensive());
         } else {
            Skill selfSkills = this._player.getFarmSystem().nextSelfSkill(null);
            if (selfSkills != null) {
               this.useMagicSkill(selfSkills, true);
            } else {
               if (canAttack) {
                  Skill attackSkill = this._player.getFarmSystem().nextAttackSkill(this._committedTarget, 0L);
                  if (attackSkill != null) {
                     this.useMagicSkill(attackSkill, false);
                     return;
                  }
               }
            }
         }
      }
   }

   private boolean selectRandomTarget() {
      if (this._player.isCastingNow()) {
         return false;
      } else {
         if (this._committedTarget != null) {
            if (!this._committedTarget.isDead() && this._committedTarget.isVisible() && GeoEngine.canSeeTarget(this._player, this._committedTarget, false)) {
               if (this._player.getTarget() != this._committedTarget) {
                  this._player.setTarget(this._committedTarget);
               }

               return true;
            }

            this._committedTarget = null;
            this._player.setTarget(null);
            if (this._player.getFarmSystem().isNeedToReturn()) {
               try {
                  Thread.sleep(FarmSettings.KEEP_LOCATION_DELAY);
               } catch (InterruptedException var3) {
               }

               return false;
            }
         }

         if (this._player.getFarmSystem().isLeaderAssist()) {
            if (this._player.getParty() == null) {
               this._committedOwner = null;
               this._player.getFarmSystem().setLeaderAssist(false, false);
            } else {
               this._committedOwner = this._player.getParty().getLeader();
            }
         }

         if (this._committedOwner != null) {
            this._committedTarget = this._player.getFarmSystem().getLeaderTarget(this._committedOwner);
            if (this._committedTarget != null) {
               return true;
            } else if (Math.sqrt(this._player.getDistanceSq(this._committedOwner)) > 200.0) {
               Location loc = Location.findPointToStay(this._committedOwner.getLocation(), 200, this._player.getGeoIndex(), false);
               if (loc != null) {
                  this._player.getAI().setIntention(CtrlIntention.MOVING, loc);
               }

               try {
                  Thread.sleep(1500L);
               } catch (InterruptedException var4) {
               }

               return false;
            } else {
               return false;
            }
         } else {
            List<Attackable> targets = this._player
               .getFarmSystem()
               .getAroundNpc(this._player, creature -> GeoEngine.canSeeTarget(this._player, creature, false) && !creature.isDead());
            if (targets.isEmpty()) {
               if (this._player.getFarmSystem().isNeedToReturn()) {
                  try {
                     Thread.sleep(FarmSettings.KEEP_LOCATION_DELAY);
                  } catch (InterruptedException var5) {
                  }

                  return false;
               } else {
                  return false;
               }
            } else {
               Attackable closestTarget = targets.stream()
                  .min((o1, o2) -> Integer.compare((int)Math.sqrt(this._player.getDistanceSq(o1)), (int)Math.sqrt(this._player.getDistanceSq(o2))))
                  .get();
               targets.clear();
               this._committedTarget = closestTarget;
               this._player.setTarget(closestTarget);
               return true;
            }
         }
      }
   }

   private void useMagicSkill(Skill skill, boolean forSelf) {
      if (skill != null) {
         if (!skill.isToggle() || !this._player.isMounted()) {
            if (!this._player.isOutOfControl()) {
               if (this._player.getFarmSystem().isRunTargetCloseUp()
                  && !forSelf
                  && this._committedTarget != null
                  && Math.sqrt(this._player.getDistanceSq(this._committedTarget)) < (double)FarmSettings.RUN_CLOSE_UP_DISTANCE) {
                  double angle = Math.toRadians(PositionUtils.calculateAngleFrom(this._committedTarget, this._player));
                  int oldX = this._player.getX();
                  int oldY = this._player.getY();
                  int x = oldX + (int)(500.0 * Math.cos(angle));
                  int y = oldY + (int)(500.0 * Math.sin(angle));
                  Location loc = Location.findPointToStay(new Location(x, y, this._player.getZ()), 100, this._player.getGeoIndex(), false);
                  if (loc != null) {
                     this._player.getAI().setIntention(CtrlIntention.MOVING, loc);

                     try {
                        Thread.sleep(FarmSettings.RUN_CLOSE_UP_DELAY);
                     } catch (InterruptedException var11) {
                     }
                  }
               }

               this._player.getFarmSystem().tryUseMagic(skill, forSelf);
            }
         }
      }
   }
}
