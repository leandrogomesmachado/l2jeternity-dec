package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class ConditionZone extends ZoneType {
   private boolean NO_ITEM_DROP = false;
   private boolean NO_BOOKMARK = false;

   public ConditionZone(int id) {
      super(id);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equalsIgnoreCase("NoBookmark")) {
         this.NO_BOOKMARK = Boolean.parseBoolean(value);
         if (this.NO_BOOKMARK) {
            this.addZoneId(ZoneId.NO_BOOKMARK);
         }
      } else if (name.equalsIgnoreCase("NoItemDrop")) {
         this.NO_ITEM_DROP = Boolean.parseBoolean(value);
         if (this.NO_ITEM_DROP) {
            this.addZoneId(ZoneId.NO_ITEM_DROP);
         }
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }
}
