package l2e.scripts.vehicles;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.VehicleTemplate;

public class KeucereusSouthController extends AirShipController {
   private static final int DOCK_ZONE = 50603;
   private static final int LOCATION = 100;
   private static final int CONTROLLER_ID = 32517;
   private static final VehicleTemplate[] ARRIVAL = new VehicleTemplate[]{
      new VehicleTemplate(-185312, 246544, 2500), new VehicleTemplate(-185312, 246544, 1336)
   };
   private static final VehicleTemplate[] DEPART = new VehicleTemplate[]{
      new VehicleTemplate(-185312, 246544, 1700, 280, 2000), new VehicleTemplate(-186900, 251699, 1700, 280, 2000)
   };
   private static final VehicleTemplate[][] TELEPORTS = new VehicleTemplate[][]{
      {
            new VehicleTemplate(-185312, 246544, 1700, 280, 2000),
            new VehicleTemplate(-186900, 251699, 1700, 280, 2000),
            new VehicleTemplate(-186373, 234000, 2500, 0, 0)
      },
      {
            new VehicleTemplate(-185312, 246544, 1700, 280, 2000),
            new VehicleTemplate(-186900, 251699, 1700, 280, 2000),
            new VehicleTemplate(-206692, 220997, 3000, 0, 0)
      },
      {
            new VehicleTemplate(-185312, 246544, 1700, 280, 2000),
            new VehicleTemplate(-186900, 251699, 1700, 280, 2000),
            new VehicleTemplate(-235693, 248843, 5100, 0, 0)
      }
   };
   private static final int[] FUEL = new int[]{0, 50, 100};

   public KeucereusSouthController(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32517);
      this.addFirstTalkId(32517);
      this.addTalkId(32517);
      this._dockZone = 50603;
      this.addEnterZoneId(new int[]{50603});
      this.addExitZoneId(50603);
      this._shipSpawnX = -184527;
      this._shipSpawnY = 243611;
      this._shipSpawnZ = 3000;
      this._locationId = 100;
      this._arrivalPath = ARRIVAL;
      this._departPath = DEPART;
      this._teleportsTable = TELEPORTS;
      this._fuelTable = FUEL;
      this._oustLoc = new Location(-186148, 246296, 1360);
      this._movieId = 1000;
      this.validityCheck();
   }

   public static void main(String[] args) {
      new KeucereusSouthController(-1, KeucereusSouthController.class.getSimpleName(), "vehicles");
   }
}
