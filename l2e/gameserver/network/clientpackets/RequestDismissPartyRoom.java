package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class RequestDismissPartyRoom extends GameClientPacket {
   private int _roomId;

   @Override
   protected void readImpl() {
      this._roomId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         MatchingRoom room = activeChar.getMatchingRoom();
         if (room != null && room.getId() == this._roomId && room.getType() == MatchingRoom.PARTY_MATCHING) {
            if (room.getLeader() == activeChar) {
               room.disband();
            }
         }
      }
   }
}
