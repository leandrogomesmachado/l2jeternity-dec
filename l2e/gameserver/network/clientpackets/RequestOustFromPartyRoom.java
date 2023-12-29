package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;

public final class RequestOustFromPartyRoom extends GameClientPacket {
   private int _charid;

   @Override
   protected void readImpl() {
      this._charid = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      MatchingRoom room = player.getMatchingRoom();
      if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING) {
         if (room.getLeader() == player) {
            Player member = World.getInstance().getPlayer(this._charid);
            if (member != null) {
               int type = room.getMemberType(member);
               if (type != MatchingRoom.ROOM_MASTER) {
                  if (type == MatchingRoom.PARTY_MEMBER) {
                     player.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
                  } else {
                     room.removeMember(member, true);
                  }
               }
            }
         }
      }
   }
}
