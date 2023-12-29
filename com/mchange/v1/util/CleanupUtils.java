package com.mchange.v1.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** @deprecated */
public final class CleanupUtils {
   public static void attemptClose(Statement var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   public static void attemptClose(Connection var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   public static void attemptRollback(Connection var0) {
      try {
         if (var0 != null) {
            var0.rollback();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   private CleanupUtils() {
   }
}
