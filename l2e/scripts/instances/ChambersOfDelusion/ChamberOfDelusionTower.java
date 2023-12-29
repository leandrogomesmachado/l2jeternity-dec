package l2e.scripts.instances.ChambersOfDelusion;

import l2e.gameserver.model.Location;

public final class ChamberOfDelusionTower extends Chamber {
   private static final Location[] _enterCoords = new Location[]{
      new Location(-108976, -153372, -6688),
      new Location(-108960, -152524, -6688),
      new Location(-107088, -155052, -6688),
      new Location(-107104, -154236, -6688),
      new Location(-108048, -151244, -6688),
      new Location(-107088, -152956, -6688),
      new Location(-108992, -154604, -6688),
      new Location(-108032, -152892, -6688),
      new Location(-108048, -154572, -6688)
   };

   private ChamberOfDelusionTower(String name, String descr) {
      super(name, descr, 132, 32663, 32693, 32701, 25695, 18823, "tower_chamber_box");
      this._coords = _enterCoords;
   }

   public static void main(String[] args) {
      new ChamberOfDelusionTower(ChamberOfDelusionTower.class.getSimpleName(), "instances");
   }
}
