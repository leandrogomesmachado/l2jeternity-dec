package l2e.scripts.ai.gracia;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class DefeatedTroopsController extends Fighter {
   public DefeatedTroopsController(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean checkAggression(Creature target) {
      return false;
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }
}
