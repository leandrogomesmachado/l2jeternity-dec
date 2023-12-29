package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class RequestOustFromMpccRoom extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         MatchingRoom room = player.getMatchingRoom();
         if (room != null && room.getType() == MatchingRoom.CC_MATCHING) {
            if (room.getLeader() == player) {
               Player member = World.getInstance().getPlayer(this._objectId);
               if (member != null) {
                  if (member != room.getLeader()) {
                     room.removeMember(member, true);
                  }
               }
            }
         }
      }
   }
}
