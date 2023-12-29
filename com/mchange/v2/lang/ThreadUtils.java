package com.mchange.v2.lang;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.lang.reflect.Method;

public final class ThreadUtils {
   private static final MLogger logger = MLog.getLogger(ThreadUtils.class);
   static final Method holdsLock;

   public static void enumerateAll(Thread[] var0) {
      ThreadGroupUtils.rootThreadGroup().enumerate(var0);
   }

   public static Boolean reflectiveHoldsLock(Object var0) {
      try {
         return holdsLock == null ? null : (Boolean)holdsLock.invoke(null, var0);
      } catch (Exception var2) {
         if (logger.isLoggable(MLevel.FINER)) {
            logger.log(MLevel.FINER, "An Exception occurred while trying to call Thread.holdsLock( ... ) reflectively.", (Throwable)var2);
         }

         return null;
      }
   }

   private ThreadUtils() {
   }

   static {
      Method var0;
      try {
         var0 = Thread.class.getMethod("holdsLock", Object.class);
      } catch (NoSuchMethodException var2) {
         var0 = null;
      }

      holdsLock = var0;
   }
}
