package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.TerritoryWard;

public class ExShowOwnthingPos extends GameServerPacket {
   public static final ExShowOwnthingPos STATIC_PACKET = new ExShowOwnthingPos();

   private ExShowOwnthingPos() {
   }

   @Override
   protected void writeImpl() {
      if (TerritoryWarManager.getInstance().isTWInProgress()) {
         List<TerritoryWard> territoryWardList = TerritoryWarManager.getInstance().getAllTerritoryWards();
         this.writeD(territoryWardList.size());

         for(TerritoryWard ward : territoryWardList) {
            this.writeD(ward.getTerritoryId());
            if (ward.getNpc() != null) {
               this.writeD(ward.getNpc().getX());
               this.writeD(ward.getNpc().getY());
               this.writeD(ward.getNpc().getZ());
            } else if (ward.getPlayer() != null) {
               this.writeD(ward.getPlayer().getX());
               this.writeD(ward.getPlayer().getY());
               this.writeD(ward.getPlayer().getZ());
            } else {
               this.writeD(0);
               this.writeD(0);
               this.writeD(0);
            }
         }
      } else {
         this.writeD(0);
      }
   }
}
