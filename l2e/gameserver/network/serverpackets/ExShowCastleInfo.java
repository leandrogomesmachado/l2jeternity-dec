package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.entity.Castle;

public class ExShowCastleInfo extends GameServerPacket {
   @Override
   protected void writeImpl() {
      List<Castle> castles = CastleManager.getInstance().getCastles();
      this.writeD(castles.size());

      for(Castle castle : castles) {
         this.writeD(castle.getId());
         if (castle.getOwnerId() > 0) {
            if (ClanHolder.getInstance().getClan(castle.getOwnerId()) != null) {
               this.writeS(ClanHolder.getInstance().getClan(castle.getOwnerId()).getName());
            } else {
               _log.warning(
                  "Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!"
               );
               this.writeS("");
            }
         } else {
            this.writeS("");
         }

         this.writeD(castle.getTaxPercent());
         this.writeD((int)(castle.getSiege().getSiegeDate().getTimeInMillis() / 1000L));
      }
   }
}
