package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class NoGeoZone extends ZoneType {
   public NoGeoZone(int id) {
      super(id);
      this.addZoneId(ZoneId.NO_GEO);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }
}
