package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneType;

public class HotSpringZone extends ZoneType {
   public HotSpringZone(int id) {
      super(id);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }

   public int getWaterZ() {
      return this.getZone().getHighZ();
   }
}
