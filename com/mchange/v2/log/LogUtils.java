package com.mchange.v2.log;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class LogUtils {
   public static String createParamsList(Object[] var0) {
      StringBuffer var1 = new StringBuffer(511);
      appendParamsList(var1, var0);
      return var1.toString();
   }

   public static void appendParamsList(StringBuffer var0, Object[] var1) {
      var0.append("[params: ");
      if (var1 != null) {
         int var2 = 0;

         for(int var3 = var1.length; var2 < var3; ++var2) {
            if (var2 != 0) {
               var0.append(", ");
            }

            var0.append(var1[var2]);
         }
      }

      var0.append(']');
   }

   public static String createMessage(String var0, String var1, String var2) {
      StringBuffer var3 = new StringBuffer(511);
      var3.append("[class: ");
      var3.append(var0);
      var3.append("; method: ");
      var3.append(var1);
      if (!var1.endsWith(")")) {
         var3.append("()");
      }

      var3.append("] ");
      var3.append(var2);
      return var3.toString();
   }

   public static String createMessage(String var0, String var1) {
      StringBuffer var2 = new StringBuffer(511);
      var2.append("[method: ");
      var2.append(var0);
      if (!var0.endsWith(")")) {
         var2.append("()");
      }

      var2.append("] ");
      var2.append(var1);
      return var2.toString();
   }

   public static String formatMessage(String var0, String var1, Object[] var2) {
      if (var1 == null) {
         return var2 == null ? "" : createParamsList(var2);
      } else {
         if (var0 != null) {
            ResourceBundle var3 = ResourceBundle.getBundle(var0);
            if (var3 != null) {
               String var4 = var3.getString(var1);
               if (var4 != null) {
                  var1 = var4;
               }
            }
         }

         return var2 == null ? var1 : MessageFormat.format(var1, var2);
      }
   }

   private LogUtils() {
   }
}
