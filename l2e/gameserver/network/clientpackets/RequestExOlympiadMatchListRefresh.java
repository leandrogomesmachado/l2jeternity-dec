package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExReceiveOlympiadList;

public class RequestExOlympiadMatchListRefresh extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.sendPacket(new ExReceiveOlympiadList.OlympiadList());
      }
   }
}
