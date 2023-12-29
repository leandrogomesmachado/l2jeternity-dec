package l2e.gameserver.model.actor.tasks.player;

import java.util.List;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import l2e.gameserver.model.skills.Skill;

public class AutoPhysicalFarmTask implements Runnable {
   private final Player _player;
   private Attackable _committedTarget = null;
   private Player _committedOwner = null;
   private long _extraDelay = 0L;

   public AutoPhysicalFarmTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      this.tryAttack();
   }

   private void tryAttack() {
      boolean canAttack = this.selectRandomTarget();
      if (canAttack && this._committedTarget != null) {
         this.physicalAttack();
      }

      this.tryUseSpell(canAttack);
      if (canAttack && this._committedTarget != null) {
         this.physicalAttack();
      }
   }

   private void tryUseSpell(boolean selected) {
      if (!this._player.isCastingNow()) {
         if (selected) {
            Skill chanceSkill = this._player.getFarmSystem().nextChanceSkill(this._committedTarget, this._extraDelay);
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
               if (selected) {
                  Skill attackSkill = this._player.getFarmSystem().nextAttackSkill(this._committedTarget, this._extraDelay);
                  if (attackSkill != null) {
                     this.useMagicSkill(attackSkill, false);
                     return;
                  }
               }
            }
         }
      }
   }

   private boolean canUseSweep() {
      Skill sweeper = this._player.getKnownSkill(42);
      Skill masSweeper = this._player.getKnownSkill(444);
      if (sweeper == null && masSweeper == null) {
         return false;
      } else if (this.canBeSweepedByMe()) {
         this.useMagicSkill(masSweeper != null ? masSweeper : sweeper, false);
         return true;
      } else {
         return false;
      }
   }

   private boolean canBeSweepedByMe() {
      return this._committedTarget != null && this._committedTarget.isDead() && this._committedTarget.isSweepActive();
   }

   private void physicalAttack() {
      if (this._committedTarget.isAutoAttackable(this._player) && !this._committedTarget.isAlikeDead()) {
         this._player.setTarget(this._committedTarget);
         if (GeoEngine.canSeeTarget(this._player, this._committedTarget, false)) {
            this._player.getAI().setIntention(CtrlIntention.ATTACK, this._committedTarget);
            return;
         }

         this._player.getAI().setIntention(CtrlIntention.MOVING, this._committedTarget.getLocation());
      }
   }

   private boolean selectRandomTarget() {
      if (this._player.isCastingNow()) {
         return false;
      } else {
         if (this._committedTarget != null) {
            if (this._committedTarget.isDead() && this._committedTarget.isSpoil() && this.canUseSweep()) {
               return false;
            }

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
               Location loc = Location.findPointToStay(this._committedOwner.getLocation(), 100, this._player.getGeoIndex(), false);
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
               if (this._player.getFarmSystem().isExtraDelaySkill()) {
                  this._extraDelay = System.currentTimeMillis() + FarmSettings.SKILLS_EXTRA_DELAY;
               }

               this._player.getFarmSystem().tryUseMagic(skill, forSelf);
            }
         }
      }
   }
}
