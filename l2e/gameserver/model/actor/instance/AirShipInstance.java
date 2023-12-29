package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.character.AirShipAI;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Vehicle;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.network.serverpackets.ExAirShipInfo;
import l2e.gameserver.network.serverpackets.ExGetOffAirShip;
import l2e.gameserver.network.serverpackets.ExGetOnAirShip;
import l2e.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import l2e.gameserver.network.serverpackets.ExStopMoveAirShip;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public class AirShipInstance extends Vehicle {
   public AirShipInstance(int objectId, CharTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.AirShipInstance);
      this.setAI(new AirShipAI(this));
   }

   @Override
   public boolean isAirShip() {
      return true;
   }

   public boolean isOwner(Player player) {
      return false;
   }

   public int getOwnerId() {
      return 0;
   }

   public boolean isCaptain(Player player) {
      return false;
   }

   public int getCaptainId() {
      return 0;
   }

   public int getHelmObjectId() {
      return 0;
   }

   public int getHelmItemId() {
      return 0;
   }

   public boolean setCaptain(Player player) {
      return false;
   }

   public int getFuel() {
      return 0;
   }

   public void setFuel(int f) {
   }

   public int getMaxFuel() {
      return 0;
   }

   public void setMaxFuel(int mf) {
   }

   @Override
   public int getId() {
      return 0;
   }

   @Override
   public boolean moveToNextRoutePoint() {
      boolean result = super.moveToNextRoutePoint();
      if (result) {
         this.broadcastPacket(new ExMoveToLocationAirShip(this));
      }

      return result;
   }

   @Override
   public boolean addPassenger(Player player) {
      if (!super.addPassenger(player)) {
         return false;
      } else {
         player.setVehicle(this);
         player.setInVehiclePosition(new Location(0, 0, 0));
         player.broadcastPacket(new ExGetOnAirShip(player, this));
         player.setXYZ(this.getX(), this.getY(), this.getZ());
         player.refreshInfos();
         player.revalidateZone(true);
         return true;
      }
   }

   @Override
   public void oustPlayer(Player player) {
      super.oustPlayer(player);
      Location loc = this.getOustLoc();
      if (player.isOnline()) {
         player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
         player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true, false);
      } else {
         player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
      }
   }

   @Override
   public void deleteMe() {
      super.deleteMe();
      AirShipManager.getInstance().removeAirShip(this);
   }

   @Override
   public void stopMove(Location loc) {
      super.stopMove(loc);
      this.broadcastPacket(new ExStopMoveAirShip(this));
   }

   @Override
   public void updateAbnormalEffect() {
      this.broadcastPacket(new ExAirShipInfo(this));
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (this.isVisibleFor(activeChar)) {
         activeChar.sendPacket(new ExAirShipInfo(this));
      }
   }

   @Override
   public GameServerPacket infoPacket() {
      return new ExAirShipInfo(this);
   }
}
