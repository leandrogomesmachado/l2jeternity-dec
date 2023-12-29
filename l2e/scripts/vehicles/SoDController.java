package l2e.scripts.vehicles;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.VehicleTemplate;

public class SoDController extends AirShipController {
   private static final int DOCK_ZONE = 50601;
   private static final int LOCATION = 102;
   private static final int CONTROLLER_ID = 32605;
   private static final VehicleTemplate[] ARRIVAL = new VehicleTemplate[]{new VehicleTemplate(-246445, 252331, 4359, 280, 2000)};
   private static final VehicleTemplate[] DEPART = new VehicleTemplate[]{new VehicleTemplate(-245245, 251040, 4359, 280, 2000)};
   private static final VehicleTemplate[][] TELEPORTS = new VehicleTemplate[][]{
      {new VehicleTemplate(-245245, 251040, 4359, 280, 2000), new VehicleTemplate(-235693, 248843, 5100, 0, 0)},
      {new VehicleTemplate(-245245, 251040, 4359, 280, 2000), new VehicleTemplate(-195357, 233430, 2500, 0, 0)}
   };
   private static final int[] FUEL = new int[]{0, 100};

   public SoDController(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32605);
      this.addFirstTalkId(32605);
      this.addTalkId(32605);
      this._dockZone = 50601;
      this.addEnterZoneId(new int[]{50601});
      this.addExitZoneId(50601);
      this._shipSpawnX = -247702;
      this._shipSpawnY = 253631;
      this._shipSpawnZ = 4359;
      this._oustLoc = new Location(-247746, 251079, 4328);
      this._locationId = 102;
      this._arrivalPath = ARRIVAL;
      this._departPath = DEPART;
      this._teleportsTable = TELEPORTS;
      this._fuelTable = FUEL;
      this._movieId = 1003;
      this.validityCheck();
   }

   public static void main(String[] args) {
      new SoDController(-1, SoDController.class.getSimpleName(), "vehicles");
   }
}
