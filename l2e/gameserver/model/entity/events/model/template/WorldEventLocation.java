package l2e.gameserver.model.entity.events.model.template;

import l2e.gameserver.model.Location;

public class WorldEventLocation {
   private final String _name;
   private final Location _loc;

   public WorldEventLocation(String name, Location loc) {
      this._name = name;
      this._loc = loc;
   }

   public String getName() {
      return this._name;
   }

   public Location getLocation() {
      return this._loc;
   }
}
