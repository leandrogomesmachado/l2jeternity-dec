package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class StateBeanImportExportGeneratorExtension implements GeneratorExtension {
   int ctor_modifiers = 1;

   @Override
   public Collection extraGeneralImports() {
      return Arrays.asList("com.mchange.v2.bean");
   }

   @Override
   public Collection extraSpecificImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraInterfaceNames() {
      return Arrays.asList("StateBeanExporter");
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      String var6 = var1.getClassName();
      int var7 = var3.length;
      Property[] var8 = new Property[var7];

      for(int var9 = 0; var9 < var7; ++var9) {
         var8[var9] = new SimplePropertyMask(var3[var9]);
      }

      var5.println("protected class MyStateBean implements StateBean");
      var5.println("{");
      var5.upIndent();

      for(int var11 = 0; var11 < var7; ++var11) {
         var8[var11] = new SimplePropertyMask(var3[var11]);
         BeangenUtils.writePropertyMember(var8[var11], var5);
         var5.println();
         BeangenUtils.writePropertyGetter(var8[var11], var5);
         var5.println();
         BeangenUtils.writePropertySetter(var8[var11], var5);
      }

      var5.println();
      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.println("public StateBean exportStateBean()");
      var5.println("{");
      var5.upIndent();
      var5.println("MyStateBean out = createEmptyStateBean();");

      for(int var12 = 0; var12 < var7; ++var12) {
         String var10 = BeangenUtils.capitalize(var3[var12].getName());
         var5.println("out.set" + var10 + "( this." + (var4[var12] == Boolean.TYPE ? "is" : "get") + var10 + "() );");
      }

      var5.println("return out;");
      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.println("public void importStateBean( StateBean bean )");
      var5.println("{");
      var5.upIndent();
      var5.println("MyStateBean msb = (MyStateBean) bean;");

      for(int var13 = 0; var13 < var7; ++var13) {
         String var14 = BeangenUtils.capitalize(var3[var13].getName());
         var5.println("this.set" + var14 + "( msb." + (var4[var13] == Boolean.TYPE ? "is" : "get") + var14 + "() );");
      }

      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.print(CodegenUtils.getModifierString(this.ctor_modifiers));
      var5.println(" " + var6 + "( StateBean bean )");
      var5.println("{ importStateBean( bean ); }");
      var5.println("protected MyStateBean createEmptyStateBean() throws StateBeanException");
      var5.println("{ return new MyStateBean(); }");
   }
}
