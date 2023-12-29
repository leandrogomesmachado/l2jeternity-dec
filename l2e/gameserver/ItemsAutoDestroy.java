package l2e.gameserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemsAutoDestroy {
   private final Map<Integer, ItemInstance> _items = new ConcurrentHashMap<>();
   protected static long _sleep;

   protected ItemsAutoDestroy() {
      _sleep = (long)(Config.AUTODESTROY_ITEM_AFTER * 1000);
      if (_sleep == 0L) {
         _sleep = 3600000L;
      }

      ThreadPoolManager.getInstance().scheduleAtFixedRate(new ItemsAutoDestroy.CheckItemsForDestroy(), 5000L, 5000L);
   }

   public static ItemsAutoDestroy getInstance() {
      return ItemsAutoDestroy.SingletonHolder._instance;
   }

   public synchronized void addItem(ItemInstance item) {
      item.setDropTime(System.currentTimeMillis());
      this._items.put(item.getObjectId(), item);
   }

   public synchronized void removeItems() {
      if (!this._items.isEmpty()) {
         long curtime = System.currentTimeMillis();

         for(ItemInstance item : this._items.values()) {
            if (item == null || item.getDropTime() == 0L || item.getItemLocation() != ItemInstance.ItemLocation.VOID) {
               this._items.remove(item.getObjectId());
            } else if (item.getItem().getAutoDestroyTime() > 0) {
               if (curtime - item.getDropTime() > (long)item.getItem().getAutoDestroyTime()) {
                  item.decayMe();
                  this._items.remove(item.getObjectId());
                  if (Config.SAVE_DROPPED_ITEM) {
                     ItemsOnGroundManager.getInstance().removeObject(item);
                  }
               }
            } else if (item.getItem().isHerb()) {
               if (curtime - item.getDropTime() > (long)Config.HERB_AUTO_DESTROY_TIME) {
                  item.decayMe();
                  this._items.remove(item.getObjectId());
                  if (Config.SAVE_DROPPED_ITEM) {
                     ItemsOnGroundManager.getInstance().removeObject(item);
                  }
               }
            } else if (curtime - item.getDropTime() > _sleep) {
               item.decayMe();
               this._items.remove(item.getObjectId());
               if (Config.SAVE_DROPPED_ITEM) {
                  ItemsOnGroundManager.getInstance().removeObject(item);
               }
            }
         }
      }
   }

   protected class CheckItemsForDestroy extends Thread {
      @Override
      public void run() {
         ItemsAutoDestroy.this.removeItems();
      }
   }

   private static class SingletonHolder {
      protected static final ItemsAutoDestroy _instance = new ItemsAutoDestroy();
   }
}
