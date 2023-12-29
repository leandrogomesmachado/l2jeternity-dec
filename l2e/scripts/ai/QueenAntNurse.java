package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Priest;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.scripts.ai.grandboss.QueenAnt;

public class QueenAntNurse extends Priest {
   public QueenAntNurse(Attackable actor) {
      super(actor);
      this.MAX_PURSUE_RANGE = 10000;
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         Creature target = this.getTopDesireTarget();
         if (target == null) {
            return false;
         } else {
            double distance = actor.getDistance(target) - target.getColRadius() - actor.getColRadius();
            if (distance > 200.0) {
               this.moveOrTeleportToLocation(Location.findFrontPosition(target, actor, 100, 150), distance > 2000.0);
               return false;
            } else {
               return !target.isCurrentHpFull() ? this.createNewTask() : false;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   protected boolean createNewTask() {
      Attackable actor = this.getActiveChar();
      Creature target = this.getTopDesireTarget();
      if (actor == null || actor.isDead() || target == null) {
         return false;
      } else if (!target.isCurrentHpFull()) {
         Skill skill = this._healSkills[Rnd.get(this._healSkills.length)];
         double distance = actor.getDistance(target);
         if ((double)skill.getAOECastRange() < distance) {
            this.moveOrTeleportToLocation(
               Location.findFrontPosition(target, actor, skill.getAOECastRange() - 30, skill.getAOECastRange() - 10), distance > 2000.0
            );
         } else {
            actor.setTarget(target);
            actor.useMagic(skill);
         }

         return true;
      } else {
         return false;
      }
   }

   private void moveOrTeleportToLocation(Location loc, boolean teleport) {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         actor.setRunning();
         actor.getAI().setIntention(CtrlIntention.MOVING, loc);
         if (teleport) {
            if (!actor.isMovementDisabled() && actor.isMoving()) {
               return;
            }

            actor.broadcastPacket(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
            actor.teleToLocation(loc.getX(), loc.getY(), GeoEngine.getHeight(loc, actor.getGeoIndex()), true);
         }
      }
   }

   private Npc getTopDesireTarget() {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         Npc queenAnt = ((MinionInstance)actor).getLeader();
         if (queenAnt == null) {
            return null;
         } else if (queenAnt.isDead()) {
            return null;
         } else {
            Npc larva = QueenAnt.getLarva();
            return larva != null && larva.getCurrentHpPercents() < 5.0 ? larva : queenAnt;
         }
      } else {
         return null;
      }
   }

   @Override
   protected void onIntentionAttack(Creature target) {
   }
}
