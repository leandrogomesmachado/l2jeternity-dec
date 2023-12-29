package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.entity.Fort;

public class ExShowFortressInfo extends GameServerPacket {
   public static final ExShowFortressInfo STATIC_PACKET = new ExShowFortressInfo();

   private ExShowFortressInfo() {
   }

   @Override
   protected void writeImpl() {
      List<Fort> forts = FortManager.getInstance().getForts();
      this.writeD(forts.size());

      for(Fort fort : forts) {
         Clan clan = fort.getOwnerClan();
         this.writeD(fort.getId());
         this.writeS(clan != null ? clan.getName() : "");
         this.writeD(fort.getSiege().getIsInProgress() ? 1 : 0);
         this.writeD(fort.getOwnedTime());
      }
   }
}
