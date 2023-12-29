package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class RequestWithdrawMpccRoom extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         MatchingRoom room = player.getMatchingRoom();
         if (room != null && room.getType() == MatchingRoom.CC_MATCHING) {
            if (room.getLeader() != player) {
               room.removeMember(player, false);
            }
         }
      }
   }
}
