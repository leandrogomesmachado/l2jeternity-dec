package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.character.BoatAI;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Vehicle;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.VehicleDeparture;
import l2e.gameserver.network.serverpackets.VehicleInfo;
import l2e.gameserver.network.serverpackets.VehicleStarted;

public class BoatInstance extends Vehicle {
   public BoatInstance(int objectId, CharTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.BoatInstance);
      this.setAI(new BoatAI(this));
   }

   @Override
   public boolean isBoat() {
      return true;
   }

   @Override
   public int getId() {
      return 0;
   }

   @Override
   public boolean moveToNextRoutePoint() {
      boolean result = super.moveToNextRoutePoint();
      if (result) {
         this.broadcastPacket(new VehicleDeparture(this));
      }

      return result;
   }

   @Override
   public void oustPlayer(Player player) {
      super.oustPlayer(player);
      Location loc = this.getOustLoc();
      if (player.isOnline()) {
         player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
      } else {
         player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
      }
   }

   @Override
   public void stopMove(Location loc) {
      super.stopMove(loc);
      this.broadcastPacket(new VehicleStarted(this, 0));
      this.broadcastPacket(new VehicleInfo(this));
   }

   @Override
   public void sendInfo(Player activeChar) {
      activeChar.sendPacket(new VehicleInfo(this));
   }

   @Override
   public GameServerPacket infoPacket() {
      return new VehicleInfo(this);
   }
}
