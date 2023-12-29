package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class ExListMpccWaiting extends GameServerPacket {
   private static final int ITEMS_PER_PAGE = 10;
   private final int _page;
   private final List<MatchingRoom> _list;

   public ExListMpccWaiting(Player player, int page, int location, boolean allLevels) {
      int first = (page - 1) * 10;
      int firstNot = page * 10;
      List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.CC_MATCHING, location, allLevels, player);
      this._page = page;
      this._list = new ArrayList<>(10);

      for(int i = 0; i < temp.size(); ++i) {
         if (i >= first && i < firstNot) {
            this._list.add(temp.get(i));
         }
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._page);
      this.writeD(this._list.size());

      for(MatchingRoom room : this._list) {
         this.writeD(room.getId());
         this.writeS(room.getTopic());
         this.writeD(room.getMemberCount());
         this.writeD(room.getMinLevel());
         this.writeD(room.getMaxLevel());
         this.writeD(room.getLeader().getParty().getCommandChannel().getPartys().size());
         this.writeD(room.getMaxMembersSize());
         this.writeS(room.getLeader() == null ? "" : room.getLeader().getName());
      }
   }
}
