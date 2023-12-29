package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public class MoveToLocationInVehicle extends GameServerPacket {
   private final int _charObjId;
   private final int _boatId;
   private final Location _destination;
   private final Location _origin;

   public MoveToLocationInVehicle(Player player, Location destination, Location origin) {
      this._charObjId = player.getObjectId();
      this._boatId = player.getBoat().getObjectId();
      this._destination = destination;
      this._origin = origin;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._boatId);
      this.writeD(this._destination.getX());
      this.writeD(this._destination.getY());
      this.writeD(this._destination.getZ());
      this.writeD(this._origin.getX());
      this.writeD(this._origin.getY());
      this.writeD(this._origin.getZ());
   }
}
