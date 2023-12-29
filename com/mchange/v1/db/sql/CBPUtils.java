package com.mchange.v1.db.sql;

public class CBPUtils {
   public static void attemptCheckin(ConnectionBundle var0, ConnectionBundlePool var1) {
      try {
         var1.checkinBundle(var0);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
}
