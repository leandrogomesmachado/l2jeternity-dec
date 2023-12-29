package com.mchange.v1.io;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.IOException;
import java.io.OutputStream;

public final class OutputStreamUtils {
   private static final MLogger logger = MLog.getLogger(OutputStreamUtils.class);

   public static void attemptClose(OutputStream var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (IOException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "OutputStream close FAILED.", (Throwable)var2);
         }
      }
   }

   private OutputStreamUtils() {
   }
}
