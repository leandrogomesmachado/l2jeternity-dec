package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class ScriptZone extends ZoneType {
   public ScriptZone(int id) {
      super(id);
      this.addZoneId(ZoneId.SCRIPT);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }
}
