package com.mchange.v2.log;

import com.mchange.v1.util.StringTokenizerUtils;
import com.mchange.v2.cfg.MultiPropertiesConfig;
import java.util.ArrayList;

public abstract class MLog {
   private static NameTransformer _transformer;
   private static MLog _mlog;
   private static MLogger _logger;

   private static synchronized NameTransformer transformer() {
      return _transformer;
   }

   private static synchronized MLog mlog() {
      return _mlog;
   }

   private static synchronized MLogger logger() {
      return _logger;
   }

   public static synchronized void refreshConfig(MultiPropertiesConfig[] var0, String var1) {
      MLogConfig.refresh(var0, var1);
      String var2 = MLogConfig.getProperty("com.mchange.v2.log.MLog");
      String[] var3 = null;
      if (var2 == null) {
         var2 = MLogConfig.getProperty("com.mchange.v2.log.mlog");
      }

      if (var2 != null) {
         var3 = StringTokenizerUtils.tokenizeToArray(var2, ", \t\r\n");
      }

      boolean var4 = false;
      Object var5 = null;
      if (var3 != null) {
         var5 = findByClassnames(var3, true);
      }

      if (var5 == null) {
         var5 = findByClassnames(MLogClasses.SEARCH_CLASSNAMES, false);
      }

      if (var5 == null) {
         var4 = true;
         var5 = new FallbackMLog();
      }

      _mlog = (MLog)var5;
      if (var4) {
         info("Using " + _mlog.getClass().getName() + " -- Named logger's not supported, everything goes to System.err.");
      }

      NameTransformer var6 = null;
      String var7 = MLogConfig.getProperty("com.mchange.v2.log.NameTransformer");
      if (var7 == null) {
         var7 = MLogConfig.getProperty("com.mchange.v2.log.nametransformer");
      }

      try {
         if (var7 != null) {
            var6 = (NameTransformer)Class.forName(var7).newInstance();
         }
      } catch (Exception var9) {
         System.err.println("Failed to instantiate com.mchange.v2.log.NameTransformer '" + var7 + "'!");
         var9.printStackTrace();
      }

      _transformer = var6;
      _logger = getLogger(MLog.class);
      Thread var8 = new Thread("MLog-Init-Reporter") {
         final MLogger logo = MLog._logger;
         String loggerDesc = MLog._mlog.getClass().getName();

         @Override
         public void run() {
            if ("com.mchange.v2.log.jdk14logging.Jdk14MLog".equals(this.loggerDesc)) {
               this.loggerDesc = "java 1.4+ standard";
            } else if ("com.mchange.v2.log.log4j.Log4jMLog".equals(this.loggerDesc)) {
               this.loggerDesc = "log4j";
            } else if ("com.mchange.v2.log.slf4j.Slf4jMLog".equals(this.loggerDesc)) {
               this.loggerDesc = "slf4j";
            }

            if (this.logo.isLoggable(MLevel.INFO)) {
               this.logo.log(MLevel.INFO, "MLog clients using " + this.loggerDesc + " logging.");
            }

            MLogConfig.logDelayedItems(this.logo);
            if (this.logo.isLoggable(MLevel.FINEST)) {
               this.logo.log(MLevel.FINEST, "Config available to MLog library: " + MLogConfig.dump());
            }
         }
      };
      var8.start();
   }

   public static MLog findByClassnames(String[] var0, boolean var1) {
      ArrayList var2 = null;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         try {
            return (MLog)Class.forName(MLogClasses.resolveIfAlias(var0[var3])).newInstance();
         } catch (Exception var6) {
            if (var2 == null) {
               var2 = new ArrayList();
            }

            var2.add(var0[var3]);
            if (var1) {
               System.err.println("com.mchange.v2.log.MLog '" + var0[var3] + "' could not be loaded!");
               var6.printStackTrace();
            }
         }
      }

      System.err.println("Tried without success to load the following MLog classes:");
      var3 = 0;

      for(int var8 = var2.size(); var3 < var8; ++var3) {
         System.err.println("\t" + var2.get(var3));
      }

