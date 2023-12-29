package l2e.gameserver.network.clientpackets;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.network.SystemMessageId;

public class RequestMoveToLocationAirShip extends GameClientPacket {
   public static final int MIN_Z = -895;
   public static final int MAX_Z = 6105;
   public static final int STEP = 300;
   private int _command;
   private int _param1;
   private int _param2 = 0;

   @Override
   protected void readImpl() {
      this._command = this.readD();
      this._param1 = this.readD();
      if (this._buf.remaining() > 0) {
         this._param2 = this.readD();
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isInAirShip()) {
            AirShipInstance ship = activeChar.getAirShip();
            if (ship.isCaptain(activeChar)) {
               int z = ship.getZ();
               switch(this._command) {
                  case 0:
                     if (!ship.canBeControlled()) {
                        return;
                     }

                     if (this._param1 < -166168) {
                        ship.getAI().setIntention(CtrlIntention.MOVING, new Location(this._param1, this._param2, z));
                     }
                     break;
                  case 1:
                     if (!ship.canBeControlled()) {
                        return;
                     }

                     ship.getAI().setIntention(CtrlIntention.ACTIVE);
                     break;
                  case 2:
                     if (!ship.canBeControlled()) {
                        return;
                     }

                     if (z < 6105) {
                        z = Math.min(z + 300, 6105);
                        ship.getAI().setIntention(CtrlIntention.MOVING, new Location(ship.getX(), ship.getY(), z));
                     }
                     break;
                  case 3:
                     if (!ship.canBeControlled()) {
                        return;
                     }

                     if (z > -895) {
                        z = Math.max(z - 300, -895);
                        ship.getAI().setIntention(CtrlIntention.MOVING, new Location(ship.getX(), ship.getY(), z));
                     }
                     break;
                  case 4:
                     if (!ship.isInDock() || ship.isMoving()) {
                        return;
                     }

                     VehicleTemplate[] dst = AirShipManager.getInstance().getTeleportDestination(ship.getDockId(), this._param1);
                     if (dst == null) {
                        return;
                     }

                     int fuelConsumption = AirShipManager.getInstance().getFuelConsumption(ship.getDockId(), this._param1);
                     if (fuelConsumption > 0) {
                        if (fuelConsumption > ship.getFuel()) {
                           activeChar.sendPacket(SystemMessageId.THE_AIRSHIP_CANNOT_TELEPORT);
                           return;
                        }

                        ship.setFuel(ship.getFuel() - fuelConsumption);
                     }

                     ship.executePath(dst);
               }
            }
         }
      }
   }
}
