package l2e.scripts.instances.ChambersOfDelusion;

import l2e.gameserver.model.Location;

public final class ChamberOfDelusionWest extends Chamber {
   private static final Location[] _enterCoords = new Location[]{
      new Location(-108960, -218892, -6720),
      new Location(-108976, -218028, -6720),
      new Location(-108960, -220204, -6720),
      new Location(-108032, -218428, -6720),
      new Location(-108032, -220140, -6720)
   };

   private ChamberOfDelusionWest(String name, String descr) {
      super(name, descr, 128, 32659, 32669, 32673, 25691, 18838, "west_chamber_box");
      this._coords = _enterCoords;
   }

   public static void main(String[] args) {
      new ChamberOfDelusionWest(ChamberOfDelusionWest.class.getSimpleName(), "instances");
   }
}
