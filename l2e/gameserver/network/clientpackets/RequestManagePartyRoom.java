package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.model.matching.PartyMatchingRoom;

public class RequestManagePartyRoom extends GameClientPacket {
   private int _lootDist;
   private int _maxMembers;
   private int _minLevel;
   private int _maxLevel;
   private int _roomId;
   private String _roomTitle;

   @Override
   protected void readImpl() {
      this._roomId = this.readD();
      this._maxMembers = this.readD();
      this._minLevel = this.readD();
      this._maxLevel = this.readD();
      this._lootDist = this.readD();
      this._roomTitle = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Party party = player.getParty();
         if (party == null || party.getLeader() == player) {
            MatchingRoom room = player.getMatchingRoom();
            if (room == null) {
               MatchingRoom var6 = new PartyMatchingRoom(player, this._minLevel, this._maxLevel, this._maxMembers, this._lootDist, this._roomTitle);
               if (party != null) {
                  for(Player member : party.getMembers()) {
                     if (member != null && member != player) {
                        var6.addMemberForce(member);
                     }
                  }
               }
            } else if (room.getId() == this._roomId && room.getType() == MatchingRoom.PARTY_MATCHING && room.getLeader() == player) {
               room.setMinLevel(this._minLevel);
               room.setMaxLevel(this._maxLevel);
               room.setMaxMemberSize(this._maxMembers);
               room.setTopic(this._roomTitle);
               room.setLootType(this._lootDist);
               room.broadCast(room.infoRoomPacket());
            }
         }
      }
   }
}
