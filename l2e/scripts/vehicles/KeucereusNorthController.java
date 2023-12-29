package l2e.scripts.vehicles;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.VehicleTemplate;

public class KeucereusNorthController extends AirShipController {
   private static final int DOCK_ZONE = 50602;
   private static final int LOCATION = 100;
   private static final int CONTROLLER_ID = 32606;
   private static final VehicleTemplate[] ARRIVAL = new VehicleTemplate[]{
      new VehicleTemplate(-183218, 239494, 2500, 280, 2000), new VehicleTemplate(-183218, 239494, 1336, 280, 2000)
   };
   private static final VehicleTemplate[] DEPART = new VehicleTemplate[]{
      new VehicleTemplate(-183218, 239494, 1700, 280, 2000), new VehicleTemplate(-181974, 235358, 1700, 280, 2000)
   };
   private static final VehicleTemplate[][] TELEPORTS = new VehicleTemplate[][]{
      {
            new VehicleTemplate(-183218, 239494, 1700, 280, 2000),
            new VehicleTemplate(-181974, 235358, 1700, 280, 2000),
            new VehicleTemplate(-186373, 234000, 2500, 0, 0)
      },
      {
            new VehicleTemplate(-183218, 239494, 1700, 280, 2000),
            new VehicleTemplate(-181974, 235358, 1700, 280, 2000),
            new VehicleTemplate(-206692, 220997, 3000, 0, 0)
      },
      {
            new VehicleTemplate(-183218, 239494, 1700, 280, 2000),
            new VehicleTemplate(-181974, 235358, 1700, 280, 2000),
            new VehicleTemplate(-235693, 248843, 5100, 0, 0)
      }
   };
   private static final int[] FUEL = new int[]{0, 50, 100};

   public KeucereusNorthController(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32606);
      this.addFirstTalkId(32606);
      this.addTalkId(32606);
      this._dockZone = 50602;
      this.addEnterZoneId(new int[]{50602});
      this.addExitZoneId(50602);
      this._shipSpawnX = -184145;
      this._shipSpawnY = 242373;
      this._shipSpawnZ = 3000;
      this._oustLoc = new Location(-183900, 239384, 1320);
      this._locationId = 100;
      this._arrivalPath = ARRIVAL;
      this._departPath = DEPART;
      this._teleportsTable = TELEPORTS;
      this._fuelTable = FUEL;
      this._movieId = 1001;
      this.validityCheck();
   }

   public static void main(String[] args) {
      new KeucereusNorthController(-1, KeucereusNorthController.class.getSimpleName(), "vehicles");
   }
}
