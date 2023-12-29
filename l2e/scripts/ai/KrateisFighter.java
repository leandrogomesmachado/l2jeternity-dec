package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class KrateisFighter extends Fighter {
   public KrateisFighter(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      super.onEvtDead(killer);
      Player player = killer.getActingPlayer();
      if (player != null) {
         KrateisCubeManager.getInstance().addKill(player);
      }
   }
}
