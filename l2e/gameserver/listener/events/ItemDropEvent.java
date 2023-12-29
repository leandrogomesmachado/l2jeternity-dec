package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemDropEvent implements EventListener {
   private ItemInstance _item;
   private Creature _dropper;
   private Location _loc;

   public ItemInstance getItem() {
      return this._item;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public Creature getDropper() {
      return this._dropper;
   }

   public void setDropper(Creature dropper) {
      this._dropper = dropper;
   }

   public Location getLocation() {
      return this._loc;
   }

   public void setLocation(Location loc) {
      this._loc = loc;
   }
}
