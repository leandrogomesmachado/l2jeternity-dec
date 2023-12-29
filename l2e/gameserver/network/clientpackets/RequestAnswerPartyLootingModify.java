package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public class RequestAnswerPartyLootingModify extends GameClientPacket {
   public int _answer;

   @Override
   protected void readImpl() {
      this._answer = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Party party = activeChar.getParty();
         if (party != null) {
            party.answerLootChangeRequest(activeChar, this._answer == 1);
         }
      }
   }
}
