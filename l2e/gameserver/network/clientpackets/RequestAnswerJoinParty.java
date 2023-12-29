package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExManagePartyRoomMember;
import l2e.gameserver.network.serverpackets.JoinParty;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinParty extends GameClientPacket {
   private int _response;

   @Override
   protected void readImpl() {
      this._response = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.isntAfk();
         Player requestor = player.getActiveRequester();
         if (requestor != null) {
            requestor.sendPacket(new JoinParty(this._response));
            if (this._response == 1) {
               if (requestor.isInParty() && requestor.getParty().getMemberCount() >= 9) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_FULL);
                  player.sendPacket(sm);
                  requestor.sendPacket(sm);
                  return;
               }

               player.joinParty(requestor.getParty());
               MatchingRoom requestorRoom = requestor.getMatchingRoom();
               if (requestorRoom != null) {
                  requestorRoom.addMember(player);
                  ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, requestorRoom, 1);

                  for(Player member : requestorRoom.getPlayers()) {
                     if (member != null) {
                        member.sendPacket(packet);
                     }
                  }

                  player.broadcastCharInfo();
               }
            } else if (this._response == -1) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUEST);
               sm.addPcName(player);
               requestor.sendPacket(sm);
               if (requestor.isInParty() && requestor.getParty().getMemberCount() == 1) {
                  requestor.getParty().removePartyMember(requestor, Party.messageType.None);
               }
            } else if (requestor.isInParty() && requestor.getParty().getMemberCount() == 1) {
               requestor.getParty().removePartyMember(requestor, Party.messageType.None);
            }

            if (requestor.isInParty()) {
               requestor.getParty().setPendingInvitation(false);
            }

            player.setActiveRequester(null);
            requestor.onTransactionResponse();
         }
      }
   }
}
