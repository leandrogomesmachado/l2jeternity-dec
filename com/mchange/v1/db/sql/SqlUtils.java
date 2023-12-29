package com.mchange.v1.db.sql;

import com.mchange.lang.ThrowableUtils;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** @deprecated */
public final class SqlUtils {
   static final DateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");

   public static String escapeBadSqlPatternChars(String var0) {
      StringBuffer var1 = new StringBuffer(var0);
      int var2 = 0;

      for(int var3 = var1.length(); var2 < var3; ++var2) {
         if (var1.charAt(var2) == '\'') {
            var1.insert(var2, '\'');
            ++var3;
            var2 += 2;
         }
      }

      return var1.toString();
   }

   public static String escapeAsTimestamp(Date var0) {
      return "{ts '" + tsdf.format(var0) + "'}";
   }

   public static SQLException toSQLException(Throwable var0) {
      if (var0 instanceof SQLException) {
         return (SQLException)var0;
      } else {
         var0.printStackTrace();
         return new SQLException(ThrowableUtils.extractStackTrace(var0));
      }
   }

   private SqlUtils() {
   }
}
