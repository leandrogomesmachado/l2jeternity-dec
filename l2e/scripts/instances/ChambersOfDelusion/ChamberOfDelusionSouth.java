package l2e.scripts.instances.ChambersOfDelusion;

import l2e.gameserver.model.Location;

public final class ChamberOfDelusionSouth extends Chamber {
   private static final Location[] _enterCoords = new Location[]{
      new Location(-122368, -207820, -6720),
      new Location(-122368, -206940, -6720),
      new Location(-122368, -209116, -6720),
      new Location(-121456, -207356, -6720),
      new Location(-121440, -209004, -6720)
   };

   private ChamberOfDelusionSouth(String name, String descr) {
      super(name, descr, 129, 32660, 32674, 32678, 25692, 18838, "south_chamber_box");
      this._coords = _enterCoords;
   }

   public static void main(String[] args) {
      new ChamberOfDelusionSouth(ChamberOfDelusionSouth.class.getSimpleName(), "instances");
   }
}
