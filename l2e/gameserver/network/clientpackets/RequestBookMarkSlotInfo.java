package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExGetBookMarkInfo;

public final class RequestBookMarkSlotInfo extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      player.sendPacket(new ExGetBookMarkInfo(player));
   }
}
