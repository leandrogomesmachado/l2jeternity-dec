package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.GetOffVehicle;
import l2e.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestGetOffVehicle extends GameClientPacket {
   private int _boatId;
   private int _x;
   private int _y;
   private int _z;

   @Override
   protected void readImpl() {
      this._boatId = this.readD();
      this._x = this.readD();
      this._y = this.readD();
      this._z = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isInBoat() && activeChar.getBoat().getObjectId() == this._boatId && !activeChar.getBoat().isMoving()) {
            activeChar.broadcastPacket(new StopMoveInVehicle(activeChar, this._boatId));
            activeChar.setVehicle(null);
            activeChar.setInVehiclePosition(null);
            this.sendActionFailed();
            activeChar.broadcastPacket(new GetOffVehicle(activeChar.getObjectId(), this._boatId, this._x, this._y, this._z));
            activeChar.setXYZ(this._x, this._y, this._z);
            activeChar.revalidateZone(true);
         } else {
            this.sendActionFailed();
         }
      }
   }
}
