package l2e.scripts.instances.ChambersOfDelusion;

import l2e.gameserver.model.Location;

public final class ChamberOfDelusionSquare extends Chamber {
   private static final Location[] _enterCoords = new Location[]{
      new Location(-122368, -153388, -6688),
      new Location(-122368, -152524, -6688),
      new Location(-120480, -155116, -6688),
      new Location(-120480, -154236, -6688),
      new Location(-121440, -151212, -6688),
      new Location(-120464, -152908, -6688),
      new Location(-122368, -154700, -6688),
      new Location(-121440, -152908, -6688),
      new Location(-121440, -154572, -6688)
   };

   private ChamberOfDelusionSquare(int questId, String name, String descr) {
      super(name, descr, 131, 32662, 32684, 32692, 25694, 18820, "square_chamber_box");
      this._coords = _enterCoords;
   }

   public static void main(String[] args) {
      new ChamberOfDelusionSquare(-1, ChamberOfDelusionSquare.class.getSimpleName(), "instances");
   }
}
