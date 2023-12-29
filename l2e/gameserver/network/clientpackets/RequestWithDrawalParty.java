package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public final class RequestWithDrawalParty extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Party party = player.getParty();
         if (party != null) {
            if (party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(player)) {
               player.sendMessage("You can't exit party when you are in Dimensional Rift.");
            } else {
               party.removePartyMember(player, Party.messageType.Left);
            }
         }
      }
   }
}
