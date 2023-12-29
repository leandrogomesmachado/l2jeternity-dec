package l2e.scripts.instances.ChambersOfDelusion;

import l2e.gameserver.model.Location;

public final class ChamberOfDelusionNorth extends Chamber {
   private static final Location[] _enterCoords = new Location[]{
      new Location(-108976, -207772, -6720),
      new Location(-108976, -206972, -6720),
      new Location(-108960, -209164, -6720),
      new Location(-108048, -207340, -6720),
      new Location(-108048, -209020, -6720)
   };

   private ChamberOfDelusionNorth(String name, String descr) {
      super(name, descr, 130, 32661, 32679, 32683, 25693, 18838, "north_chamber_box");
      this._coords = _enterCoords;
   }

   public static void main(String[] args) {
      new ChamberOfDelusionNorth(ChamberOfDelusionNorth.class.getSimpleName(), "instances");
   }
}
