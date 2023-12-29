package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.CCMatchingRoom;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ListPartyWaiting;

public final class RequestListPartyWaiting extends GameClientPacket {
   private int _page;
   private int _region;
   private int _allLevels;

   @Override
   protected void readImpl() {
      this._page = this.readD();
      this._region = this.readD();
      this._allLevels = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Party party = player.getParty();
         CommandChannel channel = party != null ? party.getCommandChannel() : null;
         if (channel != null && channel.getLeader() == player) {
            if (channel.getMatchingRoom() == null) {
               CCMatchingRoom room = new CCMatchingRoom(player, 1, player.getLevel(), 50, party.getLootDistribution(), player.getName());
               channel.setMatchingRoom(room);

               for(Party ccParty : player.getParty().getCommandChannel().getPartys()) {
                  for(Player ccMember : ccParty.getMembers()) {
                     if (ccParty.isLeader(ccMember) && ccParty.getLeader() != player) {
                        room.addMember(ccMember);
                        ccMember.setMatchingRoomWindowOpened(true);
                        ccMember.sendPacket(room.infoRoomPacket(), room.membersPacket(ccMember));
                     }
                  }
               }
            }
         } else if (channel != null && !channel.getPartys().contains(party)) {
            player.sendPacket(SystemMessageId.THE_COMMAND_CHANNEL_AFFILIATED_PARTY_S_PARTY_MEMBER_CANNOT_USE_THE_MATCHING_SCREEN);
         } else if (party != null && !party.isLeader(player)) {
            MatchingRoom room = player.getMatchingRoom();
            if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING) {
               player.setMatchingRoomWindowOpened(true);
               player.sendPacket(room.infoRoomPacket(), room.membersPacket(player));
            } else {
               player.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
            }
         } else {
            if (party == null) {
               MatchingRoomManager.getInstance().addToWaitingList(player);
            }

            player.sendPacket(new ListPartyWaiting(this._region, this._allLevels == 1, this._page, player));
         }
      }
   }
}
