package com.mchange.v1.db.sql;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionUtils {
   private static final MLogger logger = MLog.getLogger(ConnectionUtils.class);

   public static boolean attemptClose(Connection var0) {
      try {
         if (var0 != null) {
            var0.close();
         }

         return true;
      } catch (SQLException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "Connection close FAILED.", (Throwable)var2);
         }

         return false;
      }
   }

   public static boolean attemptRollback(Connection var0) {
      try {
         if (var0 != null) {
            var0.rollback();
         }

         return true;
      } catch (SQLException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "Rollback FAILED.", (Throwable)var2);
         }

         return false;
      }
   }

   private ConnectionUtils() {
   }
}
