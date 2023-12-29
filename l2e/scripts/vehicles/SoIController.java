package l2e.scripts.vehicles;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.VehicleTemplate;

public class SoIController extends AirShipController {
   private static final int DOCK_ZONE = 50600;
   private static final int LOCATION = 101;
   private static final int CONTROLLER_ID = 32604;
   private static final VehicleTemplate[] ARRIVAL = new VehicleTemplate[]{
      new VehicleTemplate(-214422, 211396, 5000, 280, 2000), new VehicleTemplate(-214422, 211396, 4422, 280, 2000)
   };
   private static final VehicleTemplate[] DEPART = new VehicleTemplate[]{
      new VehicleTemplate(-214422, 211396, 5000, 280, 2000), new VehicleTemplate(-215877, 209709, 5000, 280, 2000)
   };
   private static final VehicleTemplate[][] TELEPORTS = new VehicleTemplate[][]{
      {
            new VehicleTemplate(-214422, 211396, 5000, 280, 2000),
            new VehicleTemplate(-215877, 209709, 5000, 280, 2000),
            new VehicleTemplate(-206692, 220997, 3000, 0, 0)
      },
      {
            new VehicleTemplate(-214422, 211396, 5000, 280, 2000),
            new VehicleTemplate(-215877, 209709, 5000, 280, 2000),
            new VehicleTemplate(-195357, 233430, 2500, 0, 0)
      }
   };
   private static final int[] FUEL = new int[]{0, 50};

   public SoIController(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32604);
      this.addFirstTalkId(32604);
      this.addTalkId(32604);
      this._dockZone = 50600;
      this.addEnterZoneId(new int[]{50600});
      this.addExitZoneId(50600);
      this._shipSpawnX = -212719;
      this._shipSpawnY = 213348;
      this._shipSpawnZ = 5000;
      this._oustLoc = new Location(-213401, 210401, 4408);
      this._locationId = 101;
      this._arrivalPath = ARRIVAL;
      this._departPath = DEPART;
      this._teleportsTable = TELEPORTS;
      this._fuelTable = FUEL;
      this._movieId = 1002;
      this.validityCheck();
   }

   public static void main(String[] args) {
      new SoIController(-1, SoIController.class.getSimpleName(), "vehicles");
   }
}
