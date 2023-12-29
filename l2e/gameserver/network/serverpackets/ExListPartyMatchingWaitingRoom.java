package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;

public class ExListPartyMatchingWaitingRoom extends GameServerPacket {
   private static final int ITEMS_PER_PAGE = 64;
   private final List<ExListPartyMatchingWaitingRoom.PartyMatchingWaitingInfo> _waitingList = new ArrayList<>(64);
   private final int _fullSize;

   public ExListPartyMatchingWaitingRoom(Player searcher, int minLevel, int maxLevel, int page, int[] classes) {
      List<Player> temp = MatchingRoomManager.getInstance().getWaitingList(minLevel, maxLevel, classes);
      this._fullSize = temp.size();
      int first = Math.max((page - 1) * 64, 0);
      int firstNot = Math.min(page * 64, this._fullSize);

      for(int i = first; i < firstNot; ++i) {
         this._waitingList.add(new ExListPartyMatchingWaitingRoom.PartyMatchingWaitingInfo(temp.get(i)));
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._fullSize);
      this.writeD(this._waitingList.size());

      for(ExListPartyMatchingWaitingRoom.PartyMatchingWaitingInfo waitingInfo : this._waitingList) {
         this.writeS(waitingInfo.name);
         this.writeD(waitingInfo.classId);
         this.writeD(waitingInfo.level);
         this.writeD(waitingInfo.locationId);
         this.writeD(waitingInfo.instanceReuses.size());

         for(int i : waitingInfo.instanceReuses) {
            this.writeD(i);
         }
      }
   }

   static class PartyMatchingWaitingInfo {
      public final int classId;
      public final int level;
      public final int locationId;
      public final String name;
      public final List<Integer> instanceReuses;

      public PartyMatchingWaitingInfo(Player member) {
         this.name = member.getName();
         this.classId = member.getClassId().getId();
         this.level = member.getLevel();
         this.locationId = MapRegionManager.getInstance().getBBs(member.getLocation());
         this.instanceReuses = ReflectionManager.getInstance().getLockedReflectionList(member.getObjectId());
      }
   }
}
