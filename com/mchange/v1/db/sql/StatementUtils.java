package com.mchange.v1.db.sql;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.sql.SQLException;
import java.sql.Statement;

public final class StatementUtils {
   private static final MLogger logger = MLog.getLogger(StatementUtils.class);

   public static boolean attemptClose(Statement var0) {
      try {
         if (var0 != null) {
            var0.close();
         }

         return true;
      } catch (SQLException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "Statement close FAILED.", (Throwable)var2);
         }

         return false;
      }
   }

   private StatementUtils() {
   }
}
