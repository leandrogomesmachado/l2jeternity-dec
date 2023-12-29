package com.mchange.v2.log;

import com.mchange.v2.cfg.DelayedLogItem;
import com.mchange.v2.cfg.MConfig;
import com.mchange.v2.cfg.MLogConfigSource;
import com.mchange.v2.cfg.MultiPropertiesConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class MLogConfig {
   private static MultiPropertiesConfig CONFIG = null;
   private static List BOOTSTRAP_LOG_ITEMS = null;
   private static Method delayedDumpToLogger = null;

   public static synchronized void refresh(MultiPropertiesConfig[] var0, String var1) {
      String[] var2 = new String[]{"/com/mchange/v2/log/default-mchange-log.properties"};
      String[] var3 = new String[]{"/mchange-log.properties", "/"};
      ArrayList var4 = new ArrayList();
      MultiPropertiesConfig var5 = MLogConfigSource.readVmConfig(var2, var3, var4);
      boolean var6 = CONFIG == null;
      if (var0 != null) {
         int var7 = var0.length;
         MultiPropertiesConfig[] var8 = new MultiPropertiesConfig[var7 + 1];
         var8[0] = var5;

         for(int var9 = 0; var9 < var7; ++var9) {
            var8[var9 + 1] = var0[var9];
         }

         var4.add(
            new DelayedLogItem(
               DelayedLogItem.Level.INFO,
               (var6 ? "Loaded" : "Refreshed") + " MLog library log configuration, with overrides" + (var1 == null ? "." : ": " + var1)
            )
         );
         CONFIG = MConfig.combine(var8);
      } else {
         if (!var6) {
            var4.add(new DelayedLogItem(DelayedLogItem.Level.INFO, "Refreshed MLog library log configuration, without overrides."));
         }

         CONFIG = var5;
      }

      BOOTSTRAP_LOG_ITEMS = var4;
   }

   private static void ensureLoad() {
      if (CONFIG == null) {
         refresh(null, null);
      }
   }

   private static void ensureDelayedDumpToLogger() {
      try {
         if (delayedDumpToLogger == null) {
            Class var0 = Class.forName("com.mchange.v2.cfg.MConfig");
            Class var1 = Class.forName("com.mchange.v2.cfg.DelayedLogItem");
            delayedDumpToLogger = var0.getMethod("dumpToLogger", var1, MLogger.class);
         }
      } catch (RuntimeException var2) {
         var2.printStackTrace();
         throw var2;
      } catch (Exception var3) {
         var3.printStackTrace();
         throw new RuntimeException(var3);
      }
   }

   public static synchronized String getProperty(String var0) {
      ensureLoad();
      return CONFIG.getProperty(var0);
   }

   public static synchronized void logDelayedItems(MLogger var0) {
      ensureLoad();
      ensureDelayedDumpToLogger();
      ArrayList var1 = new ArrayList();
      var1.addAll(BOOTSTRAP_LOG_ITEMS);
      var1.addAll(CONFIG.getDelayedLogItems());
      HashSet var2 = new HashSet();
      var2.addAll(var1);

      for(Object var4 : var1) {
         if (var2.contains(var4)) {
            var2.remove(var4);

            try {
               delayedDumpToLogger.invoke(null, var4, var0);
            } catch (Exception var6) {
               var6.printStackTrace();
               throw new Error(var6);
            }
         }
      }
   }

   public static synchronized String dump() {
      return CONFIG.toString();
   }

   private MLogConfig() {
   }
}
