package l2e.gameserver.model.entity.events.model.template;

import l2e.gameserver.model.Location;

public class WorldEventSpawn {
   private final int _npcId;
   private final Location _loc;

   public WorldEventSpawn(int npcId, Location loc) {
      this._npcId = npcId;
      this._loc = loc;
   }

   public int getNpcId() {
      return this._npcId;
   }

   public Location getLocation() {
      return this._loc;
   }
}
