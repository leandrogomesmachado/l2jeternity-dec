package l2e.gameserver.network.serverpackets;

import java.util.Map;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;

public class ExShowAgitInfo extends GameServerPacket {
   @Override
   protected void writeImpl() {
      Map<Integer, AuctionableHall> clannhalls = ClanHallManager.getInstance().getAllAuctionableClanHalls();
      this.writeD(clannhalls.size());

      for(AuctionableHall ch : clannhalls.values()) {
         this.writeD(ch.getId());
         this.writeS(ch.getOwnerId() <= 0 ? "" : ClanHolder.getInstance().getClan(ch.getOwnerId()).getName());
         this.writeS(ch.getOwnerId() <= 0 ? "" : ClanHolder.getInstance().getClan(ch.getOwnerId()).getLeaderName());
         this.writeD(ch.getGrade() > 0 ? 0 : 1);
      }
   }
}
