package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class CompleteConstructorGeneratorExtension implements GeneratorExtension {
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
      var5.print(var1.getClassName() + "( ");
      BeangenUtils.writeArgList(var3, true, var5);
      var5.println(" )");
      var5.println("{");
      var5.upIndent();
      int var6 = 0;

      for(int var7 = var3.length; var6 < var7; ++var6) {
         var5.print("this." + var3[var6].getName() + " = ");
         String var8 = var3[var6].getDefensiveCopyExpression();
         if (var8 == null) {
            var8 = var3[var6].getName();
         }

         var5.println(var8 + ';');
      }

      var5.downIndent();
      var5.println("}");
   }
}
