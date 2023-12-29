package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;

public class ExReplyDominionInfo extends GameServerPacket {
   public static final ExReplyDominionInfo STATIC_PACKET = new ExReplyDominionInfo();

   private ExReplyDominionInfo() {
   }

   @Override
   protected void writeImpl() {
      List<TerritoryWarManager.Territory> territoryList = TerritoryWarManager.getInstance().getAllTerritories();
      this.writeD(territoryList.size());

      for(TerritoryWarManager.Territory t : territoryList) {
         this.writeD(t.getTerritoryId());
         this.writeS(CastleManager.getInstance().getCastleById(t.getCastleId()).getName().toLowerCase() + "_dominion");
         this.writeS(t.getOwnerClan().getName());
         this.writeD(t.getOwnedWardIds().size());

         for(int i : t.getOwnedWardIds()) {
            this.writeD(i);
         }

         this.writeD((int)(TerritoryWarManager.getInstance().getTWStartTimeInMillis() / 1000L));
      }
   }
}
