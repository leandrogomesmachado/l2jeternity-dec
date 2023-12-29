package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.Clan;

public class PledgePowerGradeList extends GameServerPacket {
   private final List<Clan.RankPrivs> _privs = new ArrayList<>();

   public PledgePowerGradeList(Clan clan) {
      for(Clan.RankPrivs priv : clan.getAllRankPrivs()) {
         priv.setParty(clan.countMembersByRank(priv.getRank()));
         this._privs.add(priv);
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._privs.size());

      for(Clan.RankPrivs temp : this._privs) {
         this.writeD(temp.getRank());
         this.writeD(temp.getParty());
      }
   }
}
