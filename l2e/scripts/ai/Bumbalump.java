package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.scripts.quests._625_TheFinestIngredientsPart2;

public class Bumbalump extends Fighter {
   public Bumbalump(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      ServerVariables.set(_625_TheFinestIngredientsPart2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
      if (_625_TheFinestIngredientsPart2._npc != null) {
         _625_TheFinestIngredientsPart2._npc = null;
      }

      super.onEvtDead(killer);
   }
}
