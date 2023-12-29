package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public final class RequestHandOverPartyMaster extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Party party = activeChar.getParty();
         if (party != null && party.isLeader(activeChar)) {
            party.changePartyLeader(this._name);
         }
      }
   }
}
