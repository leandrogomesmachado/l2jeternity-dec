package l2e.gameserver.model.items.itemcontainer;

import java.util.LinkedList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.listener.clan.ClanWarehouseListener;
import l2e.gameserver.listener.events.ClanWarehouseAddItemEvent;
import l2e.gameserver.listener.events.ClanWarehouseDeleteItemEvent;
import l2e.gameserver.listener.events.ClanWarehouseTransferEvent;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class ClanWarehouse extends Warehouse {
   private final Clan _clan;
   private final List<ClanWarehouseListener> clanWarehouseListeners = new LinkedList<>();

   public ClanWarehouse(Clan clan) {
      this._clan = clan;
   }

   @Override
   public String getName() {
      return "ClanWarehouse";
   }

   @Override
   public int getOwnerId() {
      return this._clan.getId();
   }

   public Player getOwner() {
      return this._clan.getLeader().getPlayerInstance();
   }

   @Override
   public ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.CLANWH;
   }

   public String getLocationId() {
      return "0";
   }

   public int getLocationId(boolean dummy) {
      return 0;
   }

   public void setLocationId(Player dummy) {
   }

   @Override
   public boolean validateCapacity(long slots) {
      return (long)this._items.size() + slots <= (long)Config.WAREHOUSE_SLOTS_CLAN;
   }

   @Override
   public ItemInstance addItem(String process, int itemId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByItemId(itemId);
      return !this.fireClanWarehouseAddItemListeners(process, item, actor, count) ? null : super.addWareHouseItem(process, itemId, count, actor, reference);
   }

   @Override
   public ItemInstance addItem(String process, ItemInstance item, Player actor, Object reference) {
      return !this.fireClanWarehouseAddItemListeners(process, item, actor, item.getCount()) ? null : super.addWaheHouseItem(process, item, actor, reference);
   }

   @Override
   public ItemInstance destroyItem(String process, ItemInstance item, long count, Player actor, Object reference) {
      return !this.fireClanWarehouseDeleteItemListeners(process, item, actor, count) ? null : super.destroyItem(process, item, count, actor, reference);
   }

   @Override
   public ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, Player actor, Object reference) {
      ItemInstance sourceitem = this.getItemByObjectId(objectId);
      return !this.fireClanWarehouseTransferListeners(process, sourceitem, count, target, actor)
         ? null
         : super.transferItem(process, objectId, count, target, actor, reference);
   }

   private boolean fireClanWarehouseAddItemListeners(String process, ItemInstance item, Player actor, long count) {
      if (!this.clanWarehouseListeners.isEmpty() && actor != null && item != null) {
         ClanWarehouseAddItemEvent event = new ClanWarehouseAddItemEvent();
         event.setActor(actor);
         event.setItem(item);
         event.setCount(count);
         event.setProcess(process);

         for(ClanWarehouseListener listener : this.clanWarehouseListeners) {
            if (!listener.onAddItem(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireClanWarehouseDeleteItemListeners(String process, ItemInstance item, Player actor, long count) {
      if (!this.clanWarehouseListeners.isEmpty() && actor != null && item != null) {
         ClanWarehouseDeleteItemEvent event = new ClanWarehouseDeleteItemEvent();
         event.setActor(actor);
         event.setCount(count);
         event.setItem(item);
         event.setProcess(process);

         for(ClanWarehouseListener listener : this.clanWarehouseListeners) {
            if (!listener.onDeleteItem(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireClanWarehouseTransferListeners(String process, ItemInstance item, long count, ItemContainer target, Player actor) {
      if (!this.clanWarehouseListeners.isEmpty() && actor != null && item != null && target != null) {
         ClanWarehouseTransferEvent event = new ClanWarehouseTransferEvent();
         event.setActor(actor);
         event.setCount(count);
         event.setItem(item);
         event.setProcess(process);
         event.setTarget(target);

         for(ClanWarehouseListener listener : this.clanWarehouseListeners) {
            if (!listener.onTransferItem(event)) {
               return false;
            }
         }
      }

      return true;
   }

   public void addWarehouseListener(ClanWarehouseListener listener) {
      if (!this.clanWarehouseListeners.contains(listener)) {
         this.clanWarehouseListeners.add(listener);
      }
   }

   public void removeWarehouseListener(ClanWarehouseListener listener) {
      this.clanWarehouseListeners.remove(listener);
   }
}
