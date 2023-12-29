package l2e.scripts.ai.isle_of_prayer;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class WaterDragonDetractor extends Fighter {
   public WaterDragonDetractor(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (killer != null) {
         Player player = killer.getActingPlayer();
         if (player != null) {
            Attackable actor = this.getActiveChar();
            actor.dropItem(player, 9689, 1L);
            if (Rnd.chance(10)) {
               actor.dropItem(player, 9595, 1L);
            }
         }
      }

      super.onEvtDead(killer);
   }
}
