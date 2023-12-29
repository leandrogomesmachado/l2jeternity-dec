package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class CopyConstructorGeneratorExtension implements GeneratorExtension {
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
      var5.print(CodegenUtils.getModifierString(this.ctor_modifiers));
      var5.print(" " + var1.getClassName() + "( ");
      var5.print(var1.getClassName() + " copyMe");
      var5.println(" )");
      var5.println("{");
      var5.upIndent();
      int var6 = 0;

      for(int var7 = var3.length; var6 < var7; ++var6) {
         String var8;
         if (var4[var6] == Boolean.TYPE) {
            var8 = "is" + BeangenUtils.capitalize(var3[var6].getName()) + "()";
         } else {
            var8 = "get" + BeangenUtils.capitalize(var3[var6].getName()) + "()";
         }

         var5.println(var3[var6].getSimpleTypeName() + ' ' + var3[var6].getName() + " = copyMe." + var8 + ';');
         var5.print("this." + var3[var6].getName() + " = ");
         String var9 = var3[var6].getDefensiveCopyExpression();
         if (var9 == null) {
            var9 = var3[var6].getName();
         }

         var5.println(var9 + ';');
      }

      var5.downIndent();
      var5.println("}");
   }
}
