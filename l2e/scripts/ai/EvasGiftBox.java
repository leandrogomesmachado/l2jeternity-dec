package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class EvasGiftBox extends Fighter {
   public EvasGiftBox(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      if (killer != null) {
         Player player = killer.getActingPlayer();
         if (player != null && (player.getFirstEffect(1073) != null || player.getFirstEffect(3141) != null || player.getFirstEffect(3252) != null)) {
            actor.dropItem(player, Rnd.chance(50) ? 9692 : 9693, 1L);
         }
      }

      super.onEvtDead(killer);
   }
}
