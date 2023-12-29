package l2e.gameserver.model.holders;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.interfaces.ILocational;

public class NpcRoutesHolder {
   private final Map<String, String> _correspondences = new HashMap<>();

   public void addRoute(String routeName, Location loc) {
      this._correspondences.put(this.getUniqueKey(loc), routeName);
   }

   public String getRouteName(Npc npc) {
      if (npc.getSpawn() != null) {
         String key = this.getUniqueKey(npc.getSpawn().getLocation());
         return this._correspondences.containsKey(key) ? this._correspondences.get(key) : "";
      } else {
         return "";
      }
   }

   private String getUniqueKey(ILocational loc) {
      return loc.getX() + "-" + loc.getY() + "-" + loc.getZ();
   }
}
