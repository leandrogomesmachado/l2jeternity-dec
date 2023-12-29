package l2e.scripts.ai.gracia;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class SoulCoffin extends DefaultAI {
   public SoulCoffin(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }
}
