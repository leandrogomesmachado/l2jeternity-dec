package com.mchange.v1.util;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;

public final class ClosableResourceUtils {
   private static final MLogger logger = MLog.getLogger(ClosableResourceUtils.class);

   public static Exception attemptClose(ClosableResource var0) {
      try {
         if (var0 != null) {
            var0.close();
         }

         return null;
      } catch (Exception var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "CloseableResource close FAILED.", (Throwable)var2);
         }

         return var2;
      }
   }

   private ClosableResourceUtils() {
   }
}