      return null;
   }

   public static MLog instance() {
      return mlog();
   }

   public static MLogger getLogger(String var0) {
      NameTransformer var1 = null;
      MLog var2 = null;
      synchronized(MLog.class) {
         var1 = transformer();
         var2 = instance();
      }

      MLogger var8;
      if (var1 == null) {
         var8 = instance().getMLogger(var0);
      } else {
         String var4 = var1.transformName(var0);
         if (var4 != null) {
            var8 = var2.getMLogger(var4);
         } else {
            var8 = var2.getMLogger(var0);
         }
      }

      return var8;
   }

   public static MLogger getLogger(Class var0) {
      NameTransformer var1 = null;
      MLog var2 = null;
      synchronized(MLog.class) {
         var1 = transformer();
         var2 = instance();
      }

      MLogger var8;
      if (var1 == null) {
         var8 = var2.getMLogger(var0);
      } else {
         String var4 = var1.transformName(var0);
         if (var4 != null) {
            var8 = var2.getMLogger(var4);
         } else {
            var8 = var2.getMLogger(var0);
         }
      }

      return var8;
   }

   public static MLogger getLogger() {
      NameTransformer var0 = null;
      MLog var1 = null;
      synchronized(MLog.class) {
         var0 = transformer();
         var1 = instance();
      }

      MLogger var7;
      if (var0 == null) {
         var7 = var1.getMLogger();
      } else {
         String var3 = var0.transformName();
         if (var3 != null) {
            var7 = var1.getMLogger(var3);
         } else {
            var7 = var1.getMLogger();
         }
      }

      return var7;
   }

   public static void log(MLevel var0, String var1) {
      instance();
      getLogger().log(var0, var1);
   }

   public static void log(MLevel var0, String var1, Object var2) {
      instance();
      getLogger().log(var0, var1, var2);
   }

   public static void log(MLevel var0, String var1, Object[] var2) {
      instance();
      getLogger().log(var0, var1, var2);
   }

   public static void log(MLevel var0, String var1, Throwable var2) {
      instance();
      getLogger().log(var0, var1, var2);
   }

   public static void logp(MLevel var0, String var1, String var2, String var3) {
      instance();
      getLogger().logp(var0, var1, var2, var3);
   }

   public static void logp(MLevel var0, String var1, String var2, String var3, Object var4) {
      instance();
      getLogger().logp(var0, var1, var2, var3, var4);
   }

   public static void logp(MLevel var0, String var1, String var2, String var3, Object[] var4) {
      instance();
      getLogger().logp(var0, var1, var2, var3, var4);
   }

   public static void logp(MLevel var0, String var1, String var2, String var3, Throwable var4) {
      instance();
      getLogger().logp(var0, var1, var2, var3, var4);
   }

   public static void logrb(MLevel var0, String var1, String var2, String var3, String var4) {
      instance();
      getLogger().logp(var0, var1, var2, var3, var4);
   }

   public static void logrb(MLevel var0, String var1, String var2, String var3, String var4, Object var5) {
      instance();
      getLogger().logrb(var0, var1, var2, var3, var4, var5);
   }

   public static void logrb(MLevel var0, String var1, String var2, String var3, String var4, Object[] var5) {
      instance();
      getLogger().logrb(var0, var1, var2, var3, var4, var5);
   }

   public static void logrb(MLevel var0, String var1, String var2, String var3, String var4, Throwable var5) {
      instance();
      getLogger().logrb(var0, var1, var2, var3, var4, var5);
   }

   public static void entering(String var0, String var1) {
      instance();
      getLogger().entering(var0, var1);
   }

   public static void entering(String var0, String var1, Object var2) {
      instance();
      getLogger().entering(var0, var1, var2);
   }

   public static void entering(String var0, String var1, Object[] var2) {
      instance();
      getLogger().entering(var0, var1, var2);
   }

   public static void exiting(String var0, String var1) {
      instance();
      getLogger().exiting(var0, var1);
   }

   public static void exiting(String var0, String var1, Object var2) {
      instance();
      getLogger().exiting(var0, var1, var2);
   }

   public static void throwing(String var0, String var1, Throwable var2) {
      instance();
      getLogger().throwing(var0, var1, var2);
   }

   public static void severe(String var0) {
      instance();
      getLogger().severe(var0);
   }

   public static void warning(String var0) {
      instance();
      getLogger().warning(var0);
   }

   public static void info(String var0) {
      instance();
      getLogger().info(var0);
   }

   public static void config(String var0) {
      instance();
      getLogger().config(var0);
   }

   public static void fine(String var0) {
      instance();
      getLogger().fine(var0);
   }

   public static void finer(String var0) {
      instance();
      getLogger().finer(var0);
   }

   public static void finest(String var0) {
      instance();
      getLogger().finest(var0);
   }

   public MLogger getMLogger(Class var1) {
      return this.getMLogger(var1.getName());
   }

   public abstract MLogger getMLogger(String var1);

   public abstract MLogger getMLogger();

   static {
      refreshConfig(null, null);
   }
}
