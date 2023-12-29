package l2e.gameserver.model.actor.tasks.player;

import java.util.List;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class AutoHealFarmTask implements Runnable {
   private final Player _player;
   private Attackable _committedTarget = null;
   private Player _committedOwner = null;

   public AutoHealFarmTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      this.tryAttack();
   }

   private void tryAttack() {
      boolean canAttack = this.selectRandomTarget();
      if (canAttack) {
         this.physicalAttack();
      }

      this.tryUseSpell(canAttack);
      if (canAttack) {
         this.physicalAttack();
      }
   }

   private void tryUseSpell(boolean selected) {
      if (!this._player.isCastingNow()) {
         if (selected) {
            Skill chanceSkill = this._player.getFarmSystem().nextChanceSkill(this._committedTarget, 0L);
            if (chanceSkill != null) {
               this.useMagicSkill(chanceSkill, false);
               return;
            }
         }

         Skill lowLifeSkill = this._player.getFarmSystem().nextHealSkill(this._committedTarget, this._committedOwner);
         if (lowLifeSkill != null) {
            this.useMagicSkill(lowLifeSkill, lowLifeSkill.getTargetType() == TargetType.SELF);
         } else {
            Skill selfSkills = this._player.getFarmSystem().nextSelfSkill(this._committedOwner);
            if (selfSkills != null) {
               this.useMagicSkill(selfSkills, selfSkills.getTargetType() == TargetType.SELF);
            } else {
               if (selected) {
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

   private void physicalAttack() {
      if (this._committedOwner != null && Math.sqrt(this._player.getDistanceSq(this._committedOwner)) > 200.0) {
         this._player.setTarget(null);
         Location loc = Location.findPointToStay(this._committedOwner.getLocation(), 100, this._player.getGeoIndex(), false);
         if (loc != null) {
            this._player.getAI().setIntention(CtrlIntention.MOVING, loc);
         }
      } else if (!this._player.isCastingNow()) {
         if (this._committedTarget != null && (this._player.getFarmSystem().isAssistMonsterAttack() || !this._player.getFarmSystem().isLeaderAssist())) {
            this._player.setTarget(this._committedTarget);
            if (this._committedTarget.isAutoAttackable(this._player) && !this._committedTarget.isAlikeDead()) {
               if (GeoEngine.canSeeTarget(this._player, this._committedTarget, false)) {
                  this._player.getAI().setIntention(CtrlIntention.ATTACK, this._committedTarget);
               } else {
                  this._player.getAI().setIntention(CtrlIntention.MOVING, this._committedTarget.getLocation());
               }
            }
         }
      }
   }

   private boolean selectRandomTarget() {
      if (this._player.isCastingNow()) {
         return true;
      } else {
         if (this._committedTarget != null && (this._player.getFarmSystem().isAssistMonsterAttack() || !this._player.getFarmSystem().isLeaderAssist())) {
            if (!this._committedTarget.isDead() && this._committedTarget.isVisible() && GeoEngine.canSeeTarget(this._player, this._committedTarget, false)) {
               if (this._player.getTarget() != this._committedTarget) {
                  this._player.setTarget(this._committedTarget);
               }

               return true;
            }

            this._committedTarget = null;
            this._player.setTarget(null);
         }

         if (this._player.getFarmSystem().isLeaderAssist()) {
            if (this._player.getParty() == null) {
               this._committedOwner = null;
               this._player.getFarmSystem().setLeaderAssist(false, false);
            } else {
               this._committedOwner = this._player.getParty().getLeader();
            }
         }

         if (this._committedOwner != null && !this._committedOwner.isDead() && this._player.getFarmSystem().isAssistMonsterAttack()) {
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
               } catch (InterruptedException var3) {
               }

               return false;
            } else {
               return false;
            }
         } else if (this._player.getFarmSystem().isLeaderAssist()) {
            return true;
         } else {
            List<Attackable> targets = this._player
               .getFarmSystem()
               .getAroundNpc(this._player, creature -> GeoEngine.canSeeTarget(this._player, creature, false) && !creature.isDead());
            if (targets.isEmpty()) {
               return false;
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
               this._player.getFarmSystem().tryUseMagic(skill, forSelf);
            }
         }
      }
   }
}
