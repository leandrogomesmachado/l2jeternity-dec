package com.mchange.v2.management;

import java.util.Comparator;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class ManagementUtils {
   public static final Comparator PARAM_INFO_COMPARATOR = new Comparator() {
      @Override
      public int compare(Object var1, Object var2) {
         MBeanParameterInfo var3 = (MBeanParameterInfo)var1;
         MBeanParameterInfo var4 = (MBeanParameterInfo)var2;
         int var5 = var3.getType().compareTo(var4.getType());
         if (var5 == 0) {
            var5 = var3.getName().compareTo(var4.getName());
            if (var5 == 0) {
               String var6 = var3.getDescription();
               String var7 = var4.getDescription();
               if (var6 == null && var7 == null) {
                  var5 = 0;
               } else if (var6 == null) {
                  var5 = -1;
               } else if (var7 == null) {
                  var5 = 1;
               } else {
                  var5 = var6.compareTo(var7);
               }
            }
         }

         return var5;
      }
   };
   public static final Comparator OP_INFO_COMPARATOR = new Comparator() {
      @Override
      public int compare(Object var1, Object var2) {
         MBeanOperationInfo var3 = (MBeanOperationInfo)var1;
         MBeanOperationInfo var4 = (MBeanOperationInfo)var2;
         String var5 = var3.getName();
         String var6 = var4.getName();
         int var7 = String.CASE_INSENSITIVE_ORDER.compare(var5, var6);
         if (var7 == 0) {
            if (var5.equals(var6)) {
               MBeanParameterInfo[] var8 = var3.getSignature();
               MBeanParameterInfo[] var9 = var4.getSignature();
               if (var8.length < var9.length) {
                  var7 = -1;
               } else if (var8.length > var9.length) {
                  var7 = 1;
               } else {
                  int var10 = 0;

                  for(int var11 = var8.length; var10 < var11; ++var10) {
                     var7 = ManagementUtils.PARAM_INFO_COMPARATOR.compare(var8[var10], var9[var10]);
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
