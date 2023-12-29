package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;

public class RequestManageMpccRoom extends GameClientPacket {
   private int _id;
   private int _memberSize;
   private int _minLevel;
   private int _maxLevel;
   private String _topic;

   @Override
   protected void readImpl() {
      this._id = this.readD();
      this._memberSize = this.readD();
      this._minLevel = this.readD();
      this._maxLevel = this.readD();
      this.readD();
      this._topic = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         MatchingRoom room = player.getMatchingRoom();
         if (room != null && room.getId() == this._id && room.getType() == MatchingRoom.CC_MATCHING) {
            if (room.getLeader() == player) {
               room.setTopic(this._topic);
               room.setMaxMemberSize(this._memberSize);
               room.setMinLevel(this._minLevel);
               room.setMaxLevel(this._maxLevel);
               room.broadCast(room.infoRoomPacket());
               player.sendPacket(SystemMessageId.THE_COMMAND_CHANNEL_MATCHING_ROOM_INFORMATION_WAS_EDITED);
            }
         }
      }
   }
}
