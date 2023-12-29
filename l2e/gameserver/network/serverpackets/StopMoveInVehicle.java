package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public class StopMoveInVehicle extends GameServerPacket {
   private final int _charObjId;
   private final int _boatId;
   private final Location _pos;
   private final int _heading;

   public StopMoveInVehicle(Player player, int boatId) {
      this._charObjId = player.getObjectId();
      this._boatId = boatId;
      this._pos = player.getInVehiclePosition();
      this._heading = player.getHeading();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._boatId);
      this.writeD(this._pos.getX());
      this.writeD(this._pos.getY());
      this.writeD(this._pos.getZ());
      this.writeD(this._heading);
   }
}
