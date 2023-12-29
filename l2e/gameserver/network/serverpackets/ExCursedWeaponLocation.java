package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.model.Location;

public class ExCursedWeaponLocation extends GameServerPacket {
   private final List<ExCursedWeaponLocation.CursedWeaponInfo> _cursedWeaponInfo;

   public ExCursedWeaponLocation(List<ExCursedWeaponLocation.CursedWeaponInfo> cursedWeaponInfo) {
      this._cursedWeaponInfo = cursedWeaponInfo;
   }

   @Override
   protected void writeImpl() {
      if (!this._cursedWeaponInfo.isEmpty()) {
         this.writeD(this._cursedWeaponInfo.size());

         for(ExCursedWeaponLocation.CursedWeaponInfo w : this._cursedWeaponInfo) {
            this.writeD(w._id);
            this.writeD(w._activated);
            this.writeD(w._loc.getX());
            this.writeD(w._loc.getY());
            this.writeD(w._loc.getZ());
         }
      } else {
         this.writeD(0);
         this.writeD(0);
      }
   }

   public static class CursedWeaponInfo {
      public Location _loc;
      public int _id;
      public int _activated;

      public CursedWeaponInfo(Location loc, int id, int status) {
         this._loc = loc;
         this._id = id;
         this._activated = status;
      }
   }
}
