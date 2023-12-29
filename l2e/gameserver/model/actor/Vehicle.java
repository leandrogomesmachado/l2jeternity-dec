package l2e.gameserver.model.actor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.stat.VehicleStat;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public abstract class Vehicle extends Creature {
   protected int _dockId = 0;
   protected final List<Player> _passengers = new CopyOnWriteArrayList<>();
   protected Location _oustLoc = null;
   private Runnable _engine = null;
   protected VehicleTemplate[] _currentPath = null;
   protected int _runState = 0;

   public Vehicle(int objectId, CharTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.Vehicle);
      this.setIsFlying(true);
   }

   public abstract GameServerPacket infoPacket();

   public boolean isBoat() {
      return false;
   }

   public boolean isAirShip() {
      return false;
   }

   public boolean canBeControlled() {
      return this._engine == null;
   }

   public void registerEngine(Runnable r) {
      this._engine = r;
   }

   public void runEngine(int delay) {
      if (this._engine != null) {
         ThreadPoolManager.getInstance().schedule(this._engine, (long)delay);
      }
   }

   public void executePath(VehicleTemplate[] path) {
      this._runState = 0;
      this._currentPath = path;
      if (this._currentPath != null && this._currentPath.length > 0) {
         VehicleTemplate point = this._currentPath[0];
         if (point.getMoveSpeed() > 0) {
            this.getStat().setMoveSpeed((float)point.getMoveSpeed());
         }

         if (point.getRotationSpeed() > 0) {
            this.getStat().setRotationSpeed(point.getRotationSpeed());
         }

         this.getAI().setIntention(CtrlIntention.MOVING, new Location(point.getX(), point.getY(), point.getZ(), 0));
      } else {
         this.getAI().setIntention(CtrlIntention.ACTIVE);
      }
   }

   @Override
   public boolean moveToNextRoutePoint() {
      this._move = null;
      if (this._currentPath != null) {
         ++this._runState;
         if (this._runState < this._currentPath.length) {
            VehicleTemplate point = this._currentPath[this._runState];
            if (!this.isMovementDisabled()) {
               if (point.getMoveSpeed() != 0) {
                  if (point.getMoveSpeed() > 0) {
                     this.getStat().setMoveSpeed((float)point.getMoveSpeed());
                  }

                  if (point.getRotationSpeed() > 0) {
                     this.getStat().setRotationSpeed(point.getRotationSpeed());
                  }

                  Creature.MoveData m = new Creature.MoveData();
                  m.disregardingGeodata = false;
                  m.onGeodataPathIndex = -1;
                  m._xDestination = point.getX();
                  m._yDestination = point.getY();
                  m._zDestination = point.getZ();
                  m._heading = 0;
                  double dx = (double)(point.getX() - this.getX());
                  double dy = (double)(point.getY() - this.getY());
                  double distance = Math.sqrt(dx * dx + dy * dy);
                  if (distance > 1.0) {
                     this.setHeading(Util.calculateHeadingFrom(this.getX(), this.getY(), point.getX(), point.getY()));
                  }

                  m._moveStartTime = GameTimeController.getInstance().getGameTicks();
                  this._move = m;
                  GameTimeController.getInstance().registerMovingObject(this);
                  return true;
               }

               this.teleToLocation(point.getX(), point.getY(), point.getZ(), point.getRotationSpeed(), false);
               this._currentPath = null;
            }
         } else {
            this._currentPath = null;
         }
      }

      this.runEngine(10);
      return false;
   }

   public VehicleStat getStat() {
      return (VehicleStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new VehicleStat(this));
   }

   public boolean isInDock() {
      return this._dockId > 0;
   }

   public int getDockId() {
      return this._dockId;
   }

   public void setInDock(int d) {
      this._dockId = d;
   }

   public void setOustLoc(Location loc) {
      this._oustLoc = loc;
   }

   public Location getOustLoc() {
      return this._oustLoc != null ? this._oustLoc : MapRegionManager.getInstance().getTeleToLocation(this, TeleportWhereType.TOWN);
   }

   public void oustPlayers() {
      this._passengers.forEach(p -> this.oustPlayer(p));
      this._passengers.clear();
   }

   public void oustPlayer(Player player) {
      player.setVehicle(null);
      player.setInVehiclePosition(null);
      this.removePassenger(player);
   }

   public boolean addPassenger(Player player) {
      if (player != null && !this._passengers.contains(player)) {
         if (player.getVehicle() != null && player.getVehicle() != this) {
            return false;
         } else {
            this._passengers.add(player);
            return true;
         }
      } else {
         return false;
      }
   }

   public void removePassenger(Player player) {
      try {
         this._passengers.remove(player);
      } catch (Exception var3) {
      }
   }

   public boolean isEmpty() {
      return this._passengers.isEmpty();
   }

   public List<Player> getPassengers() {
      return this._passengers;
   }

   public void broadcastToPassengers(GameServerPacket sm) {
      for(Player player : this._passengers) {
         if (player != null) {
            player.sendPacket(sm);
         }
      }
   }

   public void payForRide(int itemId, int count, int oustX, int oustY, int oustZ) {
      Collection<Player> passengers = World.getInstance().getAroundPlayers(this, 1000, 200);
      if (passengers != null && !passengers.isEmpty()) {
         for(Player player : passengers) {
            if (player != null && player.isInBoat() && player.getBoat() == this) {
               if (itemId > 0) {
                  ItemInstance ticket = player.getInventory().getItemByItemId(itemId);
                  if (ticket == null || player.getInventory().destroyItem("Boat", ticket, (long)count, player, this) == null) {
                     player.sendPacket(SystemMessageId.NOT_CORRECT_BOAT_TICKET);
                     player.teleToLocation(oustX, oustY, oustZ, true);
                     continue;
                  }

                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(ticket);
                  player.sendPacket(iu);
               }

               this.addPassenger(player);
            }
         }
      }
   }

   @Override
   public boolean updatePosition() {
      boolean result = super.updatePosition();

      for(Player player : this._passengers) {
         if (player != null && player.getVehicle() == this) {
            player.setXYZ(this.getX(), this.getY(), this.getZ());
            player.revalidateZone(false);
         }
      }

      return result;
   }

   @Override
   public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset) {
      if (this.isMoving()) {
         this.stopMove(null);
      }

      this.setIsTeleporting(true);
      this.getAI().setIntention(CtrlIntention.ACTIVE);

      for(Player player : this._passengers) {
         if (player != null) {
            player.teleToLocation(x, y, z, true);
         }
      }

      this.decayMe();
      this.setXYZ(x, y, z);
      if (heading != 0) {
         this.setHeading(heading);
      }

      this.onTeleported();
      this.revalidateZone(true);
   }

   @Override
   public void stopMove(Location loc) {
      this._move = null;
      if (loc != null) {
         this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
         this.setHeading(loc.getHeading());
         this.revalidateZone(true);
      }
   }

   @Override
   public void deleteMe() {
      this._engine = null;

      try {
         if (this.isMoving()) {
            this.stopMove(null);
         }
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "Failed stopMove().", (Throwable)var3);
      }

      try {
         this.oustPlayers();
      } catch (Exception var2) {
         _log.log(Level.SEVERE, "Failed oustPlayers().", (Throwable)var2);
      }

      this.decayMe();
      super.deleteMe();
   }

   @Override
   public void updateAbnormalEffect() {
   }

   @Override
   public ItemInstance getActiveWeaponInstance() {
      return null;
   }

   @Override
   public Weapon getActiveWeaponItem() {
      return null;
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return null;
   }

   public Weapon getSecondaryWeaponItem() {
      return null;
   }

   @Override
   public int getLevel() {
      return 0;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return false;
   }

   @Override
   public void detachAI() {
   }

   @Override
   public boolean isWalker() {
      return true;
   }

   @Override
   public boolean isVehicle() {
      return true;
   }
}
