package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestReplyStartPledgeWar extends GameClientPacket {
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
               ClanHolder.getInstance().storeclanswars(requestor.getClanId(), activeChar.getClanId());
            } else {
               requestor.sendPacket(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED);
            }

            activeChar.setActiveRequester(null);
            requestor.onTransactionResponse();
         }
      }
   }
}
