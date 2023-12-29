package l2e.scripts.ai;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class Cabrio extends Fighter {
   public Cabrio(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      NpcUtils.spawnSingleNpc(31027, Location.findAroundPosition(this.getActiveChar(), 80, 120), 60000L);
      super.onEvtDead(killer);
   }
}
