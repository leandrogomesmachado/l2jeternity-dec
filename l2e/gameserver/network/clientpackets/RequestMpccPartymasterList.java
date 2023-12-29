package l2e.gameserver.network.clientpackets;

import java.util.HashSet;
import java.util.Set;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.serverpackets.ExMpccPartymasterList;

public class RequestMpccPartymasterList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         MatchingRoom room = player.getMatchingRoom();
         if (room != null && room.getType() == MatchingRoom.CC_MATCHING) {
            Set<String> set = new HashSet<>();

            for(Player member : room.getPlayers()) {
               if (member.getParty() != null) {
                  set.add(member.getParty().getLeader().getName());
               }
            }

            player.sendPacket(new ExMpccPartymasterList(set));
         }
      }
   }
}
