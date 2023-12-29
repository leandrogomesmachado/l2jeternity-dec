package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;

public class RequestExCleftEnter extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (!AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()) {
            AerialCleftEvent.getInstance().removePlayer(activeChar.getObjectId(), false);
         } else {
            AerialCleftEvent.getInstance().removePlayer(activeChar.getObjectId(), true);
         }
      }
   }
}
