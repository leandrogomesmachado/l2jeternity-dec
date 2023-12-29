package com.mchange.v2.lang.reflect;

import java.lang.reflect.Method;
import java.util.Comparator;

public final class MethodUtils {
   public static final Comparator METHOD_COMPARATOR = new Comparator() {
      @Override
      public int compare(Object var1, Object var2) {
         Method var3 = (Method)var1;
         Method var4 = (Method)var2;
         String var5 = var3.getName();
         String var6 = var4.getName();
         int var7 = String.CASE_INSENSITIVE_ORDER.compare(var5, var6);
         if (var7 == 0) {
            if (var5.equals(var6)) {
               Class[] var8 = var3.getParameterTypes();
               Class[] var9 = var4.getParameterTypes();
               if (var8.length < var9.length) {
                  var7 = -1;
               } else if (var8.length > var9.length) {
                  var7 = 1;
               } else {
                  int var10 = 0;

                  for(int var11 = var8.length; var10 < var11; ++var10) {
                     String var12 = var8[var10].getName();
                     String var13 = var9[var10].getName();
                     var7 = var12.compareTo(var13);
                     if (var7 != 0) {
                        break;
                     }
                  }
               }
            } else {
               var7 = var5.compareTo(var6);
            }
         }

         return var7;
      }
   };
}
