package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class LandingZone extends ZoneType {
   public LandingZone(int id) {
      super(id);
      this.addZoneId(ZoneId.LANDING);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }
}
