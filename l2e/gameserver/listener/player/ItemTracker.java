package l2e.gameserver.listener.player;

import java.util.List;
import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.AddToInventoryEvent;
import l2e.gameserver.listener.events.ItemDestroyEvent;
import l2e.gameserver.listener.events.ItemDropEvent;
import l2e.gameserver.listener.events.ItemTransferEvent;
import l2e.gameserver.model.items.itemcontainer.PcInventory;

public abstract class ItemTracker extends AbstractListener {
   private final List<Integer> _itemIds;

   public ItemTracker(List<Integer> itemIds) {
      this._itemIds = itemIds;
      this.register();
   }

   public abstract void onDrop(ItemDropEvent var1);

   public abstract void onAddToInventory(AddToInventoryEvent var1);

   public abstract void onDestroy(ItemDestroyEvent var1);

   public abstract void onTransfer(ItemTransferEvent var1);

   @Override
   public void register() {
      PcInventory.addItemTracker(this);
   }

   @Override
   public void unregister() {
      PcInventory.removeItemTracker(this);
   }

   public boolean containsItemId(int itemId) {
      return this._itemIds.contains(itemId);
   }
}
