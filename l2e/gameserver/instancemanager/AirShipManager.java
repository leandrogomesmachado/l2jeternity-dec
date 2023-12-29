package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.actor.instance.ControllableAirShipInstance;
import l2e.gameserver.model.actor.templates.AirShipTeleportTemplate;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.serverpackets.ExAirShipTeleportList;

public class AirShipManager {
   private static final Logger _log = Logger.getLogger(AirShipManager.class.getName());
   private static final String LOAD_DB = "SELECT * FROM airships";
   private static final String ADD_DB = "INSERT INTO airships (owner_id,fuel) VALUES (?,?)";
   private static final String UPDATE_DB = "UPDATE airships SET fuel=? WHERE owner_id=?";
   private CharTemplate _airShipTemplate = null;
   private final Map<Integer, StatsSet> _airShipsInfo = new HashMap<>();
   private final Map<Integer, AirShipInstance> _airShips = new HashMap<>();
   private final Map<Integer, AirShipTeleportTemplate> _teleports = new HashMap<>();

   protected AirShipManager() {
      StatsSet npcDat = new StatsSet();
      npcDat.set("npcId", 9);
      npcDat.set("level", 0);
      npcDat.set("jClass", "boat");
      npcDat.set("baseSTR", 0);
      npcDat.set("baseCON", 0);
      npcDat.set("baseDEX", 0);
      npcDat.set("baseINT", 0);
      npcDat.set("baseWIT", 0);
      npcDat.set("baseMEN", 0);
      npcDat.set("baseShldDef", 0);
      npcDat.set("baseShldRate", 0);
      npcDat.set("baseAccCombat", 38);
      npcDat.set("baseEvasRate", 38);
      npcDat.set("baseCritRate", 38);
      npcDat.set("collision_radius", 0);
      npcDat.set("collision_height", 0);
      npcDat.set("sex", "male");
      npcDat.set("type", "");
      npcDat.set("baseAtkRange", 0);
      npcDat.set("baseMpMax", 0);
      npcDat.set("baseCpMax", 0);
      npcDat.set("rewardExp", 0);
      npcDat.set("rewardSp", 0);
      npcDat.set("basePAtk", 0);
      npcDat.set("baseMAtk", 0);
      npcDat.set("basePAtkSpd", 0);
      npcDat.set("aggroRange", 0);
      npcDat.set("baseMAtkSpd", 0);
      npcDat.set("rhand", 0);
      npcDat.set("lhand", 0);
      npcDat.set("armor", 0);
      npcDat.set("baseWalkSpd", 0);
      npcDat.set("baseRunSpd", 0);
      npcDat.set("name", "AirShip");
      npcDat.set("baseHpMax", 50000);
      npcDat.set("baseHpReg", 3.0);
      npcDat.set("baseMpReg", 3.0);
      npcDat.set("basePDef", 100);
      npcDat.set("baseMDef", 100);
      this._airShipTemplate = new CharTemplate(npcDat);
      this.load();
   }

   public AirShipInstance getNewAirShip(int x, int y, int z, int heading) {
      AirShipInstance airShip = new AirShipInstance(IdFactory.getInstance().getNextId(), this._airShipTemplate);
      airShip.setHeading(heading);
      airShip.setXYZInvisible(x, y, z);
      airShip.spawnMe();
      airShip.getStat().setMoveSpeed(280.0F);
      airShip.getStat().setRotationSpeed(2000);
      return airShip;
   }

   public AirShipInstance getNewAirShip(int x, int y, int z, int heading, int ownerId) {
      StatsSet info = this._airShipsInfo.get(ownerId);
      if (info == null) {
         return null;
      } else {
         AirShipInstance airShip;
         if (this._airShips.containsKey(ownerId)) {
            airShip = this._airShips.get(ownerId);
            airShip.refreshID();
         } else {
            airShip = new ControllableAirShipInstance(IdFactory.getInstance().getNextId(), this._airShipTemplate, ownerId);
            this._airShips.put(ownerId, airShip);
            airShip.setMaxFuel(600);
            airShip.setFuel(info.getInteger("fuel"));
            airShip.getStat().setMoveSpeed(280.0F);
            airShip.getStat().setRotationSpeed(2000);
         }

         airShip.setHeading(heading);
         airShip.setXYZInvisible(x, y, z);
         airShip.spawnMe();
         return airShip;
      }
   }

