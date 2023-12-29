package l2e.scripts.ai.pagan_temple;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class ZombieGatekeeper extends Fighter {
   public ZombieGatekeeper(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
   }

   @Override
   public boolean checkAggression(Creature target) {
      Attackable actor = this.getActiveChar();
      if (target == null || actor.isDead() || actor.getAI().getIntention() != CtrlIntention.ACTIVE) {
         return false;
      } else if (target.isAlikeDead()
         || !target.isPlayer()
         || !target.isInRangeZ(actor.getSpawnedLoc(), (long)actor.getAggroRange())
         || !GeoEngine.canSeeTarget(actor, target, false)) {
         return false;
      } else if (target.getInventory().getItemByItemId(8067) == null && target.getInventory().getItemByItemId(8064) == null) {
         if (this.getIntention() != CtrlIntention.ATTACK) {
            actor.addDamageHate(target, 0, 1);
            actor.getAI().setIntention(CtrlIntention.ATTACK, target);
         }

         return true;
      } else {
         return false;
      }
   }
}
