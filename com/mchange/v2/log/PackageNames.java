package com.mchange.v2.log;

public class PackageNames implements NameTransformer {
   @Override
   public String transformName(String var1) {
      return null;
   }

   @Override
   public String transformName(Class var1) {
      String var2 = var1.getName();
      int var3 = var2.lastIndexOf(46);
      return var3 <= 0 ? "" : var2.substring(0, var3);
   }

   @Override
   public String transformName() {
      return null;
   }
}
