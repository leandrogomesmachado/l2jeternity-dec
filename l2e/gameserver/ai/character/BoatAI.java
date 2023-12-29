package l2e.gameserver.ai.character;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BoatInstance;
import l2e.gameserver.network.serverpackets.VehicleDeparture;
import l2e.gameserver.network.serverpackets.VehicleInfo;
import l2e.gameserver.network.serverpackets.VehicleStarted;

public class BoatAI extends VehicleAI {
   public BoatAI(BoatInstance accessor) {
      super(accessor);
   }

   @Override
   protected void moveTo(int x, int y, int z, int offset) {
      if (!this._actor.isMovementDisabled()) {
         if (!this._clientMoving) {
            this._actor.broadcastPacket(new VehicleStarted(this.getActor(), 1));
         }

         this._clientMoving = true;
         this._actor.moveToLocation(x, y, z, offset);
         this._actor.broadcastPacket(new VehicleDeparture(this.getActor()));
      }
   }

   @Override
   protected void moveTo(Location loc, int offset) {
      if (!this._actor.isMovementDisabled()) {
         if (!this._clientMoving) {
            this._actor.broadcastPacket(new VehicleStarted(this.getActor(), 1));
         }

         this._clientMoving = true;
         this._actor.moveToLocation(loc.getX(), loc.getY(), loc.getZ(), offset);
         this._actor.broadcastPacket(new VehicleDeparture(this.getActor()));
      }
   }

   @Override
   public void clientStopMoving(Location loc) {
      if (this._actor.isMoving()) {
         this._actor.stopMove(loc);
      }

      if (this._clientMoving || loc != null) {
         this._clientMoving = false;
         this._actor.broadcastPacket(new VehicleStarted(this.getActor(), 0));
         this._actor.broadcastPacket(new VehicleInfo(this.getActor()));
      }
   }

   @Override
   public void describeStateToPlayer(Player player) {
      if (this._clientMoving) {
         player.sendPacket(new VehicleDeparture(this.getActor()));
      }
   }

   public BoatInstance getActor() {
      return (BoatInstance)this._actor;
   }
}
