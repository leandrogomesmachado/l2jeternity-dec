package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class RequestJoinMpccRoom extends GameClientPacket {
   private int _roomId;

   @Override
   protected void readImpl() {
      this._roomId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.getMatchingRoom() == null) {
            MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.CC_MATCHING, this._roomId);
            if (room != null) {
               room.addMember(player);
            }
         }
      }
   }
}
