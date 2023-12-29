package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.actor.Player;

public final class RequestReplySurrenderPledgeWar extends GameClientPacket {
   private String _reqName;
   private int _answer;

   @Override
   protected void readImpl() {
      this._reqName = this.readS();
      this._answer = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         Player requestor = activeChar.getActiveRequester();
         if (requestor != null) {
            if (this._answer == 1) {
               requestor.deathPenalty(null, false, false, false);
               ClanHolder.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
            } else {
               _log.info(this.getClass().getSimpleName() + ": Missing implementation for answer: " + this._answer + " and name: " + this._reqName + "!");
            }

            activeChar.onTransactionRequest(requestor);
         }
      }
   }
}
