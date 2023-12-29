package l2e.gameserver.listener.events;

import l2e.commons.listener.Listener;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneType;

public interface OnZoneEnterLeaveListener extends Listener<ZoneType> {
   void onZoneEnter(ZoneType var1, Creature var2);

   void onZoneLeave(ZoneType var1, Creature var2);
}
