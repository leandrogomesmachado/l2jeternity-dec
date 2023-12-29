package l2e.gameserver.ai.character;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import l2e.gameserver.network.serverpackets.ExStopMoveAirShip;

public class AirShipAI extends VehicleAI {
   public AirShipAI(AirShipInstance accessor) {
      super(accessor);
   }

   @Override
   protected void moveTo(int x, int y, int z, int offset) {
      if (!this._actor.isMovementDisabled()) {
         this._clientMoving = true;
         this._actor.moveToLocation(x, y, z, offset);
         this._actor.broadcastPacket(new ExMoveToLocationAirShip(this.getActor()));
      }
   }

   @Override
   protected void moveTo(Location loc, int offset) {
      if (!this._actor.isMovementDisabled()) {
         this._clientMoving = true;
         this._actor.moveToLocation(loc.getX(), loc.getY(), loc.getZ(), offset);
         this._actor.broadcastPacket(new ExMoveToLocationAirShip(this.getActor()));
      }
   }

   @Override
   public void clientStopMoving(Location loc) {
      if (this._actor.isMoving()) {
         this._actor.stopMove(loc);
      }

      if (this._clientMoving || loc != null) {
         this._clientMoving = false;
         this._actor.broadcastPacket(new ExStopMoveAirShip(this.getActor()));
      }
   }

   @Override
   public void describeStateToPlayer(Player player) {
      if (this._clientMoving) {
         player.sendPacket(new ExMoveToLocationAirShip(this.getActor()));
      }
   }

   public AirShipInstance getActor() {
      return (AirShipInstance)this._actor;
   }
}
