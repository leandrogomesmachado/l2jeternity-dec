package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneType;

public class ConditionPlayerInsideZoneId extends Condition {
   private final List<Integer> _zones;

   public ConditionPlayerInsideZoneId(ArrayList<Integer> zones) {
      this._zones = zones;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() != null && !this._zones.isEmpty()) {
         List<ZoneType> zones = ZoneManager.getInstance().getZones(env.getCharacter());
         if (zones != null && !zones.isEmpty()) {
            for(ZoneType zone : zones) {
               if (zone != null && this._zones.contains(zone.getId())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
