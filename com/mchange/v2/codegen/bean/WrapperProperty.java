package com.mchange.v2.codegen.bean;

public abstract class WrapperProperty implements Property {
   Property p;

   public WrapperProperty(Property var1) {
      this.p = var1;
   }

   protected Property getInner() {
      return this.p;
   }

   @Override
   public int getVariableModifiers() {
      return this.p.getVariableModifiers();
   }

   @Override
   public String getName() {
      return this.p.getName();
   }

   @Override
   public String getSimpleTypeName() {
      return this.p.getSimpleTypeName();
   }

   @Override
   public String getDefensiveCopyExpression() {
      return this.p.getDefensiveCopyExpression();
   }

   @Override
   public String getDefaultValueExpression() {
      return this.p.getDefaultValueExpression();
   }

   @Override
   public int getGetterModifiers() {
      return this.p.getGetterModifiers();
   }

   @Override
   public int getSetterModifiers() {
      return this.p.getSetterModifiers();
   }

   @Override
   public boolean isReadOnly() {
      return this.p.isReadOnly();
   }

   @Override
   public boolean isBound() {
      return this.p.isBound();
   }

   @Override
   public boolean isConstrained() {
      return this.p.isConstrained();
   }
}
