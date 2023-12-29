package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.PledgePowerGradeList;

public final class RequestPledgePowerGradeList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      Clan clan = player.getClan();
      if (clan != null) {
         player.sendPacket(new PledgePowerGradeList(clan));
      }
   }
}
