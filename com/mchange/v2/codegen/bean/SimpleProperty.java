package com.mchange.v2.codegen.bean;

public class SimpleProperty implements Property {
   int variable_modifiers;
   String name;
   String simpleTypeName;
   String defensiveCopyExpression;
   String defaultValueExpression;
   int getter_modifiers;
   int setter_modifiers;
   boolean is_read_only;
   boolean is_bound;
   boolean is_constrained;

   @Override
   public int getVariableModifiers() {
      return this.variable_modifiers;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getSimpleTypeName() {
      return this.simpleTypeName;
   }

   @Override
   public String getDefensiveCopyExpression() {
      return this.defensiveCopyExpression;
   }

   @Override
   public String getDefaultValueExpression() {
      return this.defaultValueExpression;
   }

   @Override
   public int getGetterModifiers() {
      return this.getter_modifiers;
   }

   @Override
   public int getSetterModifiers() {
      return this.setter_modifiers;
   }

   @Override
   public boolean isReadOnly() {
      return this.is_read_only;
   }

   @Override
   public boolean isBound() {
      return this.is_bound;
   }

   @Override
   public boolean isConstrained() {
      return this.is_constrained;
   }

   public SimpleProperty(int var1, String var2, String var3, String var4, String var5, int var6, int var7, boolean var8, boolean var9, boolean var10) {
      this.variable_modifiers = var1;
      this.name = var2;
      this.simpleTypeName = var3;
      this.defensiveCopyExpression = var4;
      this.defaultValueExpression = var5;
      this.getter_modifiers = var6;
      this.setter_modifiers = var7;
      this.is_read_only = var8;
      this.is_bound = var9;
      this.is_constrained = var10;
   }

   public SimpleProperty(String var1, String var2, String var3, String var4, boolean var5, boolean var6, boolean var7) {
      this(2, var1, var2, var3, var4, 1, 1, var5, var6, var7);
   }
}
