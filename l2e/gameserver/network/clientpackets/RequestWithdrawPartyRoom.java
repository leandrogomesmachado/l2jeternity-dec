package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public final class RequestWithdrawPartyRoom extends GameClientPacket {
   private int _roomId;

   @Override
   protected void readImpl() {
      this._roomId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         MatchingRoom room = player.getMatchingRoom();
         if (room != null && room.getId() == this._roomId && room.getType() == MatchingRoom.PARTY_MATCHING) {
            int type = room.getMemberType(player);
            if (type != MatchingRoom.ROOM_MASTER && type != MatchingRoom.PARTY_MEMBER) {
               room.removeMember(player, false);
            } else {
               player.setMatchingRoomWindowOpened(false);
            }
         }
      }
   }
}
