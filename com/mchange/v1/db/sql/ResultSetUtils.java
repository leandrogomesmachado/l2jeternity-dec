package com.mchange.v1.db.sql;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ResultSetUtils {
   private static final MLogger logger = MLog.getLogger(ResultSetUtils.class);

   public static boolean attemptClose(ResultSet var0) {
      try {
         if (var0 != null) {
            var0.close();
         }

         return true;
      } catch (SQLException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "ResultSet close FAILED.", (Throwable)var2);
         }

         return false;
      }
   }

   private ResultSetUtils() {
   }
}
