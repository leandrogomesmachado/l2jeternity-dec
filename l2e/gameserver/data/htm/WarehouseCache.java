package l2e.gameserver.data.htm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;

public class WarehouseCache {
   protected final Map<Player, Long> _cachedWh = new ConcurrentHashMap<>();
   protected final long _cacheTime = (long)Config.WAREHOUSE_CACHE_TIME * 60000L;

   protected WarehouseCache() {
      ThreadPoolManager.getInstance().scheduleAtFixedRate(new WarehouseCache.CacheScheduler(), 120000L, 60000L);
   }

   public void addCacheTask(Player pc) {
      this._cachedWh.put(pc, System.currentTimeMillis());
   }

   public void remCacheTask(Player pc) {
      this._cachedWh.remove(pc);
   }

   public static WarehouseCache getInstance() {
      return WarehouseCache.SingletonHolder._instance;
   }

   public class CacheScheduler implements Runnable {
      @Override
      public void run() {
         long cTime = System.currentTimeMillis();

         for(Player pc : WarehouseCache.this._cachedWh.keySet()) {
            if (cTime - WarehouseCache.this._cachedWh.get(pc) > WarehouseCache.this._cacheTime) {
               pc.clearWarehouse();
               WarehouseCache.this._cachedWh.remove(pc);
            }
         }
      }
   }

   private static class SingletonHolder {
      protected static final WarehouseCache _instance = new WarehouseCache();
   }
}
