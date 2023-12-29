package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public class RequestPartyLootingModify extends GameClientPacket {
   private byte _mode;

   @Override
   protected void readImpl() {
      this._mode = (byte)this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._mode >= 0 && this._mode <= 4) {
            Party party = activeChar.getParty();
            if (party != null && party.isLeader(activeChar) && this._mode != party.getLootDistribution()) {
               party.requestLootChange(this._mode);
            }
         }
      }
   }
}
