package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public final class RequestOustPartyMember extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isInParty() && activeChar.getParty().isLeader(activeChar)) {
            if (activeChar.getParty().isInDimensionalRift() && !activeChar.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar)) {
               activeChar.sendMessage("You can't dismiss party member when you are in Dimensional Rift.");
            } else {
               activeChar.getParty().removePartyMember(this._name, Party.messageType.Expelled);
            }
         }
      }
   }
}
