package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class DerbyTrackZone extends ZoneType {
   public DerbyTrackZone(int id) {
      super(id);
      this.addZoneId(ZoneId.MONSTER_TRACK);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }
}
