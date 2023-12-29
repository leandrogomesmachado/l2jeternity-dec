package l2e.scripts.instances.ChambersOfDelusion;

import l2e.gameserver.model.Location;

public final class ChamberOfDelusionEast extends Chamber {
   private static final Location[] _enterCoords = new Location[]{
      new Location(-122368, -218972, -6720),
      new Location(-122352, -218044, -6720),
      new Location(-122368, -220220, -6720),
      new Location(-121440, -218444, -6720),
      new Location(-121424, -220124, -6720)
   };

   private ChamberOfDelusionEast(String name, String descr) {
      super(name, descr, 127, 32658, 32664, 32668, 25690, 18838, "east_chamber_box");
      this._coords = _enterCoords;
   }

   public static void main(String[] args) {
      new ChamberOfDelusionEast(ChamberOfDelusionEast.class.getSimpleName(), "instances");
   }
}
