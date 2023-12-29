package l2e.gameserver.network.clientpackets;

import java.util.logging.Logger;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.model.actor.Player;

public final class RequestExFishRanking extends GameClientPacket {
   protected static final Logger _log = Logger.getLogger(RequestExFishRanking.class.getName());

   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         FishingChampionship.getInstance().showMidResult(activeChar);
      }
   }
}
