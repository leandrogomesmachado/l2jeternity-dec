package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestReplyStopPledgeWar extends GameClientPacket {
   private int _answer;
   protected String _reqName;

   @Override
   protected void readImpl() {
      this._reqName = this.readS();
      this._answer = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Player requestor = activeChar.getActiveRequester();
         if (requestor != null) {
            if (this._answer == 1) {
               ClanHolder.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
            } else {
               requestor.sendPacket(SystemMessageId.REQUEST_TO_END_WAR_HAS_BEEN_DENIED);
            }

            activeChar.setActiveRequester(null);
            requestor.onTransactionResponse();
         }
      }
   }
}
