package com.mchange.v2.codegen.bean;

public abstract class WrapperClassInfo implements ClassInfo {
   ClassInfo inner;

   public WrapperClassInfo(ClassInfo var1) {
      this.inner = var1;
   }

   @Override
   public String getPackageName() {
      return this.inner.getPackageName();
   }

   @Override
   public int getModifiers() {
      return this.inner.getModifiers();
   }

   @Override
   public String getClassName() {
      return this.inner.getClassName();
   }

   @Override
   public String getSuperclassName() {
      return this.inner.getSuperclassName();
   }

   @Override
   public String[] getInterfaceNames() {
      return this.inner.getInterfaceNames();
   }

   @Override
   public String[] getGeneralImports() {
      return this.inner.getGeneralImports();
   }

   @Override
   public String[] getSpecificImports() {
      return this.inner.getSpecificImports();
   }
}
