package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ItemDropEvent;
import l2e.gameserver.listener.events.ItemPickupEvent;
import l2e.gameserver.model.items.instance.ItemInstance;

public abstract class DropListener extends AbstractListener {
   public DropListener() {
      this.register();
   }

   public abstract boolean onDrop(ItemDropEvent var1);

   public abstract boolean onPickup(ItemPickupEvent var1);

   @Override
   public void register() {
      ItemInstance.addDropListener(this);
   }

   @Override
   public void unregister() {
      ItemInstance.removeDropListener(this);
   }
}