   public void removeAirShip(AirShipInstance ship) {
      if (ship.getOwnerId() != 0) {
         this.storeInDb(ship.getOwnerId());
         StatsSet info = this._airShipsInfo.get(ship.getOwnerId());
         if (info != null) {
            info.set("fuel", ship.getFuel());
         }
      }
   }

   public boolean hasAirShipLicense(int ownerId) {
      return this._airShipsInfo.containsKey(ownerId);
   }

   public void registerLicense(int ownerId) {
      if (!this._airShipsInfo.containsKey(ownerId)) {
         StatsSet info = new StatsSet();
         info.set("fuel", 600);
         this._airShipsInfo.put(ownerId, info);

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO airships (owner_id,fuel) VALUES (?,?)");
         ) {
            ps.setInt(1, ownerId);
            ps.setInt(2, info.getInteger("fuel"));
            ps.executeUpdate();
         } catch (SQLException var37) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not add new airship license: " + var37.getMessage(), (Throwable)var37);
         } catch (Exception var38) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing: " + var38.getMessage(), (Throwable)var38);
         }
      }
   }

   public boolean hasAirShip(int ownerId) {
      AirShipInstance ship = this._airShips.get(ownerId);
      return ship != null && (ship.isVisible() || ship.isTeleporting());
   }

   public void registerAirShipTeleportList(int dockId, int locationId, VehicleTemplate[][] tp, int[] fuelConsumption) {
      if (tp.length == fuelConsumption.length) {
         this._teleports.put(dockId, new AirShipTeleportTemplate(locationId, fuelConsumption, tp));
      }
   }

   public void sendAirShipTeleportList(Player player) {
      if (player != null && player.isInAirShip()) {
         AirShipInstance ship = player.getAirShip();
         if (ship.isCaptain(player) && ship.isInDock() && !ship.isMoving()) {
            int dockId = ship.getDockId();
            if (this._teleports.containsKey(dockId)) {
               AirShipTeleportTemplate all = this._teleports.get(dockId);
               player.sendPacket(new ExAirShipTeleportList(all.getLocation(), all.getRoute(), all.getFuel()));
            }
         }
      }
   }

   public VehicleTemplate[] getTeleportDestination(int dockId, int index) {
      AirShipTeleportTemplate all = this._teleports.get(dockId);
      if (all == null) {
         return null;
      } else {
         return index >= -1 && index < all.getRoute().length ? all.getRoute()[index + 1] : null;
      }
   }

   public int getFuelConsumption(int dockId, int index) {
      AirShipTeleportTemplate all = this._teleports.get(dockId);
      if (all == null) {
         return 0;
      } else {
         return index >= -1 && index < all.getFuel().length ? all.getFuel()[index + 1] : 0;
      }
   }

   private void load() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rs = s.executeQuery("SELECT * FROM airships");
      ) {
         while(rs.next()) {
            StatsSet info = new StatsSet();
            info.set("fuel", rs.getInt("fuel"));
            this._airShipsInfo.put(rs.getInt("owner_id"), info);
         }
      } catch (SQLException var62) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not load airships table: " + var62.getMessage(), (Throwable)var62);
      } catch (Exception var63) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing: " + var63.getMessage(), (Throwable)var63);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._airShipsInfo.size() + " private airships.");
   }

   private void storeInDb(int ownerId) {
      StatsSet info = this._airShipsInfo.get(ownerId);
      if (info != null) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE airships SET fuel=? WHERE owner_id=?");
         ) {
            ps.setInt(1, info.getInteger("fuel"));
            ps.setInt(2, ownerId);
            ps.executeUpdate();
         } catch (SQLException var37) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Could not update airships table: " + var37.getMessage(), (Throwable)var37);
         } catch (Exception var38) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while save: " + var38.getMessage(), (Throwable)var38);
         }
      }
   }

   public static final AirShipManager getInstance() {
      return AirShipManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final AirShipManager _instance = new AirShipManager();
   }
}
