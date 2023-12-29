package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;

public final class AnswerJoinPartyRoom extends GameClientPacket {
   private int _response;

   @Override
   protected void readImpl() {
      if (this._buf.hasRemaining()) {
         this._response = this.readD();
      } else {
         this._response = 0;
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (player != null) {
         Player partner = player.getActiveRequester();
         if (partner == null) {
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.setActiveRequester(null);
         } else if (this._response == 0) {
            player.setActiveRequester(null);
            partner.sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE);
         } else if (player.getMatchingRoom() != null) {
            player.setActiveRequester(null);
         } else {
            if (this._response == 1 && !partner.isRequestExpired()) {
               MatchingRoom room = partner.getMatchingRoom();
               if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING) {
                  return;
               }

               room.addMember(player);
            } else {
               partner.sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE);
            }

            player.setActiveRequester(null);
            partner.onTransactionResponse();
         }
      }
   }
}
