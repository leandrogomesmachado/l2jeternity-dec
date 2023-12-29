package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.templates.VehicleTemplate;

public class ExAirShipTeleportList extends GameServerPacket {
   private final int _dockId;
   private final VehicleTemplate[][] _teleports;
   private final int[] _fuelConsumption;

   public ExAirShipTeleportList(int dockId, VehicleTemplate[][] teleports, int[] fuelConsumption) {
      this._dockId = dockId;
      this._teleports = teleports;
      this._fuelConsumption = fuelConsumption;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._dockId);
      if (this._teleports != null) {
         this.writeD(this._teleports.length);

         for(int i = 0; i < this._teleports.length; ++i) {
            this.writeD(i - 1);
            this.writeD(this._fuelConsumption[i]);
            VehicleTemplate[] path = this._teleports[i];
            VehicleTemplate dst = path[path.length - 1];
            this.writeD(dst.getX());
            this.writeD(dst.getY());
            this.writeD(dst.getZ());
         }
      } else {
         this.writeD(0);
      }
   }
}
