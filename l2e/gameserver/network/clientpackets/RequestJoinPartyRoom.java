package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public final class RequestJoinPartyRoom extends GameClientPacket {
   private int _roomId;
   private int _locations;
   private int _level;

   @Override
   protected void readImpl() {
      this._roomId = this.readD();
      this._locations = this.readD();
      this._level = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.getMatchingRoom() == null) {
            if (this._roomId > 0) {
               MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.PARTY_MATCHING, this._roomId);
               if (room == null) {
                  return;
               }

               room.addMember(player);
            } else {
               for(MatchingRoom room : MatchingRoomManager.getInstance()
                  .getMatchingRooms(MatchingRoom.PARTY_MATCHING, this._locations, this._level == 1, player)) {
                  if (room.addMember(player)) {
                     break;
                  }
               }
            }
         }
      }
   }
}
