package l2e.gameserver.instancemanager;

import java.util.List;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.TownZone;

public class TownManager {
   public static final int getTownCastle(int townId) {
      switch(townId) {
         case 912:
            return 1;
         case 916:
            return 2;
         case 918:
            return 3;
         case 922:
            return 4;
         case 924:
            return 5;
         case 926:
            return 6;
         case 1537:
            return 8;
         case 1538:
            return 7;
         case 1714:
            return 9;
         default:
            return 0;
      }
   }

   public static final boolean townHasCastleInSiege(int townId) {
      int castleIndex = getTownCastle(townId);
      if (castleIndex > 0) {
         Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
         if (castle != null) {
            return castle.getSiege().getIsInProgress();
         }
      }

      return false;
   }

   public static final boolean townHasCastleInSiege(int x, int y) {
      return townHasCastleInSiege(MapRegionManager.getInstance().getMapRegionLocId(x, y));
   }

   public static final TownZone getTown(int townId) {
      for(TownZone temp : ZoneManager.getInstance().getAllZones(TownZone.class)) {
         if (temp != null && temp.getTownId() == townId) {
            return temp;
         }
      }

      return null;
   }

   public static final TownZone getTown(int x, int y, int z) {
      List<ZoneType> zones = ZoneManager.getInstance().getZones(x, y, z);
      if (zones != null && !zones.isEmpty()) {
         for(ZoneType zone : zones) {
            if (zone != null && zone instanceof TownZone) {
               return (TownZone)zone;
            }
         }
      }

      return null;
   }

   public static final TownZone getTownZone(int x, int y, int z) {
      TownZone zone = ZoneManager.getInstance().getZone(x, y, z, TownZone.class);
      return zone != null && zone.getTaxById() > 0 ? zone : null;
   }
}
