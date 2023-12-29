package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;

public class RequestStartShowCrataeCubeRank extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isInFightEvent()) {
            FightEventPlayer fPlayer = player.getFightEvent().getFightEventPlayer(player);
            fPlayer.setShowRank(true);
         }
      }
   }
}
