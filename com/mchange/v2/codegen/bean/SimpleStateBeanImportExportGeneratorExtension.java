package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class SimpleStateBeanImportExportGeneratorExtension implements GeneratorExtension {
   int ctor_modifiers = 1;

   @Override
   public Collection extraGeneralImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraSpecificImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraInterfaceNames() {
      return Collections.EMPTY_SET;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      int var6 = var3.length;
      Property[] var7 = new Property[var6];

      for(int var8 = 0; var8 < var6; ++var8) {
         var7[var8] = new SimpleStateBeanImportExportGeneratorExtension.SimplePropertyMask(var3[var8]);
      }

      var5.println("protected static class SimpleStateBean implements ExportedState");
      var5.println("{");
      var5.upIndent();

      for(int var9 = 0; var9 < var6; ++var9) {
         var7[var9] = new SimpleStateBeanImportExportGeneratorExtension.SimplePropertyMask(var3[var9]);
         BeangenUtils.writePropertyMember(var7[var9], var5);
         var5.println();
         BeangenUtils.writePropertyGetter(var7[var9], var5);
         var5.println();
         BeangenUtils.writePropertySetter(var7[var9], var5);
      }

      var5.downIndent();
      var5.println("}");
   }

   static class SimplePropertyMask implements Property {
      Property p;

      SimplePropertyMask(Property var1) {
         this.p = var1;
      }

      @Override
      public int getVariableModifiers() {
         return 2;
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
         return null;
      }

      @Override
      public String getDefaultValueExpression() {
         return null;
      }

      @Override
      public int getGetterModifiers() {
         return 1;
      }

      @Override
      public int getSetterModifiers() {
         return 1;
      }

      @Override
      public boolean isReadOnly() {
         return false;
      }

      @Override
      public boolean isBound() {
         return false;
      }

      @Override
      public boolean isConstrained() {
         return false;
      }
   }
}
