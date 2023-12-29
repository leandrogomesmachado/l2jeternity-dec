package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class VehicleCheckLocation extends GameServerPacket {
   private final Creature _boat;

   public VehicleCheckLocation(Creature boat) {
      this._boat = boat;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._boat.getObjectId());
      this.writeD(this._boat.getX());
      this.writeD(this._boat.getY());
      this.writeD(this._boat.getZ());
      this.writeD(this._boat.getHeading());
   }
}
