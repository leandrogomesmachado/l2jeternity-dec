package l2e.scripts.ai.kamaloka;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class KnightMontagnarFollower extends Fighter {
   public KnightMontagnarFollower(Attackable actor) {
      super(actor);
      actor.setIsInvul(true);
   }

   @Override
   protected void onEvtAggression(Creature attacker, int aggro) {
      if (aggro >= 1000000) {
         super.onEvtAggression(attacker, aggro);
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }
}
