package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (client != null) {
         for(Fort fort : FortManager.getInstance().getForts()) {
            if (fort != null && fort.getSiege().getIsInProgress()) {
               client.sendPacket(new ExShowFortressSiegeInfo(fort));
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
