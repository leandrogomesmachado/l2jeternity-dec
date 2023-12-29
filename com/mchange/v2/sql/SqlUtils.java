package com.mchange.v2.sql;

import com.mchange.lang.ThrowableUtils;
import com.mchange.v2.lang.VersionUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class SqlUtils {
   static final MLogger logger = MLog.getLogger(SqlUtils.class);
   static final DateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
   public static final String DRIVER_MANAGER_USER_PROPERTY = "user";
   public static final String DRIVER_MANAGER_PASSWORD_PROPERTY = "password";

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

   public static synchronized String escapeAsTimestamp(Date var0) {
      return "{ts '" + tsdf.format(var0) + "'}";
   }

   public static SQLException toSQLException(Throwable var0) {
      return toSQLException(null, var0);
   }

   public static SQLException toSQLException(String var0, Throwable var1) {
      return toSQLException(var0, null, var1);
   }

   public static SQLException toSQLException(String var0, String var1, Throwable var2) {
      if (!(var2 instanceof SQLException)) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "Converting Throwable to SQLException...", var2);
         }

         if (var0 == null) {
            var0 = "An SQLException was provoked by the following failure: " + var2.toString();
         }

         if (VersionUtils.isAtLeastJavaVersion14()) {
            SQLException var6 = new SQLException(var0);
            var6.initCause(var2);
            return var6;
         } else {
            return new SQLException(var0 + System.getProperty("line.separator") + "[Cause: " + ThrowableUtils.extractStackTrace(var2) + ']', var1);
         }
      } else {
         if (logger.isLoggable(MLevel.FINER)) {
            SQLException var3 = (SQLException)var2;
            StringBuffer var4 = new StringBuffer(255);
            var4.append("Attempted to convert SQLException to SQLException. Leaving it alone.");
            var4.append(" [SQLState: ");
            var4.append(var3.getSQLState());
            var4.append("; errorCode: ");
            var4.append(var3.getErrorCode());
            var4.append(']');
            if (var0 != null) {
               var4.append(" Ignoring suggested message: '" + var0 + "'.");
            }

            logger.log(MLevel.FINER, var4.toString(), var2);
            SQLException var5 = var3;

            while((var5 = var5.getNextException()) != null) {
               logger.log(MLevel.FINER, "Nested SQLException or SQLWarning: ", (Throwable)var5);
            }
         }

         return (SQLException)var2;
      }
   }

   public static SQLClientInfoException toSQLClientInfoException(Throwable var0) {
      if (var0 instanceof SQLClientInfoException) {
         return (SQLClientInfoException)var0;
      } else if (var0.getCause() instanceof SQLClientInfoException) {
         return (SQLClientInfoException)var0.getCause();
      } else if (var0 instanceof SQLException) {
         SQLException var1 = (SQLException)var0;
         return new SQLClientInfoException(var1.getMessage(), var1.getSQLState(), var1.getErrorCode(), null, var0);
      } else {
         return new SQLClientInfoException(var0.getMessage(), null, var0);
      }
   }

   private SqlUtils() {
   }
}
