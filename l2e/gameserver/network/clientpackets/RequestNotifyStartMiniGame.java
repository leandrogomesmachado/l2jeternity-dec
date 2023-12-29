package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public class RequestNotifyStartMiniGame extends GameClientPacket {
   @Override
   protected void runImpl() {
   }

   @Override
   protected void readImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ;
      }
   }
}
