package com.mchange.v2.codegen.bean;

public class SimpleClassInfo implements ClassInfo {
   String packageName;
   int modifiers;
   String className;
   String superclassName;
   String[] interfaceNames;
   String[] generalImports;
   String[] specificImports;

   @Override
   public String getPackageName() {
      return this.packageName;
   }

   @Override
   public int getModifiers() {
      return this.modifiers;
   }

   @Override
   public String getClassName() {
      return this.className;
   }

   @Override
   public String getSuperclassName() {
      return this.superclassName;
   }

   @Override
   public String[] getInterfaceNames() {
      return this.interfaceNames;
   }

   @Override
   public String[] getGeneralImports() {
      return this.generalImports;
   }

   @Override
   public String[] getSpecificImports() {
      return this.specificImports;
   }

   public SimpleClassInfo(String var1, int var2, String var3, String var4, String[] var5, String[] var6, String[] var7) {
      this.packageName = var1;
      this.modifiers = var2;
      this.className = var3;
      this.superclassName = var4;
      this.interfaceNames = var5;
      this.generalImports = var6;
      this.specificImports = var7;
   }
}
