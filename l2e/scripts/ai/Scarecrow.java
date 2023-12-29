package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Corpse;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class Scarecrow extends Corpse {
   public Scarecrow(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
      actor.setIsInvul(true);
   }

   @Override
   protected boolean checkAggression(Creature target) {
      return false;
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }
}
