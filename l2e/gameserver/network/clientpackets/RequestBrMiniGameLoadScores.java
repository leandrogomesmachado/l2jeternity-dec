package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExBrMiniGameLoadScores;

public class RequestBrMiniGameLoadScores extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.sendPacket(new ExBrMiniGameLoadScores(player));
      }
   }
}
