package l2e.scripts.ai.hellbound;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.quest.Quest;

public class Darnel extends Fighter {
   public Darnel(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Quest.addSpawn(32279, new Location(152761, 145950, -12588, 0), killer.getReflectionId());
      super.onEvtDead(killer);
   }
}
