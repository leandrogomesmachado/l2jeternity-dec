package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.actor.Player;

public final class RequestExitPartyMatchingWaitingRoom extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         MatchingRoomManager.getInstance().removeFromWaitingList(activeChar);
      }
   }
}
