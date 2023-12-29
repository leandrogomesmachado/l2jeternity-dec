package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class PropertyMapConstructorGeneratorExtension implements GeneratorExtension {
   int ctor_modifiers = 1;

   @Override
   public Collection extraGeneralImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraSpecificImports() {
      HashSet var1 = new HashSet();
      var1.add("java.util.Map");
      return var1;
   }

   @Override
   public Collection extraInterfaceNames() {
      return Collections.EMPTY_SET;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      var5.print(CodegenUtils.getModifierString(this.ctor_modifiers));
      var5.print(' ' + var1.getClassName() + "( Map map )");
      var5.println("{");
      var5.upIndent();
      var5.println("Object raw;");
      int var6 = 0;

      for(int var7 = var3.length; var6 < var7; ++var6) {
         Property var8 = var3[var6];
         String var9 = var8.getName();
         Class var10 = var4[var6];
         var5.println("raw = map.get( \"" + var9 + "\" );");
         var5.println("if (raw != null)");
         var5.println("{");
         var5.upIndent();
         var5.print("this." + var9 + " = ");
         if (var10 == Boolean.TYPE) {
            var5.println("((Boolean) raw ).booleanValue();");
         } else if (var10 == Byte.TYPE) {
            var5.println("((Byte) raw ).byteValue();");
         } else if (var10 == Character.TYPE) {
            var5.println("((Character) raw ).charValue();");
         } else if (var10 == Short.TYPE) {
            var5.println("((Short) raw ).shortValue();");
         } else if (var10 == Integer.TYPE) {
            var5.println("((Integer) raw ).intValue();");
         } else if (var10 == Long.TYPE) {
            var5.println("((Long) raw ).longValue();");
         } else if (var10 == Float.TYPE) {
            var5.println("((Float) raw ).floatValue();");
         } else if (var10 == Double.TYPE) {
            var5.println("((Double) raw ).doubleValue();");
         }

         var5.println("raw = null;");
         var5.downIndent();
         var5.println("}");
      }

      var5.downIndent();
      var5.println("}");
   }
}
