package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class ListPartyWaiting extends GameServerPacket {
   private static final int ITEMS_PER_PAGE = 16;
   private final Collection<MatchingRoom> _rooms = new ArrayList<>(16);
   private final int _page;

   public ListPartyWaiting(int region, boolean allLevels, int page, Player activeChar) {
      this._page = page;
      List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);
      int first = Math.max((page - 1) * 16, 0);
      int firstNot = Math.min(page * 16, temp.size());

      for(int i = first; i < firstNot; ++i) {
         this._rooms.add(temp.get(i));
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._page);
      this.writeD(this._rooms.size());

      for(MatchingRoom room : this._rooms) {
         this.writeD(room.getId());
         this.writeS(room.getTopic());
         this.writeD(room.getLocationId());
         this.writeD(room.getMinLevel());
         this.writeD(room.getMaxLevel());
         this.writeD(room.getMaxMembersSize());
         this.writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
         Collection<Player> players = room.getPlayers();
         this.writeD(players.size());

         for(Player player : players) {
            this.writeD(player.getClassId().getId());
            this.writeS(player.getName());
         }
      }
   }
}
