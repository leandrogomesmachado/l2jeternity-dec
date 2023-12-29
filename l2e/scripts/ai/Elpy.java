package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class Elpy extends Fighter {
   public Elpy(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null) {
         if (actor.getId() >= 18150 && actor.getId() <= 18157) {
            Location pos = Location.findPointToStay(actor, 40, 60, false);
            if (pos != null) {
               actor.setRunning();
               actor.getAI().setIntention(CtrlIntention.MOVING, new Location(pos.getX(), pos.getY(), pos.getZ()));
            }
         } else if (Rnd.chance(50)) {
            Location pos = Location.findPointToStay(actor, 150, 200, false);
            if (pos != null) {
               actor.setRunning();
               actor.getAI().setIntention(CtrlIntention.MOVING, new Location(pos.getX(), pos.getY(), pos.getZ()));
            }
         }
      }
   }

   @Override
   public boolean checkAggression(Creature target) {
      return false;
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }
}
