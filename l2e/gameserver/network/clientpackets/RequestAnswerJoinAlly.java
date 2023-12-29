package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestAnswerJoinAlly extends GameClientPacket {
   private int _response;

   @Override
   protected void readImpl() {
      this._response = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.isntAfk();
         Player requestor = activeChar.getRequest().getPartner();
         if (requestor != null) {
            if (this._response == 0) {
               activeChar.sendPacket(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION);
               requestor.sendPacket(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION);
            } else {
               if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly)) {
                  return;
               }

               Clan clan = requestor.getClan();
               if (clan.checkAllyJoinCondition(requestor, activeChar)) {
                  requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
                  activeChar.sendPacket(SystemMessageId.YOU_ACCEPTED_ALLIANCE);
                  activeChar.getClan().setAllyId(clan.getAllyId());
                  activeChar.getClan().setAllyName(clan.getAllyName());
                  activeChar.getClan().setAllyPenaltyExpiryTime(0L, 0);
                  activeChar.getClan().changeAllyCrest(clan.getAllyCrestId(), true);
                  activeChar.getClan().updateClanInDB();
               }
            }

            activeChar.getRequest().onRequestResponse();
         }
      }
   }
}
