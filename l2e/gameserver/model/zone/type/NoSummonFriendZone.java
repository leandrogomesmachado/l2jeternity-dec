package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class NoSummonFriendZone extends ZoneType {
   public NoSummonFriendZone(int id) {
      super(id);
      this.addZoneId(ZoneId.NO_SUMMON_FRIEND);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }
}
