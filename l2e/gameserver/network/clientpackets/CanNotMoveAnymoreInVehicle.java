package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.StopMoveInVehicle;

public final class CanNotMoveAnymoreInVehicle extends GameClientPacket {
   private int _x;
   private int _y;
   private int _z;
   private int _heading;
   private int _boatId;

   @Override
   protected void readImpl() {
      this._boatId = this.readD();
      this._x = this.readD();
      this._y = this.readD();
      this._z = this.readD();
      this._heading = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isInBoat() && player.getBoat().getObjectId() == this._boatId) {
            player.setInVehiclePosition(new Location(this._x, this._y, this._z));
            player.setHeading(this._heading);
            StopMoveInVehicle msg = new StopMoveInVehicle(player, this._boatId);
            player.broadcastPacket(msg);
         }
      }
   }
}
