package l2e.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

public class MatchingRoomManager {
   private static final MatchingRoomManager _instance = new MatchingRoomManager();
   private final MatchingRoomManager.RoomsHolder[] _holder = new MatchingRoomManager.RoomsHolder[2];
   private final Set<Player> _players = new CopyOnWriteArraySet<>();

   public static MatchingRoomManager getInstance() {
      return _instance;
   }

   public MatchingRoomManager() {
      this._holder[MatchingRoom.PARTY_MATCHING] = new MatchingRoomManager.RoomsHolder();
      this._holder[MatchingRoom.CC_MATCHING] = new MatchingRoomManager.RoomsHolder();
   }

   public void addToWaitingList(Player player) {
      this._players.add(player);
   }

   public void removeFromWaitingList(Player player) {
      this._players.remove(player);
   }

   public List<Player> getWaitingList(int minLevel, int maxLevel, int[] classes) {
      List<Player> res = new ArrayList<>();

      for(Player member : this._players) {
         if (member.getLevel() >= minLevel
            && member.getLevel() <= maxLevel
            && (classes.length == 0 || ArrayUtils.contains(classes, member.getClassId().getId()))) {
            res.add(member);
         }
      }

      return res;
   }

   public List<MatchingRoom> getMatchingRooms(int type, int region, boolean allLevels, Player activeChar) {
      List<MatchingRoom> res = new ArrayList<>();

      for(MatchingRoom room : this._holder[type]._rooms.valueCollection()) {
         if ((region <= 0 || room.getLocationId() == region)
            && (region != -2 || room.getLocationId() == MapRegionManager.getInstance().getBBs(activeChar.getLocation()))
            && (allLevels || room.getMinLevel() <= activeChar.getLevel() && room.getMaxLevel() >= activeChar.getLevel())) {
            res.add(room);
         }
      }

      return res;
   }

   public int addMatchingRoom(MatchingRoom r) {
      return this._holder[r.getType()].addRoom(r);
   }

   public void removeMatchingRoom(MatchingRoom r) {
      this._holder[r.getType()]._rooms.remove(r.getId());
   }

   public MatchingRoom getMatchingRoom(int type, int id) {
      return this._holder[type]._rooms.get(id);
   }

   private class RoomsHolder {
      private int _id = 1;
      private final IntObjectMap<MatchingRoom> _rooms = new CHashIntObjectMap<>();

      private RoomsHolder() {
      }

      public int addRoom(MatchingRoom r) {
         int val = this._id++;
         this._rooms.put(val, r);
         return val;
      }
   }
}
