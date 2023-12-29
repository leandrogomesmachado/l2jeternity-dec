package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public class ValidateLocationInVehicle extends GameServerPacket {
   private final int _charObjId;
   private final int _boatObjId;
   private final int _heading;
   private final Location _pos;

   public ValidateLocationInVehicle(Player player) {
      this._charObjId = player.getObjectId();
      this._boatObjId = player.getBoat().getObjectId();
      this._heading = player.getHeading();
      this._pos = player.getInVehiclePosition();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._boatObjId);
      this.writeD(this._pos.getX());
      this.writeD(this._pos.getY());
      this.writeD(this._pos.getZ());
      this.writeD(this._heading);
   }
}
