package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.BoatManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BoatInstance;
import l2e.gameserver.network.serverpackets.GetOnVehicle;

public final class RequestGetOnVehicle extends GameClientPacket {
   private int _boatId;
   private Location _pos;

   @Override
   protected void readImpl() {
      this._boatId = this.readD();
      int x = this.readD();
      int y = this.readD();
      int z = this.readD();
      this._pos = new Location(x, y, z);
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         BoatInstance boat;
         if (activeChar.isInBoat()) {
            boat = activeChar.getBoat();
            if (boat.getObjectId() != this._boatId) {
               this.sendActionFailed();
               return;
            }
         } else {
            boat = BoatManager.getInstance().getBoat(this._boatId);
            if (boat == null || boat.isMoving()) {
               this.sendActionFailed();
               return;
            }
         }

         activeChar.setInVehiclePosition(this._pos);
         activeChar.setVehicle(boat);
         activeChar.broadcastPacket(new GetOnVehicle(activeChar.getObjectId(), boat.getObjectId(), this._pos));
         activeChar.setXYZ(boat.getX(), boat.getY(), boat.getZ());
         activeChar.revalidateZone(true);
      }
   }
}
