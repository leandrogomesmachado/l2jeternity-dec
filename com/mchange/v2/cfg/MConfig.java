package com.mchange.v2.cfg;

import com.mchange.v1.cachedstore.CachedStore;
import com.mchange.v1.cachedstore.CachedStoreException;
import com.mchange.v1.cachedstore.CachedStoreFactory;
import com.mchange.v1.cachedstore.CachedStoreUtils;
import com.mchange.v1.util.ArrayUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MConfig {
   private static final MLogger logger = MLog.getLogger(MConfig.class);
   private static final Map<DelayedLogItem.Level, MLevel> levelMap;
   static final CachedStore cache;

   public static MultiPropertiesConfig readVmConfig(String[] var0, String[] var1) {
      try {
         return (MultiPropertiesConfig)cache.find(new MConfig.PathsKey(var0, var1));
      } catch (CachedStoreException var3) {
         throw new RuntimeException(var3);
      }
   }

   public static MultiPropertiesConfig readVmConfig() {
      return readVmConfig(ConfigUtils.NO_PATHS, ConfigUtils.NO_PATHS);
   }

   public static MultiPropertiesConfig readConfig(String[] var0) {
      try {
         return (MultiPropertiesConfig)cache.find(new MConfig.PathsKey(var0));
      } catch (CachedStoreException var2) {
         throw new RuntimeException(var2);
      }
   }

   public static MultiPropertiesConfig combine(MultiPropertiesConfig[] var0) {
      return ConfigUtils.combine(var0);
   }

   public static void dumpToLogger(List<DelayedLogItem> var0, MLogger var1) {
      for(DelayedLogItem var3 : var0) {
         dumpToLogger(var3, var1);
      }
   }

   public static void dumpToLogger(DelayedLogItem var0, MLogger var1) {
      var1.log(levelMap.get(var0.getLevel()), var0.getText(), var0.getException());
   }

   private MConfig() {
   }

   static {
      try {
         HashMap var0 = new HashMap();

         for(DelayedLogItem.Level var4 : DelayedLogItem.Level.values()) {
            var0.put(var4, (MLevel)MLevel.class.getField(var4.toString()).get(null));
         }

         levelMap = Collections.unmodifiableMap(var0);
      } catch (RuntimeException var5) {
         var5.printStackTrace();
         throw var5;
      } catch (Exception var6) {
         var6.printStackTrace();
         throw new RuntimeException(var6);
      }

      cache = CachedStoreUtils.synchronizedCachedStore(CachedStoreFactory.createNoCleanupCachedStore(new MConfig.CSManager()));
   }

   private static class CSManager implements CachedStore.Manager {
      private CSManager() {
      }

      @Override
      public boolean isDirty(Object var1, Object var2) throws Exception {
         return false;
      }

      @Override
      public Object recreateFromKey(Object var1) throws Exception {
         MConfig.PathsKey var2 = (MConfig.PathsKey)var1;
         ArrayList var3 = new ArrayList();
         var3.addAll(var2.delayedLogItems);
         MultiPropertiesConfig var4 = ConfigUtils.read(var2.paths, var3);
         MConfig.dumpToLogger(var3, MConfig.logger);
         return var4;
      }
   }

   private static final class PathsKey {
      String[] paths;
      List delayedLogItems;

      @Override
      public boolean equals(Object var1) {
         return var1 instanceof MConfig.PathsKey ? Arrays.equals((Object[])this.paths, (Object[])((MConfig.PathsKey)var1).paths) : false;
      }

      @Override
      public int hashCode() {
         return ArrayUtils.hashArray((Object[])this.paths);
      }

      PathsKey(String[] var1, String[] var2) {
         this.delayedLogItems = new ArrayList();
         List var3 = ConfigUtils.vmCondensedPaths(var1, var2, this.delayedLogItems);
         this.paths = var3.toArray(new String[var3.size()]);
      }

      PathsKey(String[] var1) {
         this.delayedLogItems = Collections.emptyList();
         this.paths = var1;
      }
   }
}
